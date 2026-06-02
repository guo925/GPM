package com.gjx.gpms.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 文件存储配置，支持本地和 OSS 两种模式。
 */
@Data
@Component
@ConfigurationProperties(prefix = "file.storage")
public class FileStorageProperties {

    private String type = "oss";

    private String localPath = "./uploads";

    private String localUrlPrefix = "/uploads";
}
