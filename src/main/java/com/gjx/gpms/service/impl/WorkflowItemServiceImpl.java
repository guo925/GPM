package com.gjx.gpms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gjx.gpms.common.exception.BusinessException;
import com.gjx.gpms.dto.WorkflowItemDTO;
import com.gjx.gpms.dto.WorkflowReviewDTO;
import com.gjx.gpms.entity.WorkflowItem;
import com.gjx.gpms.mapper.WorkflowItemMapper;
import com.gjx.gpms.security.context.UserContext;
import com.gjx.gpms.service.WorkflowItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 通用流程事项服务实现
 */
@Service
@RequiredArgsConstructor
public class WorkflowItemServiceImpl implements WorkflowItemService {

    private final WorkflowItemMapper workflowItemMapper;

    @Override
    @Transactional
    public List<WorkflowItem> list(String workflowType, Long batchId, String keyword, String status) {
        if (!StringUtils.hasText(workflowType)) {
            throw new BusinessException("流程类型不能为空");
        }
        ensureSeedData(workflowType, batchId);
        LambdaQueryWrapper<WorkflowItem> wrapper = new LambdaQueryWrapper<WorkflowItem>()
                .eq(WorkflowItem::getWorkflowType, workflowType)
                .eq(batchId != null, WorkflowItem::getBatchId, batchId)
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
        return workflowItemMapper.selectList(wrapper);
    }

    @Override
    public void save(WorkflowItemDTO dto) {
        WorkflowItem item = dto.getId() == null ? new WorkflowItem() : workflowItemMapper.selectById(dto.getId());
        if (item == null) {
            throw new BusinessException("流程事项不存在");
        }
        BeanUtils.copyProperties(dto, item);
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

    @Override
    public void review(WorkflowReviewDTO dto) {
        WorkflowItem item = workflowItemMapper.selectById(dto.getId());
        if (item == null) {
            throw new BusinessException("流程事项不存在");
        }
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
        if (workflowItemMapper.selectById(id) == null) {
            throw new BusinessException("流程事项不存在");
        }
        workflowItemMapper.deleteById(id);
    }

    private void ensureSeedData(String workflowType, Long batchId) {
        LambdaQueryWrapper<WorkflowItem> countWrapper = new LambdaQueryWrapper<WorkflowItem>()
                .eq(WorkflowItem::getWorkflowType, workflowType)
                .eq(batchId != null, WorkflowItem::getBatchId, batchId);
        Long count = workflowItemMapper.selectCount(countWrapper);
        if (count != null && count > 0) {
            return;
        }
        insertSeed(batchId, workflowType, "李明", "20260001", "程丽华", "基于管理系统的毕业设计", "pending", BigDecimal.valueOf(86), "按当前批次要求提交。", "");
        insertSeed(batchId, workflowType, "王佳", "20260002", "张老师", "数据可视化设计", "approved", BigDecimal.valueOf(91), "材料完整，等待归档。", "通过。");
        insertSeed(batchId, workflowType, "陈航", "20260003", "刘老师", "智能审核流程优化", "rejected", BigDecimal.valueOf(72), "需要补充说明。", "请完善关键内容。");
    }

    private void insertSeed(Long batchId, String workflowType, String studentName, String studentNo, String advisorName,
                            String title, String status, BigDecimal score, String remark, String comment) {
        WorkflowItem item = new WorkflowItem();
        item.setBatchId(batchId);
        item.setWorkflowType(workflowType);
        item.setStudentName(studentName);
        item.setStudentNo(studentNo);
        item.setAdvisorName(advisorName);
        item.setTitle(title);
        item.setStatus(status);
        item.setScore(score);
        item.setRemark(remark);
        item.setComment(comment);
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        workflowItemMapper.insert(item);
    }
}
