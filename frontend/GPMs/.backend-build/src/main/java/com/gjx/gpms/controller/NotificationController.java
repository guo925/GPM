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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Notification 控制器。
 */
@Tag(name = "通知管理")
@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private static final Long PUBLIC_RECIPIENT_ID = 0L;

    private final NotificationMapper notificationMapper;
    private final JdbcTemplate jdbcTemplate;

    @Operation(summary = "我的通知")
    @GetMapping("/page")
    public Result<IPage<Notification>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        Long userId = UserContext.getUserId();
        long safeCurrent = Math.max(current, 1);
        long safeSize = Math.max(size, 1);
        long offset = (safeCurrent - 1) * safeSize;
        Long total = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM notification n
                WHERE n.recipient_id=? OR (n.recipient_id=? AND n.type='announcement')
                """, Long.class, userId, PUBLIC_RECIPIENT_ID);
        List<Notification> records = jdbcTemplate.query("""
                SELECT n.id,
                       n.recipient_id,
                       n.title,
                       n.content,
                       n.type,
                       CASE
                           WHEN n.recipient_id=? AND n.type='announcement' THEN
                               CASE WHEN EXISTS (
                                   SELECT 1 FROM announcement_read ar
                                   WHERE ar.announcement_id=n.id AND ar.user_id=?
                               ) THEN 1 ELSE 0 END
                           ELSE n.is_read
                       END AS is_read,
                       n.read_at,
                       n.created_at
                FROM notification n
                WHERE n.recipient_id=? OR (n.recipient_id=? AND n.type='announcement')
                ORDER BY n.created_at DESC
                LIMIT ? OFFSET ?
                """, (rs, rowNum) -> {
                    Notification notification = new Notification();
                    notification.setId(rs.getLong("id"));
                    notification.setRecipientId(rs.getLong("recipient_id"));
                    notification.setTitle(rs.getString("title"));
                    notification.setContent(rs.getString("content"));
                    notification.setType(rs.getString("type"));
                    notification.setIsRead(rs.getByte("is_read"));
                    notification.setReadAt(toLocalDateTime(rs.getTimestamp("read_at")));
                    notification.setCreatedAt(toLocalDateTime(rs.getTimestamp("created_at")));
                    return notification;
                }, PUBLIC_RECIPIENT_ID, userId, userId, PUBLIC_RECIPIENT_ID, safeSize, offset);
        Page<Notification> page = new Page<>(safeCurrent, safeSize);
        page.setTotal(total == null ? 0 : total);
        page.setRecords(records);
        return Result.success(page);
    }

    /**
     * 未读数量
     */
    @Operation(summary = "未读数量")
    @GetMapping("/unread-count")
    public Result<Long> unreadCount() {
        long count = notificationMapper.selectCount(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getRecipientId, UserContext.getUserId())
                        .eq(Notification::getIsRead, (byte) 0)
        );
        Long announcementCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM notification n
                WHERE n.recipient_id=0 AND n.type='announcement'
                AND NOT EXISTS (
                    SELECT 1 FROM announcement_read ar
                    WHERE ar.announcement_id=n.id AND ar.user_id=?
                )
                """, Long.class, UserContext.getUserId());
        return Result.success(count + (announcementCount == null ? 0 : announcementCount));
    }

    /**
     * 标记已读
     */
    @Operation(summary = "标记已读")
    @PutMapping("/read/{id}")
    public Result<Void> markRead(@PathVariable Long id) {
        Notification n = notificationMapper.selectById(id);
        if (n != null) {
            if (PUBLIC_RECIPIENT_ID.equals(n.getRecipientId()) && "announcement".equals(n.getType())) {
                jdbcTemplate.update(
                        "INSERT IGNORE INTO announcement_read(announcement_id, user_id, read_at) VALUES (?,?,?)",
                        id,
                        UserContext.getUserId(),
                        LocalDateTime.now()
                );
            } else if (UserContext.getUserId().equals(n.getRecipientId())) {
                n.setIsRead((byte) 1);
                n.setReadAt(LocalDateTime.now());
                notificationMapper.updateById(n);
            }
        }
        return Result.success();
    }

    /**
     * 转换local date time相关逻辑。
     */
    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
