package com.gjx.gpms.service;

import com.gjx.gpms.dto.WorkflowItemDTO;
import com.gjx.gpms.dto.WorkflowReviewDTO;
import com.gjx.gpms.entity.WorkflowItem;

import java.util.List;

/**
 * 通用流程事项服务
 */
public interface WorkflowItemService {
    List<WorkflowItem> list(String workflowType, Long batchId, String keyword, String status);
    void save(WorkflowItemDTO dto);
    void review(WorkflowReviewDTO dto);
    void delete(Long id);
}
