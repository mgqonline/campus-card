package com.campus.card.gateway.controller;

import com.campus.card.common.result.Result;
import com.campus.card.gateway.config.GatewayAuthProperties;
import com.campus.card.gateway.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "认证授权", description = "登录认证、Token刷新、权限验证、SSO")
@RestController
@RequestMapping("/api/v1/gw/auth")
@Validated
public class AuthController {

    private final JwtUtil jwtUtil;
    private final GatewayAuthProperties props;

    public AuthController(GatewayAuthProperties props) {
        this.props = props;
        this.jwtUtil = new JwtUtil(props);
    }

    @Operation(summary = "登录认证")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody @Validated LoginRequest request) {
        // 这里应接入真实用户体系；演示按grantType进行校验
        Map<String, Object> claims = new HashMap<>();
        claims.put("grant", request.getGrantType());
        claims.put("scope", "basic");
        String token = jwtUtil.generateToken(resolveSubject(request), claims, props.getExpireMinutes());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("expireIn", props.getExpireMinutes() * 60);
        return Result.ok(data);
    }

    @Operation(summary = "刷新Token")
    @PostMapping("/refresh")
    public Result<Map<String, Object>> refresh(@RequestBody RefreshTokenRequest request) {
        // 演示：实际应验证refreshToken；此处仅重新签发
        Map<String, Object> claims = new HashMap<>();
        claims.put("grant", "refresh");
        claims.put("scope", "basic");
        String token = jwtUtil.generateToken("refresh-subject", claims, props.getExpireMinutes());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("expireIn", props.getExpireMinutes() * 60);
        return Result.ok(data);
    }

    @Operation(summary = "权限验证")
    @GetMapping("/permission/check")
    public Result<Boolean> checkPerm(@RequestParam("perm") String perm) {
        // 演示：权限由后续与用户体系对接；此处返回true
        return Result.ok(Boolean.TRUE);
    }

    @Operation(summary = "单点登录SSO")
    @PostMapping("/sso")
    public Result<Map<String, Object>> sso(@RequestBody SsoRequest request) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("grant", "sso");
        claims.put("provider", request.getProvider());
        String token = jwtUtil.generateToken("sso:" + request.getTicket(), claims, props.getExpireMinutes());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("expireIn", props.getExpireMinutes() * 60);
        return Result.ok(data);
    }

    private String resolveSubject(LoginRequest req) {
        if ("password".equalsIgnoreCase(req.getGrantType())) {
            return req.getAccount() != null ? req.getAccount() : "account";
        }
        if ("sms".equalsIgnoreCase(req.getGrantType())) {
            return req.getPhone() != null ? req.getPhone() : "phone";
        }
        return "guest";
    }

    @Data
    public static class LoginRequest {
        /** password/sms/other */
        @NotBlank(message = "grantType不能为空")
        private String grantType;
        private String account;
        private String password;
        private String phone;
        private String code;
    }

    @Data
    public static class RefreshTokenRequest {
        @NotBlank(message = "refreshToken不能为空")
        private String refreshToken;
    }

    @Data
    public static class SsoRequest {
        @NotBlank(message = "ticket不能为空")
        private String ticket;
        private String provider; // CAS/Keycloak/WeCom 等
    }
}