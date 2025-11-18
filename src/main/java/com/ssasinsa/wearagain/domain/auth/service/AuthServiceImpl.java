package com.ssasinsa.wearagain.domain.auth.service;

import com.ssasinsa.wearagain.domain.auth.config.GoogleOAuthProperties;
import com.ssasinsa.wearagain.domain.auth.config.JwtProperties;
import com.ssasinsa.wearagain.domain.auth.config.KakaoOAuthProperties;
import com.ssasinsa.wearagain.domain.auth.entity.AuthProvider;
import com.ssasinsa.wearagain.domain.auth.entity.User;
import com.ssasinsa.wearagain.domain.auth.entity.UserOAuthAccount;
import com.ssasinsa.wearagain.domain.auth.repository.UserOAuthAccountRepository;
import com.ssasinsa.wearagain.domain.auth.dto.request.AppleOAuthLoginRequest;
import com.ssasinsa.wearagain.domain.auth.dto.request.GoogleOAuthLoginRequest;
import com.ssasinsa.wearagain.domain.auth.dto.request.KakaoIdTokenLoginRequest;
import com.ssasinsa.wearagain.domain.auth.dto.request.KakaoOAuthLoginRequest;
import com.ssasinsa.wearagain.domain.auth.dto.request.TokenRefreshRequest;
import com.ssasinsa.wearagain.domain.auth.dto.response.OAuthLoginResponse;
import com.ssasinsa.wearagain.domain.auth.dto.response.TokenRefreshResponse;
import com.ssasinsa.wearagain.domain.auth.exception.AuthErrorCode;
import com.ssasinsa.wearagain.domain.auth.exception.AuthException;
import com.ssasinsa.wearagain.domain.auth.infrastructure.RefreshTokenRedisKeyManager;
import com.ssasinsa.wearagain.domain.auth.infrastructure.client.AppleOAuthClient;
import com.ssasinsa.wearagain.domain.auth.infrastructure.client.AppleOAuthClient.AppleUserInfo;
import com.ssasinsa.wearagain.domain.auth.infrastructure.client.AppleOAuthTokenResponse;
import com.ssasinsa.wearagain.domain.auth.infrastructure.client.GoogleOAuthClient;
import com.ssasinsa.wearagain.domain.auth.infrastructure.client.GoogleOAuthTokenResponse;
import com.ssasinsa.wearagain.domain.auth.infrastructure.client.GoogleUserInfoResponse;
import com.ssasinsa.wearagain.domain.auth.infrastructure.client.KakaoOAuthClient;
import com.ssasinsa.wearagain.domain.auth.infrastructure.client.KakaoOAuthTokenResponse;
import com.ssasinsa.wearagain.domain.auth.infrastructure.client.KakaoUserInfoResponse;
import com.ssasinsa.wearagain.domain.auth.infrastructure.jwt.JwtToken;
import com.ssasinsa.wearagain.domain.auth.infrastructure.jwt.JwtTokenProvider;
import com.ssasinsa.wearagain.domain.auth.infrastructure.jwt.JwtTokenProvider.RefreshTokenClaims;
import com.ssasinsa.wearagain.domain.auth.repository.UserRepository;
import com.ssasinsa.wearagain.domain.growth.service.GrowthInitializer;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import io.jsonwebtoken.JwtException;
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
    private final KakaoOAuthClient kakaoOAuthClient;
    private final AppleOAuthClient appleOAuthClient;
    private final UserRepository userRepository;
    private final UserOAuthAccountRepository userOAuthAccountRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRedisKeyManager refreshTokenRedisKeyManager;
    private final RedisTemplate<String, String> redisTemplate;
    private final JwtProperties jwtProperties;
    private final GoogleOAuthProperties googleOAuthProperties;
    private final KakaoOAuthProperties kakaoOAuthProperties;
    private final GrowthInitializer growthInitializer;

    @Override
    @Transactional
    public OAuthLoginResponse loginWithGoogle(GoogleOAuthLoginRequest request) {
        if (!StringUtils.hasText(request.authorizationCode())) {
            throw new AuthException(AuthErrorCode.AUTHORIZATION_CODE_REQUIRED);
        }

        GoogleOAuthTokenResponse tokenResponse = googleOAuthClient.requestToken(request.authorizationCode());
        if (!StringUtils.hasText(tokenResponse.accessToken())) {
            throw new AuthException(AuthErrorCode.GOOGLE_TOKEN_REQUEST_FAILED);
        }

        GoogleUserInfoResponse userInfo = googleOAuthClient.fetchUserInfo(tokenResponse.accessToken());

        if (!StringUtils.hasText(userInfo.id()) || !StringUtils.hasText(userInfo.email())) {
            throw new AuthException(AuthErrorCode.GOOGLE_USERINFO_REQUEST_FAILED);
        }

        User user = findOrCreateOAuthUser(
                AuthProvider.GOOGLE,
                userInfo.id(),
                userInfo.email(),
                userInfo.name(),
                userInfo.pictureUrl()
        );

        return issueTokens(user);
    }

    @Override
    @Transactional
    public OAuthLoginResponse loginWithKakao(KakaoOAuthLoginRequest request) {
        if (!StringUtils.hasText(request.authorizationCode())) {
            throw new AuthException(AuthErrorCode.AUTHORIZATION_CODE_REQUIRED);
        }

        KakaoOAuthTokenResponse tokenResponse = kakaoOAuthClient.requestToken(request.authorizationCode());
        if (!StringUtils.hasText(tokenResponse.accessToken())) {
            throw new AuthException(AuthErrorCode.KAKAO_TOKEN_REQUEST_FAILED);
        }

        KakaoUserInfoResponse userInfo = kakaoOAuthClient.fetchUserInfo(tokenResponse.accessToken());
        if (userInfo.id() == null) {
            throw new AuthException(AuthErrorCode.KAKAO_USERINFO_REQUEST_FAILED);
        }
        if (!userInfo.hasEmail() || !StringUtils.hasText(userInfo.email())) {
            throw new AuthException(AuthErrorCode.KAKAO_EMAIL_NOT_PROVIDED);
        }

        User user = findOrCreateOAuthUser(
                AuthProvider.KAKAO,
                userInfo.id().toString(),
                userInfo.email(),
                userInfo.nickname(),
                userInfo.profileImageUrl()
        );

        return issueTokens(user);
    }

    @Override
    @Transactional
    public OAuthLoginResponse loginWithKakaoIdToken(KakaoIdTokenLoginRequest request) {
        if (!StringUtils.hasText(request.idToken())) {
            throw new AuthException(AuthErrorCode.KAKAO_USERINFO_REQUEST_FAILED);
        }
        KakaoOAuthClient.KakaoIdTokenPayload payload = kakaoOAuthClient.parseIdToken(request.idToken());
        if (!StringUtils.hasText(payload.email())) {
            throw new AuthException(AuthErrorCode.KAKAO_EMAIL_NOT_PROVIDED);
        }

        User user = findOrCreateOAuthUser(
                AuthProvider.KAKAO,
                payload.providerUserId(),
                payload.email(),
                payload.nickname(),
                payload.profileImageUrl()
        );

        return issueTokens(user);
    }

    @Override
    @Transactional
    public OAuthLoginResponse loginWithApple(AppleOAuthLoginRequest request) {
        if (!StringUtils.hasText(request.code()) || !StringUtils.hasText(request.idToken())) {
            throw new AuthException(AuthErrorCode.AUTHORIZATION_CODE_REQUIRED);
        }

        AppleUserInfo requestUserInfo = appleOAuthClient.parseIdToken(request.idToken());
        if (!StringUtils.hasText(requestUserInfo.email())) {
            throw new AuthException(AuthErrorCode.APPLE_USERINFO_REQUEST_FAILED);
        }

        AppleOAuthTokenResponse tokenResponse = appleOAuthClient.requestToken(request.code());
        if (!StringUtils.hasText(tokenResponse.idToken())) {
            throw new AuthException(AuthErrorCode.APPLE_TOKEN_REQUEST_FAILED);
        }

        AppleUserInfo appleUserInfo = appleOAuthClient.parseIdToken(tokenResponse.idToken());
        if (!Objects.equals(requestUserInfo.providerUserId(), appleUserInfo.providerUserId())) {
            throw new AuthException(AuthErrorCode.APPLE_USERINFO_REQUEST_FAILED);
        }
        if (!StringUtils.hasText(appleUserInfo.email())) {
            throw new AuthException(AuthErrorCode.APPLE_USERINFO_REQUEST_FAILED);
        }

        User user = findOrCreateOAuthUser(
                AuthProvider.APPLE,
                appleUserInfo.providerUserId(),
                appleUserInfo.email(),
                null,
                null
        );

        return issueTokens(user);
    }

    @Override
    @Transactional
    public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {
        String refreshTokenValue = request.refreshToken();
        if (!StringUtils.hasText(refreshTokenValue)) {
            throw new AuthException(AuthErrorCode.REFRESH_TOKEN_INVALID);
        }

        RefreshTokenClaims claims;
        try {
            claims = jwtTokenProvider.parseRefreshToken(refreshTokenValue);
        } catch (JwtException | IllegalArgumentException exception) {
            throw new AuthException(AuthErrorCode.REFRESH_TOKEN_INVALID, exception);
        }

        Long userId = claims.userId();
        UUID tokenId = claims.tokenId();

        String userKey = refreshTokenRedisKeyManager.userRefreshTokenKey(userId);
        String storedToken = redisTemplate.opsForValue().get(userKey);
        if (!StringUtils.hasText(storedToken) || !storedToken.equals(refreshTokenValue)) {
            throw new AuthException(AuthErrorCode.REFRESH_TOKEN_INVALID);
        }

        String rotationKey = refreshTokenRedisKeyManager.rotationDetectorKey(tokenId.toString());
        Boolean deleted = redisTemplate.delete(rotationKey);
        if (!Boolean.TRUE.equals(deleted)) {
            throw new AuthException(AuthErrorCode.REFRESH_TOKEN_REUSED);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.REFRESH_TOKEN_INVALID));

        JwtToken newAccessToken = jwtTokenProvider.createAccessToken(user);
        JwtToken newRefreshToken = jwtTokenProvider.createRefreshToken(user);
        storeRefreshToken(user.getId(), newRefreshToken);

        return TokenRefreshResponse.of(newAccessToken, newRefreshToken);
    }

    @Override
    public String generateGoogleAuthorizationUrl() {
        List<String> scopes = List.of("openid", "email", "profile");
        String encodedScope = URLEncoder.encode(String.join(" ", scopes), StandardCharsets.UTF_8);
        return UriComponentsBuilder
                .fromUriString("https://accounts.google.com/o/oauth2/v2/auth")
                .queryParam("client_id", googleOAuthProperties.clientId())
                .queryParam("redirect_uri", googleOAuthProperties.redirectUri())
                .queryParam("response_type", "code")
                .queryParam("scope", encodedScope)
                .queryParam("access_type", "offline")
                .queryParam("prompt", "consent")
                .build(true)
                .toUriString();
    }

    @Override
    public String generateKakaoAuthorizationUrl() {
        List<String> scopes = List.of("openid", "profile_nickname", "account_email");
        String encodedScope = URLEncoder.encode(String.join(" ", scopes), StandardCharsets.UTF_8);
        return UriComponentsBuilder
                .fromUriString("https://kauth.kakao.com/oauth/authorize")
                .queryParam("client_id", kakaoOAuthProperties.clientId())
                .queryParam("redirect_uri", kakaoOAuthProperties.redirectUri())
                .queryParam("response_type", "code")
                .queryParam("scope", encodedScope)
                .queryParam("prompt", "select_account")
                .build(true)
                .toUriString();
    }

    private User findOrCreateOAuthUser(
            AuthProvider provider,
            String providerUserId,
            String email,
            String preferredName,
            String profileImageUrl
    ) {
        Optional<UserOAuthAccount> existingAccount = userOAuthAccountRepository.findByProviderAndProviderUserId(
                provider,
                providerUserId
        );
        if (existingAccount.isPresent()) {
            User user = existingAccount.get().getUser();
            growthInitializer.initialize(user);
            return user;
        }

        Optional<User> existingUser = userRepository.findByEmail(email);
        User user = existingUser.orElseGet(() ->
                userRepository.save(User.create(email, resolveDisplayName(preferredName, email), profileImageUrl))
        );

        UserOAuthAccount account = UserOAuthAccount.create(provider, providerUserId, email, user);
        userOAuthAccountRepository.save(account);
        growthInitializer.initialize(user);
        return user;
    }

    private String resolveDisplayName(String preferredName, String email) {
        if (StringUtils.hasText(preferredName)) {
            return preferredName;
        }
        int atIndex = email.indexOf('@');
        return atIndex > 0 ? email.substring(0, atIndex) : email;
    }

    private OAuthLoginResponse issueTokens(User user) {
        JwtToken accessToken = jwtTokenProvider.createAccessToken(user);
        JwtToken refreshToken = jwtTokenProvider.createRefreshToken(user);

        storeRefreshToken(user.getId(), refreshToken);

        return OAuthLoginResponse.of(user, accessToken, refreshToken);
    }

    private void storeRefreshToken(Long userId, JwtToken refreshToken) {
        Duration validity = Duration.ofMillis(jwtProperties.refreshToken().validity());
        String userKey = refreshTokenRedisKeyManager.userRefreshTokenKey(userId);
        redisTemplate.opsForValue().set(userKey, refreshToken.value(), validity);

        if (refreshToken.tokenId() != null) {
            String rotationKey = refreshTokenRedisKeyManager.rotationDetectorKey(refreshToken.tokenId().toString());
            redisTemplate.opsForValue().set(rotationKey, userId.toString(), validity);
        }
    }
}
