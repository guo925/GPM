package com.gjx.gpms.controller;

import com.gjx.gpms.common.result.Result;
import com.gjx.gpms.dto.ProcessReviewDTO;
import com.gjx.gpms.dto.ProcessSubmitDTO;
import com.gjx.gpms.service.ProcessInstanceService;
import com.gjx.gpms.vo.ProcessInstanceVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 流程实例Controller
 *
 * @author gpms
 */
@Tag(name = "流程管理")
@RestController
@RequestMapping("/api/process")
@RequiredArgsConstructor
public class ProcessInstanceController {

    private final ProcessInstanceService processInstanceService;

    @Operation(summary = "提交阶段任务")
    @PreAuthorize("hasAuthority('process:submit')")
    @PostMapping("/submit")
    public Result<Void> submit(@Valid @RequestBody ProcessSubmitDTO dto) {
        processInstanceService.submit(dto);
        return Result.success();
    }

    @Operation(summary = "审核阶段任务")
    @PreAuthorize("hasAuthority('process:review')")
    @PutMapping("/review")
    public Result<Void> review(@Valid @RequestBody ProcessReviewDTO dto) {
        processInstanceService.review(dto);
        return Result.success();
    }

    @Operation(summary = "查看流程记录")
    @PreAuthorize("hasAuthority('process:query')")
    @GetMapping("/list/{studentTopicId}")
    public Result<List<ProcessInstanceVO>> getByStudentTopic(@PathVariable Long studentTopicId) {
        return Result.success(processInstanceService.getByStudentTopic(studentTopicId));
    }

    @Operation(summary = "查看当前阶段")
    @PreAuthorize("hasAuthority('process:query')")
    @GetMapping("/stage")
    public Result<ProcessInstanceVO> getCurrentStage(
            @RequestParam Long studentTopicId,
            @RequestParam String stage) {
        return Result.success(processInstanceService.getCurrentStage(studentTopicId, stage));
    }
}
