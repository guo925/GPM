package com.gjx.gpms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gjx.gpms.common.exception.BusinessException;
import com.gjx.gpms.dto.WorkflowItemDTO;
import com.gjx.gpms.dto.WorkflowReviewDTO;
import com.gjx.gpms.entity.Batch;
import com.gjx.gpms.entity.StudentTopic;
import com.gjx.gpms.entity.WorkflowItem;
import com.gjx.gpms.mapper.BatchMapper;
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
            wrapper.eq(WorkflowItem::getStudentName, currentUser.getRealName());
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
        item.setStudentNo(student.getUsername());

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
            item.setBatchId(resolveBatchId(dto.getGrade()));
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
    }

    @Override
    public void delete(Long id) {
        WorkflowItem item = workflowItemMapper.selectById(id);
        if (item == null) {
            throw new BusinessException("流程事项不存在");
        }
        checkOwnership(item);
        workflowItemMapper.deleteById(id);
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
                && !currentUser.getRealName().equals(item.getStudentName())) {
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

}
