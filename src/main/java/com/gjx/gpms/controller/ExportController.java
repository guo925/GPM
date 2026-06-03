package com.gjx.gpms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gjx.gpms.common.result.Result;
import com.gjx.gpms.entity.Batch;
import com.gjx.gpms.entity.ScoreSheet;
import com.gjx.gpms.entity.StudentTopic;
import com.gjx.gpms.mapper.BatchMapper;
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

import java.util.Collections;
import java.util.HashMap;
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
    private final BatchMapper batchMapper;

    /**
     * 导出成绩单
     */
    @Operation(summary = "导出成绩单")
    @PreAuthorize("hasAuthority('export:score')")
    @GetMapping("/scores")
    public Result<List<Map<String, Object>>> exportScoresByGrade(@RequestParam(required = false) String grade) {
        return Result.success(buildScoreExport(resolveBatchIds(null, grade)));
    }

    @Operation(summary = "按批次导出成绩单")
    @PreAuthorize("hasAuthority('export:score')")
    @GetMapping("/scores/{batchId}")
    public Result<List<Map<String, Object>>> exportScores(@PathVariable Long batchId,
                                                           @RequestParam(required = false) String grade) {
        return Result.success(buildScoreExport(resolveBatchIds(batchId, grade)));
    }

    private List<Map<String, Object>> buildScoreExport(List<Long> batchIds) {
        List<ScoreSheet> sheets = scoreSheetMapper.selectList(
                new LambdaQueryWrapper<ScoreSheet>().in(batchIds != null && !batchIds.isEmpty(), ScoreSheet::getBatchId, batchIds)
        );

        Map<Long, String> userMap = userMapper.selectList(null).stream()
                .collect(Collectors.toMap(User::getId, User::getRealName));

        List<Map<String, Object>> data = sheets.stream().map(s -> {
            StudentTopic st = studentTopicMapper.selectById(s.getStudentTopicId());
            Map<String, Object> row = new HashMap<>();
            row.put("studentName", st != null ? userMap.getOrDefault(st.getStudentId(), "") : "");
            row.put("advisorName", st != null ? userMap.getOrDefault(st.getAdvisorId(), "") : "");
            row.put("finalScore", s.getFinalScore());
            row.put("gradeLevel", s.getGradeLevel());
            row.put("status", s.getStatus());
            return row;
        }).collect(Collectors.toList());

        return data;
    }

    private List<Long> resolveBatchIds(Long batchId, String grade) {
        if (grade != null && !grade.isBlank()) {
            return batchMapper.selectList(
                    new LambdaQueryWrapper<Batch>().eq(Batch::getGrade, grade).select(Batch::getId)
            ).stream().map(Batch::getId).collect(Collectors.toList());
        }
        return batchId != null ? List.of(batchId) : null;
    }
}
