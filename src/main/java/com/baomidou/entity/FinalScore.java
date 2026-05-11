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
 * 加权最终成绩
 * </p>
 *
 * @author baomidou
 * @since 2026-05-11
 */
@Data
@TableName("final_score")
@ApiModel(value = "FinalScore对象", description = "加权最终成绩")
public class FinalScore implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 最终成绩ID
     */
    @ApiModelProperty("最终成绩ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 学生ID
     */
    @ApiModelProperty("学生ID")
    private Integer studentId;

    /**
     * 批次ID
     */
    @ApiModelProperty("批次ID")
    private Integer batchId;

    /**
     * 指导教师评分
     */
    @ApiModelProperty("指导教师评分")
    private BigDecimal teacherScore;

    /**
     * 评阅分数（平均或单项）
     */
    @ApiModelProperty("评阅分数（平均或单项）")
    private BigDecimal reviewScore;

    /**
     * 答辩总分
     */
    @ApiModelProperty("答辩总分")
    private BigDecimal defenseScore;

    /**
     * 加权总评成绩
     */
    @ApiModelProperty("加权总评成绩")
    private BigDecimal totalScore;

    /**
     * 记录当时教师权重
     */
    @ApiModelProperty("记录当时教师权重")
    private BigDecimal weightA;

    /**
     * 记录当时评阅权重
     */
    @ApiModelProperty("记录当时评阅权重")
    private BigDecimal weightB;

    /**
     * 记录当时答辩权重
     */
    @ApiModelProperty("记录当时答辩权重")
    private BigDecimal weightC;

    /**
     * 是否已发布 0未1是
     */
    @ApiModelProperty("是否已发布 0未1是")
    private Boolean published;

    /**
     * 发布人
     */
    @ApiModelProperty("发布人")
    private Integer publishedBy;

    /**
     * 发布时间
     */
    @ApiModelProperty("发布时间")
    private LocalDateTime publishedAt;
}
