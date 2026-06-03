package com.gjx.gpms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 毕设流程实例表(状态机)
 * </p>
 *
 * @author gpms
 * @since 2026-05-12
 */
@Data
@TableName("process_instance")

public class ProcessInstance implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 所属学生选题
     */
    
    private Long studentTopicId;

    /**
     * 阶段：task_book, opening_report, opening_defense, guidance_week, midterm_check, thesis_draft, thesis_final, post_defense_modify
     */
    
    private String stage;

    /**
     * 状态：not_started, submitted, approved, rejected
     */
    
    private String status;

    /**
     * 提交人
     */
    
    private Long submitterId;

    /**
     * 提交时间
     */
    
    private LocalDateTime submittedAt;

    /**
     * 附件路径
     */
    
    private String filePath;

    /**
     * 文本内容（周记等）
     */
    
    private String content;

    /**
     * 审核人
     */
    
    private Long reviewerId;

    private LocalDateTime reviewedAt;

    private String reviewComment;

    /**
     * 版本号（修改后递增）
     */
    
    private Integer version;

    /**
     * 通过后是否仍可修改 1是 0否
     */
    
    private Byte isEditable;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
