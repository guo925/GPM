package com.gjx.gpms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 角色表
 * </p>
 *
 * @author gpms
 * @since 2026-05-12
 */
@Data

public class Role implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 角色名称
     */
    
    private String name;

    /**
     * 角色编码（程序用）
     */
    
    private String code;

    /**
     * 权限层级：0超管 1校级 2院级 3年级 4专业 5教师 6学生
     */
    
    private Byte level;

    private String description;
}
