package com.gjx.gpms.controller;

import com.gjx.gpms.common.result.Result;
import com.gjx.gpms.dto.WorkflowItemDTO;
import com.gjx.gpms.dto.WorkflowReviewDTO;
import com.gjx.gpms.entity.WorkflowItem;
import com.gjx.gpms.service.WorkflowItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通用流程事项控制器
 */
@Tag(name = "通用流程事项")
@RestController
@RequestMapping("/api/workflow")
@RequiredArgsConstructor
public class WorkflowItemController {

    private static final String WORKFLOW_ROLES =
            "hasAnyRole('SUPER_ADMIN','UNIVERSITY_ADMIN','COLLEGE_ADMIN','GRADE_ADMIN','MAJOR_ADMIN','TEACHER','STUDENT')";

    private final WorkflowItemService workflowItemService;

    @Operation(summary = "流程事项列表")
    @PreAuthorize(WORKFLOW_ROLES)
    @GetMapping("/items")
    public Result<List<WorkflowItem>> list(
            @RequestParam String workflowType,
            @RequestParam(required = false) Long batchId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return Result.success(workflowItemService.list(workflowType, batchId, keyword, status));
    }

    @Operation(summary = "保存流程事项")
    @PreAuthorize(WORKFLOW_ROLES)
    @PostMapping("/item")
    public Result<Void> save(@Valid @RequestBody WorkflowItemDTO dto) {
        workflowItemService.save(dto);
        return Result.success();
    }

    @Operation(summary = "审核或评分流程事项")
    @PreAuthorize(WORKFLOW_ROLES)
    @PutMapping("/review")
    public Result<Void> review(@Valid @RequestBody WorkflowReviewDTO dto) {
        workflowItemService.review(dto);
        return Result.success();
    }

    @Operation(summary = "删除流程事项")
    @PreAuthorize(WORKFLOW_ROLES)
    @DeleteMapping("/item/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        workflowItemService.delete(id);
        return Result.success();
    }
}
