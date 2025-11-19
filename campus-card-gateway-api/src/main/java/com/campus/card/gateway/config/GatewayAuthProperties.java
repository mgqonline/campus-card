package com.campus.card.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "gateway.auth")
public class GatewayAuthProperties {
    private String secret = "change-me-secret-please-change-32-bytes-minimum!";
    private String issuer = "campus-card-gw";
    private int expireMinutes = 60;
    private int refreshExpireMinutes = 7 * 24 * 60;
    private boolean enabled = true;

    // Swagger访问控制
    private boolean swaggerEnabled = true;
    private boolean docAllowLocalOnly = true;
    private String docAccessSecret = "doc-secret";

    // CORS
    private List<String> corsAllowedOrigins = new ArrayList<>();

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }
    public int getExpireMinutes() { return expireMinutes; }
    public void setExpireMinutes(int expireMinutes) { this.expireMinutes = expireMinutes; }
    public int getRefreshExpireMinutes() { return refreshExpireMinutes; }
    public void setRefreshExpireMinutes(int refreshExpireMinutes) { this.refreshExpireMinutes = refreshExpireMinutes; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public boolean isSwaggerEnabled() { return swaggerEnabled; }
    public void setSwaggerEnabled(boolean swaggerEnabled) { this.swaggerEnabled = swaggerEnabled; }
    public boolean isDocAllowLocalOnly() { return docAllowLocalOnly; }
    public void setDocAllowLocalOnly(boolean docAllowLocalOnly) { this.docAllowLocalOnly = docAllowLocalOnly; }
    public String getDocAccessSecret() { return docAccessSecret; }
    public void setDocAccessSecret(String docAccessSecret) { this.docAccessSecret = docAccessSecret; }
    public List<String> getCorsAllowedOrigins() { return corsAllowedOrigins; }
    public void setCorsAllowedOrigins(List<String> corsAllowedOrigins) { this.corsAllowedOrigins = corsAllowedOrigins; }
}