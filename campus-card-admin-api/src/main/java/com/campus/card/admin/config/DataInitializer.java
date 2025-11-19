package com.campus.card.admin.config;

import com.campus.card.admin.domain.Menu;
import com.campus.card.admin.domain.Role;
import com.campus.card.admin.domain.StudentInfo;
import com.campus.card.admin.domain.User;
import com.campus.card.admin.repository.MenuRepository;
import com.campus.card.admin.repository.RoleRepository;
import com.campus.card.admin.repository.StudentRepository;
import com.campus.card.admin.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!mysql")
public class DataInitializer {

    @Bean
    CommandLineRunner initData(StudentRepository studentRepository,
                               UserRepository userRepository,
                               RoleRepository roleRepository,
                               MenuRepository menuRepository) {
        return args -> {
            if (studentRepository.count() == 0) {
                for (int i = 1; i <= 50; i++) {
                    StudentInfo s = new StudentInfo();
                    s.setName("张三" + i);
                    s.setStudentNo("S2024" + String.format("%03d", i));
                    s.setClassId(100L + (i % 5));
                    s.setStatus(i % 2);
                    studentRepository.save(s);
                }
            }
            if (roleRepository.count() == 0) {
                Role admin = new Role(); admin.setName("ADMIN");
                Role teacher = new Role(); teacher.setName("TEACHER");
                roleRepository.save(admin);
                roleRepository.save(teacher);
            }
            if (menuRepository.count() == 0) {
                Menu m1 = new Menu(); m1.setName("学生管理"); m1.setPath("/student/list"); m1.setSortOrder(1);
                Menu m2 = new Menu(); m2.setName("用户管理"); m2.setPath("/system/user"); m2.setSortOrder(2);
                menuRepository.save(m1);
                menuRepository.save(m2);
            }
            if (userRepository.count() == 0) {
                User u = new User();
                u.setUsername("admin");
                u.setPassword("123456"); // 示例，生产使用加密
                u.setPhone("13800000000"); // 将被加密存储
                u.setStatus(1);
                // 简单关联ADMIN角色
                roleRepository.findAll().stream().filter(r -> "ADMIN".equals(r.getName())).findFirst()
                        .ifPresent(r -> u.getRoles().add(r));
                userRepository.save(u);
            }
        };
    }
}