package com.gjx.gpms.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 成绩单VO
 */
@Data
@Schema(description = "成绩单VO")
public class ScoreSheetVO {
    private Long id;
    private Long studentTopicId;
    private Long batchId;
    private String studentName;
    private String topicTitle;
    private BigDecimal advisorScore;
    private BigDecimal reviewerScore;
    private BigDecimal defenseScore;
    private BigDecimal finalScore;
    private String gradeLevel;
    private String status;
    private String reviewComment;
    private List<ScoreDetailVO> details;
    private LocalDateTime createdAt;

    /**
     * ScoreDetail 视图对象。
     */
    @Data
    public static class ScoreDetailVO {
        private String type;
        private BigDecimal score;
        private BigDecimal weight;
        private String comment;
    }
}
