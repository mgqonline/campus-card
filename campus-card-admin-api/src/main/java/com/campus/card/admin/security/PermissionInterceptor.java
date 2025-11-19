package com.campus.card.admin.security;

import com.campus.card.admin.domain.Permission;
import com.campus.card.admin.domain.Role;
import com.campus.card.admin.domain.User;
import com.campus.card.admin.repository.UserRepository;
import org.springframework.http.MediaType;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class PermissionInterceptor implements HandlerInterceptor {
    private final UserRepository userRepository;

    public PermissionInterceptor(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行登录与公共接口
        String uri = request.getRequestURI();
        if (uri.startsWith("/api/v1/auth/login") || uri.startsWith("/api/v1/auth/logout") || uri.startsWith("/api/v1/auth/profile")) {
            return true;
        }
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod hm = (HandlerMethod) handler;
        RequiresPermission requires = resolveAnnotation(hm);
        if (requires == null || requires.value().length == 0) {
            // 未声明权限时默认放行（保持现有功能）
            return true;
        }
        // 从Authorization解析用户ID（简单token：admin-token-<id>）
        String auth = Optional.ofNullable(request.getHeader("Authorization")).orElse("");
        Long userId = parseUserIdFromToken(auth);
        if (userId == null) {
            return deny(response, 401, "未登录或令牌无效");
        }
        // 关键修复：使用fetch join预加载角色/权限/菜单，避免懒加载异常
        Optional<User> ou = userRepository.findByIdWithRolesPermsMenus(userId);
        if (!ou.isPresent()) {
            return deny(response, 401, "用户不存在");
        }
        User user = ou.get();
        // 超级管理员放行（按角色名）
        boolean isAdmin = user.getRoles() != null && user.getRoles().stream().anyMatch(r -> {
            String n = Optional.ofNullable(r.getName()).orElse("");
            return "ADMIN".equalsIgnoreCase(n) || "超级管理员".equals(n);
        });
        if (isAdmin) return true;
        // 计算权限码（含通配符继承）
        Set<String> codes = user.getRoles() == null ? Collections.emptySet() : user.getRoles().stream()
                .filter(Objects::nonNull)
                .flatMap((Role r) -> Optional.ofNullable(r.getPermissions()).orElse(Collections.emptySet()).stream())
                .filter(Objects::nonNull)
                .map(Permission::getCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<String> expanded = expandWildcards(codes);
        // 校验所需权限是否全部满足（AND）
        List<String> required = Arrays.stream(requires.value()).filter(Objects::nonNull).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
        boolean ok = required.stream().allMatch(expanded::contains);
        if (!ok) {
            return deny(response, 403, "无访问权限");
        }
        return true;
    }

    private RequiresPermission resolveAnnotation(HandlerMethod hm) {
        RequiresPermission ann = hm.getMethodAnnotation(RequiresPermission.class);
        if (ann != null) return ann;
        Annotation[] anns = hm.getBeanType().getAnnotations();
        for (Annotation a : anns) {
            if (a instanceof RequiresPermission) return (RequiresPermission) a;
        }
        return null;
    }

    private void writeJson(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(code);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String json = String.format("{\"code\":%d,\"message\":\"%s\"}", code, message);
        response.getOutputStream().write(json.getBytes(StandardCharsets.UTF_8));
    }

    private boolean deny(HttpServletResponse response, int code, String message) throws IOException {
        writeJson(response, code, message);
        return false;
    }

    private Long parseUserIdFromToken(String auth) {
        if (auth == null) return null;
        String token = auth.trim();
        if (token.toLowerCase(Locale.ROOT).startsWith("bearer ")) {
            token = token.substring(7).trim();
        }
        if (!token.startsWith("admin-token-")) return null;
        String tail = token.substring("admin-token-".length());
        try {
            return Long.parseLong(tail);
        } catch (Exception e) {
            return null;
        }
    }

    private Set<String> expandWildcards(Set<String> codes) {
        if (codes == null || codes.isEmpty()) return Collections.emptySet();
        Set<String> expanded = new HashSet<>(codes);
        // 简单通配逻辑：system:user:* -> 展开为所有同前缀的具体权限（这里仅保留占位，后续可根据数据库中权限集扩展）
        // 当前实现直接返回已有权限集
        return expanded;
    }
}