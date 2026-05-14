package com.gjx.gpms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gjx.gpms.common.result.Result;
import com.gjx.gpms.entity.OperationLog;
import com.gjx.gpms.mapper.OperationLogMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "操作日志")
@RestController
@RequestMapping("/api/log/operation")
@RequiredArgsConstructor
public class OperationLogController {

    private final OperationLogMapper operationLogMapper;

    @Operation(summary = "操作日志分页")
    @PreAuthorize("hasAuthority('log:page')")
    @GetMapping("/page")
    public Result<IPage<OperationLog>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String action) {
        Page<OperationLog> page = new Page<>(current, size);
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(userId != null, OperationLog::getUserId, userId);
        wrapper.like(action != null, OperationLog::getAction, action);
        wrapper.orderByDesc(OperationLog::getCreatedAt);
        return Result.success(operationLogMapper.selectPage(page, wrapper));
    }
}
