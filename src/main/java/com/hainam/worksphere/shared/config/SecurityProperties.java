package com.hainam.worksphere.shared.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.security")
@Data
public class SecurityProperties {

    private Rbac rbac = new Rbac();

    @Data
    public static class Rbac {
        private boolean enabled = true;
    }
}
