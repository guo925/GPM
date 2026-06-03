package com.gjx.gpms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * GpMsApplication 类。
 */
@EnableScheduling
@SpringBootApplication
public class GpMsApplication {

    /**
     * 应用启动入口。
     */
    public static void main(String[] args) {
        SpringApplication.run(GpMsApplication.class, args);
    }
}
