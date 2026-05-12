package com.gjx.gpms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 用户角色关联表
 * </p>
 *
 * @author gpms
 * @since 2026-05-12
 */
@Data
@TableName("user_role")
@ApiModel(value = "UserRole对象", description = "用户角色关联表")
public class UserRole implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long roleId;

    /**
     * 角色作用范围：院级管理员绑定的学院
     */
    @ApiModelProperty("角色作用范围：院级管理员绑定的学院")
    private Long scopeCollegeId;

    /**
     * 专业管理员绑定的专业
     */
    @ApiModelProperty("专业管理员绑定的专业")
    private Long scopeMajorId;

    /**
     * 年级管理员绑定的年级，如2026
     */
    @ApiModelProperty("年级管理员绑定的年级，如2026")
    private String scopeGrade;
}
