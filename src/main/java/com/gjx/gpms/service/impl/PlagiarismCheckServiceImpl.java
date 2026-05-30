package com.gjx.gpms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gjx.gpms.common.exception.BusinessException;
import com.gjx.gpms.dto.PlagiarismCheckDTO;
import com.gjx.gpms.entity.ProcessInstance;
import com.gjx.gpms.entity.StudentTopic;
import com.gjx.gpms.entity.Topic;
import com.gjx.gpms.mapper.ProcessInstanceMapper;
import com.gjx.gpms.mapper.StudentTopicMapper;
import com.gjx.gpms.mapper.TopicMapper;
import com.gjx.gpms.service.PlagiarismCheckService;
import com.gjx.gpms.system.entity.User;
import com.gjx.gpms.system.mapper.UserMapper;
import com.gjx.gpms.vo.PlagiarismCheckVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlagiarismCheckServiceImpl implements PlagiarismCheckService {

    private static final int SHINGLE_SIZE = 8;

    private final ProcessInstanceMapper processInstanceMapper;
    private final StudentTopicMapper studentTopicMapper;
    private final TopicMapper topicMapper;
    private final UserMapper userMapper;

    @Override
    public PlagiarismCheckVO check(PlagiarismCheckDTO dto) {
        ProcessInstance target = processInstanceMapper.selectById(dto.getProcessInstanceId());
        if (target == null) {
            throw new BusinessException("流程实例不存在");
        }
        if (!StringUtils.hasText(target.getContent())) {
            throw new BusinessException("当前提交内容为空，无法查重");
        }

        String targetText = cleanText(target.getContent());
        Set<String> targetShingles = shingles(targetText);
        if (targetShingles.isEmpty()) {
            throw new BusinessException("当前提交内容过短，无法查重");
        }

        List<ProcessInstance> candidates = processInstanceMapper.selectList(
                new LambdaQueryWrapper<ProcessInstance>()
                        .ne(ProcessInstance::getId, target.getId())
                        .isNotNull(ProcessInstance::getContent)
        );

        Map<Long, User> users = userMapper.selectList(null).stream()
                .collect(Collectors.toMap(User::getId, Function.identity(), (a, b) -> a));

        List<PlagiarismCheckVO.MatchSource> sources = candidates.stream()
                .filter(item -> StringUtils.hasText(item.getContent()))
                .map(item -> buildSource(item, targetShingles, targetText, users))
                .filter(item -> item.getSimilarity() > 0)
                .sorted(Comparator.comparing(PlagiarismCheckVO.MatchSource::getSimilarity).reversed())
                .limit(5)
                .collect(Collectors.toList());

        double maxSimilarity = sources.isEmpty() ? 0 : sources.get(0).getSimilarity();
        PlagiarismCheckVO vo = new PlagiarismCheckVO();
        vo.setSimilarity(maxSimilarity);
        vo.setStatus(resolveStatus(maxSimilarity));
        vo.setStatusText(resolveStatusText(vo.getStatus()));
        vo.setWordCount(targetText.length());
        vo.setTopSource(sources.isEmpty() ? null : sources.get(0));
        vo.setSources(sources);
        vo.setSuspiciousSegments(findSuspiciousSegments(targetText, sources));
        vo.setAiSummary(buildSummary(vo));
        return vo;
    }

    private PlagiarismCheckVO.MatchSource buildSource(ProcessInstance candidate, Set<String> targetShingles,
                                                      String targetText, Map<Long, User> users) {
        String candidateText = cleanText(candidate.getContent());
        Set<String> candidateShingles = shingles(candidateText);
        double similarity = jaccard(targetShingles, candidateShingles) * 100;

        StudentTopic st = studentTopicMapper.selectById(candidate.getStudentTopicId());
        Topic topic = st == null ? null : topicMapper.selectById(st.getTopicId());
        User student = st == null ? null : users.get(st.getStudentId());

        PlagiarismCheckVO.MatchSource source = new PlagiarismCheckVO.MatchSource();
        source.setProcessInstanceId(candidate.getId());
        source.setStudentTopicId(candidate.getStudentTopicId());
        source.setStudentName(student == null ? "" : student.getRealName());
        source.setTopicTitle(topic == null ? "" : topic.getTitle());
        source.setStage(candidate.getStage());
        source.setSimilarity(round(similarity));
        source.setMatchedText(findMatchedText(targetText, candidateText));
        return source;
    }

    private List<String> findSuspiciousSegments(String targetText, List<PlagiarismCheckVO.MatchSource> sources) {
        Set<String> segments = new LinkedHashSet<>();
        for (PlagiarismCheckVO.MatchSource source : sources) {
            if (StringUtils.hasText(source.getMatchedText())) {
                segments.add(source.getMatchedText());
            }
            if (segments.size() >= 3) {
                break;
            }
        }
        if (segments.isEmpty() && targetText.length() > 120) {
            segments.add(targetText.substring(0, 120));
        }
        return new ArrayList<>(segments);
    }

    private String findMatchedText(String targetText, String candidateText) {
        int max = Math.min(120, targetText.length());
        for (int len = max; len >= 24; len -= 8) {
            for (int start = 0; start + len <= targetText.length(); start += Math.max(8, len / 3)) {
                String fragment = targetText.substring(start, start + len);
                if (candidateText.contains(fragment)) {
                    return fragment;
                }
            }
        }
        return "";
    }

    private Set<String> shingles(String text) {
        Set<String> result = new HashSet<>();
        if (text.length() < SHINGLE_SIZE) {
            if (StringUtils.hasText(text)) {
                result.add(text);
            }
            return result;
        }
        for (int i = 0; i <= text.length() - SHINGLE_SIZE; i++) {
            result.add(text.substring(i, i + SHINGLE_SIZE));
        }
        return result;
    }

    private double jaccard(Set<String> left, Set<String> right) {
        if (left.isEmpty() || right.isEmpty()) {
            return 0;
        }
        Set<String> intersection = new HashSet<>(left);
        intersection.retainAll(right);
        Set<String> union = new HashSet<>(left);
        union.addAll(right);
        return (double) intersection.size() / union.size();
    }

    private String cleanText(String text) {
        return text == null ? "" : text
                .replaceAll("\\s+", "")
                .replaceAll("[\\p{Punct}，。！？；：“”‘’（）【】《》、]", "")
                .toLowerCase();
    }

    private String resolveStatus(double similarity) {
        if (similarity >= 40) {
            return "risk";
        }
        if (similarity >= 20) {
            return "warning";
        }
        return "pass";
    }

    private String resolveStatusText(String status) {
        return switch (status) {
            case "risk" -> "高风险";
            case "warning" -> "需复核";
            default -> "通过";
        };
    }

    private String buildSummary(PlagiarismCheckVO vo) {
        if ("risk".equals(vo.getStatus())) {
            return "AI查重发现较高文本相似度，建议重点核查相似来源和疑似重复片段后再审核。";
        }
        if ("warning".equals(vo.getStatus())) {
            return "AI查重发现部分相似表达，建议导师结合论文主题、引用说明和附件内容人工复核。";
        }
        return "AI查重未发现明显高相似文本，可进入常规审核流程。";
    }

    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
