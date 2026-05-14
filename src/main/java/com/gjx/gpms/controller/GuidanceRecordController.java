package com.gjx.gpms.controller;

import com.gjx.gpms.common.result.Result;
import com.gjx.gpms.dto.GuidanceRecordCreateDTO;
import com.gjx.gpms.service.GuidanceRecordService;
import com.gjx.gpms.vo.GuidanceRecordVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 指导记录Controller
 *
 * @author gpms
 */
@Tag(name = "指导记录")
@RestController
@RequestMapping("/api/guidance")
@RequiredArgsConstructor
public class GuidanceRecordController {

    private final GuidanceRecordService guidanceRecordService;

    @Operation(summary = "提交周记")
    @PreAuthorize("hasAuthority('guidance:submit')")
    @PostMapping("/create")
    public Result<Void> create(@Valid @RequestBody GuidanceRecordCreateDTO dto) {
        guidanceRecordService.create(dto);
        return Result.success();
    }

    @Operation(summary = "批阅周记")
    @PreAuthorize("hasAuthority('guidance:review')")
    @PutMapping("/review/{id}")
    public Result<Void> review(@PathVariable Long id, @RequestParam String comment) {
        guidanceRecordService.review(id, comment);
        return Result.success();
    }

    @Operation(summary = "查看指导记录")
    @PreAuthorize("hasAuthority('guidance:query')")
    @GetMapping("/list/{studentTopicId}")
    public Result<List<GuidanceRecordVO>> list(@PathVariable Long studentTopicId) {
        return Result.success(guidanceRecordService.getByStudentTopic(studentTopicId));
    }
}
