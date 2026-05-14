package com.gjx.gpms.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gjx.gpms.common.result.Result;
import com.gjx.gpms.system.dto.RoleCreateDTO;
import com.gjx.gpms.system.dto.RolePageDTO;
import com.gjx.gpms.system.dto.RoleUpdateDTO;
import com.gjx.gpms.system.service.RoleService;
import com.gjx.gpms.system.vo.RoleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理Controller
 *
 * @author gpms
 */
@Tag(name = "角色管理")
@RestController
@RequestMapping("/api/system/role")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "新增角色")
    @PreAuthorize("hasAuthority('system:role:add')")
    @PostMapping("/create")
    public Result<Void> create(@RequestBody @Valid RoleCreateDTO dto) {
        roleService.create(dto);
        return Result.success();
    }

    @Operation(summary = "修改角色")
    @PreAuthorize("hasAuthority('system:role:update')")
    @PutMapping("/update")
    public Result<Void> update(@RequestBody @Valid RoleUpdateDTO dto) {
        roleService.update(dto);
        return Result.success();
    }

    @Operation(summary = "删除角色")
    @PreAuthorize("hasAuthority('system:role:delete')")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.deleteById(id);
        return Result.success();
    }

    @Operation(summary = "角色分页")
    @PreAuthorize("hasAuthority('system:role:page')")
    @GetMapping("/page")
    public Result<IPage<RoleVO>> page(RolePageDTO dto) {
        return Result.success(roleService.page(dto));
    }

    @Operation(summary = "角色列表")
    @PreAuthorize("hasAuthority('system:role:list')")
    @GetMapping("/list")
    public Result<List<RoleVO>> list() {
        return Result.success(roleService.listAll());
    }

    @Operation(summary = "角色详情")
    @PreAuthorize("hasAuthority('system:role:query')")
    @GetMapping("/{id}")
    public Result<RoleVO> getById(@PathVariable Long id) {
        return Result.success(roleService.getRoleById(id));
    }
}
