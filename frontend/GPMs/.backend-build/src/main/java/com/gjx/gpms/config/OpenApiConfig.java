package com.gjx.gpms.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j 配置类
 */
@Configuration
public class OpenApiConfig {

    /**
     * 处理springOpenAPI相关逻辑。
     */
    @Bean
    public OpenAPI springOpenAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("GPMS 毕设管理系统接口文档")
                        .description("企业级毕业设计管理系统")
                        .version("1.0"))
                .externalDocs(new ExternalDocumentation()
                        .description("项目文档"));
    }
}