package com.campus.card.admin.controller;

import com.campus.card.admin.domain.Role;
import com.campus.card.admin.domain.User;
import com.campus.card.admin.repository.RoleRepository;
import com.campus.card.admin.repository.UserRepository;
import com.campus.card.admin.security.RequiresPermission;
import com.campus.card.common.result.PageResult;
import com.campus.card.common.result.Result;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserController(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @GetMapping
    @RequiresPermission({"system:user:list"})
    public Result<PageResult<User>> list(@RequestParam(defaultValue = "1") @Min(1) int page,
                                         @RequestParam(defaultValue = "10") @Min(1) int size,
                                         @RequestParam(required = false) String username,
                                         @RequestParam(required = false) Integer status) {
        PageResult<User> pr = new PageResult<>();
        java.util.List<User> all = userRepository.findAll();
        if (username != null && !username.trim().isEmpty()) {
            String kw = username.trim().toLowerCase();
            all = all.stream().filter(u -> u.getUsername() != null && u.getUsername().toLowerCase().contains(kw)).collect(java.util.stream.Collectors.toList());
        }
        if (status != null) {
            all = all.stream().filter(u -> java.util.Objects.equals(u.getStatus(), status)).collect(java.util.stream.Collectors.toList());
        }
        pr.setTotal(all.size());
        int from = Math.max(0, (page - 1) * size);
        int to = Math.min(all.size(), from + size);
        pr.setRecords(from >= all.size() ? java.util.Collections.emptyList() : all.subList(from, to));
        return Result.ok(pr);
    }

    @PostMapping
    public Result<User> create(@RequestBody CreateUserReq req) {
        if (req.getUsername() == null || req.getPassword() == null) return Result.error("用户名与密码不能为空", 400);
        User u = new User();
        u.setUsername(req.getUsername());
        u.setPassword(req.getPassword());
        u.setPhone(req.getPhone()); // 将被加密保存
        u.setStatus(1);
        if (req.getRoleIds() != null && !req.getRoleIds().isEmpty()) {
            u.setRoles(new java.util.HashSet<>(roleRepository.findAllById(req.getRoleIds())));
        }
        return Result.ok(userRepository.save(u));
    }

    @PutMapping("/{id}/disable")
    public Result<User> disable(@PathVariable Long id) {
        Optional<User> ou = userRepository.findById(id);
        if (!ou.isPresent()) return Result.error("用户不存在", 404);
        User u = ou.get();
        u.setStatus(0);
        return Result.ok(userRepository.save(u));
    }

    @GetMapping("/{id}")
    @RequiresPermission({"system:user:detail"})
    public Result<User> detail(@PathVariable Long id) {
        Optional<User> ou = userRepository.findById(id);
        if (!ou.isPresent()) return Result.error("用户不存在", 404);
        return Result.ok(ou.get());
    }

    @PutMapping("/{id}/enable")
    @RequiresPermission({"system:user:enable"})
    public Result<User> enable(@PathVariable Long id) {
        Optional<User> ou = userRepository.findById(id);
        if (!ou.isPresent()) return Result.error("用户不存在", 404);
        User u = ou.get();
        u.setStatus(1);
        return Result.ok(userRepository.save(u));
    }

    @GetMapping("/{id}/roles")
    @RequiresPermission({"system:user:roles:get"})
    public Result<List<Long>> roles(@PathVariable Long id) {
        Optional<User> ou = userRepository.findById(id);
        if (!ou.isPresent()) return Result.error("用户不存在", 404);
        List<Long> ids = ou.get().getRoles().stream().map(Role::getId).collect(java.util.stream.Collectors.toList());
        return Result.ok(ids);
    }

    @PutMapping("/{id}/roles")
    @RequiresPermission({"system:user:roles:assign"})
    public Result<User> assignRoles(@PathVariable Long id, @RequestBody AssignRolesReq req) {
        Optional<User> ou = userRepository.findById(id);
        if (!ou.isPresent()) return Result.error("用户不存在", 404);
        User u = ou.get();
        List<Long> roleIds = req.getRoleIds() == null ? java.util.Collections.emptyList() : req.getRoleIds();
        u.setRoles(new java.util.HashSet<>(roleRepository.findAllById(roleIds)));
        return Result.ok(userRepository.save(u));
    }

    @PutMapping("/{id}")
    public Result<User> update(@PathVariable Long id, @RequestBody UpdateUserReq req) {
        Optional<User> ou = userRepository.findById(id);
        if (!ou.isPresent()) return Result.error("用户不存在", 404);
        User u = ou.get();
        if (req.getUsername() != null && !req.getUsername().trim().isEmpty()) {
            u.setUsername(req.getUsername().trim());
        }
        if (req.getPhone() != null) {
            u.setPhone(req.getPhone());
        }
        return Result.ok(userRepository.save(u));
    }

    @PutMapping("/{id}/password")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody ResetPasswordReq req) {
        if (req.getNewPassword() == null || req.getNewPassword().trim().isEmpty()) {
            return Result.error("新密码不能为空", 400);
        }
        Optional<User> ou = userRepository.findById(id);
        if (!ou.isPresent()) return Result.error("用户不存在", 404);
        User u = ou.get();
        u.setPassword(req.getNewPassword());
        userRepository.save(u);
        return Result.ok(null);
    }

    @Data
    public static class UpdateUserReq {
        private String username;
        private String phone;
    }

    @Data
    public static class ResetPasswordReq {
        private String newPassword;
    }

    @Data
    public static class AssignRolesReq {
        private List<Long> roleIds;
    }

    @Data
    public static class CreateUserReq {
        private String username;
        private String password;
        private String phone;
        private List<Long> roleIds;
    }
}