package com.gjx.gpms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 权限点表
 * </p>
 *
 * @author gpms
 * @since 2026-05-12
 */
@Data
@ApiModel(value = "Permission对象", description = "权限点表")
public class Permission implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 权限名称
     */
    @ApiModelProperty("权限名称")
    private String name;

    /**
     * 权限编码，如 batch:create
     */
    @ApiModelProperty("权限编码，如 batch:create")
    private String code;

    /**
     * 权限分组（批次/选题/答辩/成绩等）
     */
    @ApiModelProperty("权限分组（批次/选题/答辩/成绩等）")
    private String groupName;
}
