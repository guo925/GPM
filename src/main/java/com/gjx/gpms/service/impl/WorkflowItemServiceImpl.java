package com.gjx.gpms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gjx.gpms.common.exception.BusinessException;
import com.gjx.gpms.dto.WorkflowItemDTO;
import com.gjx.gpms.dto.WorkflowReviewDTO;
import com.gjx.gpms.entity.Batch;
import com.gjx.gpms.entity.ProcessInstance;
import com.gjx.gpms.entity.StudentTopic;
import com.gjx.gpms.entity.WorkflowItem;
import com.gjx.gpms.mapper.BatchMapper;
import com.gjx.gpms.mapper.ProcessInstanceMapper;
import com.gjx.gpms.mapper.StudentTopicMapper;
import com.gjx.gpms.mapper.WorkflowItemMapper;
import com.gjx.gpms.security.context.UserContext;
import com.gjx.gpms.security.model.LoginUser;
import com.gjx.gpms.service.WorkflowItemService;
import com.gjx.gpms.system.entity.User;
import com.gjx.gpms.system.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 通用流程事项服务实现
 */
@Service
@RequiredArgsConstructor
public class WorkflowItemServiceImpl implements WorkflowItemService {

    private final WorkflowItemMapper workflowItemMapper;
    private final UserMapper userMapper;
    private final StudentTopicMapper studentTopicMapper;
    private final BatchMapper batchMapper;
    private final ProcessInstanceMapper processInstanceMapper;

    @Override
    @Transactional
    public List<WorkflowItem> list(String workflowType, Long batchId, String grade, String keyword, String status) {
        if (!StringUtils.hasText(workflowType)) {
            throw new BusinessException("流程类型不能为空");
        }
        List<Long> batchIds = resolveBatchIds(batchId, grade);
        LambdaQueryWrapper<WorkflowItem> wrapper = new LambdaQueryWrapper<WorkflowItem>()
                .eq(WorkflowItem::getWorkflowType, workflowType)
                .in(batchIds != null && !batchIds.isEmpty(), WorkflowItem::getBatchId, batchIds)
                .eq(StringUtils.hasText(status), WorkflowItem::getStatus, status)
                .and(StringUtils.hasText(keyword), query -> query
                        .like(WorkflowItem::getStudentName, keyword)
                        .or()
                        .like(WorkflowItem::getStudentNo, keyword)
                        .or()
                        .like(WorkflowItem::getAdvisorName, keyword)
                        .or()
                        .like(WorkflowItem::getTitle, keyword))
                .orderByDesc(WorkflowItem::getUpdatedAt)
                .orderByDesc(WorkflowItem::getCreatedAt);
        applyUserScope(wrapper);
        return workflowItemMapper.selectList(wrapper);
    }

    private void applyUserScope(LambdaQueryWrapper<WorkflowItem> wrapper) {
        LoginUser loginUser = UserContext.getLoginUser();
        if (loginUser == null || loginUser.getRoleCodes() == null) {
            return;
        }
        User currentUser = userMapper.selectById(loginUser.getUserId());
        if (currentUser == null) {
            return;
        }
        if (loginUser.getRoleCodes().contains("STUDENT")) {
            wrapper.eq(WorkflowItem::getStudentNo, resolveStudentNo(currentUser));
        } else if (loginUser.getRoleCodes().contains("TEACHER")) {
            applyTeacherScope(wrapper, loginUser.getUserId());
        }
    }

    private void applyTeacherScope(LambdaQueryWrapper<WorkflowItem> wrapper, Long teacherId) {
        List<StudentTopic> relations = studentTopicMapper.selectList(
                new LambdaQueryWrapper<StudentTopic>()
                        .eq(StudentTopic::getAdvisorId, teacherId));
        if (relations.isEmpty()) {
            wrapper.eq(WorkflowItem::getStudentName, "");
            return;
        }
        List<Long> studentIds = relations.stream()
                .map(StudentTopic::getStudentId)
                .distinct()
                .collect(Collectors.toList());
        List<String> studentNames = userMapper.selectBatchIds(studentIds).stream()
                .filter(Objects::nonNull)
                .map(User::getRealName)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
        if (studentNames.isEmpty()) {
            wrapper.eq(WorkflowItem::getStudentName, "");
        } else {
            wrapper.in(WorkflowItem::getStudentName, studentNames);
        }
    }

