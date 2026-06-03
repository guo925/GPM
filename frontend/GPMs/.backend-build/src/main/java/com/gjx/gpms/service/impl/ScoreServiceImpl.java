package com.gjx.gpms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gjx.gpms.common.exception.BusinessException;
import com.gjx.gpms.dto.ScoreSheetDTO;
import com.gjx.gpms.entity.*;
import com.gjx.gpms.mapper.*;
import com.gjx.gpms.service.ScoreService;
import com.gjx.gpms.system.entity.User;
import com.gjx.gpms.system.mapper.UserMapper;
import com.gjx.gpms.vo.ScoreSheetVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Score 服务实现类。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScoreServiceImpl extends ServiceImpl<ScoreSheetMapper, ScoreSheet> implements ScoreService {

    private final ScoreDetailMapper scoreDetailMapper;
    private final StudentTopicMapper studentTopicMapper;
    private final TopicMapper topicMapper;
    private final UserMapper userMapper;

    /**
     * 计算相关逻辑。
     */
    @Override
    public ScoreSheetVO calculate(ScoreSheetDTO dto) {
        log.info("计算成绩，学生选题[{}]", dto.getStudentTopicId());

        ScoreSheet sheet = this.getOne(
                new LambdaQueryWrapper<ScoreSheet>()
                        .eq(ScoreSheet::getStudentTopicId, dto.getStudentTopicId())
        );

        if (sheet == null) {
            sheet = new ScoreSheet();
            sheet.setStudentTopicId(dto.getStudentTopicId());
            sheet.setBatchId(dto.getBatchId());
            sheet.setStatus("draft");
        }

        // 计算加权总分
        BigDecimal advisorWeight = dto.getAdvisorWeight() != null ?
                dto.getAdvisorWeight() : new BigDecimal("0.3");
        BigDecimal reviewerWeight = dto.getReviewerWeight() != null ?
                dto.getReviewerWeight() : new BigDecimal("0.3");
        BigDecimal defenseWeight = dto.getDefenseWeight() != null ?
                dto.getDefenseWeight() : new BigDecimal("0.4");

        BigDecimal finalScore = BigDecimal.ZERO;

        if (dto.getAdvisorScore() != null) {
            finalScore = finalScore.add(dto.getAdvisorScore().multiply(advisorWeight));
        }
        if (dto.getReviewerScore() != null) {
            finalScore = finalScore.add(dto.getReviewerScore().multiply(reviewerWeight));
        }
        if (dto.getDefenseScore() != null) {
            finalScore = finalScore.add(dto.getDefenseScore().multiply(defenseWeight));
        }

        sheet.setFinalScore(finalScore.setScale(2, RoundingMode.HALF_UP));

        // 等级评定
        double score = finalScore.doubleValue();
        if (score >= 90) sheet.setGradeLevel("优秀");
        else if (score >= 80) sheet.setGradeLevel("良好");
        else if (score >= 70) sheet.setGradeLevel("中等");
        else if (score >= 60) sheet.setGradeLevel("及格");
        else sheet.setGradeLevel("不及格");

        this.saveOrUpdate(sheet);

        // 保存评分明细
        scoreDetailMapper.delete(
                new LambdaQueryWrapper<ScoreDetail>().eq(ScoreDetail::getSheetId, sheet.getId())
        );

        if (dto.getAdvisorScore() != null) {
            saveDetail(sheet.getId(), "advisor", dto.getAdvisorScore(), advisorWeight);
        }
        if (dto.getReviewerScore() != null) {
            saveDetail(sheet.getId(), "reviewer", dto.getReviewerScore(), reviewerWeight);
        }
        if (dto.getDefenseScore() != null) {
            saveDetail(sheet.getId(), "defense", dto.getDefenseScore(), defenseWeight);
        }

        log.info("成绩计算完成，总分：{}，等级：{}", sheet.getFinalScore(), sheet.getGradeLevel());
        return getDetail(dto.getStudentTopicId());
    }

    /**
     * 保存detail相关逻辑。
     */
    private void saveDetail(Long sheetId, String type, BigDecimal score, BigDecimal weight) {
        ScoreDetail detail = new ScoreDetail();
        detail.setSheetId(sheetId);
        detail.setType(type);
        detail.setScore(score);
        detail.setWeight(weight);
        detail.setIsBlind((byte) ("reviewer".equals(type) ? 1 : 0));
        scoreDetailMapper.insert(detail);
    }

    /**
     * 提交相关逻辑。
     */
    @Override
    public void submit(Long id) {
        ScoreSheet sheet = this.getById(id);
        if (sheet == null) throw new BusinessException("成绩单不存在");
        sheet.setStatus("submitted");
        this.updateById(sheet);
    }

    /**
     * 审核相关逻辑。
     */
    @Override
    public void review(Long id, String status, String comment) {
        ScoreSheet sheet = this.getById(id);
        if (sheet == null) throw new BusinessException("成绩单不存在");
        sheet.setStatus(status);
        sheet.setReviewComment(comment);
        this.updateById(sheet);
    }

    /**
     * 获取Detail。
     */
    @Override
    public ScoreSheetVO getDetail(Long studentTopicId) {
        ScoreSheet sheet = this.getOne(
                new LambdaQueryWrapper<ScoreSheet>()
                        .eq(ScoreSheet::getStudentTopicId, studentTopicId)
        );

        if (sheet == null) throw new BusinessException("成绩单不存在");

        List<ScoreDetail> details = scoreDetailMapper.selectList(
                new LambdaQueryWrapper<ScoreDetail>().eq(ScoreDetail::getSheetId, sheet.getId())
        );

        StudentTopic st = studentTopicMapper.selectById(studentTopicId);
        Map<Long, String> userMap = userMapper.selectList(null).stream()
                .collect(Collectors.toMap(User::getId, User::getRealName));

        ScoreSheetVO vo = new ScoreSheetVO();
        vo.setId(sheet.getId());
        vo.setStudentTopicId(sheet.getStudentTopicId());
        vo.setBatchId(sheet.getBatchId());
        vo.setFinalScore(sheet.getFinalScore());
        vo.setGradeLevel(sheet.getGradeLevel());
        vo.setStatus(sheet.getStatus());
        vo.setReviewComment(sheet.getReviewComment());

        if (st != null) {
            vo.setStudentName(userMap.getOrDefault(st.getStudentId(), ""));
            Topic topic = topicMapper.selectById(st.getTopicId());
            if (topic != null) vo.setTopicTitle(topic.getTitle());
        }

        List<ScoreSheetVO.ScoreDetailVO> detailVOs = new ArrayList<>();
        for (ScoreDetail d : details) {
            ScoreSheetVO.ScoreDetailVO dv = new ScoreSheetVO.ScoreDetailVO();
            dv.setType(d.getType());
            dv.setScore(d.getScore());
            dv.setWeight(d.getWeight());
            dv.setComment(d.getComment());
            detailVOs.add(dv);

            switch (d.getType()) {
                case "advisor": vo.setAdvisorScore(d.getScore()); break;
                case "reviewer": vo.setReviewerScore(d.getScore()); break;
                case "defense": vo.setDefenseScore(d.getScore()); break;
            }
        }
        vo.setDetails(detailVOs);

        return vo;
    }

    /**
     * 查询列表by batch相关逻辑。
     */
    @Override
    public List<ScoreSheetVO> listByBatch(Long batchId) {
        List<ScoreSheet> sheets = this.list(
                new LambdaQueryWrapper<ScoreSheet>().eq(ScoreSheet::getBatchId, batchId)
        );

        return sheets.stream().map(s -> {
            try { return getDetail(s.getStudentTopicId()); }
            catch (Exception e) { return null; }
        }).filter(v -> v != null).collect(Collectors.toList());
    }
}
