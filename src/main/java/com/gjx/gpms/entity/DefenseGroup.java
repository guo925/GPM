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
 * 答辩组
 * </p>
 *
 * @author gpms
 * @since 2026-05-12
 */
@Data
@TableName("defense_group")
@ApiModel(value = "DefenseGroup对象", description = "答辩组")
public class DefenseGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long defenseBatchId;

    /**
     * 组名
     */
    @ApiModelProperty("组名")
    private String name;

    /**
     * 答辩组长ID
     */
    @ApiModelProperty("答辩组长ID")
    private Long leaderId;

    private LocalDateTime createdAt;
}
