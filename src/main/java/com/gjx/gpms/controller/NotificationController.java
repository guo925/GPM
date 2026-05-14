package com.gjx.gpms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gjx.gpms.common.result.Result;
import com.gjx.gpms.entity.Notification;
import com.gjx.gpms.mapper.NotificationMapper;
import com.gjx.gpms.security.context.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "通知管理")
@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationMapper notificationMapper;

    @Operation(summary = "我的通知")
    @GetMapping("/page")
    public Result<IPage<Notification>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        Page<Notification> page = new Page<>(current, size);
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getRecipientId, UserContext.getUserId());
        wrapper.orderByDesc(Notification::getCreatedAt);
        return Result.success(notificationMapper.selectPage(page, wrapper));
    }

    @Operation(summary = "未读数量")
    @GetMapping("/unread-count")
    public Result<Long> unreadCount() {
        long count = notificationMapper.selectCount(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getRecipientId, UserContext.getUserId())
                        .eq(Notification::getIsRead, (byte) 0)
        );
        return Result.success(count);
    }

    @Operation(summary = "标记已读")
    @PutMapping("/read/{id}")
    public Result<Void> markRead(@PathVariable Long id) {
        Notification n = notificationMapper.selectById(id);
        if (n != null) {
            n.setIsRead((byte) 1);
            n.setReadAt(LocalDateTime.now());
            notificationMapper.updateById(n);
        }
        return Result.success();
    }
}
