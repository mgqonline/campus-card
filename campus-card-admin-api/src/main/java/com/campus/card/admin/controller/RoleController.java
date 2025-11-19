package com.campus.card.admin.controller;

import com.campus.card.admin.domain.Permission;
import com.campus.card.admin.domain.Role;
import com.campus.card.admin.domain.User;
import com.campus.card.admin.repository.PermissionRepository;
import com.campus.card.admin.repository.RoleRepository;
import com.campus.card.admin.repository.UserRepository;
import com.campus.card.admin.security.RequiresPermission;
import com.campus.card.common.result.PageResult;
import com.campus.card.common.result.Result;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;

    public RoleController(RoleRepository roleRepository, PermissionRepository permissionRepository, UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    @RequiresPermission({"system:role:list"})
    public Result<PageResult<Role>> list(@RequestParam(defaultValue = "1") @Min(1) int page,
                                         @RequestParam(defaultValue = "10") @Min(1) int size,
                                         @RequestParam(required = false) String name) {
        java.util.List<Role> all = roleRepository.findAll();
        if (name != null && !name.trim().isEmpty()) {
            String kw = name.trim().toLowerCase();
            all = all.stream().filter(r -> r.getName() != null && r.getName().toLowerCase().contains(kw)).collect(java.util.stream.Collectors.toList());
        }
        int total = all.size();
        int from = Math.max(0, (page - 1) * size);
        int to = Math.min(total, from + size);
        java.util.List<Role> pageList = from >= total ? java.util.Collections.emptyList() : all.subList(from, to);
        return Result.ok(PageResult.of(total, pageList));
    }

    @GetMapping("/{id}")
    public Result<Role> detail(@PathVariable Long id) {
        Optional<Role> or = roleRepository.findById(id);
        if (!or.isPresent()) return Result.error("角色不存在", 404);
        return Result.ok(or.get());
    }

    @PostMapping
    public Result<Role> create(@RequestBody CreateRoleReq req) {
        if (req.getName() == null || req.getName().trim().isEmpty()) {
            return Result.error("角色名不能为空", 400);
        }
        Role r = new Role();
        r.setName(req.getName().trim());
        return Result.ok(roleRepository.save(r));
    }

    @PutMapping("/{id}")
    public Result<Role> update(@PathVariable Long id, @RequestBody UpdateRoleReq req) {
        Optional<Role> or = roleRepository.findById(id);
        if (!or.isPresent()) return Result.error("角色不存在", 404);
        Role r = or.get();
        if (req.getName() != null && !req.getName().trim().isEmpty()) {
            r.setName(req.getName().trim());
        }
        // 目前无status字段，忽略禁用逻辑；保留占位以后扩展
        return Result.ok(roleRepository.save(r));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        if (!roleRepository.existsById(id)) {
            return Result.error("角色不存在", 404);
        }
        roleRepository.deleteById(id);
        return Result.ok(null);
    }

    @PutMapping("/{id}/disable")
    public Result<Map<String, Object>> disable(@PathVariable Long id) {
        // 角色实体暂无status字段，这里做无副作用的成功返回以兼容前端
        if (!roleRepository.existsById(id)) return Result.error("角色不存在", 404);
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        return Result.ok(resp);
    }

    @GetMapping("/{id}/permissions")
    @RequiresPermission({"system:role:permissions:get"})
    public Result<List<Long>> getPermissions(@PathVariable Long id) {
        Optional<Role> or = roleRepository.findById(id);
        if (!or.isPresent()) return Result.error("角色不存在", 404);
        List<Long> ids = or.get().getPermissions().stream().map(Permission::getId).collect(java.util.stream.Collectors.toList());
        return Result.ok(ids);
    }

    @PutMapping("/{id}/permissions")
    @RequiresPermission({"system:role:permissions:assign"})
    public Result<Map<String, Object>> assignPermissions(@PathVariable Long id, @RequestBody AssignPermissionsReq req) {
        Optional<Role> or = roleRepository.findById(id);
        if (!or.isPresent()) return Result.error("角色不存在", 404);
        Role r = or.get();
        List<Long> permIds = req.getPermissionIds() == null ? java.util.Collections.emptyList() : req.getPermissionIds();
        r.getPermissions().clear();
        r.getPermissions().addAll(new java.util.HashSet<>(permissionRepository.findAllById(permIds)));
        roleRepository.save(r);
        Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("success", true);
        return Result.ok(resp);
    }

    @GetMapping("/{id}/members")
    @RequiresPermission({"system:role:members:get"})
    public Result<List<Long>> getMembers(@PathVariable Long id) {
        Optional<Role> or = roleRepository.findById(id);
        if (!or.isPresent()) return Result.error("角色不存在", 404);
        Role role = or.get();
        List<Long> userIds = userRepository.findAll().stream()
                .filter(u -> u.getRoles() != null && u.getRoles().stream().anyMatch(r -> java.util.Objects.equals(r.getId(), role.getId())))
                .map(User::getId)
                .collect(java.util.stream.Collectors.toList());
        return Result.ok(userIds);
    }

    @PutMapping("/{id}/members")
    public Result<Map<String, Object>> assignMembers(@PathVariable Long id, @RequestBody AssignMembersReq req) {
        Optional<Role> or = roleRepository.findById(id);
        if (!or.isPresent()) return Result.error("角色不存在", 404);
        Role role = or.get();
        Set<Long> target = new HashSet<>(req.getUserIds() == null ? Collections.emptyList() : req.getUserIds());
        List<User> users = userRepository.findAll();
        for (User u : users) {
            boolean has = u.getRoles() != null && u.getRoles().stream().anyMatch(r -> Objects.equals(r.getId(), role.getId()));
            boolean shouldHave = target.contains(u.getId());
            if (has && !shouldHave) {
                u.getRoles().removeIf(r -> Objects.equals(r.getId(), role.getId()));
                userRepository.save(u);
            } else if (!has && shouldHave) {
                u.getRoles().add(role);
                userRepository.save(u);
            }
        }
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        return Result.ok(resp);
    }

    @Data
    public static class CreateRoleReq {
        private String name;
    }

    @Data
    public static class UpdateRoleReq {
        private String name;
        private Integer status; // 预留
        private String description; // 预留
    }

    @Data
    public static class AssignPermissionsReq {
        private List<Long> permissionIds;
    }

    @Data
    public static class AssignMembersReq {
        private List<Long> userIds;
    }
}