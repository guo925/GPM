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
 * 答辩批次
 * </p>
 *
 * @author gpms
 * @since 2026-05-12
 */
@Data
@TableName("defense_batch")
@ApiModel(value = "DefenseBatch对象", description = "答辩批次")
public class DefenseBatch implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 所属毕设批次
     */
    @ApiModelProperty("所属毕设批次")
    private Long batchId;

    /**
     * 答辩类型: opening_defense / final_defense
     */
    @ApiModelProperty("答辩类型: opening_defense / final_defense")
    private String type;

    /**
     * 名称，如 2026届开题答辩
     */
    @ApiModelProperty("名称，如 2026届开题答辩")
    private String name;

    /**
     * 答辩开始时间
     */
    @ApiModelProperty("答辩开始时间")
    private LocalDateTime startTime;

    /**
     * 答辩结束时间
     */
    @ApiModelProperty("答辩结束时间")
    private LocalDateTime endTime;

    /**
     * 地点模版
     */
    @ApiModelProperty("地点模版")
    private String locationTemplate;

    /**
     * 是否补答辩批次
     */
    @ApiModelProperty("是否补答辩批次")
    private Byte isSupplementary;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
