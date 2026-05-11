package com.baomidou.entity;

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
 * 最终成绩计算权重
 * </p>
 *
 * @author baomidou
 * @since 2026-05-11
 */
@Data
@TableName("score_weight")
@ApiModel(value = "ScoreWeight对象", description = "最终成绩计算权重")
public class ScoreWeight implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 权重配置ID
     */
    @ApiModelProperty("权重配置ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 批次ID（一批次一个权重）
     */
    @ApiModelProperty("批次ID（一批次一个权重）")
    private Integer batchId;

    /**
     * 指导教师评分权重
     */
    @ApiModelProperty("指导教师评分权重")
    private BigDecimal teacherWeight;

    /**
     * 评阅分数权重
     */
    @ApiModelProperty("评阅分数权重")
    private BigDecimal reviewWeight;

    /**
     * 答辩分数权重
     */
    @ApiModelProperty("答辩分数权重")
    private BigDecimal defenseWeight;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
