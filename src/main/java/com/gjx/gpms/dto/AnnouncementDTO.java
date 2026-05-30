package com.gjx.gpms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "公告请求")
public class AnnouncementDTO {

    @NotBlank(message = "公告标题不能为空")
    @Schema(description = "公告标题")
    private String title;

    @NotBlank(message = "公告内容不能为空")
    @Schema(description = "公告内容")
    private String content;
}
