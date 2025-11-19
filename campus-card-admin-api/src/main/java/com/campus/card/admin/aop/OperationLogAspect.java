package com.campus.card.admin.aop;

import com.campus.card.admin.domain.OpLog;
import com.campus.card.admin.repository.OpLogRepository;
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
public class OperationLogAspect {
    private final OpLogRepository opLogRepo;

    public OperationLogAspect(OpLogRepository opLogRepo) {
        this.opLogRepo = opLogRepo;
    }

    @Around("execution(* com.campus.card.admin.controller..*(..))")
    public Object logOperation(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        Integer resultCode = 200;
        Object ret;
        Exception error = null;
        try {
            ret = pjp.proceed();
        } catch (Exception e) {
            resultCode = 500;
            error = e;
            throw e;
        } finally {
            int duration = (int) (System.currentTimeMillis() - start);
            OpLog log = new OpLog();
            HttpServletRequest req = getRequest();
            if (req != null) {
                log.setMethod(req.getMethod());
                log.setUri(req.getRequestURI());
                log.setClientIp(req.getRemoteAddr());
                log.setParams(safe(req.getQueryString()));
                log.setAction(pjp.getSignature().getName());
                log.setUserId(extractUserId(req));
                log.setSubjectType("USER");
            } else {
                log.setMethod("N/A");
                log.setUri(pjp.getSignature().toShortString());
                log.setClientIp("N/A");
                log.setParams(null);
                log.setAction(pjp.getSignature().getName());
            }
            log.setOccurredAt(LocalDateTime.now());
            log.setResultCode(resultCode);
            log.setDurationMs(duration);
            try { opLogRepo.save(log); } catch (Exception ignore) {}
        }
        return ret;
    }

    private HttpServletRequest getRequest() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes) {
            return ((ServletRequestAttributes) attrs).getRequest();
        }
        return null;
    }

    private String safe(String s) { return s == null ? null : s; }

    private Long extractUserId(HttpServletRequest req) {
        try {
            String auth = req.getHeader("Authorization");
            if (auth == null) return null;
            // 支持 Bearer admin-token-1 或直接 admin-token-1
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
}