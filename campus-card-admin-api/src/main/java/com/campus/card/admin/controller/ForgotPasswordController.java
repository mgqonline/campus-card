package com.campus.card.admin.controller;

import com.campus.card.admin.domain.User;
import com.campus.card.admin.repository.UserRepository;
import com.campus.card.common.result.Result;
import lombok.Data;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/auth/forgot")
public class ForgotPasswordController {

    private final UserRepository userRepository;
    private final StringRedisTemplate redis;

    public ForgotPasswordController(UserRepository userRepository, StringRedisTemplate redis) {
        this.userRepository = userRepository;
        this.redis = redis;
    }

    private String keyCode(String username) { return "forgot:code:" + username; }
    private String keySent(String username) { return "forgot:sent:" + username; }
    private String keyErr(String username) { return "forgot:err:" + username; }
    private String keyLock(String username) { return "forgot:lock:" + username; }

    @PostMapping("/send-code")
    public Result<Map<String, Object>> sendCode(@RequestBody SendCodeReq req) {
        if (req == null || !StringUtils.hasText(req.getUsername())) {
            return Result.error("用户名不能为空", 400);
        }
        String username = req.getUsername().trim();
        Optional<User> ou = userRepository.findByUsername(username);
        if (!ou.isPresent()) return Result.error("用户不存在", 404);
        User u = ou.get();
        // 若未绑定手机号，仍允许开发联调：生成验证码并打印日志，不实际发送

        if (Boolean.TRUE.equals(redis.hasKey(keyLock(username)))) {
            return Result.error("操作过于频繁，请稍后再试", 429);
        }
        if (Boolean.TRUE.equals(redis.hasKey(keySent(username)))) {
            return Result.error("验证码发送过于频繁，请稍后再试", 429);
        }

        String code = String.valueOf(100000 + (int)(Math.random() * 900000));
        redis.opsForValue().set(keyCode(username), code, Duration.ofSeconds(300));
        redis.opsForValue().set(keySent(username), "1", Duration.ofSeconds(60));
        redis.delete(keyErr(username));

        System.out.println("[FORGOT] send code to user=" + username + ", phone=" + (StringUtils.hasText(u.getPhone()) ? maskPhone(u.getPhone()) : "(未绑定)") + ", code=" + code);

        Map<String, Object> resp = new HashMap<>();
        resp.put("sentAt", System.currentTimeMillis());
        resp.put("expireIn", 300);
        resp.put("phoneMasked", StringUtils.hasText(u.getPhone()) ? maskPhone(u.getPhone()) : "****");
        return Result.ok(resp);
    }

    @PostMapping("/reset")
    public Result<Map<String, Object>> reset(@RequestBody ResetReq req) {
        if (req == null || !StringUtils.hasText(req.getUsername())) return Result.error("用户名不能为空", 400);
        if (!StringUtils.hasText(req.getCode())) return Result.error("验证码不能为空", 400);
        if (!StringUtils.hasText(req.getNewPassword()) || req.getNewPassword().length() < 6) {
            return Result.error("新密码至少6位", 400);
        }
        String username = req.getUsername().trim();
        if (Boolean.TRUE.equals(redis.hasKey(keyLock(username)))) {
            return Result.error("操作已被暂时锁定，请稍后再试", 429);
        }
        String cached = redis.opsForValue().get(keyCode(username));
        if (cached == null) {
            return Result.error("验证码已过期或未发送", 400);
        }
        if (!cached.equals(req.getCode().trim())) {
            Long err = redis.opsForValue().increment(keyErr(username));
            if (err != null && err >= 5) {
                redis.opsForValue().set(keyLock(username), "1", 10, TimeUnit.MINUTES);
            }
            redis.expire(keyErr(username), 10, TimeUnit.MINUTES);
            return Result.error("验证码错误", 400);
        }
        Optional<User> ou = userRepository.findByUsername(username);
        if (!ou.isPresent()) return Result.error("用户不存在", 404);
        User u = ou.get();
        u.setPassword(req.getNewPassword());
        userRepository.save(u);
        redis.delete(keyCode(username));
        redis.delete(keyErr(username));
        redis.delete(keyLock(username));

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        return Result.ok(resp);
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return "****";
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    @Data
    public static class SendCodeReq { private String username; }

    @Data
    public static class ResetReq { private String username; private String code; private String newPassword; }
}