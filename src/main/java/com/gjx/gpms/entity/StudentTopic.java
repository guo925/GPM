package com.gjx.gpms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 学生选题关系
 * </p>
 *
 * @author gpms
 * @since 2026-05-12
 */
@Data
@TableName("student_topic")

public class StudentTopic implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long batchId;

    /**
     * 学生ID
     */
    
    private Long studentId;

    /**
     * 题目ID
     */
    
    private Long topicId;

    /**
     * 指导教师ID
     */
    
    private Long advisorId;

    /**
     * active/transferred/deferred/extended
     */
    
    private String status;

    /**
     * 分配时间
     */
    
    private LocalDateTime allocationTime;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
