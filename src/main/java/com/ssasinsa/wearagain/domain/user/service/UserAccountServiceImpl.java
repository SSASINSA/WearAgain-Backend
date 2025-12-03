package com.ssasinsa.wearagain.domain.user.service;

import com.ssasinsa.wearagain.domain.auth.entity.User;
import com.ssasinsa.wearagain.domain.auth.repository.UserOAuthAccountRepository;
import com.ssasinsa.wearagain.domain.auth.repository.UserRepository;
import com.ssasinsa.wearagain.domain.auth.infrastructure.RefreshTokenRedisKeyManager;
import com.ssasinsa.wearagain.domain.user.exception.UserErrorCode;
import com.ssasinsa.wearagain.domain.user.exception.UserException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserAccountServiceImpl implements UserAccountService {

    private static final String WITHDRAWN_DISPLAY_NAME = "탈퇴한 사용자";
    private static final String ANONYMIZED_EMAIL_TEMPLATE = "withdrawn_%d_%s@wearagain.local";

    private final UserRepository userRepository;
    private final UserOAuthAccountRepository userOAuthAccountRepository;
    private final RefreshTokenRedisKeyManager refreshTokenRedisKeyManager;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void withdraw(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
        if (user.isDeleted()) {
            throw new UserException(UserErrorCode.USER_ALREADY_WITHDRAWN);
        }

        userOAuthAccountRepository.deleteAllByUser(user);
        user.withdraw(buildAnonymizedEmail(user), WITHDRAWN_DISPLAY_NAME);
        clearRefreshToken(user.getId());
    }

    private void clearRefreshToken(Long userId) {
        redisTemplate.delete(refreshTokenRedisKeyManager.userRefreshTokenKey(userId));
    }

    private String buildAnonymizedEmail(User user) {
        String randomSuffix = UUID.randomUUID().toString().replace("-", "");
        return String.format(ANONYMIZED_EMAIL_TEMPLATE, user.getId(), randomSuffix);
    }
}
