package com.gjx.gpms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 答辩批次DTO
 */
@Data
@Schema(description = "答辩批次DTO")
public class DefenseBatchDTO {
    @NotNull private Long batchId;
    @NotBlank private String type;
    @NotBlank private String name;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String locationTemplate;
}
