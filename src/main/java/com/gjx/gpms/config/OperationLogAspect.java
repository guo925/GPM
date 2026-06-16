package com.gjx.gpms.config;

import com.gjx.gpms.entity.OperationLog;
import com.gjx.gpms.mapper.OperationLogMapper;
import com.gjx.gpms.security.context.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * 操作日志兜底落库，避免依赖 MQ 时漏记管理端写操作。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogMapper operationLogMapper;

    @AfterReturning("within(@org.springframework.web.bind.annotation.RestController *)")
    public void recordSuccessfulWrite() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }
        HttpServletRequest request = attributes.getRequest();
        String method = request.getMethod();
        if (!("POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method))) {
            return;
        }
        String uri = request.getRequestURI();
        if (uri == null || !uri.startsWith("/api/") || uri.startsWith("/api/auth/")) {
            return;
        }
        try {
            OperationLog operationLog = new OperationLog();
            operationLog.setUserId(UserContext.getUserId());
            operationLog.setAction(method);
            operationLog.setTargetType(resolveTargetType(uri));
            operationLog.setIpAddress(request.getRemoteAddr());
            operationLog.setUserAgent(request.getHeader("User-Agent"));
            operationLog.setRemark(uri);
            operationLog.setCreatedAt(LocalDateTime.now());
            operationLogMapper.insert(operationLog);
        } catch (Exception e) {
            log.warn("操作日志写入失败：{}", e.getMessage());
        }
    }

    private String resolveTargetType(String uri) {
        String[] parts = uri.split("/");
        if (parts.length >= 3 && "system".equals(parts[2]) && parts.length >= 4) {
            return parts[2] + "_" + parts[3];
        }
        return parts.length >= 3 ? parts[2] : uri;
    }
}
