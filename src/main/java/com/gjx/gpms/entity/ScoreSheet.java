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
 * 成绩单
 * </p>
 *
 * @author gpms
 * @since 2026-05-12
 */
@Data
@TableName("score_sheet")
@ApiModel(value = "ScoreSheet对象", description = "成绩单")
public class ScoreSheet implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long studentTopicId;

    /**
     * 冗余，便于统计
     */
    @ApiModelProperty("冗余，便于统计")
    private Long batchId;

    /**
     * 综合总分
     */
    @ApiModelProperty("综合总分")
    private BigDecimal finalScore;

    /**
     * 等级（优/良/中/及格/不及格）
     */
    @ApiModelProperty("等级（优/良/中/及格/不及格）")
    private String gradeLevel;

    /**
     * draft/submitted/approved/rejected
     */
    @ApiModelProperty("draft/submitted/approved/rejected")
    private String status;

    /**
     * 审核人（校级管理员
     */
    @ApiModelProperty("审核人（校级管理员")
    private Long reviewedBy;

    private String reviewComment;

    private LocalDateTime submittedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
