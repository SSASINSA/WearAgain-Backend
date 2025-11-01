package com.ssasinsa.wearagain.domain.auth.infrastructure;

import com.ssasinsa.wearagain.domain.auth.config.AuthRedisProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class AdminRefreshTokenKeyManager {

    private final String refreshTokenPrefix;

    public AdminRefreshTokenKeyManager(AuthRedisProperties properties) {
        this.refreshTokenPrefix = properties.refreshTokenPrefix();
    }

    public String adminRefreshTokenKey(Long adminId) {
        Assert.notNull(adminId, "adminId must not be null");
        return refreshTokenPrefix + ":admin:" + adminId;
    }

    public String rotationDetectorKey(String tokenId) {
        Assert.hasText(tokenId, "tokenId must not be empty");
        return refreshTokenPrefix + ":admin:rotation:" + tokenId;
    }
}
