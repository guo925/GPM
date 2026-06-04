package com.gjx.gpms.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gjx.gpms.common.result.Result;
import com.gjx.gpms.dto.BatchCreateDTO;
import com.gjx.gpms.dto.BatchUpdateDTO;
import com.gjx.gpms.service.BatchService;
import com.gjx.gpms.vo.BatchVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 批次管理Controller
 *
 * @author gpms
 */
@Tag(name = "批次管理")
@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
public class BatchController {

    private final BatchService batchService;

    @Operation(summary = "批次分页")
    @PreAuthorize("hasAuthority('batch:page')")
    @GetMapping("/page")
    public Result<IPage<BatchVO>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status) {
        return Result.success(batchService.page(current, size, name, status));
    }

    /**
     * 当前批次
     */
    @Operation(summary = "当前批次")
    @PreAuthorize("hasAuthority('batch:query')")
    @GetMapping("/current")
    public Result<BatchVO> getCurrentBatch() {
        return Result.success(batchService.getCurrentBatch());
    }

    /**
     * 年级列表
     */
    @Operation(summary = "年级列表")
    @PreAuthorize("hasAuthority('batch:query')")
    @GetMapping("/grades")
    public Result<List<String>> listDistinctGrades() {
        return Result.success(batchService.listDistinctGrades());
    }

    /**
     * 按年级查询批次
     */
    @Operation(summary = "按年级查询批次")
    @PreAuthorize("hasAuthority('batch:query')")
    @GetMapping("/by-grade")
    public Result<List<BatchVO>> listByGrade(@RequestParam String grade) {
        return Result.success(batchService.listByGrade(grade));
    }

    /**
     * 批次详情
     */
    @Operation(summary = "批次详情")
    @PreAuthorize("hasAuthority('batch:query')")
    @GetMapping("/{id:\\d+}")
    public Result<BatchVO> getDetail(@PathVariable Long id) {
        return Result.success(batchService.getDetail(id));
    }

    /**
     * 新增批次
     */
    @Operation(summary = "新增批次")
    @PreAuthorize("hasAuthority('batch:add')")
    @PostMapping("/create")
    public Result<Void> create(@Valid @RequestBody BatchCreateDTO dto) {
        batchService.create(dto);
        return Result.success();
    }

    /**
     * 修改批次
     */
    @Operation(summary = "修改批次")
    @PreAuthorize("hasAuthority('batch:update')")
    @PutMapping("/update")
    public Result<Void> update(@Valid @RequestBody BatchUpdateDTO dto) {
        batchService.update(dto);
        return Result.success();
    }

    /**
     * 删除批次
     */
    @Operation(summary = "删除批次")
    @PreAuthorize("hasAuthority('batch:delete')")
    @DeleteMapping("/{id:\\d+}")
    public Result<Void> delete(@PathVariable Long id) {
        batchService.deleteById(id);
        return Result.success();
    }

    /**
     * 推进阶段
     */
    @Operation(summary = "推进阶段")
    @PreAuthorize("hasAuthority('batch:stage')")
    @PutMapping("/{id:\\d+}/stage")
    public Result<Void> advanceStage(@PathVariable Long id, @RequestParam String stage) {
        batchService.advanceStage(id, stage);
        return Result.success();
    }
}
