package com.gjx.gpms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 论文评阅分配及评分
 * </p>
 *
 * @author gpms
 * @since 2026-05-11
 */
@Data
@TableName("review_assignment")
@ApiModel(value = "ReviewAssignment对象", description = "论文评阅分配及评分")
public class ReviewAssignment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 评阅分配ID
     */
    @ApiModelProperty("评阅分配ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 批次ID
     */
    @ApiModelProperty("批次ID")
    private Integer batchId;

    /**
     * 学生ID
     */
    @ApiModelProperty("学生ID")
    private Integer studentId;

    /**
     * 被评阅的定稿提交记录ID
     */
    @ApiModelProperty("被评阅的定稿提交记录ID")
    private Integer submissionId;

    /**
     * 评阅教师ID
     */
    @ApiModelProperty("评阅教师ID")
    private Integer reviewerId;

    /**
     * 评阅分数(百分制,两位小数)
     */
    @ApiModelProperty("评阅分数(百分制,两位小数)")
    private BigDecimal score;

    /**
     * 评阅意见
     */
    @ApiModelProperty("评阅意见")
    private String comment;

    /**
     * 评分状态
     */
    @ApiModelProperty("评分状态")
    private String status;

    /**
     * 提交时间
     */
    @ApiModelProperty("提交时间")
    private LocalDateTime submitTime;
}
