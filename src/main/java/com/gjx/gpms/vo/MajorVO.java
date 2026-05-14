package com.gjx.gpms.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 专业VO
 *
 * @author gpms
 */
@Data
@Schema(description = "专业VO")
public class MajorVO {

    @Schema(description = "专业ID")
    private Long id;

    @Schema(description = "所属学院ID")
    private Long collegeId;

    @Schema(description = "所属学院名称")
    private String collegeName;

    @Schema(description = "专业名称")
    private String name;

    @Schema(description = "专业代码")
    private String code;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
