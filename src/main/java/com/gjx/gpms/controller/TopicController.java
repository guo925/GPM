package com.gjx.gpms.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gjx.gpms.common.result.Result;
import com.gjx.gpms.dto.TopicCreateDTO;
import com.gjx.gpms.dto.TopicReviewDTO;
import com.gjx.gpms.service.TopicService;
import com.gjx.gpms.vo.TopicVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课题管理Controller
 *
 * @author gpms
 */
@Tag(name = "课题管理")
@RestController
@RequestMapping("/api/topic")
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;

    @Operation(summary = "课题分页")
    @PreAuthorize("hasAuthority('topic:page')")
    @GetMapping("/page")
    public Result<IPage<TopicVO>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long batchId,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) String status) {
        return Result.success(topicService.page(current, size, batchId, grade, status));
    }

    /**
     * 热门课题列表
     */
    @Operation(summary = "热门课题列表")
    @PreAuthorize("hasAuthority('topic:page')")
    @GetMapping("/hot")
    public Result<List<TopicVO>> hot() {
        return Result.success(topicService.getHotTopics());
    }

    /**
     * 课题详情
     */
    @Operation(summary = "课题详情")
    @PreAuthorize("hasAuthority('topic:page')")
    @GetMapping("/{id}")
    public Result<TopicVO> getDetail(@PathVariable Long id) {
        return Result.success(topicService.getDetail(id));
    }

    /**
     * 新增课题
     */
    @Operation(summary = "新增课题")
    @PreAuthorize("hasAuthority('topic:add')")
    @PostMapping("/create")
    public Result<Void> create(@Valid @RequestBody TopicCreateDTO dto) {
        topicService.create(dto);
        return Result.success();
    }

    /**
     * 修改课题
     */
    @Operation(summary = "修改课题")
    @PreAuthorize("hasAuthority('topic:update')")
    @PutMapping("/update/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody TopicCreateDTO dto) {
        topicService.update(id, dto);
        return Result.success();
    }

    /**
     * 删除课题
     */
    @Operation(summary = "删除课题")
    @PreAuthorize("hasAuthority('topic:delete')")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        topicService.deleteById(id);
        return Result.success();
    }

    /**
     * 审核课题
     */
    @Operation(summary = "审核课题")
    @PreAuthorize("hasAuthority('topic:review')")
    @PutMapping("/review")
    public Result<Void> review(@Valid @RequestBody TopicReviewDTO dto) {
        topicService.review(dto);
        return Result.success();
    }
}
