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
 * 毕设批次及各阶段时间开关
 * </p>
 *
 * @author gpms
 * @since 2026-05-11
 */
@Data
@ApiModel(value = "Batch对象", description = "毕设批次及各阶段时间开关")
public class Batch implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 批次ID
     */
    @ApiModelProperty("批次ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 学年学期，如2025-2026-2
     */
    @ApiModelProperty("学年学期，如2025-2026-2")
    private String yearSemester;

    /**
     * 批次名称
     */
    @ApiModelProperty("批次名称")
    private String batchName;

    /**
     * 选题开始时间
     */
    @ApiModelProperty("选题开始时间")
    private LocalDateTime selectionStart;

    /**
     * 选题截止时间
     */
    @ApiModelProperty("选题截止时间")
    private LocalDateTime selectionEnd;

    /**
     * 提交开放时间
     */
    @ApiModelProperty("提交开放时间")
    private LocalDateTime submissionStart;

    /**
     * 提交截止时间
     */
    @ApiModelProperty("提交截止时间")
    private LocalDateTime submissionEnd;

    /**
     * 定稿截止日期
     */
    @ApiModelProperty("定稿截止日期")
    private LocalDateTime finalizeDeadline;

    /**
     * 评阅开始时间
     */
    @ApiModelProperty("评阅开始时间")
    private LocalDateTime reviewStart;

    /**
     * 评阅截止时间
     */
    @ApiModelProperty("评阅截止时间")
    private LocalDateTime reviewEnd;

    /**
     * 答辩开始时间
     */
    @ApiModelProperty("答辩开始时间")
    private LocalDateTime defenseStart;

    /**
     * 答辩结束时间
     */
    @ApiModelProperty("答辩结束时间")
    private LocalDateTime defenseEnd;

    /**
     * 成绩发布开放时间
     */
    @ApiModelProperty("成绩发布开放时间")
    private LocalDateTime scorePublishStart;

    /**
     * 选题阶段开关
     */
    @ApiModelProperty("选题阶段开关")
    private Boolean selectionEnabled;

    /**
     * 提交阶段开关
     */
    @ApiModelProperty("提交阶段开关")
    private Boolean submissionEnabled;

    /**
     * 评阅阶段开关
     */
    @ApiModelProperty("评阅阶段开关")
    private Boolean reviewEnabled;

    /**
     * 答辩阶段开关
     */
    @ApiModelProperty("答辩阶段开关")
    private Boolean defenseEnabled;

    /**
     * 成绩发布开关
     */
    @ApiModelProperty("成绩发布开关")
    private Boolean scorePublishEnabled;

    /**
     * 创建者(管理员教师ID)
     */
    @ApiModelProperty("创建者(管理员教师ID)")
    private Integer createdBy;

    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @ApiModelProperty("更新时间")
    private LocalDateTime updatedAt;
}
