package com.gjx.gpms.controller;

import com.gjx.gpms.common.result.Result;
import com.gjx.gpms.dto.MajorCreateDTO;
import com.gjx.gpms.service.MajorService;
import com.gjx.gpms.vo.MajorVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 专业管理Controller
 *
 * @author gpms
 */
@Tag(name = "专业管理")
@RestController
@RequestMapping("/api/major")
@RequiredArgsConstructor
public class MajorController {

    private final MajorService majorService;

    @Operation(summary = "专业列表")
    @PreAuthorize("hasAuthority('system:major:list')")
    @GetMapping("/list")
    public Result<List<MajorVO>> list(@RequestParam(required = false) Long collegeId) {
        return Result.success(majorService.listAll(collegeId));
    }

    @Operation(summary = "新增专业")
    @PreAuthorize("hasAuthority('system:major:add')")
    @PostMapping("/create")
    public Result<Void> create(@Valid @RequestBody MajorCreateDTO dto) {
        majorService.create(dto);
        return Result.success();
    }

    @Operation(summary = "修改专业")
    @PreAuthorize("hasAuthority('system:major:update')")
    @PutMapping("/update/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody MajorCreateDTO dto) {
        majorService.update(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除专业")
    @PreAuthorize("hasAuthority('system:major:delete')")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        majorService.deleteById(id);
        return Result.success();
    }
}
