package com.gjx.gpms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@ApiModel(value = "StudentTopic对象", description = "学生选题关系")
public class StudentTopic implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long batchId;

    /**
     * 学生ID
     */
    @ApiModelProperty("学生ID")
    private Long studentId;

    /**
     * 题目ID
     */
    @ApiModelProperty("题目ID")
    private Long topicId;

    /**
     * 指导教师ID
     */
    @ApiModelProperty("指导教师ID")
    private Long advisorId;

    /**
     * active/transferred/deferred/extended
     */
    @ApiModelProperty("active/transferred/deferred/extended")
    private String status;

    /**
     * 分配时间
     */
    @ApiModelProperty("分配时间")
    private LocalDateTime allocationTime;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
