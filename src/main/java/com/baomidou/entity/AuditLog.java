package com.baomidou.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 关键操作审计日志
 * </p>
 *
 * @author baomidou
 * @since 2026-05-11
 */
@Data
@TableName("audit_log")
@ApiModel(value = "AuditLog对象", description = "关键操作审计日志")
public class AuditLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    @ApiModelProperty("日志ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 操作人ID（可能为学生/教师，通过user_type区分）
     */
    @ApiModelProperty("操作人ID（可能为学生/教师，通过user_type区分）")
    private Integer userId;

    /**
     * 用户类型
     */
    @ApiModelProperty("用户类型")
    private String userType;

    /**
     * 操作类型
     */
    @ApiModelProperty("操作类型")
    private String operation;

    /**
     * 操作目标类型
     */
    @ApiModelProperty("操作目标类型")
    private String targetType;

    /**
     * 操作目标ID
     */
    @ApiModelProperty("操作目标ID")
    private Integer targetId;

    /**
     * 操作详情
     */
    @ApiModelProperty("操作详情")
    private String detail;

    /**
     * 操作IP
     */
    @ApiModelProperty("操作IP")
    private String ipAddress;

    /**
     * 操作时间
     */
    @ApiModelProperty("操作时间")
    private LocalDateTime createdAt;
}
