package com.gjx.gpms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 通知消息
 * </p>
 *
 * @author gpms
 * @since 2026-05-12
 */
@Data
@ApiModel(value = "Notification对象", description = "通知消息")
public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 接收人用户ID
     */
    @ApiModelProperty("接收人用户ID")
    private Long recipientId;

    private String title;

    private String content;

    /**
     * system/deadline/audit
     */
    @ApiModelProperty("system/deadline/audit")
    private String type;

    private Byte isRead;

    private LocalDateTime readAt;

    private LocalDateTime createdAt;
}
