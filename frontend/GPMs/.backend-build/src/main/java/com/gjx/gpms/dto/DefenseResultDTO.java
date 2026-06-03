package com.gjx.gpms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

/**
 * 答辩结果DTO
 */
@Data
@Schema(description = "答辩结果DTO")
public class DefenseResultDTO {
    @NotNull private Long arrangementId;
    private String scoreItems;
    private BigDecimal totalScore;
    @NotBlank private String decision;
    private String comment;
}
