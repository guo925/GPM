package com.gjx.gpms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gjx.gpms.common.exception.BusinessException;
import com.gjx.gpms.common.result.Result;
import com.gjx.gpms.dto.AnnouncementDTO;
import com.gjx.gpms.entity.Notification;
import com.gjx.gpms.mapper.NotificationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "公告管理")
@RestController
@RequestMapping("/api/announcement")
@RequiredArgsConstructor
public class AnnouncementController {

    private static final Long PUBLIC_RECIPIENT_ID = 0L;
    private static final String ANNOUNCEMENT_TYPE = "announcement";

    private final NotificationMapper notificationMapper;
    private final JdbcTemplate jdbcTemplate;

    @Operation(summary = "公告分页")
    @PreAuthorize("hasAuthority('announcement:page')")
    @GetMapping("/page")
    public Result<IPage<Notification>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword) {
        Page<Notification> page = new Page<>(current, size);
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getType, ANNOUNCEMENT_TYPE);
        wrapper.eq(Notification::getRecipientId, PUBLIC_RECIPIENT_ID);
        wrapper.and(keyword != null && !keyword.isBlank(), w -> w
                .like(Notification::getTitle, keyword)
                .or()
                .like(Notification::getContent, keyword));
        wrapper.orderByDesc(Notification::getCreatedAt);
        return Result.success(notificationMapper.selectPage(page, wrapper));
    }

    @Operation(summary = "发布公告")
    @PreAuthorize("hasAuthority('announcement:add')")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody AnnouncementDTO dto) {
        Notification notification = new Notification();
        notification.setRecipientId(PUBLIC_RECIPIENT_ID);
        notification.setTitle(dto.getTitle());
        notification.setContent(dto.getContent());
        notification.setType(ANNOUNCEMENT_TYPE);
        notification.setIsRead((byte) 0);
        notification.setCreatedAt(LocalDateTime.now());
        notificationMapper.insert(notification);
        return Result.success();
    }

    @Operation(summary = "修改公告")
    @PreAuthorize("hasAuthority('announcement:update')")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody AnnouncementDTO dto) {
        Notification notification = getAnnouncement(id);
        notification.setTitle(dto.getTitle());
        notification.setContent(dto.getContent());
        notificationMapper.updateById(notification);
        return Result.success();
    }

    @Operation(summary = "删除公告")
    @PreAuthorize("hasAuthority('announcement:delete')")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        getAnnouncement(id);
        jdbcTemplate.update("DELETE FROM announcement_read WHERE announcement_id=?", id);
        notificationMapper.deleteById(id);
        return Result.success();
    }

    private Notification getAnnouncement(Long id) {
        Notification notification = notificationMapper.selectById(id);
        if (notification == null
                || !ANNOUNCEMENT_TYPE.equals(notification.getType())
                || !PUBLIC_RECIPIENT_ID.equals(notification.getRecipientId())) {
            throw new BusinessException("公告不存在");
        }
        return notification;
    }
}
