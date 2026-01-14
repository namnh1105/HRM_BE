package com.hainam.worksphere.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtProperties {

    private String secret = "mySecretKey123456789012345678901234567890123456789012345678901234567890";
    private long accessTokenExpiration = 900000; // 15 minutes
    private long refreshTokenExpiration = 604800000; // 7 days
}
