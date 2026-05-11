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
 * 答辩组成员
 * </p>
 *
 * @author gpms
 * @since 2026-05-11
 */
@Data
@TableName("defense_group_member")
@ApiModel(value = "DefenseGroupMember对象", description = "答辩组成员")
public class DefenseGroupMember implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 成员记录ID
     */
    @ApiModelProperty("成员记录ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 答辩组ID
     */
    @ApiModelProperty("答辩组ID")
    private Integer groupId;

    /**
     * 教师ID
     */
    @ApiModelProperty("教师ID")
    private Integer teacherId;

    /**
     * 角色
     */
    @ApiModelProperty("角色")
    private String role;
}
