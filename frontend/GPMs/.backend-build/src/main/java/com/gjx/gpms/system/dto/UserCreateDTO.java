package com.gjx.gpms.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户新增DTO
 */

/*
*
* 新增DTO
修改DTO

必须分开。

为什么？

因为：

新增：

密码必填

修改：

密码可能不修改
* */
@Data
public class UserCreateDTO {

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 真实姓名
     */
    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 学号
     */
    private String studentNo;

    /**
     * 年级
     */
    private String grade;

    /**
     * 所属学院ID
     */
    private Long collegeId;

    /**
     * 所属专业ID
     */
    private Long majorId;
}