    @Override
    public void save(WorkflowItemDTO dto) {
        WorkflowItem item = dto.getId() == null ? new WorkflowItem() : workflowItemMapper.selectById(dto.getId());
        if (item == null) {
            throw new BusinessException("流程事项不存在");
        }
        if (dto.getId() != null) {
            checkOwnership(item);
        }
        BeanUtils.copyProperties(dto, item);
        fillCurrentStudentInfo(item, dto);
        if (!StringUtils.hasText(item.getStudentName()) || !StringUtils.hasText(item.getStudentNo())) {
            throw new BusinessException("学生姓名和学号不能为空");
        }
        if (item.getBatchId() == null) {
            item.setBatchId(resolveBatchId(dto.getGrade()));
        }
        item.setStatus(StringUtils.hasText(dto.getStatus()) ? dto.getStatus() : "pending");
        item.setUpdatedBy(UserContext.getUserId());
        item.setUpdatedAt(LocalDateTime.now());
        if (item.getId() == null) {
            item.setCreatedBy(UserContext.getUserId());
            item.setCreatedAt(LocalDateTime.now());
            workflowItemMapper.insert(item);
        } else {
            workflowItemMapper.updateById(item);
        }
        syncProcessInstance(item);
    }

    private void fillCurrentStudentInfo(WorkflowItem item, WorkflowItemDTO dto) {
        LoginUser loginUser = UserContext.getLoginUser();
        if (loginUser == null || loginUser.getRoleCodes() == null || !loginUser.getRoleCodes().contains("STUDENT")) {
            return;
        }
        User student = userMapper.selectById(loginUser.getUserId());
        if (student == null) {
            return;
        }
        item.setStudentName(StringUtils.hasText(student.getRealName()) ? student.getRealName() : student.getUsername());
        item.setStudentNo(resolveStudentNo(student));

        StudentTopic relation = studentTopicMapper.selectOne(
                new LambdaQueryWrapper<StudentTopic>()
                        .eq(StudentTopic::getStudentId, student.getId())
                        .eq(StudentTopic::getStatus, "active")
                        .orderByDesc(StudentTopic::getAllocationTime)
                        .orderByDesc(StudentTopic::getCreatedAt)
                        .last("LIMIT 1")
        );
        if (relation != null) {
            item.setBatchId(relation.getBatchId());
            User advisor = userMapper.selectById(relation.getAdvisorId());
            if (advisor != null) {
                item.setAdvisorName(StringUtils.hasText(advisor.getRealName()) ? advisor.getRealName() : advisor.getUsername());
            }
            return;
        }

        if (item.getBatchId() == null) {
            item.setBatchId(resolveStudentBatchId(student));
        }
    }

    @Override
    public void review(WorkflowReviewDTO dto) {
        WorkflowItem item = workflowItemMapper.selectById(dto.getId());
        if (item == null) {
            throw new BusinessException("流程事项不存在");
        }
        checkOwnership(item);
        item.setStatus(dto.getStatus());
        item.setComment(dto.getComment());
        if (dto.getScore() != null) {
            item.setScore(dto.getScore());
        }
        item.setUpdatedBy(UserContext.getUserId());
        item.setUpdatedAt(LocalDateTime.now());
        workflowItemMapper.updateById(item);
        syncProcessInstance(item);
    }

