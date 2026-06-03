package com.gjx.gpms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

/**
 * 答辩组DTO
 */
@Data
@Schema(description = "答辩组DTO")
public class DefenseGroupDTO {
    @NotNull private Long defenseBatchId;
    @NotBlank private String name;
    @NotNull private Long leaderId;
    private List<Long> memberIds;
}
