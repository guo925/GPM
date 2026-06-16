package com.gjx.gpms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gjx.gpms.common.exception.BusinessException;
import com.gjx.gpms.dto.ProcessReviewDTO;
import com.gjx.gpms.dto.ProcessSubmitDTO;
import com.gjx.gpms.entity.ProcessInstance;
import com.gjx.gpms.entity.StudentTopic;
import com.gjx.gpms.entity.Topic;
import com.gjx.gpms.entity.AuditLog;
import com.gjx.gpms.entity.WorkflowItem;
import com.gjx.gpms.mapper.AuditLogMapper;
import com.gjx.gpms.mapper.ProcessInstanceMapper;
import com.gjx.gpms.mapper.StudentTopicMapper;
import com.gjx.gpms.mapper.TopicMapper;
import com.gjx.gpms.mapper.WorkflowItemMapper;
import com.gjx.gpms.security.context.UserContext;
import com.gjx.gpms.service.ProcessInstanceService;
import com.gjx.gpms.system.entity.User;
import com.gjx.gpms.system.mapper.UserMapper;
import com.gjx.gpms.vo.ProcessInstanceVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 流程实例服务实现（状态机核心）
 *
 * @author gpms
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessInstanceServiceImpl extends ServiceImpl<ProcessInstanceMapper, ProcessInstance> implements ProcessInstanceService {

    private final StudentTopicMapper studentTopicMapper;
    private final TopicMapper topicMapper;
    private final UserMapper userMapper;
    private final AuditLogMapper auditLogMapper;
    private final WorkflowItemMapper workflowItemMapper;

    /**
     * 提交相关逻辑。
     */
    @Override
    public void submit(ProcessSubmitDTO dto) {
        Long userId = UserContext.getUserId();
        log.info("用户[{}]提交阶段[{}]", userId, dto.getStage());

        StudentTopic st = studentTopicMapper.selectById(dto.getStudentTopicId());
        if (st == null) {
            throw new BusinessException("选题记录不存在");
        }

        // 验证提交人身份（学生本人或导师）
        if (!userId.equals(st.getStudentId()) && !userId.equals(st.getAdvisorId())) {
            throw new BusinessException("无权提交");
        }

        // 查找已有记录
        ProcessInstance existing = this.getOne(
                new LambdaQueryWrapper<ProcessInstance>()
                        .eq(ProcessInstance::getStudentTopicId, dto.getStudentTopicId())
                        .eq(ProcessInstance::getStage, dto.getStage())
        );

        if (existing != null) {
            if ("approved".equals(existing.getStatus())) {
                throw new BusinessException("该阶段已通过，请勿重复提交");
            }

            // 驳回后重新提交，版本号+1
            existing.setContent(dto.getContent());
            existing.setFilePath(dto.getFilePath());
            existing.setStatus("submitted");
            existing.setSubmittedAt(LocalDateTime.now());
            existing.setVersion(existing.getVersion() + 1);
            existing.setSubmitterId(userId);
            this.updateById(existing);
            syncWorkflowItem(existing);
            log.info("阶段[{}]重新提交，版本号：{}", dto.getStage(), existing.getVersion());
            return;
        }

        // 新建流程实例
        ProcessInstance pi = new ProcessInstance();
        pi.setStudentTopicId(dto.getStudentTopicId());
        pi.setStage(dto.getStage());
        pi.setStatus("submitted");
        pi.setSubmitterId(userId);
        pi.setSubmittedAt(LocalDateTime.now());
        pi.setContent(dto.getContent());
        pi.setFilePath(dto.getFilePath());
        pi.setVersion(1);
        pi.setIsEditable((byte) 0);

        this.save(pi);
        syncWorkflowItem(pi);
        log.info("阶段[{}]首次提交", dto.getStage());
    }

    /**
     * 审核相关逻辑。
     */
    @Override
    public void review(ProcessReviewDTO dto) {
        Long userId = UserContext.getUserId();
        log.info("用户[{}]审核流程实例[{}]：{}", userId, dto.getId(), dto.getStatus());

        ProcessInstance pi = this.getById(dto.getId());
        if (pi == null) {
            throw new BusinessException("流程实例不存在");
        }

        if (!"submitted".equals(pi.getStatus())) {
            throw new BusinessException("当前状态不可审核");
        }

        pi.setStatus(dto.getStatus());
        pi.setReviewerId(userId);
        pi.setReviewedAt(LocalDateTime.now());
        pi.setReviewComment(dto.getReviewComment());

        if ("approved".equals(dto.getStatus())) {
            pi.setIsEditable((byte) 0);
        } else {
            // 驳回后可修改
            pi.setIsEditable((byte) 1);
        }

        this.updateById(pi);
        syncWorkflowItem(pi);
        AuditLog auditLog = new AuditLog();
        auditLog.setProcessInstanceId(pi.getId());
        auditLog.setTargetType(pi.getStage());
        auditLog.setTargetId(pi.getId());
        auditLog.setAuditorId(userId);
        auditLog.setAction("approved".equals(dto.getStatus()) ? "approve" : "reject");
        auditLog.setComment(dto.getReviewComment());
        auditLog.setCreatedAt(LocalDateTime.now());
        auditLogMapper.insert(auditLog);
        log.info("流程实例[{}]审核完成：{}", dto.getId(), dto.getStatus());
    }

    /**
     * 获取ByStudentTopic。
     */
    @Override
    public List<ProcessInstanceVO> getByStudentTopic(Long studentTopicId) {
        List<ProcessInstance> list = this.list(
                new LambdaQueryWrapper<ProcessInstance>()
                        .eq(ProcessInstance::getStudentTopicId, studentTopicId)
                        .orderByAsc(ProcessInstance::getStage)
        );

        return toVOList(list);
    }

    /**
     * 获取CurrentStage。
     */
    @Override
    public ProcessInstanceVO getCurrentStage(Long studentTopicId, String stage) {
        ProcessInstance pi = this.getOne(
                new LambdaQueryWrapper<ProcessInstance>()
                        .eq(ProcessInstance::getStudentTopicId, studentTopicId)
                        .eq(ProcessInstance::getStage, stage)
        );

        if (pi == null) {
            ProcessInstanceVO vo = new ProcessInstanceVO();
            vo.setStudentTopicId(studentTopicId);
            vo.setStage(stage);
            vo.setStatus("not_started");
            return vo;
        }

        return toVO(pi);
    }

    /**
     * 转换vo相关逻辑。
     */
    private ProcessInstanceVO toVO(ProcessInstance pi) {
        Map<Long, String> userMap = userMapper.selectList(null).stream()
                .collect(Collectors.toMap(User::getId, User::getRealName));

        StudentTopic st = studentTopicMapper.selectById(pi.getStudentTopicId());
        String studentName = "";
        String topicTitle = "";
        if (st != null) {
            studentName = userMap.getOrDefault(st.getStudentId(), "");
            Topic topic = topicMapper.selectById(st.getTopicId());
            if (topic != null) {
                topicTitle = topic.getTitle();
            }
        }

        ProcessInstanceVO vo = new ProcessInstanceVO();
        vo.setId(pi.getId());
        vo.setStudentTopicId(pi.getStudentTopicId());
        vo.setStudentName(studentName);
        vo.setTopicTitle(topicTitle);
        vo.setStage(pi.getStage());
        vo.setStatus(pi.getStatus());
        vo.setSubmitterName(userMap.getOrDefault(pi.getSubmitterId(), ""));
        vo.setSubmittedAt(pi.getSubmittedAt());
        vo.setContent(pi.getContent());
        vo.setFilePath(pi.getFilePath());
        vo.setReviewerName(userMap.getOrDefault(pi.getReviewerId(), ""));
        vo.setReviewedAt(pi.getReviewedAt());
        vo.setReviewComment(pi.getReviewComment());
        vo.setVersion(pi.getVersion());
        vo.setCreatedAt(pi.getCreatedAt());
        return vo;
    }

    /**
     * 转换volist相关逻辑。
     */
    private List<ProcessInstanceVO> toVOList(List<ProcessInstance> list) {
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    private void syncWorkflowItem(ProcessInstance pi) {
        String workflowType = toWorkflowType(pi.getStage());
        if (!StringUtils.hasText(workflowType)) {
            return;
        }

        StudentTopic st = studentTopicMapper.selectById(pi.getStudentTopicId());
        if (st == null) {
            return;
        }
        User student = userMapper.selectById(st.getStudentId());
        User advisor = userMapper.selectById(st.getAdvisorId());
        if (student == null || advisor == null) {
            return;
        }

        String studentNo = StringUtils.hasText(student.getStudentNo()) ? student.getStudentNo() : student.getUsername();
        WorkflowItem item = workflowItemMapper.selectOne(
                new LambdaQueryWrapper<WorkflowItem>()
                        .eq(WorkflowItem::getWorkflowType, workflowType)
                        .eq(WorkflowItem::getBatchId, st.getBatchId())
                        .eq(WorkflowItem::getStudentNo, studentNo)
                        .last("LIMIT 1")
        );
        boolean isNew = item == null;
        if (isNew) {
            item = new WorkflowItem();
            item.setWorkflowType(workflowType);
            item.setBatchId(st.getBatchId());
            item.setCreatedBy(pi.getSubmitterId());
            item.setCreatedAt(pi.getCreatedAt() != null ? pi.getCreatedAt() : LocalDateTime.now());
        }

        item.setStudentName(StringUtils.hasText(student.getRealName()) ? student.getRealName() : student.getUsername());
        item.setStudentNo(studentNo);
        item.setAdvisorName(StringUtils.hasText(advisor.getRealName()) ? advisor.getRealName() : advisor.getUsername());
        item.setTitle(buildWorkflowTitle(pi));
        item.setExtra(pi.getFilePath());
        item.setRemark(pi.getContent());
        item.setStatus(toWorkflowStatus(pi.getStatus()));
        item.setComment(pi.getReviewComment());
        item.setUpdatedBy(pi.getReviewerId() != null ? pi.getReviewerId() : pi.getSubmitterId());
        item.setUpdatedAt(pi.getUpdatedAt() != null ? pi.getUpdatedAt() : LocalDateTime.now());

        if (isNew) {
            workflowItemMapper.insert(item);
        } else {
            workflowItemMapper.updateById(item);
        }
    }

    private String toWorkflowType(String stage) {
        return switch (stage) {
            case "task_book" -> "taskBook";
            case "opening_report" -> "openingReport";
            case "opening_defense" -> "openingDefense";
            case "guidance_week" -> "weeklyLog";
            case "midterm_check" -> "midterm";
            case "thesis_draft" -> "thesisGuidance";
            case "thesis_final" -> "finalThesis";
            case "post_defense_modify" -> "postDefenseRevision";
            default -> null;
        };
    }

    private String toWorkflowStatus(String status) {
        return switch (status) {
            case "approved", "rejected" -> status;
            default -> "pending";
        };
    }

    private String buildWorkflowTitle(ProcessInstance pi) {
        String label = switch (pi.getStage()) {
            case "task_book" -> "任务书";
            case "opening_report" -> "开题报告";
            case "opening_defense" -> "开题答辩";
            case "guidance_week" -> "指导周记";
            case "midterm_check" -> "中期检查";
            case "thesis_draft" -> "论文指导";
            case "thesis_final" -> "论文终稿";
            case "post_defense_modify" -> "答辩后修改";
            default -> pi.getStage();
        };
        return label + "提交";
    }
}
