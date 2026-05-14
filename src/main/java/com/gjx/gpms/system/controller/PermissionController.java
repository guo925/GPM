package com.gjx.gpms.system.controller;

import com.gjx.gpms.common.result.Result;
import com.gjx.gpms.system.dto.PermissionCreateDTO;
import com.gjx.gpms.system.dto.PermissionUpdateDTO;
import com.gjx.gpms.system.service.PermissionService;
import com.gjx.gpms.system.vo.PermissionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 权限管理Controller
 *
 * @author gpms
 */
@Tag(name = "权限管理")
@RestController
@RequestMapping("/api/system/permission")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @Operation(summary = "权限列表")
    @PreAuthorize("hasAuthority('system:permission:list')")
    @GetMapping("/list")
    public Result<List<PermissionVO>> list() {
        return Result.success(permissionService.listAll());
    }

    @Operation(summary = "权限树")
    @PreAuthorize("hasAuthority('system:permission:list')")
    @GetMapping("/tree")
    public Result<Map<String, List<PermissionVO>>> tree() {
        return Result.success(permissionService.tree());
    }

    @Operation(summary = "新增权限")
    @PreAuthorize("hasAuthority('system:permission:add')")
    @PostMapping("/create")
    public Result<Void> create(@Valid @RequestBody PermissionCreateDTO dto) {
        permissionService.create(dto);
        return Result.success();
    }

    @Operation(summary = "修改权限")
    @PreAuthorize("hasAuthority('system:permission:update')")
    @PutMapping("/update")
    public Result<Void> update(@Valid @RequestBody PermissionUpdateDTO dto) {
        permissionService.update(dto);
        return Result.success();
    }

    @Operation(summary = "删除权限")
    @PreAuthorize("hasAuthority('system:permission:delete')")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        permissionService.deleteById(id);
        return Result.success();
    }
}