    @Override
    public void delete(Long id) {
        WorkflowItem item = workflowItemMapper.selectById(id);
        if (item == null) {
            throw new BusinessException("流程事项不存在");
        }
        checkOwnership(item);
        workflowItemMapper.deleteById(id);
        deleteSyncedProcessInstance(item);
    }

    private void checkOwnership(WorkflowItem item) {
        LoginUser loginUser = UserContext.getLoginUser();
        if (loginUser == null || loginUser.getRoleCodes() == null) {
            return;
        }
        User currentUser = userMapper.selectById(loginUser.getUserId());
        if (currentUser == null) {
            return;
        }
        if (loginUser.getRoleCodes().contains("STUDENT")
                && !resolveStudentNo(currentUser).equals(item.getStudentNo())) {
            throw new BusinessException("无权操作他人的论文记录");
        }
        if (loginUser.getRoleCodes().contains("TEACHER")) {
            checkTeacherOwnership(loginUser.getUserId(), item.getStudentName());
        }
    }

    private void checkTeacherOwnership(Long teacherId, String studentName) {
        User student = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getRealName, studentName));
        if (student == null) {
            throw new BusinessException("无权操作：学生不存在");
        }
        Long count = studentTopicMapper.selectCount(
                new LambdaQueryWrapper<StudentTopic>()
                        .eq(StudentTopic::getAdvisorId, teacherId)
                        .eq(StudentTopic::getStudentId, student.getId()));
        if (count == null || count == 0) {
            throw new BusinessException("无权操作非自己学生的论文记录");
        }
    }

    private List<Long> resolveBatchIds(Long batchId, String grade) {
        if (grade != null && !grade.isBlank()) {
            return batchMapper.selectList(
                    new LambdaQueryWrapper<Batch>().eq(Batch::getGrade, grade).select(Batch::getId)
            ).stream().map(Batch::getId).collect(Collectors.toList());
        }
        return batchId != null ? List.of(batchId) : null;
    }

    private Long resolveBatchId(String grade) {
        List<Long> batchIds = resolveBatchIds(null, grade);
        if (batchIds != null && !batchIds.isEmpty()) {
            return batchIds.get(0);
        }
        return null;
    }

    private Long resolveStudentBatchId(User student) {
        if (student.getCollegeId() == null || student.getMajorId() == null
                || !StringUtils.hasText(student.getGrade())) {
            return null;
        }
        Batch batch = batchMapper.selectOne(
                new LambdaQueryWrapper<Batch>()
                        .eq(Batch::getCollegeId, student.getCollegeId())
                        .eq(Batch::getMajorId, student.getMajorId())
                        .eq(Batch::getGrade, student.getGrade())
                        .eq(Batch::getStatus, (byte) 1)
                        .orderByDesc(Batch::getUpdatedAt)
                        .last("LIMIT 1")
        );
        return batch == null ? null : batch.getId();
    }

    private String resolveStudentNo(User student) {
        return StringUtils.hasText(student.getStudentNo()) ? student.getStudentNo() : student.getUsername();
    }

    private void syncProcessInstance(WorkflowItem item) {
        String stage = toProcessStage(item.getWorkflowType());
        if (!StringUtils.hasText(stage)) {
            return;
        }

        StudentTopic studentTopic = resolveStudentTopic(item);
        if (studentTopic == null) {
            return;
        }

        if ("draft".equals(item.getStatus())) {
            deleteSyncedProcessInstance(item);
            return;
        }

        ProcessInstance process = processInstanceMapper.selectOne(
                new LambdaQueryWrapper<ProcessInstance>()
                        .eq(ProcessInstance::getStudentTopicId, studentTopic.getId())
                        .eq(ProcessInstance::getStage, stage)
                        .last("LIMIT 1")
        );
        boolean isNew = process == null;
        if (isNew) {
            process = new ProcessInstance();
            process.setStudentTopicId(studentTopic.getId());
            process.setStage(stage);
            process.setVersion(1);
            process.setCreatedAt(LocalDateTime.now());
            process.setIsEditable((byte) 0);
        } else {
            process.setVersion(process.getVersion() == null ? 1 : process.getVersion() + 1);
        }

        process.setStatus(toProcessStatus(item.getStatus()));
        process.setSubmitterId(item.getUpdatedBy() != null ? item.getUpdatedBy() : item.getCreatedBy());
        process.setSubmittedAt(item.getUpdatedAt() != null ? item.getUpdatedAt() : item.getCreatedAt());
        process.setContent(buildProcessContent(item));
        process.setFilePath(resolveFilePath(item));
        process.setReviewComment(item.getComment());
        process.setUpdatedAt(LocalDateTime.now());
        process.setReviewedAt("approved".equals(process.getStatus()) || "rejected".equals(process.getStatus()) ? LocalDateTime.now() : null);
        process.setReviewerId("approved".equals(process.getStatus()) || "rejected".equals(process.getStatus()) ? UserContext.getUserId() : null);
        process.setIsEditable("rejected".equals(process.getStatus()) ? (byte) 1 : (byte) 0);

        if (isNew) {
            processInstanceMapper.insert(process);
        } else {
            processInstanceMapper.updateById(process);
        }
    }

    private void deleteSyncedProcessInstance(WorkflowItem item) {
        String stage = toProcessStage(item.getWorkflowType());
        if (!StringUtils.hasText(stage)) {
            return;
        }
        StudentTopic studentTopic = resolveStudentTopic(item);
        if (studentTopic == null) {
            return;
        }
        processInstanceMapper.delete(
                new LambdaQueryWrapper<ProcessInstance>()
                        .eq(ProcessInstance::getStudentTopicId, studentTopic.getId())
                        .eq(ProcessInstance::getStage, stage)
        );
    }

    private StudentTopic resolveStudentTopic(WorkflowItem item) {
        if (!StringUtils.hasText(item.getStudentNo())) {
            return null;
        }
        User student = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .and(wrapper -> wrapper
                                .eq(User::getStudentNo, item.getStudentNo())
                                .or()
                                .eq(User::getUsername, item.getStudentNo()))
                        .last("LIMIT 1")
        );
        if (student == null) {
            return null;
        }
        LambdaQueryWrapper<StudentTopic> wrapper = new LambdaQueryWrapper<StudentTopic>()
                .eq(StudentTopic::getStudentId, student.getId())
                .eq(StudentTopic::getStatus, "active")
                .orderByDesc(StudentTopic::getAllocationTime)
                .orderByDesc(StudentTopic::getCreatedAt)
                .last("LIMIT 1");
        if (item.getBatchId() != null) {
            wrapper.eq(StudentTopic::getBatchId, item.getBatchId());
        }
        return studentTopicMapper.selectOne(wrapper);
    }

    private String toProcessStage(String workflowType) {
        return switch (workflowType) {
            case "taskBook" -> "task_book";
            case "openingReport" -> "opening_report";
            case "openingDefense" -> "opening_defense";
            case "weeklyLog", "thesisGuidance" -> "guidance_week";
            case "midterm" -> "midterm_check";
            case "postDefenseRevision" -> "post_defense_modify";
            case "finalThesis", "finalDesign" -> "thesis_final";
            default -> null;
        };
    }

    private String toProcessStatus(String status) {
        if ("approved".equals(status) || "rejected".equals(status)) {
            return status;
        }
        return "submitted";
    }

    private String buildProcessContent(WorkflowItem item) {
        return java.util.stream.Stream.of(item.getTitle(), item.getExtra(), item.getRemark())
                .filter(StringUtils::hasText)
                .collect(Collectors.joining("\n"));
    }

    private String resolveFilePath(WorkflowItem item) {
        String extra = item.getExtra();
        if (!StringUtils.hasText(extra)) {
            return null;
        }
        return extra.startsWith("/uploads") || extra.startsWith("http") || extra.contains("/uploads/")
                ? extra
                : null;
    }

}
