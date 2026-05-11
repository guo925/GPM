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
 * 教师基本信息（含管理员）
 * </p>
 *
 * @author gpms
 * @since 2026-05-11
 */
@Data
@ApiModel(value = "Teacher对象", description = "教师基本信息（含管理员）")
public class Teacher implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 教师ID
     */
    @ApiModelProperty("教师ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 工号
     */
    @ApiModelProperty("工号")
    private String teacherNo;

    /**
     * 姓名
     */
    @ApiModelProperty("姓名")
    private String name;

    /**
     * 职称
     */
    @ApiModelProperty("职称")
    private String title;

    /**
     * 所属教研室/系
     */
    @ApiModelProperty("所属教研室/系")
    private String department;

    /**
     * 是否管理员 0否1是
     */
    @ApiModelProperty("是否管理员 0否1是")
    private Boolean isAdmin;

    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    private LocalDateTime createdAt;
}
