package com.gjx.gpms.controller;

import com.gjx.gpms.common.result.Result;
import com.gjx.gpms.dto.CollegeCreateDTO;
import com.gjx.gpms.service.CollegeService;
import com.gjx.gpms.vo.CollegeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学院管理Controller
 *
 * @author gpms
 */
@Tag(name = "学院管理")
@RestController
@RequestMapping("/api/college")
@RequiredArgsConstructor
public class CollegeController {

    private final CollegeService collegeService;

    /**
     * 学院列表
     */
    @Operation(summary = "学院列表")
    @PreAuthorize("hasAuthority('system:college:list')")
    @GetMapping("/list")
    public Result<List<CollegeVO>> list() {
        return Result.success(collegeService.listAll());
    }

    /**
     * 新增学院
     */
    @Operation(summary = "新增学院")
    @PreAuthorize("hasAuthority('system:college:add')")
    @PostMapping("/create")
    public Result<Void> create(@Valid @RequestBody CollegeCreateDTO dto) {
        collegeService.create(dto);
        return Result.success();
    }

    /**
     * 修改学院
     */
    @Operation(summary = "修改学院")
    @PreAuthorize("hasAuthority('system:college:update')")
    @PutMapping("/update/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody CollegeCreateDTO dto) {
        collegeService.update(id, dto);
        return Result.success();
    }

    /**
     * 删除学院
     */
    @Operation(summary = "删除学院")
    @PreAuthorize("hasAuthority('system:college:delete')")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        collegeService.deleteById(id);
        return Result.success();
    }
}
