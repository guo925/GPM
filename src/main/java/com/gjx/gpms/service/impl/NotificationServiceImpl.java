package com.gjx.gpms.service.impl;

import com.gjx.gpms.entity.Notification;
import com.gjx.gpms.mapper.NotificationMapper;
import com.gjx.gpms.service.INotificationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 通知消息 服务实现类
 * </p>
 *
 * @author gpms
 * @since 2026-05-12
 */
@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements INotificationService {

}
