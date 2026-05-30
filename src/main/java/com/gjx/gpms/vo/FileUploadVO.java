package com.gjx.gpms.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 文件上传结果。
 */
@Data
@Schema(description = "文件上传结果")
public class FileUploadVO {

    @Schema(description = "原始文件名")
    private String originalName;

    @Schema(description = "存储对象Key")
    private String objectKey;

    @Schema(description = "文件访问地址")
    private String url;

    @Schema(description = "文件大小")
    private Long size;

    @Schema(description = "内容类型")
    private String contentType;
}
