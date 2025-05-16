package com.autohub.user_service.infrastructure.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "application.jwt")
public class JwtProperties {
    private String secretKey;
    private long tokenValidityInSeconds;
    private String tokenPrefix = "Bearer ";
    private String headerName = "Authorization";
    private String redirectUri = "http://localhost:3000/oauth2/redirect";

    /**
     * Interval in milliseconds for cleaning up expired tokens from the cache.
     * Default is 1 hour (3600000 ms).
     */
    private long cacheCleanupInterval = 3600000;

}
