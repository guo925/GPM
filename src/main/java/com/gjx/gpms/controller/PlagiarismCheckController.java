package com.gjx.gpms.controller;

import com.gjx.gpms.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 查重Controller
 */
@Tag(name = "论文查重")
@RestController
@RequestMapping("/api/plagiarism")
@RequiredArgsConstructor
public class PlagiarismCheckController {

    @Operation(summary = "提交查重")
    @PreAuthorize("hasAuthority('plagiarism:check')")
    @PostMapping("/check")
    public Result<Map<String, Object>> check(@RequestBody Map<String, Object> params) {
        // 对接查重服务（知网、维普等）
        // 当前返回模拟结果
        return Result.success(Map.of(
                "similarity", 12.5,
                "status", "pass",
                "reportUrl", ""
        ));
    }
}
