package com.gjx.gpms.controller;

import com.gjx.gpms.archive.ArchiveService;
import com.gjx.gpms.common.result.Result;
import com.gjx.gpms.security.context.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 冷热数据归档 Controller。
 */
@Tag(name = "冷热数据归档")
@RestController
@RequestMapping("/api/archive")
@RequiredArgsConstructor
public class ArchiveController {

    private final ArchiveService archiveService;
    private final JdbcTemplate jdbcTemplate;

    @Operation(summary = "手动归档指定批次")
    @PreAuthorize("hasAuthority('batch:stage')")
    @PostMapping("/batch/{batchId}")
    public Result<Void> archiveBatch(@PathVariable Long batchId) {
        archiveService.archiveBatch(batchId, UserContext.getUserId());
        return Result.success();
    }

    @Operation(summary = "归档所有历史批次")
    @PreAuthorize("hasAuthority('batch:stage')")
    @PostMapping("/history-batches")
    public Result<Void> archiveHistoryBatches() {
        archiveService.archiveHistoryBatches(UserContext.getUserId());
        return Result.success();
    }

    @Operation(summary = "归档日志")
    @PreAuthorize("hasAuthority('batch:stage')")
    @GetMapping("/logs")
    public Result<List<Map<String, Object>>> logs(@RequestParam(defaultValue = "20") int limit) {
        return Result.success(jdbcTemplate.queryForList(
                "SELECT * FROM archive_log ORDER BY created_at DESC LIMIT ?",
                Math.min(Math.max(limit, 1), 100)
        ));
    }
}
