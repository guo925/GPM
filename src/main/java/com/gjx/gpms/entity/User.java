package com.gjx.gpms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户表
 * </p>
 *
 * @author gpms
 * @since 2026-05-12
 */
@Data

public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 登录账号
     */
    
    private String username;

    /**
     * 加密密码
     */
    
    private String password;

    /**
     * 真实姓名
     */
    
    private String realName;

    private String email;

    private String phone;

    /**
     * 所属学院（教师/管理员/学生）
     */
    
    private Long collegeId;

    /**
     * 所属专业（学生必填）
     */
    
    private Long majorId;

    /**
     * 账号状态 1正常 0禁用
     */
    
    private Byte status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
