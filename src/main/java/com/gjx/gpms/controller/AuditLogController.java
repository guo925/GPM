package com.gjx.gpms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gjx.gpms.common.result.Result;
import com.gjx.gpms.entity.AuditLog;
import com.gjx.gpms.mapper.AuditLogMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "审核日志")
@RestController
@RequestMapping("/api/log/audit")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogMapper auditLogMapper;

    @Operation(summary = "审核日志分页")
    @PreAuthorize("hasAuthority('audit-log:page')")
    @GetMapping("/page")
    public Result<IPage<AuditLog>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long processInstanceId) {
        Page<AuditLog> page = new Page<>(current, size);
        LambdaQueryWrapper<AuditLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(processInstanceId != null, AuditLog::getProcessInstanceId, processInstanceId);
        wrapper.orderByDesc(AuditLog::getCreatedAt);
        return Result.success(auditLogMapper.selectPage(page, wrapper));
    }
}
