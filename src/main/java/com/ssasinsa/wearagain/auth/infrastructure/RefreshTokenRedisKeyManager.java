package com.ssasinsa.wearagain.auth.infrastructure;

import com.ssasinsa.wearagain.auth.config.AuthRedisProperties;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class RefreshTokenRedisKeyManager {

    private final String refreshTokenPrefix;

    public RefreshTokenRedisKeyManager(AuthRedisProperties properties) {
        this.refreshTokenPrefix = properties.refreshTokenPrefix();
    }

    public String userRefreshTokenKey(UUID userId) {
        Assert.notNull(userId, "userId must not be null");
        return refreshTokenPrefix + ":user:" + userId;
    }

    public String rotationDetectorKey(String refreshTokenId) {
        Assert.hasText(refreshTokenId, "refreshTokenId must not be empty");
        return refreshTokenPrefix + ":rotation:" + refreshTokenId;
    }
}
