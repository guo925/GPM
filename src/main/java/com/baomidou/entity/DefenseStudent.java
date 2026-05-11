package com.baomidou.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 答辩学生分配
 * </p>
 *
 * @author baomidou
 * @since 2026-05-11
 */
@Data
@TableName("defense_student")
@ApiModel(value = "DefenseStudent对象", description = "答辩学生分配")
public class DefenseStudent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 答辩分配ID
     */
    @ApiModelProperty("答辩分配ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 答辩组ID
     */
    @ApiModelProperty("答辩组ID")
    private Integer defenseGroupId;

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
}
