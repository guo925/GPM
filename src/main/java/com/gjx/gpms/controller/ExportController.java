package com.gjx.gpms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gjx.gpms.common.result.Result;
import com.gjx.gpms.entity.ScoreSheet;
import com.gjx.gpms.entity.StudentTopic;
import com.gjx.gpms.mapper.ScoreSheetMapper;
import com.gjx.gpms.mapper.StudentTopicMapper;
import com.gjx.gpms.system.entity.User;
import com.gjx.gpms.system.mapper.UserMapper;
import com.gjx.gpms.vo.ScoreSheetVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 导出Controller
 */
@Tag(name = "导出管理")
@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController {

    private final ScoreSheetMapper scoreSheetMapper;
    private final StudentTopicMapper studentTopicMapper;
    private final UserMapper userMapper;

    /**
     * 导出成绩单
     */
    @Operation(summary = "导出成绩单")
    @PreAuthorize("hasAuthority('export:score')")
    @GetMapping("/scores/{batchId}")
    public Result<List<Map<String, Object>>> exportScores(@PathVariable Long batchId) {
        List<ScoreSheet> sheets = scoreSheetMapper.selectList(
                new LambdaQueryWrapper<ScoreSheet>().eq(ScoreSheet::getBatchId, batchId)
        );

        Map<Long, String> userMap = userMapper.selectList(null).stream()
                .collect(Collectors.toMap(User::getId, User::getRealName));

        List<Map<String, Object>> data = sheets.stream().map(s -> {
            StudentTopic st = studentTopicMapper.selectById(s.getStudentTopicId());
            return Map.<String, Object>of(
                    "studentName", st != null ? userMap.getOrDefault(st.getStudentId(), "") : "",
                    "advisorName", st != null ? userMap.getOrDefault(st.getAdvisorId(), "") : "",
                    "finalScore", s.getFinalScore(),
                    "gradeLevel", s.getGradeLevel(),
                    "status", s.getStatus()
            );
        }).collect(Collectors.toList());

        return Result.success(data);
    }
}
