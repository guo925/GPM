package com.gjx.gpms.controller;

import com.gjx.gpms.common.result.Result;
import com.gjx.gpms.dto.ScoreSheetDTO;
import com.gjx.gpms.service.ScoreService;
import com.gjx.gpms.vo.ScoreSheetVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Score 控制器。
 */
@Tag(name = "成绩管理")
@RestController
@RequestMapping("/api/score")
@RequiredArgsConstructor
public class ScoreController {

    private final ScoreService scoreService;

    /**
     * 计算/更新成绩
     */
    @Operation(summary = "计算/更新成绩")
    @PreAuthorize("hasAuthority('score:calculate')")
    @PostMapping("/calculate")
    public Result<ScoreSheetVO> calculate(@Valid @RequestBody ScoreSheetDTO dto) {
        return Result.success(scoreService.calculate(dto));
    }

    /**
     * 提交成绩
     */
    @Operation(summary = "提交成绩")
    @PreAuthorize("hasAuthority('score:submit')")
    @PutMapping("/submit/{id}")
    public Result<Void> submit(@PathVariable Long id) {
        scoreService.submit(id);
        return Result.success();
    }

    /**
     * 审核成绩
     */
    @Operation(summary = "审核成绩")
    @PreAuthorize("hasAuthority('score:review')")
    @PutMapping("/review/{id}")
    public Result<Void> review(@PathVariable Long id, @RequestBody Map<String, String> params) {
        scoreService.review(id, params.get("status"), params.get("comment"));
        return Result.success();
    }

    /**
     * 成绩详情
     */
    @Operation(summary = "成绩详情")
    @GetMapping("/detail/{studentTopicId}")
    public Result<ScoreSheetVO> getDetail(@PathVariable Long studentTopicId) {
        return Result.success(scoreService.getDetail(studentTopicId));
    }

    /**
     * 批次成绩列表
     */
    @Operation(summary = "批次成绩列表")
    @GetMapping("/batch/{batchId}")
    public Result<List<ScoreSheetVO>> listByBatch(@PathVariable Long batchId) {
        return Result.success(scoreService.listByBatch(batchId));
    }
}
