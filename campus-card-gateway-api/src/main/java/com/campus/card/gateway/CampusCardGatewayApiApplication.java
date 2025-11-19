package com.campus.card.gateway;

import com.campus.card.gateway.config.GatewayAuthProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(GatewayAuthProperties.class)
public class CampusCardGatewayApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(CampusCardGatewayApiApplication.class, args);
    }
}