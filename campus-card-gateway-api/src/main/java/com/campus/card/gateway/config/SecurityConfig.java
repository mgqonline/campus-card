package com.campus.card.gateway.config;

import com.campus.card.gateway.filter.JwtAuthFilter;
import com.campus.card.gateway.filter.SwaggerAccessFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityConfig {
    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtFilterRegistration(GatewayAuthProperties props) {
        FilterRegistrationBean<JwtAuthFilter> reg = new FilterRegistrationBean<>();
        reg.setFilter(new JwtAuthFilter(props));
        reg.addUrlPatterns("/api/v1/gw/*");
        reg.setOrder(1);
        return reg;
    }

    @Bean
    public FilterRegistrationBean<SwaggerAccessFilter> swaggerFilterRegistration(GatewayAuthProperties props) {
        FilterRegistrationBean<SwaggerAccessFilter> reg = new FilterRegistrationBean<>();
        reg.setFilter(new SwaggerAccessFilter(props));
        reg.addUrlPatterns("/swagger-ui/*", "/v3/api-docs/*");
        reg.setOrder(2);
        return reg;
    }
}