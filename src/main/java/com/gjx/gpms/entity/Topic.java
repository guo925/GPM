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
 * 题目表
 * </p>
 *
 * @author gpms
 * @since 2026-05-12
 */
@Data
@ApiModel(value = "Topic对象", description = "题目表")
public class Topic implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 所属批次
     */
    @ApiModelProperty("所属批次")
    private Long batchId;

    /**
     * 题目名称
     */
    @ApiModelProperty("题目名称")
    private String title;

    /**
     * 题目描述
     */
    @ApiModelProperty("题目描述")
    private String description;

    /**
     * 来源: preset(预设)/student_propose(学生自拟)
     */
    @ApiModelProperty("来源: preset(预设)/student_propose(学生自拟)")
    private String source;

    /**
     * 创建人用户ID（教师或学生）
     */
    @ApiModelProperty("创建人用户ID（教师或学生）")
    private Long creatorId;

    /**
     * 可容纳学生数（抢选制可用）
     */
    @ApiModelProperty("可容纳学生数（抢选制可用）")
    private Integer maxCapacity;

    /**
     * 已选人数
     */
    @ApiModelProperty("已选人数")
    private Integer currentCount;

    /**
     * 状态: pending/approved/rejected
     */
    @ApiModelProperty("状态: pending/approved/rejected")
    private String status;

    /**
     * 审核意见（自拟题审核）
     */
    @ApiModelProperty("审核意见（自拟题审核）")
    private String reviewComment;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
