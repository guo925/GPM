package com.gjx.gpms.controller;

import com.gjx.gpms.common.result.Result;
import com.gjx.gpms.dto.SelectionSubmitDTO;
import com.gjx.gpms.dto.TeacherReviewDTO;
import com.gjx.gpms.service.SelectionRecordService;
import com.gjx.gpms.vo.SelectionRecordVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 选题记录Controller
 *
 * @author gpms
 */
@Tag(name = "选题管理")
@RestController
@RequestMapping("/api/selection")
@RequiredArgsConstructor
public class SelectionRecordController {

    private final SelectionRecordService selectionRecordService;

    @Operation(summary = "提交选题志愿")
    @PreAuthorize("hasAuthority('selection:submit')")
    @PostMapping("/submit")
    public Result<Void> submitPreferences(@Valid @RequestBody SelectionSubmitDTO dto) {
        selectionRecordService.submitPreferences(dto);
        return Result.success();
    }

    @Operation(summary = "我的志愿")
    @PreAuthorize("hasAuthority('selection:my')")
    @GetMapping("/my")
    public Result<List<SelectionRecordVO>> getMySelections(@RequestParam(required = false) Long batchId) {
        return Result.success(selectionRecordService.getMySelections(batchId));
    }

    @Operation(summary = "教师审核列表")
    @PreAuthorize("hasAuthority('selection:review')")
    @GetMapping("/review-list")
    public Result<List<SelectionRecordVO>> getTeacherReviewList(@RequestParam(required = false) Long batchId) {
        return Result.success(selectionRecordService.getTeacherReviewList(batchId));
    }

    @Operation(summary = "教师审核")
    @PreAuthorize("hasAuthority('selection:review')")
    @PutMapping("/review")
    public Result<Void> teacherReview(@Valid @RequestBody TeacherReviewDTO dto) {
        selectionRecordService.teacherReview(dto);
        return Result.success();
    }

    @Operation(summary = "系统自动分配")
    @PreAuthorize("hasAuthority('selection:allocate')")
    @PostMapping("/auto-allocate/{batchId}")
    public Result<Void> autoAllocate(@PathVariable Long batchId) {
        selectionRecordService.autoAllocate(batchId);
        return Result.success();
    }
}
