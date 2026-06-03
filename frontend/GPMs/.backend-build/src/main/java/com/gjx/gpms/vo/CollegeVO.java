package com.gjx.gpms.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学院VO
 *
 * @author gpms
 */
@Data
@Schema(description = "学院VO")
public class CollegeVO {

    @Schema(description = "学院ID")
    private Long id;

    @Schema(description = "学院名称")
    private String name;

    @Schema(description = "学院代码")
    private String code;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
