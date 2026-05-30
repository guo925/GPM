package com.gjx.gpms.controller;

import com.gjx.gpms.common.result.Result;
import com.gjx.gpms.dto.DefenseBatchDTO;
import com.gjx.gpms.dto.DefenseGroupDTO;
import com.gjx.gpms.dto.DefenseResultDTO;
import com.gjx.gpms.entity.DefenseArrangement;
import com.gjx.gpms.entity.DefenseBatch;
import com.gjx.gpms.entity.DefenseGroup;
import com.gjx.gpms.entity.DefenseResult;
import com.gjx.gpms.service.DefenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "答辩管理")
@RestController
@RequestMapping("/api/defense")
@RequiredArgsConstructor
public class DefenseController {

    private static final String DEFENSE_MANAGE_ROLES =
            "hasAnyRole('SUPER_ADMIN','UNIVERSITY_ADMIN','COLLEGE_ADMIN','GRADE_ADMIN','MAJOR_ADMIN','TEACHER')";

    private final DefenseService defenseService;

    @Operation(summary = "答辩批次列表")
    @GetMapping("/batches")
    public Result<List<DefenseBatch>> listBatches(@RequestParam(required = false) Long batchId) {
        return Result.success(defenseService.listBatches(batchId));
    }

    @Operation(summary = "创建答辩批次")
    @PreAuthorize("hasAuthority('defense:batch:add') or " + DEFENSE_MANAGE_ROLES)
    @PostMapping("/batch/create")
    public Result<Void> createBatch(@Valid @RequestBody DefenseBatchDTO dto) {
        defenseService.createBatch(dto);
        return Result.success();
    }

    @Operation(summary = "删除答辩批次")
    @PreAuthorize("hasAuthority('defense:batch:delete') or " + DEFENSE_MANAGE_ROLES)
    @DeleteMapping("/batch/{id}")
    public Result<Void> deleteBatch(@PathVariable Long id) {
        defenseService.deleteBatch(id);
        return Result.success();
    }

    @Operation(summary = "答辩组列表")
    @GetMapping("/groups")
    public Result<List<DefenseGroup>> listGroups(@RequestParam Long defenseBatchId) {
        return Result.success(defenseService.listGroups(defenseBatchId));
    }

    @Operation(summary = "创建答辩组")
    @PreAuthorize("hasAuthority('defense:group:add') or " + DEFENSE_MANAGE_ROLES)
    @PostMapping("/group/create")
    public Result<Void> createGroup(@Valid @RequestBody DefenseGroupDTO dto) {
        defenseService.createGroup(dto);
        return Result.success();
    }

    @Operation(summary = "删除答辩组")
    @PreAuthorize("hasAuthority('defense:group:delete') or " + DEFENSE_MANAGE_ROLES)
    @DeleteMapping("/group/{id}")
    public Result<Void> deleteGroup(@PathVariable Long id) {
        defenseService.deleteGroup(id);
        return Result.success();
    }

    @Operation(summary = "答辩安排列表")
    @GetMapping("/arrangements")
    public Result<List<DefenseArrangement>> listArrangements(@RequestParam Long groupId) {
        return Result.success(defenseService.listArrangements(groupId));
    }

    @Operation(summary = "添加答辩安排")
    @PreAuthorize("hasAuthority('defense:arrange') or " + DEFENSE_MANAGE_ROLES)
    @PostMapping("/arrange")
    public Result<Void> addArrangement(@RequestBody Map<String, Object> params) {
        defenseService.addArrangement(
                Long.valueOf(params.get("groupId").toString()),
                Long.valueOf(params.get("studentId").toString()),
                (String) params.get("defenseTime"),
                (String) params.get("location")
        );
        return Result.success();
    }

    @Operation(summary = "录入答辩结果")
    @PreAuthorize("hasAuthority('defense:result') or " + DEFENSE_MANAGE_ROLES)
    @PostMapping("/result")
    public Result<Void> saveResult(@Valid @RequestBody DefenseResultDTO dto) {
        defenseService.saveResult(dto);
        return Result.success();
    }

    @Operation(summary = "查看答辩结果")
    @GetMapping("/result/{arrangementId}")
    public Result<DefenseResult> getResult(@PathVariable Long arrangementId) {
        return Result.success(defenseService.getResult(arrangementId));
    }
}
