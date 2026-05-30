package com.gjx.gpms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class GpMsApplication {

    public static void main(String[] args) {
        SpringApplication.run(GpMsApplication.class, args);
    }
}
