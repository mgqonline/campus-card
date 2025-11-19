package com.campus.card.admin.aop;

import com.campus.card.admin.domain.DeviceOpLog;
import com.campus.card.admin.repository.DeviceOpLogRepository;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Aspect
@Component
public class DeviceOperationLogAspect {
    private final DeviceOpLogRepository repo;

    public DeviceOperationLogAspect(DeviceOpLogRepository repo) { this.repo = repo; }

    @Around("execution(* com.campus.card.admin.controller.AttendanceDeviceController.*(..)) || execution(* com.campus.card.admin.controller.DeviceController.*(..))")
    public Object logDeviceOps(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        Integer resultCode = 200;
        Object ret;
        try {
            ret = pjp.proceed();
        } catch (Exception e) {
            resultCode = 500;
            throw e;
        } finally {
            int duration = (int) (System.currentTimeMillis() - start);
            HttpServletRequest req = currentRequest();
            String methodName = pjp.getSignature().getName();
            // 排除读取类操作（列表/详情/状态），仅跳过日志写入，但不影响正常返回
            boolean readOnly = methodName.startsWith("list") || methodName.startsWith("detail") || methodName.startsWith("status");
            if (!readOnly) {
                DeviceOpLog log = new DeviceOpLog();
                log.setOccurredAt(LocalDateTime.now());
                log.setAction(methodName);
                log.setParams(req != null ? safe(req.getQueryString()) : null);
                log.setResult(resultCode != null && resultCode == 200 ? "OK" : "ERROR");
                log.setOperatorId(extractUserId(req));
                // 尝试解析设备ID
                Long deviceId = extractDeviceId(pjp, req);
                log.setDeviceId(deviceId);
                log.setDeviceCode(null);
                try { repo.save(log); } catch (Exception ignore) {}
            }
        }
        return ret;
    }

    private HttpServletRequest currentRequest() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes) return ((ServletRequestAttributes) attrs).getRequest();
        return null;
    }

    private String safe(String s) { return s == null ? null : s; }

    private Long extractUserId(HttpServletRequest req) {
        try {
            if (req == null) return null;
            String auth = req.getHeader("Authorization");
            if (auth == null) return null;
            int idx = auth.indexOf("admin-token-");
            if (idx >= 0) {
                String sub = auth.substring(idx + "admin-token-".length());
                StringBuilder sb = new StringBuilder();
                for (char c : sub.toCharArray()) { if (Character.isDigit(c)) sb.append(c); else break; }
                if (sb.length() > 0) return Long.parseLong(sb.toString());
            }
        } catch (Exception ignore) {}
        return null;
    }

    private Long extractDeviceId(ProceedingJoinPoint pjp, HttpServletRequest req) {
        // 1) 首参数可能为 Long id
        try {
            Object[] args = pjp.getArgs();
            if (args != null && args.length > 0 && args[0] instanceof Long) return (Long) args[0];
        } catch (Exception ignore) {}
        // 2) URI 尾段可能是 /{id}
        try {
            if (req != null) {
                String uri = req.getRequestURI();
                String[] parts = uri.split("/");
                if (parts.length > 0) {
                    String last = parts[parts.length - 1];
                    if (last.matches("\\d+")) return Long.parseLong(last);
                }
            }
        } catch (Exception ignore) {}
        return null;
    }
}