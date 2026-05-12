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
 * 专业
 * </p>
 *
 * @author gpms
 * @since 2026-05-12
 */
@Data
@ApiModel(value = "Major对象", description = "专业")
public class Major implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 所属学院
     */
    @ApiModelProperty("所属学院")
    private Long collegeId;

    /**
     * 专业名称
     */
    @ApiModelProperty("专业名称")
    private String name;

    /**
     * 专业代码
     */
    @ApiModelProperty("专业代码")
    private String code;

    private Integer sortOrder;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
