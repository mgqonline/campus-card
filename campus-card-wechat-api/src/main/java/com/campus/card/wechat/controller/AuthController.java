package com.campus.card.wechat.controller;

import com.campus.card.common.result.Result;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @PostMapping("/login")
    public Result<LoginResp> login(@RequestBody LoginRequest req) {
        // 验证手机号格式
        if (!req.getPhone().matches("^1[3-9]\\d{9}$")) {
            return Result.error("手机号格式不正确", 400);
        }
        
        // 验证码校验（测试环境固定为123456）
        if (!"123456".equals(req.getCode())) {
            return Result.error("验证码错误", 400);
        }
        
        LoginResp r = new LoginResp();
        r.setToken("wechat-token-" + System.currentTimeMillis());
        r.setExpireIn(3600);
        return Result.ok(r);
    }

    @PostMapping("/bind")
    public Result<String> bind(@RequestBody BindReq req) {
        return Result.ok("绑定成功: " + req.getOpenId());
    }

    @GetMapping("/profile")
    public Result<ProfileResp> profile(@RequestParam(value = "unbound", required = false) Boolean unbound) {
        ProfileResp p = new ProfileResp();
        p.setParentId(1001L);
        p.setName("张三家长");
        p.setPhone("13800001234");
        p.setEmail("parent@example.com");
        p.setAddress("北京市海淀区学院路100号");
        p.setSchoolName("第一实验小学");
        if (Boolean.TRUE.equals(unbound)) {
            p.setBoundChildren(java.util.Collections.emptyList());
        } else {
            p.setBoundChildren(Arrays.asList(
                    new ChildBrief(2001L, "张小明", 301L, "三年级一班"),
                    new ChildBrief(2002L, "张小红", 302L, "三年级二班")
            ));
        }
        return Result.ok(p);
    }

    @PutMapping("/profile")
    public Result<ProfileResp> updateProfile(@RequestBody UpdateProfileReq req) {
        ProfileResp p = new ProfileResp();
        p.setParentId(1001L);
        p.setName(req.getName() != null ? req.getName() : "张三家长");
        p.setPhone(req.getPhone() != null ? req.getPhone() : "13800001234");
        p.setEmail(req.getEmail() != null ? req.getEmail() : "parent@example.com");
        p.setAddress(req.getAddress() != null ? req.getAddress() : "北京市海淀区学院路100号");
        p.setSchoolName("第一实验小学");
        p.setBoundChildren(Arrays.asList(
                new ChildBrief(2001L, "张小明", 301L, "三年级一班"),
                new ChildBrief(2002L, "张小红", 302L, "三年级二班")
        ));
        return Result.ok(p);
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
    public static class UpdateProfileReq {
        private String name;
        private String phone;
        private String email;
        private String address;
    }

    @Data
    public static class ProfileResp {
        private Long parentId;
        private String name;
        private String phone;
        private String email;
        private String address;
        private String schoolName;
        private java.util.List<ChildBrief> boundChildren;
    }

    @Data
    public static class ChildBrief {
        private Long id;
        private String name;
        private Long classId;
        private String className;
        public ChildBrief(Long id, String name, Long classId) {
            this.id = id; this.name = name; this.classId = classId;
        }
        public ChildBrief(Long id, String name, Long classId, String className) {
            this.id = id; this.name = name; this.classId = classId; this.className = className;
        }
    }
}