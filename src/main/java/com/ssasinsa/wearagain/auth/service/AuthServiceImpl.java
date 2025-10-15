package com.ssasinsa.wearagain.auth.service;

import com.ssasinsa.wearagain.auth.config.GoogleOAuthProperties;
import com.ssasinsa.wearagain.auth.config.JwtProperties;
import com.ssasinsa.wearagain.auth.config.KakaoOAuthProperties;
import com.ssasinsa.wearagain.auth.domain.AuthProvider;
import com.ssasinsa.wearagain.auth.domain.User;
import com.ssasinsa.wearagain.auth.domain.UserOAuthAccount;
import com.ssasinsa.wearagain.auth.domain.repository.UserOAuthAccountRepository;
import com.ssasinsa.wearagain.auth.domain.repository.UserRepository;
import com.ssasinsa.wearagain.auth.dto.request.AppleOAuthLoginRequest;
import com.ssasinsa.wearagain.auth.dto.request.GoogleOAuthLoginRequest;
import com.ssasinsa.wearagain.auth.dto.request.KakaoOAuthLoginRequest;
import com.ssasinsa.wearagain.auth.dto.response.OAuthLoginResponse;
import com.ssasinsa.wearagain.auth.exception.AuthErrorCode;
import com.ssasinsa.wearagain.auth.exception.AuthException;
import com.ssasinsa.wearagain.auth.infrastructure.RefreshTokenRedisKeyManager;
import com.ssasinsa.wearagain.auth.infrastructure.client.AppleOAuthClient;
import com.ssasinsa.wearagain.auth.infrastructure.client.AppleOAuthClient.AppleUserInfo;
import com.ssasinsa.wearagain.auth.infrastructure.client.AppleOAuthTokenResponse;
import com.ssasinsa.wearagain.auth.infrastructure.client.GoogleOAuthClient;
import com.ssasinsa.wearagain.auth.infrastructure.client.GoogleOAuthTokenResponse;
import com.ssasinsa.wearagain.auth.infrastructure.client.GoogleUserInfoResponse;
import com.ssasinsa.wearagain.auth.infrastructure.client.KakaoOAuthClient;
import com.ssasinsa.wearagain.auth.infrastructure.client.KakaoOAuthTokenResponse;
import com.ssasinsa.wearagain.auth.infrastructure.client.KakaoUserInfoResponse;
import com.ssasinsa.wearagain.auth.infrastructure.jwt.JwtToken;
import com.ssasinsa.wearagain.auth.infrastructure.jwt.JwtTokenProvider;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
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
        List<String> scopes = List.of("profile_nickname", "account_email");
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
            return existingAccount.get().getUser();
        }

        Optional<User> existingUser = userRepository.findByEmail(email);
        User user = existingUser.orElseGet(() ->
                userRepository.save(User.create(email, resolveDisplayName(preferredName, email), profileImageUrl))
        );

        UserOAuthAccount account = UserOAuthAccount.create(provider, providerUserId, email, user);
        userOAuthAccountRepository.save(account);
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
