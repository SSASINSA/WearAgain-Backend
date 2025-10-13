package com.ssasinsa.wearagain.auth.service;

import com.ssasinsa.wearagain.auth.config.GoogleOAuthProperties;
import com.ssasinsa.wearagain.auth.config.JwtProperties;
import com.ssasinsa.wearagain.auth.domain.AuthProvider;
import com.ssasinsa.wearagain.auth.domain.User;
import com.ssasinsa.wearagain.auth.domain.UserOAuthAccount;
import com.ssasinsa.wearagain.auth.domain.repository.UserOAuthAccountRepository;
import com.ssasinsa.wearagain.auth.domain.repository.UserRepository;
import com.ssasinsa.wearagain.auth.dto.request.GoogleOAuthLoginRequest;
import com.ssasinsa.wearagain.auth.dto.response.OAuthLoginResponse;
import com.ssasinsa.wearagain.auth.exception.AuthErrorCode;
import com.ssasinsa.wearagain.auth.exception.AuthException;
import com.ssasinsa.wearagain.auth.infrastructure.RefreshTokenRedisKeyManager;
import com.ssasinsa.wearagain.auth.infrastructure.client.GoogleOAuthClient;
import com.ssasinsa.wearagain.auth.infrastructure.client.GoogleOAuthTokenResponse;
import com.ssasinsa.wearagain.auth.infrastructure.client.GoogleUserInfoResponse;
import com.ssasinsa.wearagain.auth.infrastructure.jwt.JwtToken;
import com.ssasinsa.wearagain.auth.infrastructure.jwt.JwtTokenProvider;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final GoogleOAuthClient googleOAuthClient;
    private final UserRepository userRepository;
    private final UserOAuthAccountRepository userOAuthAccountRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRedisKeyManager refreshTokenRedisKeyManager;
    private final RedisTemplate<String, String> redisTemplate;
    private final JwtProperties jwtProperties;
    private final GoogleOAuthProperties googleOAuthProperties;

    @Override
    @Transactional
    public OAuthLoginResponse loginWithGoogle(GoogleOAuthLoginRequest request) {
        if (!StringUtils.hasText(request.authorizationCode())) {
            throw new AuthException(AuthErrorCode.GOOGLE_AUTHORIZATION_CODE_REQUIRED);
        }

        GoogleOAuthTokenResponse tokenResponse = googleOAuthClient.requestToken(request.authorizationCode());
        if (!StringUtils.hasText(tokenResponse.accessToken())) {
            throw new AuthException(AuthErrorCode.GOOGLE_TOKEN_REQUEST_FAILED);
        }

        GoogleUserInfoResponse userInfo = googleOAuthClient.fetchUserInfo(tokenResponse.accessToken());

        if (!StringUtils.hasText(userInfo.id()) || !StringUtils.hasText(userInfo.email())) {
            throw new AuthException(AuthErrorCode.GOOGLE_USERINFO_REQUEST_FAILED);
        }

        User user = findOrCreateGoogleUser(userInfo);

        JwtToken accessToken = jwtTokenProvider.createAccessToken(user);
        JwtToken refreshToken = jwtTokenProvider.createRefreshToken(user);

        storeRefreshToken(user.getId(), refreshToken);

        return OAuthLoginResponse.of(user, accessToken, refreshToken);
    }

    @Override
    public String generateGoogleAuthorizationUrl() {
        List<String> scopes = List.of("openid", "email", "profile");
        String scopePreString = "https://www.googleapis.com/auth/userinfo.";
        return UriComponentsBuilder
                .fromUriString("https://accounts.google.com/o/oauth2/v2/auth")
                .queryParam("client_id", googleOAuthProperties.clientId())
                .queryParam("redirect_uri", googleOAuthProperties.redirectUri())
                .queryParam("response_type", "code")
                .queryParam("scope",  URLEncoder.encode(String.join(" ", scopes)))
                .queryParam("access_type", "offline")
                .queryParam("prompt", "consent")
                .build(true)
                .toUriString();
    }

    private User findOrCreateGoogleUser(GoogleUserInfoResponse userInfo) {
        Optional<UserOAuthAccount> existingAccount = userOAuthAccountRepository.findByProviderAndProviderUserId(
                AuthProvider.GOOGLE,
                userInfo.id()
        );
        if (existingAccount.isPresent()) {
            return existingAccount.get().getUser();
        }

        Optional<User> existingUser = userRepository.findByEmail(userInfo.email());
        User user = existingUser.orElseGet(() ->
                userRepository.save(User.create(userInfo.email(), resolveDisplayName(userInfo), userInfo.pictureUrl()))
        );

        UserOAuthAccount account = UserOAuthAccount.create(AuthProvider.GOOGLE, userInfo.id(), userInfo.email(), user);
        userOAuthAccountRepository.save(account);
        return user;
    }

    private String resolveDisplayName(GoogleUserInfoResponse userInfo) {
        if (StringUtils.hasText(userInfo.name())) {
            return userInfo.name();
        }
        int atIndex = userInfo.email().indexOf('@');
        return atIndex > 0 ? userInfo.email().substring(0, atIndex) : userInfo.email();
    }

    private void storeRefreshToken(UUID userId, JwtToken refreshToken) {
        Duration validity = Duration.ofMillis(jwtProperties.refreshToken().validity());
        String userKey = refreshTokenRedisKeyManager.userRefreshTokenKey(userId);
        redisTemplate.opsForValue().set(userKey, refreshToken.value(), validity);

        if (refreshToken.tokenId() != null) {
            String rotationKey = refreshTokenRedisKeyManager.rotationDetectorKey(refreshToken.tokenId().toString());
            redisTemplate.opsForValue().set(rotationKey, userId.toString(), validity);
        }
    }
}
