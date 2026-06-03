package com.gjx.gpms.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gjx.gpms.common.result.Result;
import com.gjx.gpms.system.dto.UserCreateDTO;
import com.gjx.gpms.system.dto.UserPageDTO;
import com.gjx.gpms.system.dto.UserResetPasswordDTO;
import com.gjx.gpms.system.dto.UserRoleAssignDTO;
import com.gjx.gpms.system.dto.UserStatusDTO;
import com.gjx.gpms.system.dto.UserUpdateDTO;
import com.gjx.gpms.system.service.UserService;
import com.gjx.gpms.system.vo.UserRoleVO;
import com.gjx.gpms.system.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理Controller
 *
 * @author gpms
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/system/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 新增用户
     *
     * @param dto 新增用户DTO
     * @return 结果
     */
    @Operation(summary = "新增用户")
    @PreAuthorize("hasAuthority('system:user:add')")
    @PostMapping("/create")
    public Result<Void> create(
            @RequestBody @Valid UserCreateDTO dto
    ) {

        userService.create(dto);

        return Result.success();
    }

    /**
     * 用户分页查询
     *
     * @param dto 分页参数
     * @return 分页结果
     */
    @Operation(summary = "用户分页查询")
    @PreAuthorize("hasAuthority('system:user:page')")
    @GetMapping("/page")
    public Result<IPage<UserVO>> page(
            UserPageDTO dto
    ) {

        return Result.success(
                userService.page(dto)
        );
    }

    /**
     * 用户详情
     *
     * @param id 用户ID
     * @return 用户详情
     */
    @Operation(summary = "用户详情")
    @PreAuthorize("hasAuthority('system:user:query')")
    @GetMapping("/{id}")
    public Result<UserVO> getUserById(
            @PathVariable Long id
    ) {

        return Result.success(
                userService.getUserById(id)
        );
    }


    /**
     * 修改用户
     *
     * @param dto 修改DTO
     * @return 结果
     */
    @Operation(summary = "修改用户")
    @PreAuthorize("hasAuthority('system:user:update')")
    @PutMapping("/update")
    public Result<Void> update(
            @RequestBody @Valid UserUpdateDTO dto
    ) {

        userService.update(dto);

        return Result.success();
    }

    /**
     * 删除用户
     *
     * @param id 用户ID
     * @return 结果
     */
    @Operation(summary = "删除用户")
    @PreAuthorize("hasAuthority('system:user:delete')")
    @DeleteMapping("/{id}")
    public Result<Void> deleteById(
            @PathVariable Long id
    ) {

        userService.deleteById(id);

        return Result.success();
    }

    /**
     * 修改用户状态
     *
     * @param dto 状态DTO
     * @return 结果
     */
    @Operation(summary = "修改用户状态")
    @PreAuthorize("hasAuthority('system:user:status')")
    @PutMapping("/status")
    public Result<Void> updateStatus(
            @RequestBody @Valid UserStatusDTO dto
    ) {

        userService.updateStatus(dto);

        return Result.success();
    }

    /**
     * 重置密码
     *
     * @param dto 重置密码DTO
     * @return 结果
     */
    @Operation(summary = "重置密码")
    @PreAuthorize("hasAuthority('system:user:reset-password')")
    @PutMapping("/reset-password")
    public Result<Void> resetPassword(
            @RequestBody @Valid UserResetPasswordDTO dto
    ) {

        userService.resetPassword(dto);

        return Result.success();
    }

    /**
     * 获取用户角色
     *
     * @param userId 用户ID
     * @return 用户角色
     */
    @Operation(summary = "获取用户角色")
    @PreAuthorize("hasAuthority('system:user:role:query')")
    @GetMapping("/role/{userId}")
    public Result<UserRoleVO> getUserRoles(
            @PathVariable Long userId
    ) {

        return Result.success(
                userService.getUserRoles(userId)
        );
    }

    /**
     * 分配用户角色
     *
     * @param dto 角色分配DTO
     * @return 结果
     */
    @Operation(summary = "分配用户角色")
    @PreAuthorize("hasAuthority('system:user:role:assign')")
    @PutMapping("/role/assign")
    public Result<Void> assignRoles(
            @RequestBody @Valid UserRoleAssignDTO dto
    ) {

        userService.assignRoles(dto);

        return Result.success();
    }
}