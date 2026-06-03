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
 * 答辩结果
 * </p>
 *
 * @author gpms
 * @since 2026-05-12
 */
@Data
@TableName("defense_result")

public class DefenseResult implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 一对一关联答辩安排
     */
    
    private Long arrangementId;

    /**
     * 评分项及分数 JSON，如 [{\"item\":\"选题质量\",\"score\":90},{\"item\":\"论文水平\",\"score\":85}]
     */
    
    private String scoreItems;

    /**
     * 答辩组总分
     */
    
    private BigDecimal totalScore;

    /**
     * 答辩意见: major_revision/minor_revision/pass/fail
     */
    
    private String decision;

    /**
     * 答辩评语
     */
    
    private String comment;

    /**
     * 录入人（组长）
     */
    
    private Long recordedBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
