package com.campus.card.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.context.annotation.Bean;

import com.campus.card.admin.repository.UserRepository;
import com.campus.card.admin.domain.User;
import com.campus.card.admin.domain.Permission;
import com.campus.card.admin.domain.Role;
import com.campus.card.admin.domain.Menu;
import com.campus.card.admin.repository.RoleRepository;
import com.campus.card.admin.repository.PermissionRepository;
import com.campus.card.admin.repository.MenuRepository;

@SpringBootApplication
@EnableScheduling
public class CampusCardAdminApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(CampusCardAdminApiApplication.class, args);
    }

    @Bean
    public org.springframework.boot.CommandLineRunner seedLowPermissionUser(UserRepository userRepository) {
        return args -> {
            String username = "auditor";
            if (!userRepository.findByUsername(username).isPresent()) {
                User u = new User();
                u.setUsername(username);
                u.setPassword("123456");
                u.setStatus(1);
                // 无角色，确保无任何权限
                u.setRoles(new java.util.HashSet<>());
                userRepository.save(u);
            }
        };
    }

    // 新增：初始化权限、菜单、角色，并绑定到admin用户
    @Bean
    public org.springframework.boot.CommandLineRunner seedPermissionsMenusRoles(UserRepository userRepository,
                                                                                RoleRepository roleRepository,
                                                                                PermissionRepository permissionRepository,
                                                                                MenuRepository menuRepository) {
        return args -> {
            // 1) 确保存在 admin 用户
            User admin = userRepository.findByUsername("admin").orElseGet(() -> {
                User u = new User();
                u.setUsername("admin");
                u.setPassword("123456"); // 与前端自动登录保持一致
                u.setStatus(1);
                u.setRoles(new java.util.HashSet<>());
                return userRepository.save(u);
            });
            // 强制统一密码以便开发验证
            admin.setPassword("123456");

            // 2) 需要的权限码集合（覆盖权限管理与角色/用户权限相关功能）
            java.util.Map<String, String> permDefs = new java.util.LinkedHashMap<>();
            permDefs.put("system:permission:list", "权限-列表查询");
            permDefs.put("system:permission:update", "权限-更新");
            permDefs.put("system:role:permissions:get", "角色-权限列表");
            permDefs.put("system:role:permissions:assign", "角色-分配权限");
            permDefs.put("system:role:members:get", "角色-成员列表");
            permDefs.put("system:user:detail", "用户-详情");
            permDefs.put("system:user:enable", "用户-启用");
            permDefs.put("system:user:roles:get", "用户-角色列表");
            permDefs.put("system:user:roles:assign", "用户-分配角色");
            // 可选：通配权限，便于前端指令匹配
            permDefs.put("system:permission:*", "权限-全部操作");
            permDefs.put("system:user:*", "用户-全部操作");
            permDefs.put("system:role:*", "角色-全部操作");

            java.util.List<Permission> allPerms = permissionRepository.findAll();
            java.util.Map<String, Permission> byCode = new java.util.HashMap<>();
            for (Permission p : allPerms) {
                if (p.getCode() != null) byCode.put(p.getCode(), p);
            }
            java.util.Set<Permission> ensurePerms = new java.util.HashSet<>();
            for (java.util.Map.Entry<String, String> e : permDefs.entrySet()) {
                Permission p = byCode.get(e.getKey());
                if (p == null) {
                    p = new Permission();
                    p.setCode(e.getKey());
                    p.setName(e.getValue());
                    p.setDescription("系统自动初始化");
                    p = permissionRepository.save(p);
                }
                ensurePerms.add(p);
            }

            // 3) 菜单：确保“权限管理”可见
            java.util.List<Menu> allMenus = menuRepository.findAll();
            java.util.Map<String, Menu> byPath = new java.util.HashMap<>();
            for (Menu m : allMenus) {
                if (m.getPath() != null) byPath.put(m.getPath(), m);
            }
            String permListPath = "/permission/list";
            Menu permMenu = byPath.get(permListPath);
            if (permMenu == null) {
                permMenu = new Menu();
                permMenu.setName("权限管理");
                permMenu.setPath(permListPath);
                permMenu.setStatus(1);
                permMenu.setSortOrder(10);
                permMenu = menuRepository.save(permMenu);
            }

            // 4) 角色：ADMIN（超级管理员）包含上述权限与菜单
            Role adminRole = roleRepository.findAll().stream()
                    .filter(r -> {
                        String n = java.util.Optional.ofNullable(r.getName()).orElse("");
                        return "ADMIN".equalsIgnoreCase(n) || "超级管理员".equals(n);
                    })
                    .findFirst()
                    .orElseGet(() -> {
                        Role r = new Role();
                        r.setName("ADMIN");
                        return roleRepository.save(r);
                    });
            // 直接覆盖绑定权限与菜单，避免懒加载集合初始化异常
            adminRole.setPermissions(new java.util.HashSet<>(ensurePerms));
            adminRole.setMenus(new java.util.HashSet<>(java.util.Collections.singleton(permMenu)));
            adminRole = roleRepository.save(adminRole);

            // 5) 将 admin 用户加入 ADMIN 角色（覆盖赋值，保证有权限）
            admin.setRoles(new java.util.HashSet<>(java.util.Collections.singleton(adminRole)));
            userRepository.save(admin);
        };
    }
}