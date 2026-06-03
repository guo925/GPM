package com.gjx.gpms.controller;

import com.gjx.gpms.common.exception.BusinessException;
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
import java.util.Map;

/**
 * 图片菜单对应的逐项功能接口。
 */
@Tag(name = "论文/成绩/特殊情况功能接口")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FeatureWorkflowController {

    private static final String WORKFLOW_ROLES =
            "hasAnyRole('SUPER_ADMIN','UNIVERSITY_ADMIN','COLLEGE_ADMIN','GRADE_ADMIN','MAJOR_ADMIN','TEACHER','STUDENT')";
    private static final String REVIEW_ROLES =
            "hasAnyRole('SUPER_ADMIN','UNIVERSITY_ADMIN','COLLEGE_ADMIN','GRADE_ADMIN','MAJOR_ADMIN','TEACHER')";

    private static final Map<String, String> THESIS_TYPES = Map.ofEntries(
            Map.entry("task-book", "taskBook"),
            Map.entry("opening-report", "openingReport"),
            Map.entry("opening-defense", "openingDefense"),
            Map.entry("opening-minutes", "openingMinutes"),
            Map.entry("weekly-log", "weeklyLog"),
            Map.entry("midterm", "midterm"),
            Map.entry("guidance", "thesisGuidance"),
            Map.entry("post-defense-revision", "postDefenseRevision"),
            Map.entry("final-thesis", "finalThesis"),
            Map.entry("final-design", "finalDesign")
    );

    private static final Map<String, String> SCORE_TYPES = Map.of(
            "advisor", "advisorScore",
            "reviewer", "reviewerScore",
            "deputy-review", "deputyReview",
            "history", "scoreHistory",
            "special-advisor", "specialAdvisorScore"
    );

    private static final Map<String, String> SPECIAL_TYPES = Map.of(
            "title-change", "titleChange",
            "extension", "extension",
            "advisor-score", "specialAdvisorScore",
            "attachment-review", "attachmentReview",
            "completion-edit-review", "completionEditReview"
    );

    private final WorkflowItemService workflowItemService;

    @Operation(summary = "论文管理功能列表")
    @PreAuthorize(WORKFLOW_ROLES)
    @GetMapping("/thesis/{feature}")
    public Result<List<WorkflowItem>> listThesis(@PathVariable String feature,
                                                 @RequestParam(required = false) Long batchId,
                                                 @RequestParam(required = false) String grade,
                                                 @RequestParam(required = false) String keyword,
                                                 @RequestParam(required = false) String status) {
        return Result.success(workflowItemService.list(resolve(THESIS_TYPES, feature), batchId, grade, keyword, status));
    }

    @Operation(summary = "论文管理功能保存")
    @PreAuthorize(WORKFLOW_ROLES)
    @PostMapping("/thesis/{feature}")
    public Result<Void> saveThesis(@PathVariable String feature, @Valid @RequestBody WorkflowItemDTO dto) {
        save(resolve(THESIS_TYPES, feature), dto);
        return Result.success();
    }

    @Operation(summary = "论文管理功能审核")
    @PreAuthorize(REVIEW_ROLES)
    @PutMapping("/thesis/{feature}/review")
    public Result<Void> reviewThesis(@PathVariable String feature, @Valid @RequestBody WorkflowReviewDTO dto) {
        resolve(THESIS_TYPES, feature);
        workflowItemService.review(dto);
        return Result.success();
    }

    @Operation(summary = "论文管理功能删除")
    @PreAuthorize(WORKFLOW_ROLES)
    @DeleteMapping("/thesis/{feature}/{id}")
    public Result<Void> deleteThesis(@PathVariable String feature, @PathVariable Long id) {
        resolve(THESIS_TYPES, feature);
        workflowItemService.delete(id);
        return Result.success();
    }

    @Operation(summary = "成绩评定功能列表")
    @PreAuthorize(WORKFLOW_ROLES)
    @GetMapping("/score-workflow/{feature}")
    public Result<List<WorkflowItem>> listScore(@PathVariable String feature,
                                                @RequestParam(required = false) Long batchId,
                                                @RequestParam(required = false) String grade,
                                                @RequestParam(required = false) String keyword,
                                                @RequestParam(required = false) String status) {
        return Result.success(workflowItemService.list(resolve(SCORE_TYPES, feature), batchId, grade, keyword, status));
    }

    @Operation(summary = "成绩评定功能保存")
    @PreAuthorize(WORKFLOW_ROLES)
    @PostMapping("/score-workflow/{feature}")
    public Result<Void> saveScore(@PathVariable String feature, @Valid @RequestBody WorkflowItemDTO dto) {
        save(resolve(SCORE_TYPES, feature), dto);
        return Result.success();
    }

    @Operation(summary = "成绩评定功能审核或评分")
    @PreAuthorize(REVIEW_ROLES)
    @PutMapping("/score-workflow/{feature}/review")
    public Result<Void> reviewScore(@PathVariable String feature, @Valid @RequestBody WorkflowReviewDTO dto) {
        resolve(SCORE_TYPES, feature);
        workflowItemService.review(dto);
        return Result.success();
    }

    @Operation(summary = "成绩评定功能删除")
    @PreAuthorize(WORKFLOW_ROLES)
    @DeleteMapping("/score-workflow/{feature}/{id}")
    public Result<Void> deleteScore(@PathVariable String feature, @PathVariable Long id) {
        resolve(SCORE_TYPES, feature);
        workflowItemService.delete(id);
        return Result.success();
    }

    @Operation(summary = "特殊情况功能列表")
    @PreAuthorize(WORKFLOW_ROLES)
    @GetMapping("/special/{feature}")
    public Result<List<WorkflowItem>> listSpecial(@PathVariable String feature,
                                                  @RequestParam(required = false) Long batchId,
                                                  @RequestParam(required = false) String grade,
                                                  @RequestParam(required = false) String keyword,
                                                  @RequestParam(required = false) String status) {
        return Result.success(workflowItemService.list(resolve(SPECIAL_TYPES, feature), batchId, grade, keyword, status));
    }

    @Operation(summary = "特殊情况功能保存")
    @PreAuthorize(WORKFLOW_ROLES)
    @PostMapping("/special/{feature}")
    public Result<Void> saveSpecial(@PathVariable String feature, @Valid @RequestBody WorkflowItemDTO dto) {
        save(resolve(SPECIAL_TYPES, feature), dto);
        return Result.success();
    }

    @Operation(summary = "特殊情况功能审核")
    @PreAuthorize(REVIEW_ROLES)
    @PutMapping("/special/{feature}/review")
    public Result<Void> reviewSpecial(@PathVariable String feature, @Valid @RequestBody WorkflowReviewDTO dto) {
        resolve(SPECIAL_TYPES, feature);
        workflowItemService.review(dto);
        return Result.success();
    }

    @Operation(summary = "特殊情况功能删除")
    @PreAuthorize(WORKFLOW_ROLES)
    @DeleteMapping("/special/{feature}/{id}")
    public Result<Void> deleteSpecial(@PathVariable String feature, @PathVariable Long id) {
        resolve(SPECIAL_TYPES, feature);
        workflowItemService.delete(id);
        return Result.success();
    }

    private String resolve(Map<String, String> mapping, String feature) {
        String workflowType = mapping.get(feature);
        if (workflowType == null) {
            throw new BusinessException("功能不存在");
        }
        return workflowType;
    }

    private void save(String workflowType, WorkflowItemDTO dto) {
        dto.setWorkflowType(workflowType);
        workflowItemService.save(dto);
    }
}
