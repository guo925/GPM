package com.gjx.gpms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 答辩安排DTO
 */
@Data
@Schema(description = "答辩安排DTO")
public class DefenseArrangementDTO {
    @NotNull(message = "请选择答辩组")
    private Long groupId;

    @NotNull(message = "请输入学生ID")
    private Long studentId;

    private String defenseTime;
    private String location;
}
