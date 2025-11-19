package com.campus.card.admin.aop;

import com.campus.card.admin.domain.DataChangeLog;
import com.campus.card.admin.domain.StudentInfo;
import com.campus.card.admin.repository.DataChangeLogRepository;
import com.campus.card.admin.repository.StudentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Aspect
@Component
public class DataChangeLogAspect {
    private final DataChangeLogRepository repo;
    private final StudentRepository studentRepo;
    private final ObjectMapper mapper = new ObjectMapper();

    public DataChangeLogAspect(DataChangeLogRepository repo, StudentRepository studentRepo) {
        this.repo = repo;
        this.studentRepo = studentRepo;
    }

    @Around("execution(* com.campus.card.admin.service.StudentService.create(..)) || execution(* com.campus.card.admin.service.StudentService.update(..)) || execution(* com.campus.card.admin.service.StudentService.delete(..)) || execution(* com.campus.card.admin.service.StudentService.enable(..)) || execution(* com.campus.card.admin.service.StudentService.disable(..)) || execution(* com.campus.card.admin.service.StudentService.graduate(..)) || execution(* com.campus.card.admin.controller.StudentController.updateArchive(..))")
    public Object logStudentChanges(ProceedingJoinPoint pjp) throws Throwable {
        String method = pjp.getSignature().getName();
        Long id = extractId(pjp);
        StudentInfo before = null;
        if (!"create".equals(method) && id != null) {
            before = studentRepo.findById(id).orElse(null);
        }
        Object retObj = null;
        Integer resultCode = 200;
        try {
            retObj = pjp.proceed();
        } catch (Exception e) {
            resultCode = 500;
            throw e;
        } finally {
            Object data = retObj;
            if (retObj instanceof com.campus.card.common.result.Result) {
                Object d = ((com.campus.card.common.result.Result<?>) retObj).getData();
                if (d != null) data = d;
            }
            StudentInfo after = data instanceof StudentInfo ? (StudentInfo) data : null;
            DataChangeLog log = new DataChangeLog();
            log.setOccurredAt(LocalDateTime.now());
            log.setEntity("StudentInfo");
            log.setEntityId(id != null ? String.valueOf(id) : (after != null && after.getId() != null ? String.valueOf(after.getId()) : null));
            log.setChangeType(mapChangeType(method));
            log.setBeforeJson(toJson(before));
            log.setAfterJson(toJson(after));
            log.setChangedFields(diff(before, after));
            log.setOperatorId(extractUserId(currentRequest()));
            log.setRemark(resultCode == 200 ? "OK" : "ERROR");
            try { repo.save(log); } catch (Exception ignore) {}
        }
        return retObj;
    }

    private String mapChangeType(String method) {
        switch (method) {
            case "create": return "CREATE";
            case "update": return "UPDATE";
            case "delete": return "DELETE";
            case "enable": return "STATUS";
            case "disable": return "STATUS";
            case "graduate": return "STATUS";
            case "updateArchive": return "ARCHIVE";
            default: return method.toUpperCase();
        }
    }

    private String toJson(Object obj) {
        try { return obj == null ? null : mapper.writeValueAsString(obj); } catch (Exception e) { return null; }
    }

    private String diff(StudentInfo before, StudentInfo after) {
        List<String> changed = new ArrayList<>();
        if (before == null && after != null) return "ALL"; // 新建
        if (before != null && after == null) return "ALL"; // 删除
        if (before == null && after == null) return null;
        if (!eq(before.getName(), after.getName())) changed.add("name");
        if (!eq(before.getStudentNo(), after.getStudentNo())) changed.add("studentNo");
        if (!eq(before.getClassId(), after.getClassId())) changed.add("classId");
        if (!eq(before.getStatus(), after.getStatus())) changed.add("status");
        if (!eq(before.getPhotoPath(), after.getPhotoPath())) changed.add("photoPath");
        if (!eq(before.getArchive(), after.getArchive())) changed.add("archive");
        return changed.isEmpty() ? null : String.join(",", changed);
    }

    private boolean eq(Object a, Object b) { return a == b || (a != null && a.equals(b)); }

    private Long extractId(ProceedingJoinPoint pjp) {
        Object[] args = pjp.getArgs();
        for (Object a : args) {
            if (a instanceof Long) return (Long) a;
            if (a instanceof StudentInfo) {
                Long id = ((StudentInfo) a).getId();
                if (id != null) return id;
            }
        }
        return null;
    }

    private HttpServletRequest currentRequest() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes) return ((ServletRequestAttributes) attrs).getRequest();
        return null;
    }

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
}