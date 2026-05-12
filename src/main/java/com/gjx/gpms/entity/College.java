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
 * 学院
 * </p>
 *
 * @author gpms
 * @since 2026-05-12
 */
@Data
@ApiModel(value = "College对象", description = "学院")
public class College implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 学院名称
     */
    @ApiModelProperty("学院名称")
    private String name;

    /**
     * 学院代码
     */
    @ApiModelProperty("学院代码")
    private String code;

    /**
     * 排序
     */
    @ApiModelProperty("排序")
    private Integer sortOrder;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
