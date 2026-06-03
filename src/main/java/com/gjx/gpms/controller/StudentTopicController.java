package com.gjx.gpms.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gjx.gpms.common.result.Result;
import com.gjx.gpms.security.context.UserContext;
import com.gjx.gpms.service.StudentTopicService;
import com.gjx.gpms.vo.StudentTopicVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 选题结果Controller
 */
@Tag(name = "选题结果")
@RestController
@RequestMapping("/api/student-topic")
@RequiredArgsConstructor
public class StudentTopicController {

    private final StudentTopicService studentTopicService;

    @Operation(summary = "选题结果分页")
    @PreAuthorize("hasAuthority('student-topic:page')")
    @GetMapping("/page")
    public Result<IPage<StudentTopicVO>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long batchId,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) Long advisorId) {
        return Result.success(studentTopicService.page(current, size, batchId, grade, advisorId));
    }

    /**
     * 我的选题结果
     */
    @Operation(summary = "我的选题结果")
    @GetMapping("/my")
    public Result<StudentTopicVO> getMyTopic() {
        Long userId = UserContext.getUserId();
        return Result.success(studentTopicService.getByStudentId(userId));
    }
}
