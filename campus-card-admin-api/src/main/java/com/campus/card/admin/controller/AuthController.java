package com.campus.card.admin.controller;

import com.campus.card.admin.domain.User;
import com.campus.card.admin.domain.Role;
import com.campus.card.admin.domain.Permission;
import com.campus.card.admin.domain.Menu;
import com.campus.card.admin.repository.UserRepository;
import com.campus.card.common.result.Result;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginReq req) {
        Optional<User> u = userRepository.findByUsername(req.getUsername());
        if (!u.isPresent()) {
            return Result.error("用户不存在", 404);
        }
        // 简单明文密码对比（示例），生产应使用BCrypt等
        if (!u.get().getPassword().equals(req.getPassword())) {
            return Result.error("密码错误", 401);
        }
        Map<String, Object> resp = new HashMap<>();
        resp.put("token", "admin-token-" + u.get().getId());
        resp.put("expireIn", 3600);
        return Result.ok(resp);
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        // 简单返回成功，前端清理token
        return Result.ok(null);
    }

    @GetMapping("/profile")
    public Result<Map<String, Object>> profile(@RequestParam String username) {
        Optional<User> u = userRepository.findByUsername(username);
        if (!u.isPresent()) return Result.error("用户不存在", 404);
        Map<String, Object> resp = new HashMap<>();
        resp.put("id", u.get().getId());
        resp.put("username", u.get().getUsername());
        resp.put("phone", u.get().getPhone());
        resp.put("status", u.get().getStatus());
        return Result.ok(resp);
    }

    @GetMapping("/me")
    public Result<Map<String, Object>> me(@RequestHeader(value = "Authorization", required = false) String auth) {
        if (auth == null || auth.isEmpty()) return Result.error("未登录或令牌无效", 401);
        String token = auth.trim();
        if (token.toLowerCase(java.util.Locale.ROOT).startsWith("bearer ")) {
            token = token.substring(7).trim();
        }
        if (!token.startsWith("admin-token-")) return Result.error("令牌格式错误", 401);
        String tail = token.substring("admin-token-".length());
        Long userId;
        try { userId = Long.parseLong(tail); } catch (Exception e) { return Result.error("令牌解析失败", 401); }
        // 关键修复：预先加载角色/权限/菜单，避免懒加载异常
        Optional<User> ou = userRepository.findByIdWithRolesPermsMenus(userId);
        if (!ou.isPresent()) return Result.error("用户不存在", 404);
        User u = ou.get();
        Map<String, Object> resp = new HashMap<>();
        resp.put("id", u.getId());
        resp.put("username", u.getUsername());
        // 角色
        java.util.Set<String> roles = u.getRoles() == null ? java.util.Collections.emptySet() : u.getRoles().stream()
                .filter(java.util.Objects::nonNull)
                .map(Role::getName)
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());
        resp.put("roles", roles);
        // 权限码
        java.util.Set<String> perms = u.getRoles() == null ? java.util.Collections.emptySet() : u.getRoles().stream()
                .filter(java.util.Objects::nonNull)
                .flatMap((Role r) -> java.util.Optional.ofNullable(r.getPermissions()).orElse(java.util.Collections.emptySet()).stream())
                .filter(java.util.Objects::nonNull)
                .map(Permission::getCode)
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());
        resp.put("permissions", perms);
        // 菜单路径
        java.util.Set<String> menus = u.getRoles() == null ? java.util.Collections.emptySet() : u.getRoles().stream()
                .filter(java.util.Objects::nonNull)
                .flatMap((Role r) -> java.util.Optional.ofNullable(r.getMenus()).orElse(java.util.Collections.emptySet()).stream())
                .filter(java.util.Objects::nonNull)
                .map(Menu::getPath)
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());
        resp.put("menus", menus);
        return Result.ok(resp);
    }

    @Data
    public static class LoginReq {
        private String username;
        private String password;
    }
}