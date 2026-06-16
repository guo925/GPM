package com.gjx.gpms.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

/**
 * 统一应用层时间为北京时间，避免服务器默认 UTC 导致页面时间相差 8 小时。
 */
@Configuration
@RequiredArgsConstructor
public class TimeZoneConfig {

    private static final String ZONE_ID = "Asia/Shanghai";

    private final ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        TimeZone timeZone = TimeZone.getTimeZone(ZONE_ID);
        TimeZone.setDefault(timeZone);
        objectMapper.setTimeZone(timeZone);
    }
}
