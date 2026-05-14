package com.gjx.gpms.system.controller;

import com.gjx.gpms.common.result.Result;
import com.gjx.gpms.system.vo.StatisticsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Tag(name = "数据统计")
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final JdbcTemplate jdbcTemplate;

    @Operation(summary = "获取总览统计数据")
    @GetMapping("/overview")
    @PreAuthorize("hasAuthority('batch:page')")
    public Result<StatisticsVO> overview() {
        StatisticsVO vo = new StatisticsVO();

        // 学院/专业
        vo.setTotalColleges(count("college"));
        vo.setTotalMajors(count("major"));

        // 用户（排除逻辑删除的）
        vo.setTotalUsers(queryInt("SELECT COUNT(*) FROM sys_user WHERE is_deleted = 0"));
        vo.setTotalTeachers(queryInt(
            "SELECT COUNT(*) FROM sys_user u INNER JOIN sys_user_role ur ON u.id=ur.user_id WHERE ur.role_id=6 AND u.is_deleted = 0"));
        vo.setTotalStudents(queryInt(
            "SELECT COUNT(*) FROM sys_user u INNER JOIN sys_user_role ur ON u.id=ur.user_id WHERE ur.role_id=7 AND u.is_deleted = 0"));

        // 批次
        vo.setTotalBatches(count("batch"));
        vo.setActiveBatches(queryInt("SELECT COUNT(*) FROM batch WHERE status=1"));

        // 课题
        vo.setTotalTopics(count("topic"));
        vo.setApprovedTopics(queryInt("SELECT COUNT(*) FROM topic WHERE status='approved'"));

        // 选题统计
        Integer allStudents = vo.getTotalStudents();
        Integer selected = queryInt("SELECT COUNT(DISTINCT student_id) FROM student_topic WHERE status='active'");
        vo.setSelectedStudents(selected != null ? selected : 0);
        vo.setUnselectedStudents(Math.max(0, (allStudents != null ? allStudents : 0) - vo.getSelectedStudents()));

        // 各阶段完成数量
        List<Map<String, Object>> stageStats = new ArrayList<>();
        String[] stages = {"task_book","opening_report","opening_defense","guidance_week","midterm_check","thesis_draft","thesis_final","post_defense_modify"};
        String[] labels = {"任务书","开题报告","开题答辩","指导周记","中期检查","论文初稿","论文终稿","答辩后修改"};
        for (int i = 0; i < stages.length; i++) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("stage", stages[i]);
            m.put("label", labels[i]);
            m.put("submitted", queryInt("SELECT COUNT(*) FROM process_instance WHERE stage='" + stages[i] + "' AND status='submitted'"));
            m.put("approved", queryInt("SELECT COUNT(*) FROM process_instance WHERE stage='" + stages[i] + "' AND status='approved'"));
            m.put("rejected", queryInt("SELECT COUNT(*) FROM process_instance WHERE stage='" + stages[i] + "' AND status='rejected'"));
            stageStats.add(m);
        }
        vo.setStageStats(stageStats);

        // 成绩分布
        List<Map<String, Object>> scoreDist = new ArrayList<>();
        String[][] grades = {{"优","A"},{"良","B"},{"中","C"},{"及格","D"},{"不及格","F"}};
        for (String[] g : grades) {
            int cnt = 0;
            try {
                String sql = "SELECT COUNT(*) FROM score_sheet WHERE grade_level IN ('" + g[0] + "','优秀','良好','中等')";
                if ("优".equals(g[0])) cnt = queryInt("SELECT COUNT(*) FROM score_sheet WHERE grade_level IN ('优','优秀')");
                else if ("良".equals(g[0])) cnt = queryInt("SELECT COUNT(*) FROM score_sheet WHERE grade_level IN ('良','良好')");
                else if ("中".equals(g[0])) cnt = queryInt("SELECT COUNT(*) FROM score_sheet WHERE grade_level IN ('中','中等')");
                else if ("及格".equals(g[0])) cnt = queryInt("SELECT COUNT(*) FROM score_sheet WHERE grade_level = '及格'");
                else cnt = queryInt("SELECT COUNT(*) FROM score_sheet WHERE grade_level = '不及格'");
            } catch (Exception ignored) {}
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("grade", g[0]);
            m.put("count", cnt);
            scoreDist.add(m);
        }
        vo.setScoreDistribution(scoreDist);

        return Result.success(vo);
    }

    private Integer count(String table) {
        try {
            return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class);
        } catch (Exception e) {
            return 0;
        }
    }

    private Integer queryInt(String sql) {
        try {
            return jdbcTemplate.queryForObject(sql, Integer.class);
        } catch (Exception e) {
            return 0;
        }
    }
}
