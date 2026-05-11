package com.baomidou.entity;

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
 * 答辩分组信息
 * </p>
 *
 * @author baomidou
 * @since 2026-05-11
 */
@Data
@TableName("defense_group")
@ApiModel(value = "DefenseGroup对象", description = "答辩分组信息")
public class DefenseGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 答辩组ID
     */
    @ApiModelProperty("答辩组ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 批次ID
     */
    @ApiModelProperty("批次ID")
    private Integer batchId;

    /**
     * 答辩组名称
     */
    @ApiModelProperty("答辩组名称")
    private String groupName;

    /**
     * 答辩地点
     */
    @ApiModelProperty("答辩地点")
    private String location;

    /**
     * 计划答辩时间
     */
    @ApiModelProperty("计划答辩时间")
    private LocalDateTime defenseDate;

    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    private LocalDateTime createdAt;
}
