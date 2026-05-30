package com.gjx.gpms.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

/**
 * Web MVC 静态资源配置。
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final FileStorageProperties fileStorageProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String prefix = normalizePrefix(fileStorageProperties.getLocalUrlPrefix());
        String location = Path.of(fileStorageProperties.getLocalPath()).toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler(prefix + "/**").addResourceLocations(location.endsWith("/") ? location : location + "/");
    }

    private String normalizePrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return "/uploads";
        }
        return prefix.startsWith("/") ? prefix : "/" + prefix;
    }
}
