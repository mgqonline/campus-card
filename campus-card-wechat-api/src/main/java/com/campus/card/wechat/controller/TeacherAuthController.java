package com.campus.card.wechat.controller;

import com.campus.card.common.result.Result;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1/t/auth")
public class TeacherAuthController {

    @PostMapping("/login")
    public Result<LoginResp> login(@RequestBody LoginRequest req) {
        if (!req.getPhone().matches("^1[3-9]\\d{9}$")) {
            return Result.error("手机号格式不正确", 400);
        }
        if (!"123456".equals(req.getCode())) {
            return Result.error("验证码错误", 400);
        }
        LoginResp r = new LoginResp();
        r.setToken("teacher-token-" + System.currentTimeMillis());
        r.setExpireIn(3600);
        return Result.ok(r);
    }

    @PostMapping("/bind")
    public Result<String> bind(@RequestBody BindReq req) {
        return Result.ok("绑定成功: " + req.getOpenId());
    }

    @GetMapping("/profile")
    public Result<TeacherProfile> profile() {
        TeacherProfile p = new TeacherProfile();
        p.setTeacherId(9001L);
        p.setName("李老师");
        p.setPhone("13900001234");
        p.setEmail("teacher@example.com");
        p.setSubjects(Arrays.asList("语文", "班主任"));
        p.setSchoolName("第一实验小学");
        p.setAssignedClasses(Arrays.asList(
                new ClassBrief(301L, "三年级一班"),
                new ClassBrief(302L, "三年级二班")
        ));
        return Result.ok(p);
    }

    @PutMapping("/password")
    public Result<String> updatePassword(@RequestBody UpdatePasswordReq req) {
        if (req.getNewPassword() == null || req.getNewPassword().length() < 6) {
            return Result.error("新密码长度至少为6位", 400);
        }
        return Result.ok("密码修改成功");
    }

    /** 教师端登出（模拟接口，前端清除令牌即可） */
    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.ok("退出成功");
    }

    @Data
    public static class LoginRequest {
        @NotBlank(message = "手机号不能为空")
        private String phone;
        @NotBlank(message = "验证码不能为空")
        private String code;
    }

    @Data
    public static class LoginResp {
        private String token;
        private int expireIn;
    }

    @Data
    public static class BindReq {
        private String openId;
        private String phone;
    }

    @Data
    public static class UpdatePasswordReq {
        private String oldPassword;
        private String newPassword;
    }

    @Data
    public static class TeacherProfile {
        private Long teacherId;
        private String name;
        private String phone;
        private String email;
        private String schoolName;
        private List<String> subjects;
        private List<ClassBrief> assignedClasses;
    }

    @Data
    public static class ClassBrief {
        private Long classId;
        private String className;
        public ClassBrief(Long classId, String className) { this.classId = classId; this.className = className; }
    }
}