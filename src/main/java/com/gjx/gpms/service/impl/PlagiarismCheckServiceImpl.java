package com.gjx.gpms.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gjx.gpms.common.exception.BusinessException;
import com.gjx.gpms.config.AliyunOssProperties;
import com.gjx.gpms.config.FileStorageProperties;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 查重服务实现类
 * 负责：
 * 1. 获取提交文本
 * 2. 提取附件内容
 * 3. 相似度计算
 * 4. 返回查重结果
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlagiarismCheckServiceImpl implements PlagiarismCheckService {

    /**
     * 分片长度，用于生成 Shingle
     */
    private static final int SHINGLE_SIZE = 8;

    /**
     * 文本最大提取长度
     */
    private static final int MAX_EXTRACT_CHARS = 300_000;

    /**
     * 流程实例表
     */
    private final ProcessInstanceMapper processInstanceMapper;

    /**
     * 学生选题表
     */
    private final StudentTopicMapper studentTopicMapper;

    /**
     * 课题表
     */
    private final TopicMapper topicMapper;

    /**
     * 用户表
     */
    private final UserMapper userMapper;

    /**
     * 文件存储配置
     */
    private final FileStorageProperties fileStorageProperties;

    /**
     * 阿里云OSS配置
     */
    private final AliyunOssProperties aliyunOssProperties;

    /**
     * 执行查重
     */
    @Override
    public PlagiarismCheckVO check(PlagiarismCheckDTO dto) {

        // 查询目标流程实例
        ProcessInstance target = processInstanceMapper.selectById(dto.getProcessInstanceId());

        // 流程不存在
        if (target == null) {
            throw new BusinessException("流程实例不存在");
        }

        // 获取待检测文本
        String rawTargetText = resolveCheckText(target);

        // 文本为空无法查重
        if (!StringUtils.hasText(rawTargetText)) {
            throw new BusinessException("当前提交内容和附件均无可查重文本");
        }

        // 清洗文本
        String targetText = cleanText(rawTargetText);

        // 切片
        Set<String> targetShingles = shingles(targetText);

        // 文本太短
        if (targetShingles.isEmpty()) {
            throw new BusinessException("当前提交内容过短，无法查重");
        }

        // 查询所有候选流程（排除自己）
        List<ProcessInstance> candidates = processInstanceMapper.selectList(
                new LambdaQueryWrapper<ProcessInstance>()
                        .ne(ProcessInstance::getId, target.getId())
        );

        // 查询所有用户，转Map方便快速查找
        Map<Long, User> users = userMapper.selectList(null).stream()
                .collect(Collectors.toMap(
                        User::getId,
                        Function.identity(),
                        (a, b) -> a
                ));

        // 构建查重来源
        List<PlagiarismCheckVO.MatchSource> sources = candidates.stream()
                .map(item -> new CandidateText(item, resolveCheckText(item)))
                .filter(item -> StringUtils.hasText(item.text()))
                .map(item -> buildSource(
                        item.processInstance(),
                        item.text(),
                        targetShingles,
                        targetText,
                        users
                ))
                .filter(item -> item.getSimilarity() > 0)
                .sorted(Comparator.comparing(
                        PlagiarismCheckVO.MatchSource::getSimilarity
                ).reversed())
                .limit(5)
                .collect(Collectors.toList());

        // 取最高相似度
        double maxSimilarity = sources.isEmpty()
                ? 0
                : sources.get(0).getSimilarity();

        // 封装返回结果
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

    /**
     * 构建单个匹配来源
     */
    private PlagiarismCheckVO.MatchSource buildSource(
            ProcessInstance candidate,
            String rawCandidateText,
            Set<String> targetShingles,
            String targetText,
            Map<Long, User> users) {

        // 清洗候选文本
        String candidateText = cleanText(rawCandidateText);

        // 切分
        Set<String> candidateShingles = shingles(candidateText);

        // Jaccard计算相似度
        double similarity =
                jaccard(targetShingles, candidateShingles) * 100;

        // 查学生选题
        StudentTopic st =
                studentTopicMapper.selectById(candidate.getStudentTopicId());

        // 查题目
        Topic topic =
                st == null ? null : topicMapper.selectById(st.getTopicId());

        // 查学生
        User student =
                st == null ? null : users.get(st.getStudentId());

        // 封装结果
        PlagiarismCheckVO.MatchSource source =
                new PlagiarismCheckVO.MatchSource();

        source.setProcessInstanceId(candidate.getId());
        source.setStudentTopicId(candidate.getStudentTopicId());
        source.setStudentName(student == null ? "" : student.getRealName());
        source.setTopicTitle(topic == null ? "" : topic.getTitle());
        source.setStage(candidate.getStage());
        source.setSimilarity(round(similarity));
        source.setMatchedText(findMatchedText(targetText, candidateText));

        return source;
    }

    /**
     * 获取待查重文本
     * 优先取 content，没有则读取附件内容
     */
    private String resolveCheckText(ProcessInstance processInstance) {

        if (StringUtils.hasText(processInstance.getContent())) {
            return processInstance.getContent();
        }

        if (!StringUtils.hasText(processInstance.getFilePath())) {
            return "";
        }

        try {
            return extractAttachmentText(processInstance.getFilePath());
        } catch (Exception e) {
            log.warn("附件文本抽取失败，processInstanceId={}, filePath={}, error={}",
                    processInstance.getId(),
                    processInstance.getFilePath(),
                    e.getMessage());
            return "";
        }
    }

    /**
     * 提取附件文本
     */
    private String extractAttachmentText(String filePath)
            throws IOException, TikaException, SAXException {

        try (InputStream inputStream = openAttachment(filePath)) {

            BodyContentHandler handler =
                    new BodyContentHandler(MAX_EXTRACT_CHARS);

            AutoDetectParser parser = new AutoDetectParser();

            parser.parse(
                    inputStream,
                    handler,
                    new Metadata(),
                    new ParseContext()
            );

            return handler.toString();
        }
    }

    /**
     * 打开附件输入流
     */
    private InputStream openAttachment(String filePath)
            throws IOException {

        // URL解码
        String decodedPath =
                URLDecoder.decode(filePath, StandardCharsets.UTF_8);

        // 解析OSS对象key
        String objectKey = resolveOssObjectKey(decodedPath);

        // OSS读取
        if (objectKey != null && isOssConfigured()) {
            OSS ossClient = new OSSClientBuilder().build(
                    aliyunOssProperties.getEndpoint(),
                    aliyunOssProperties.getAccessKeyId(),
                    aliyunOssProperties.getAccessKeySecret()
            );

            return new OssObjectInputStream(
                    ossClient.getObject(
                            aliyunOssProperties.getBucketName(),
                            objectKey
                    ).getObjectContent(),
                    ossClient
            );
        }

        // HTTP读取
        if (decodedPath.startsWith("http://")
                || decodedPath.startsWith("https://")) {
            return URI.create(decodedPath).toURL().openStream();
        }

        // 本地文件读取
        String prefix =
                normalizePrefix(fileStorageProperties.getLocalUrlPrefix());

        String localObjectKey = decodedPath;

        if (localObjectKey.startsWith(prefix + "/")) {
            localObjectKey =
                    localObjectKey.substring(prefix.length() + 1);
        } else if (localObjectKey.startsWith("/")) {
            throw new IOException("文件路径不合法");
        }

        Path root = Path.of(fileStorageProperties.getLocalPath())
                .toAbsolutePath()
                .normalize();

        Path target = root.resolve(localObjectKey).normalize();

        if (!target.startsWith(root)
                || !Files.isRegularFile(target)) {
            throw new IOException("附件文件不存在");
        }

        return Files.newInputStream(target);
    }

    /**
     * 获取OSS对象Key
     */
    private String resolveOssObjectKey(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return null;
        }

        if (!filePath.startsWith("http://")
                && !filePath.startsWith("https://")) {
            return "oss".equalsIgnoreCase(fileStorageProperties.getType())
                    ? filePath
                    : null;
        }

        if (!StringUtils.hasText(aliyunOssProperties.getEndpoint())
                || !StringUtils.hasText(aliyunOssProperties.getBucketName())) {
            return null;
        }

        String endpoint =
                aliyunOssProperties.getEndpoint()
                        .replace("https://", "")
                        .replace("http://", "");

        String ossHost =
                aliyunOssProperties.getBucketName() + "." + endpoint;

        URI uri = URI.create(filePath);

        if (!ossHost.equalsIgnoreCase(uri.getHost())) {
            return null;
        }

        String path = uri.getPath();

        return path != null && path.startsWith("/")
                ? path.substring(1)
                : path;
    }

    /**
     * 判断OSS配置是否完整
     */
    private boolean isOssConfigured() {
        return StringUtils.hasText(aliyunOssProperties.getEndpoint())
                && StringUtils.hasText(aliyunOssProperties.getBucketName())
                && StringUtils.hasText(aliyunOssProperties.getAccessKeyId())
                && StringUtils.hasText(aliyunOssProperties.getAccessKeySecret());
    }

    /**
     * 标准化路径前缀
     */
    private String normalizePrefix(String prefix) {
        if (!StringUtils.hasText(prefix)) {
            return "/uploads";
        }
        return prefix.startsWith("/") ? prefix : "/" + prefix;
    }

    /**
     * 查找疑似重复片段
     */
    private List<String> findSuspiciousSegments(String targetText,
                                                List<PlagiarismCheckVO.MatchSource> sources) {
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

    /**
     * 查找重复文本片段
     */
    private String findMatchedText(String targetText,
                                   String candidateText) {
        int max = Math.min(120, targetText.length());

        for (int len = max; len >= 24; len -= 8) {
            for (int start = 0;
                 start + len <= targetText.length();
                 start += Math.max(8, len / 3)) {

                String fragment =
                        targetText.substring(start, start + len);

                if (candidateText.contains(fragment)) {
                    return fragment;
                }
            }
        }

        return "";
    }

    /**
     * 文本切片
     */
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

    /**
     * Jaccard相似度算法
     */
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

    /**
     * 清洗文本
     */
    private String cleanText(String text) {
        return text == null ? "" : text
                .replaceAll("\\s+", "")
                .replaceAll("[\\p{Punct}，。！？；：“”‘’（）【】《》、]", "")
                .toLowerCase();
    }

    /**
     * 根据相似度判断状态
     */
    private String resolveStatus(double similarity) {
        if (similarity >= 40) {
            return "risk";
        }
        if (similarity >= 20) {
            return "warning";
        }
        return "pass";
    }

    /**
     * 状态中文描述
     */
    private String resolveStatusText(String status) {
        return switch (status) {
            case "risk" -> "高风险";
            case "warning" -> "需复核";
            default -> "通过";
        };
    }

    /**
     * AI总结说明
     */
    private String buildSummary(PlagiarismCheckVO vo) {
        if ("risk".equals(vo.getStatus())) {
            return "AI查重发现较高文本相似度，建议重点核查相似来源和疑似重复片段后再审核。";
        }
        if ("warning".equals(vo.getStatus())) {
            return "AI查重发现部分相似表达，建议导师结合论文主题、引用说明和附件内容人工复核。";
        }
        return "AI查重未发现明显高相似文本，可进入常规审核流程。";
    }

    /**
     * 保留两位小数
     */
    private double round(double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * 候选文本记录对象
     */
    private record CandidateText(ProcessInstance processInstance, String text) {
    }

    /**
     * OSS输入流包装类
     */
    private static class OssObjectInputStream extends InputStream {

        private final InputStream delegate;
        private final OSS ossClient;

        private OssObjectInputStream(InputStream delegate, OSS ossClient) {
            this.delegate = delegate;
            this.ossClient = ossClient;
        }

        @Override
        public int read() throws IOException {
            return delegate.read();
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return delegate.read(b, off, len);
        }

        @Override
        public void close() throws IOException {
            try {
                delegate.close();
            } finally {
                ossClient.shutdown();
            }
        }
    }
}