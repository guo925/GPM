package com.gjx.gpms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 成绩明细（分项评分）
 * </p>
 *
 * @author gpms
 * @since 2026-05-12
 */
@Data
@TableName("score_detail")

public class ScoreDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 所属成绩单
     */
    
    private Long sheetId;

    /**
     * 评分类型: advisor / reviewer / defense
     */
    
    private String type;

    /**
     * 原始分数
     */
    
    private BigDecimal score;

    /**
     * 权重，如0.5
     */
    
    private BigDecimal weight;

    /**
     * 评语
     */
    
    private String comment;

    /**
     * 评阅教师/指导老师
     */
    
    private Long reviewerId;

    /**
     * 是否盲审 1是
     */
    
    private Byte isBlind;

    private LocalDateTime createdAt;
}
