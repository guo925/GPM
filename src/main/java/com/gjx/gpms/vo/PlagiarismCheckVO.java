package com.gjx.gpms.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * PlagiarismCheck 视图对象。
 */
@Data
@Schema(description = "AI查重结果")
public class PlagiarismCheckVO {

    @Schema(description = "总体相似度百分比")
    private Double similarity;

    @Schema(description = "风险等级：pass, warning, risk")
    private String status;

    @Schema(description = "风险等级中文说明")
    private String statusText;

    @Schema(description = "AI分析结论")
    private String aiSummary;

    @Schema(description = "检测字数")
    private Integer wordCount;

    @Schema(description = "最高相似来源")
    private MatchSource topSource;

    @Schema(description = "相似来源列表")
    private List<MatchSource> sources;

    @Schema(description = "疑似重复片段")
    private List<String> suspiciousSegments;

    /**
     * MatchSource 类。
     */
    @Data
    @Schema(description = "相似来源")
    public static class MatchSource {
        private Long processInstanceId;
        private Long studentTopicId;
        private String studentName;
        private String topicTitle;
        private String stage;
        private Double similarity;
        private String matchedText;
    }
}
