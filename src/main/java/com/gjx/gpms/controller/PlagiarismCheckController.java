package com.gjx.gpms.controller;

import com.gjx.gpms.common.result.Result;
import com.gjx.gpms.dto.PlagiarismCheckDTO;
import com.gjx.gpms.service.PlagiarismCheckService;
import com.gjx.gpms.vo.PlagiarismCheckVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 查重Controller
 */
@Tag(name = "论文查重")
@RestController
@RequestMapping("/api/plagiarism")
@RequiredArgsConstructor
public class PlagiarismCheckController {

    private final PlagiarismCheckService plagiarismCheckService;

    @Operation(summary = "提交查重")
    @PreAuthorize("hasAuthority('plagiarism:check')")
    @PostMapping("/check")
    public Result<PlagiarismCheckVO> check(@Valid @RequestBody PlagiarismCheckDTO dto) {
        return Result.success(plagiarismCheckService.check(dto));
    }
}
