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
 * 答辩各项评分及总分
 * </p>
 *
 * @author baomidou
 * @since 2026-05-11
 */
@Data
@TableName("defense_score")
@ApiModel(value = "DefenseScore对象", description = "答辩各项评分及总分")
public class DefenseScore implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 答辩成绩ID
     */
    @ApiModelProperty("答辩成绩ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 答辩学生分配ID
     */
    @ApiModelProperty("答辩学生分配ID")
    private Integer defenseStudentId;

    /**
     * 陈述表现分
     */
    @ApiModelProperty("陈述表现分")
    private BigDecimal presentationScore;

    /**
     * 回答问题分
     */
    @ApiModelProperty("回答问题分")
    private BigDecimal qaScore;

    /**
     * 创新性分
     */
    @ApiModelProperty("创新性分")
    private BigDecimal innovationScore;

    /**
     * 答辩总分（自动计算）
     */
    @ApiModelProperty("答辩总分（自动计算）")
    private BigDecimal totalScore;

    /**
     * 秘书（确认人）
     */
    @ApiModelProperty("秘书（确认人）")
    private Integer secretaryId;

    /**
     * 状态
     */
    @ApiModelProperty("状态")
    private String status;

    /**
     * 秘书提交确认时间
     */
    @ApiModelProperty("秘书提交确认时间")
    private LocalDateTime confirmTime;
}
