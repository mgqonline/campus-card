package com.campus.card.gateway.filter;

import com.campus.card.gateway.config.GatewayAuthProperties;
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

public class SwaggerAccessFilter extends OncePerRequestFilter {
    private final GatewayAuthProperties props;
    private final AntPathMatcher matcher = new AntPathMatcher();

    public SwaggerAccessFilter(GatewayAuthProperties props) {
        this.props = props;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !(matcher.match("/swagger-ui/**", path) || matcher.match("/v3/api-docs/**", path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (!props.isSwaggerEnabled()) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            response.setContentType(MediaType.TEXT_PLAIN_VALUE);
            response.getOutputStream().write("Not Found".getBytes(StandardCharsets.UTF_8));
            return;
        }

        String remote = request.getRemoteAddr();
        boolean isLocal = "127.0.0.1".equals(remote) || "0:0:0:0:0:0:0:1".equals(remote);
        if (props.isDocAllowLocalOnly() && !isLocal) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.TEXT_PLAIN_VALUE);
            response.getOutputStream().write("Forbidden".getBytes(StandardCharsets.UTF_8));
            return;
        }

        String header = request.getHeader("X-Doc-Access");
        if (!props.isDocAllowLocalOnly()) {
            if (header == null || !header.equals(props.getDocAccessSecret())) {
                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.setContentType(MediaType.TEXT_PLAIN_VALUE);
                response.getOutputStream().write("Forbidden".getBytes(StandardCharsets.UTF_8));
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}