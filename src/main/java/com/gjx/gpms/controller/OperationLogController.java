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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * OperationLog 控制器。
 */
@Tag(name = "操作日志")
@RestController
@RequestMapping("/api/log/operation")
@RequiredArgsConstructor
public class OperationLogController {

    private final OperationLogMapper operationLogMapper;
    private final JdbcTemplate jdbcTemplate;

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

    /**
     * 操作日志统计
     */
    @Operation(summary = "操作日志统计")
    @PreAuthorize("hasAuthority('log:statistics')")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> statistics() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalLogs", queryLong("SELECT COUNT(*) FROM operation_log"));
        data.put("todayLogs", queryLong("SELECT COUNT(*) FROM operation_log WHERE DATE(created_at) = CURDATE()"));
        data.put("activeUsers", queryLong("SELECT COUNT(DISTINCT user_id) FROM operation_log WHERE user_id IS NOT NULL"));
        data.put("todayActiveUsers", queryLong("SELECT COUNT(DISTINCT user_id) FROM operation_log WHERE user_id IS NOT NULL AND DATE(created_at) = CURDATE()"));
        data.put("actionStats", queryList("SELECT action AS name, COUNT(*) AS count FROM operation_log GROUP BY action ORDER BY count DESC LIMIT 8"));
        data.put("targetStats", queryList("SELECT target_type AS name, COUNT(*) AS count FROM operation_log WHERE target_type IS NOT NULL GROUP BY target_type ORDER BY count DESC LIMIT 8"));
        data.put("dailyTrend", queryList("SELECT DATE(created_at) AS date, COUNT(*) AS count FROM operation_log WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) GROUP BY DATE(created_at) ORDER BY date ASC"));
        return Result.success(data);
    }

    /**
     * 处理queryLong相关逻辑。
     */
    private Long queryLong(String sql) {
        try {
            Long value = jdbcTemplate.queryForObject(sql, Long.class);
            return value == null ? 0L : value;
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * 处理queryList相关逻辑。
     */
    private List<Map<String, Object>> queryList(String sql) {
        try {
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            return List.of();
        }
    }
}
