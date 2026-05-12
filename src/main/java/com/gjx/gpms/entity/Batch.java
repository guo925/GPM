package com.gjx.gpms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 毕设批次
 * </p>
 *
 * @author gpms
 * @since 2026-05-12
 */
@Data
@ApiModel(value = "Batch对象", description = "毕设批次")
public class Batch implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 年级，如2026
     */
    @ApiModelProperty("年级，如2026")
    private String grade;

    /**
     * 专业ID
     */
    @ApiModelProperty("专业ID")
    private Long majorId;

    /**
     * 学院ID（冗余加速查询）
     */
    @ApiModelProperty("学院ID（冗余加速查询）")
    private Long collegeId;

    /**
     * 批次名称，如 2026计算机科学与技术毕设
     */
    @ApiModelProperty("批次名称，如 2026计算机科学与技术毕设")
    private String name;

    /**
     * 当前阶段：topic_selection/task_book/opening_report/midterm/defense/completed
     */
    @ApiModelProperty("当前阶段：topic_selection/task_book/opening_report/midterm/defense/completed")
    private String currentStage;

    /**
     * 时间节点与规则配置，JSON结构：{\"stages\":{\"topic_start\":\"2026-09-01\",\"topic_end\":\"2026-09-20\",...}}
     */
    @ApiModelProperty("时间节点与规则配置，JSON结构：{\"stages\":{\"topic_start\":\"2026-09-01\",\"topic_end\":\"2026-09-20\",...}}")
    private String config;

    /**
     * 每导师最多带学生数
     */
    @ApiModelProperty("每导师最多带学生数")
    private Integer maxStudentPerTeacher;

    /**
     * 双选模式: voluntary / first_come / teacher_choose
     */
    @ApiModelProperty("双选模式: voluntary / first_come / teacher_choose")
    private String selectionMode;

    /**
     * 志愿制下学生可选志愿数
     */
    @ApiModelProperty("志愿制下学生可选志愿数")
    private Byte studentMaxChoices;

    /**
     * 是否允许导师拒绝 1是 0否
     */
    @ApiModelProperty("是否允许导师拒绝 1是 0否")
    private Byte allowTeacherReject;

    /**
     * 被拒后策略: pool(回池)/manual
     */
    @ApiModelProperty("被拒后策略: pool(回池)/manual")
    private String rejectStrategy;

    /**
     * 批次状态 1进行中 0已结束
     */
    @ApiModelProperty("批次状态 1进行中 0已结束")
    private Byte status;

    /**
     * 创建者用户ID
     */
    @ApiModelProperty("创建者用户ID")
    private Long createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
