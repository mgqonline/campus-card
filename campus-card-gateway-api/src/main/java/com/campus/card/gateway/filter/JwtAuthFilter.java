package com.campus.card.gateway.filter;

import com.campus.card.gateway.config.GatewayAuthProperties;
import com.campus.card.gateway.security.JwtUtil;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JwtAuthFilter extends OncePerRequestFilter {
    private final GatewayAuthProperties props;
    private final JwtUtil jwtUtil;
    private final AntPathMatcher matcher = new AntPathMatcher();

    // 简单每IP滑动窗口限流（60 req/min）
    private final Map<String, Window> rate = new ConcurrentHashMap<>();
    private final int limitPerMinute = 60;

    public JwtAuthFilter(GatewayAuthProperties props) {
        this.props = props;
        this.jwtUtil = new JwtUtil(props);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // 放行认证与文档、健康检查等特殊路径
        return matcher.match("/api/v1/gw/auth/**", path)
                || matcher.match("/swagger-ui/**", path)
                || matcher.match("/v3/api-docs/**", path)
                || matcher.match("/actuator/**", path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (!props.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        // 速率限制
        String ip = request.getRemoteAddr();
        if (!allow(ip)) {
            writeJson(response, HttpStatus.TOO_MANY_REQUESTS.value(), "Too Many Requests");
            return;
        }

        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            writeJson(response, HttpStatus.UNAUTHORIZED.value(), "Unauthorized");
            return;
        }
        String token = auth.substring("Bearer ".length());
        try {
            jwtUtil.parseToken(token); // 验证签名与issuer/过期
        } catch (JwtException e) {
            writeJson(response, HttpStatus.UNAUTHORIZED.value(), "Invalid token");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean allow(String ip) {
        long now = Instant.now().getEpochSecond();
        Window w = rate.computeIfAbsent(ip, k -> new Window(now, 0));
        if (now - w.second >= 60) {
            w.second = now; w.count = 0;
        }
        w.count++;
        return w.count <= limitPerMinute;
    }

    private void writeJson(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        byte[] bytes = ("{\"code\":" + status + ",\"message\":\"" + message + "\"}").getBytes(StandardCharsets.UTF_8);
        response.getOutputStream().write(bytes);
    }

    private static class Window { long second; int count; Window(long s,int c){this.second=s;this.count=c;} }
}