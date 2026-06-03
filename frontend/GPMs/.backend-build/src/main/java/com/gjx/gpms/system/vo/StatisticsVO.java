package com.gjx.gpms.system.vo;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 数据统计VO
 */
@Data
public class StatisticsVO {

    /** 学院总数 */
    private Integer totalColleges;
    /** 专业总数 */
    private Integer totalMajors;
    /** 用户总数 */
    private Integer totalUsers;
    /** 教师数 */
    private Integer totalTeachers;
    /** 学生数 */
    private Integer totalStudents;
    /** 批次总数 */
    private Integer totalBatches;
    /** 进行中批次 */
    private Integer activeBatches;
    /** 课题总数 */
    private Integer totalTopics;
    /** 已通过课题 */
    private Integer approvedTopics;
    /** 已选题学生数 */
    private Integer selectedStudents;
    /** 未选题学生数 */
    private Integer unselectedStudents;
    /** 各阶段完成数量 */
    private List<Map<String, Object>> stageStats;
    /** 成绩分布 */
    private List<Map<String, Object>> scoreDistribution;
}
