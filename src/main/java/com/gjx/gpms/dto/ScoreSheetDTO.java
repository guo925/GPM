package com.gjx.gpms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 成绩单DTO
 */
@Data
@Schema(description = "成绩单DTO")
public class ScoreSheetDTO {
    @NotNull private Long studentTopicId;
    @NotNull private Long batchId;
    private BigDecimal advisorScore;
    private BigDecimal reviewerScore;
    private BigDecimal defenseScore;
    private String advisorWeight;
    private String reviewerWeight;
    private String defenseWeight;
}
