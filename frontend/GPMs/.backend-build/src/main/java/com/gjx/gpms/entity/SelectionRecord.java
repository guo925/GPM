package com.gjx.gpms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 双选过程记录
 * </p>
 *
 * @author gpms
 * @since 2026-05-12
 */
@Data
@TableName("selection_record")

public class SelectionRecord implements Serializable {

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
     * 志愿序号（1,2,3），抢选制为NULL
     */
    
    private Byte priority;

    /**
     * 教师操作: approve/reject，未操作为NULL
     */
    
    private String teacherAction;

    private String teacherComment;

    /**
     * 是否最终选中 1是 0否
     */
    
    private Byte isSelected;

    /**
     * 学生操作时间
     */
    
    private LocalDateTime createdAt;

    /**
     * 教师操作时间
     */
    
    private LocalDateTime updatedAt;
}
