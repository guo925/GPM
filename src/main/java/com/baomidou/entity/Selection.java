package com.baomidou.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 选题结果（双向选择）
 * </p>
 *
 * @author baomidou
 * @since 2026-05-11
 */
@Data
@ApiModel(value = "Selection对象", description = "选题结果（双向选择）")
public class Selection implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 选题记录ID
     */
    @ApiModelProperty("选题记录ID")
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
     * 课题ID（自拟题确认后关联）
     */
    @ApiModelProperty("课题ID（自拟题确认后关联）")
    private Integer topicId;

    /**
     * 意向/确认的指导教师ID
     */
    @ApiModelProperty("意向/确认的指导教师ID")
    private Integer teacherId;

    /**
     * 申请理由
     */
    @ApiModelProperty("申请理由")
    private String applyReason;

    /**
     * 选题状态
     */
    @ApiModelProperty("选题状态")
    private String status;

    /**
     * 申请时间
     */
    @ApiModelProperty("申请时间")
    private LocalDateTime applyTime;

    /**
     * 教师确认时间
     */
    @ApiModelProperty("教师确认时间")
    private LocalDateTime confirmTime;
}
