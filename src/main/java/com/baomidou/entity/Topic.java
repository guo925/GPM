package com.baomidou.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 毕设课题
 * </p>
 *
 * @author baomidou
 * @since 2026-05-11
 */
@Data
@ApiModel(value = "Topic对象", description = "毕设课题")
public class Topic implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 课题ID
     */
    @ApiModelProperty("课题ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 所属批次ID
     */
    @ApiModelProperty("所属批次ID")
    private Integer batchId;

    /**
     * 发布/指导教师ID
     */
    @ApiModelProperty("发布/指导教师ID")
    private Integer teacherId;

    /**
     * 课题题目
     */
    @ApiModelProperty("课题题目")
    private String title;

    /**
     * 课题类型：预设/自拟
     */
    @ApiModelProperty("课题类型：预设/自拟")
    private String type;

    /**
     * 课题简介
     */
    @ApiModelProperty("课题简介")
    private String description;

    /**
     * 预期目标（自拟题常用）
     */
    @ApiModelProperty("预期目标（自拟题常用）")
    private String expectedGoal;

    /**
     * 可选容量
     */
    @ApiModelProperty("可选容量")
    private Integer capacity;

    /**
     * 状态（自拟题初始为待审核）
     */
    @ApiModelProperty("状态（自拟题初始为待审核）")
    private String status;

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
