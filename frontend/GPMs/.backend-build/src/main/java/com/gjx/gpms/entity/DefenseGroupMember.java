package com.gjx.gpms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 答辩组成员
 * </p>
 *
 * @author gpms
 * @since 2026-05-12
 */
@Data
@TableName("defense_group_member")

public class DefenseGroupMember implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long groupId;

    private Long teacherId;

    /**
     * leader/member
     */
    
    private String role;
}
