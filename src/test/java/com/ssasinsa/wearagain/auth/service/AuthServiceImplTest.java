package com.ssasinsa.wearagain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ssasinsa.wearagain.auth.config.GoogleOAuthProperties;
import com.ssasinsa.wearagain.auth.config.JwtProperties;
import com.ssasinsa.wearagain.auth.config.JwtProperties.TokenProperties;
import com.ssasinsa.wearagain.auth.config.KakaoOAuthProperties;
import com.ssasinsa.wearagain.auth.domain.AuthProvider;
import com.ssasinsa.wearagain.auth.domain.User;
import com.ssasinsa.wearagain.auth.domain.UserOAuthAccount;
import com.ssasinsa.wearagain.auth.domain.repository.UserOAuthAccountRepository;
import com.ssasinsa.wearagain.auth.domain.repository.UserRepository;
import com.ssasinsa.wearagain.auth.dto.request.AppleOAuthLoginRequest;
import com.ssasinsa.wearagain.auth.dto.request.KakaoIdTokenLoginRequest;
import com.ssasinsa.wearagain.auth.dto.request.TokenRefreshRequest;
import com.ssasinsa.wearagain.auth.dto.response.OAuthLoginResponse;
import com.ssasinsa.wearagain.auth.dto.response.TokenRefreshResponse;
import com.ssasinsa.wearagain.auth.exception.AuthErrorCode;
import com.ssasinsa.wearagain.auth.exception.AuthException;
import com.ssasinsa.wearagain.auth.infrastructure.RefreshTokenRedisKeyManager;
import com.ssasinsa.wearagain.auth.infrastructure.client.AppleOAuthClient;
import com.ssasinsa.wearagain.auth.infrastructure.client.AppleOAuthClient.AppleUserInfo;
import com.ssasinsa.wearagain.auth.infrastructure.client.AppleOAuthTokenResponse;
import com.ssasinsa.wearagain.auth.infrastructure.client.GoogleOAuthClient;
import com.ssasinsa.wearagain.auth.infrastructure.client.KakaoOAuthClient;
import com.ssasinsa.wearagain.auth.infrastructure.client.KakaoOAuthClient.KakaoIdTokenPayload;
import com.ssasinsa.wearagain.auth.infrastructure.jwt.JwtToken;
import com.ssasinsa.wearagain.auth.infrastructure.jwt.JwtTokenProvider;
import com.ssasinsa.wearagain.auth.infrastructure.jwt.JwtTokenProvider.RefreshTokenClaims;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import io.jsonwebtoken.JwtException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceImplTest {

    @Mock
    private GoogleOAuthClient googleOAuthClient;
    @Mock
    private KakaoOAuthClient kakaoOAuthClient;
    @Mock
    private AppleOAuthClient appleOAuthClient;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserOAuthAccountRepository userOAuthAccountRepository;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private RefreshTokenRedisKeyManager refreshTokenRedisKeyManager;
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    private AuthServiceImpl authService;

    private JwtProperties jwtProperties;
    private GoogleOAuthProperties googleOAuthProperties;
    private KakaoOAuthProperties kakaoOAuthProperties;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties(
                "wearagain",
                new TokenProperties("access-secret-key-should-be-long-enough", 900000L),
                new TokenProperties("refresh-secret-key-should-be-long-enough", 1209600000L)
        );
        googleOAuthProperties = new GoogleOAuthProperties("google-client", "google-secret", "google-redirect", "google-token", "google-userinfo");
        kakaoOAuthProperties = new KakaoOAuthProperties(
                "kakao-client",
                "kakao-secret",
                "kakao-app-client",
                "kakao-redirect",
                "kakao-token",
                "kakao-userinfo",
                "https://kauth.kakao.com",
                "https://kauth.kakao.com/.well-known/jwks.json"
        );

        authService = new AuthServiceImpl(
                googleOAuthClient,
                kakaoOAuthClient,
                appleOAuthClient,
                userRepository,
                userOAuthAccountRepository,
                jwtTokenProvider,
                refreshTokenRedisKeyManager,
                redisTemplate,
                jwtProperties,
                googleOAuthProperties,
                kakaoOAuthProperties
        );

        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void should_login_with_kakao_when_id_token_valid() {
        KakaoIdTokenLoginRequest request = new KakaoIdTokenLoginRequest("kakao-id-token");
        Instant issuedAt = Instant.now().minusSeconds(30);
        Instant expiresAt = Instant.now().plusSeconds(900);
        Instant authTime = issuedAt;
        KakaoIdTokenPayload payload = new KakaoIdTokenPayload(
                "kakao-user-id",
                "user@example.com",
                "카카오 사용자",
                "https://example.com/profile.png",
                issuedAt,
                expiresAt,
                authTime,
                "nonce-value"
        );

        when(kakaoOAuthClient.parseIdToken("kakao-id-token")).thenReturn(payload);

        UUID userId = UUID.randomUUID();
        User user = org.mockito.Mockito.mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(user.getEmail()).thenReturn("user@example.com");
        when(user.getDisplayName()).thenReturn("카카오 사용자");

        UserOAuthAccount oauthAccount = org.mockito.Mockito.mock(UserOAuthAccount.class);
        when(oauthAccount.getUser()).thenReturn(user);
        when(userOAuthAccountRepository.findByProviderAndProviderUserId(AuthProvider.KAKAO, "kakao-user-id"))
                .thenReturn(Optional.of(oauthAccount));

        UUID refreshTokenId = UUID.randomUUID();
        JwtToken accessToken = new JwtToken("access-token", Instant.now().plusSeconds(900), null);
        JwtToken refreshToken = new JwtToken("refresh-token", Instant.now().plusSeconds(7200), refreshTokenId);
        when(jwtTokenProvider.createAccessToken(user)).thenReturn(accessToken);
        when(jwtTokenProvider.createRefreshToken(user)).thenReturn(refreshToken);

        String userKey = "auth:refresh-token:user:" + userId;
        String rotationKey = "auth:refresh-token:rotation:" + refreshTokenId;
        when(refreshTokenRedisKeyManager.userRefreshTokenKey(userId)).thenReturn(userKey);
        when(refreshTokenRedisKeyManager.rotationDetectorKey(refreshTokenId.toString())).thenReturn(rotationKey);

        OAuthLoginResponse response = authService.loginWithKakaoIdToken(request);

        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.email()).isEqualTo("user@example.com");
        assertThat(response.displayName()).isEqualTo("카카오 사용자");
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");

        verify(valueOperations).set(eq(userKey), eq("refresh-token"), eq(Duration.ofMillis(jwtProperties.refreshToken().validity())));
        verify(valueOperations).set(eq(rotationKey), eq(userId.toString()), eq(Duration.ofMillis(jwtProperties.refreshToken().validity())));
    }

    @Test
    void should_throw_exception_when_kakao_id_token_missing() {
        KakaoIdTokenLoginRequest request = new KakaoIdTokenLoginRequest(" ");

        assertThatThrownBy(() -> authService.loginWithKakaoIdToken(request))
                .isInstanceOf(AuthException.class)
                .satisfies(exception -> {
                    AuthException authException = (AuthException) exception;
                    assertThat(authException.getErrorCode()).isEqualTo(AuthErrorCode.KAKAO_USERINFO_REQUEST_FAILED);
                });
    }

    @Test
    void should_login_with_apple_when_authorization_code_and_id_token_valid() {
        AppleOAuthLoginRequest request = new AppleOAuthLoginRequest("auth-code", "id-token");
        AppleUserInfo requestUserInfo = new AppleUserInfo("apple-user-id", "user@example.com", true);
        AppleUserInfo responseUserInfo = new AppleUserInfo("apple-user-id", "user@example.com", true);
        AppleOAuthTokenResponse tokenResponse = new AppleOAuthTokenResponse("apple-access-token", 3600L, "id-token", "apple-refresh-token", "Bearer");

        when(appleOAuthClient.parseIdToken("id-token")).thenReturn(requestUserInfo, responseUserInfo);
        when(appleOAuthClient.requestToken("auth-code")).thenReturn(tokenResponse);
        when(userOAuthAccountRepository.findByProviderAndProviderUserId(AuthProvider.APPLE, "apple-user-id"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());

        UUID userId = UUID.randomUUID();
        User savedUser = org.mockito.Mockito.mock(User.class);
        when(savedUser.getId()).thenReturn(userId);
        when(savedUser.getEmail()).thenReturn("user@example.com");
        when(savedUser.getDisplayName()).thenReturn("apple-user");
        when(savedUser.getProfileImageUrl()).thenReturn(null);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userOAuthAccountRepository.save(any(UserOAuthAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JwtToken accessToken = new JwtToken("access-token", Instant.now().plusSeconds(900), null);
        UUID refreshTokenId = UUID.randomUUID();
        JwtToken refreshToken = new JwtToken("refresh-token", Instant.now().plusSeconds(3600), refreshTokenId);
        when(jwtTokenProvider.createAccessToken(savedUser)).thenReturn(accessToken);
        when(jwtTokenProvider.createRefreshToken(savedUser)).thenReturn(refreshToken);

        String userKey = "auth:refresh-token:user:" + userId;
        String rotationKey = "auth:refresh-token:rotation:" + refreshTokenId;
        when(refreshTokenRedisKeyManager.userRefreshTokenKey(userId)).thenReturn(userKey);
        when(refreshTokenRedisKeyManager.rotationDetectorKey(refreshTokenId.toString())).thenReturn(rotationKey);

        OAuthLoginResponse response = authService.loginWithApple(request);

        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.email()).isEqualTo("user@example.com");
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");

        verify(valueOperations).set(eq(userKey), eq("refresh-token"), eq(Duration.ofMillis(jwtProperties.refreshToken().validity())));
        verify(valueOperations).set(eq(rotationKey), eq(userId.toString()), eq(Duration.ofMillis(jwtProperties.refreshToken().validity())));
    }

    @Test
    void should_throw_exception_when_apple_authorization_code_or_id_token_missing() {
        AppleOAuthLoginRequest request = new AppleOAuthLoginRequest(" ", " ");

        assertThatThrownBy(() -> authService.loginWithApple(request))
                .isInstanceOf(AuthException.class)
                .satisfies(exception -> {
                    AuthException authException = (AuthException) exception;
                    assertThat(authException.getErrorCode()).isEqualTo(AuthErrorCode.AUTHORIZATION_CODE_REQUIRED);
                });
    }

    @Test
    void should_issue_new_tokens_when_refresh_token_valid() {
        UUID userId = UUID.randomUUID();
        UUID currentTokenId = UUID.randomUUID();
        String refreshTokenValue = "old-refresh-token";
        TokenRefreshRequest request = new TokenRefreshRequest(refreshTokenValue);
        Instant issuedAt = Instant.now().minusSeconds(60);
        Instant expiresAt = Instant.now().plusSeconds(3600);
        RefreshTokenClaims claims = new RefreshTokenClaims(userId, currentTokenId, issuedAt, expiresAt);

        when(jwtTokenProvider.parseRefreshToken(refreshTokenValue)).thenReturn(claims);

        User user = org.mockito.Mockito.mock(User.class);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(user.getId()).thenReturn(userId);
        when(user.getEmail()).thenReturn("user@example.com");
        when(user.getDisplayName()).thenReturn("Refresh User");

        String userKey = "auth:refresh-token:user:" + userId;
        when(refreshTokenRedisKeyManager.userRefreshTokenKey(userId)).thenReturn(userKey);
        when(redisTemplate.opsForValue().get(userKey)).thenReturn(refreshTokenValue);

        String rotationKey = "auth:refresh-token:rotation:" + currentTokenId;
        when(refreshTokenRedisKeyManager.rotationDetectorKey(currentTokenId.toString())).thenReturn(rotationKey);
        when(redisTemplate.delete(rotationKey)).thenReturn(true);

        UUID newTokenId = UUID.randomUUID();
        JwtToken newAccessToken = new JwtToken("new-access", Instant.now().plusSeconds(900), null);
        JwtToken newRefreshToken = new JwtToken("new-refresh", Instant.now().plusSeconds(7200), newTokenId);
        when(jwtTokenProvider.createAccessToken(user)).thenReturn(newAccessToken);
        when(jwtTokenProvider.createRefreshToken(user)).thenReturn(newRefreshToken);

        String newRotationKey = "auth:refresh-token:rotation:" + newTokenId;
        when(refreshTokenRedisKeyManager.rotationDetectorKey(newTokenId.toString())).thenReturn(newRotationKey);

        TokenRefreshResponse response = authService.refreshToken(request);

        assertThat(response.accessToken()).isEqualTo("new-access");
        assertThat(response.refreshToken()).isEqualTo("new-refresh");
        assertThat(response.accessTokenExpiresIn()).isPositive();
        assertThat(response.refreshTokenExpiresIn()).isPositive();

        verify(redisTemplate).delete(rotationKey);
        verify(valueOperations).set(eq(userKey), eq("new-refresh"), eq(Duration.ofMillis(jwtProperties.refreshToken().validity())));
        verify(valueOperations).set(eq(newRotationKey), eq(userId.toString()), eq(Duration.ofMillis(jwtProperties.refreshToken().validity())));
    }

    @Test
    void should_throw_exception_when_refresh_token_reused() {
        UUID userId = UUID.randomUUID();
        UUID currentTokenId = UUID.randomUUID();
        String refreshTokenValue = "old-refresh-token";
        RefreshTokenClaims claims = new RefreshTokenClaims(userId, currentTokenId, Instant.now().minusSeconds(60), Instant.now().plusSeconds(3600));

        when(jwtTokenProvider.parseRefreshToken(refreshTokenValue)).thenReturn(claims);

        String userKey = "auth:refresh-token:user:" + userId;
        when(refreshTokenRedisKeyManager.userRefreshTokenKey(userId)).thenReturn(userKey);
        when(redisTemplate.opsForValue().get(userKey)).thenReturn(refreshTokenValue);

        String rotationKey = "auth:refresh-token:rotation:" + currentTokenId;
        when(refreshTokenRedisKeyManager.rotationDetectorKey(currentTokenId.toString())).thenReturn(rotationKey);
        when(redisTemplate.delete(rotationKey)).thenReturn(false);

        assertThatThrownBy(() -> authService.refreshToken(new TokenRefreshRequest(refreshTokenValue)))
                .isInstanceOf(AuthException.class)
                .satisfies(exception -> {
                    AuthException authException = (AuthException) exception;
                    assertThat(authException.getErrorCode()).isEqualTo(AuthErrorCode.REFRESH_TOKEN_REUSED);
                });
    }

    @Test
    void should_throw_exception_when_refresh_token_parsing_fails() {
        String refreshTokenValue = "invalid-token";
        when(jwtTokenProvider.parseRefreshToken(refreshTokenValue)).thenThrow(new JwtException("invalid"));

        assertThatThrownBy(() -> authService.refreshToken(new TokenRefreshRequest(refreshTokenValue)))
                .isInstanceOf(AuthException.class)
                .satisfies(exception -> {
                    AuthException authException = (AuthException) exception;
                    assertThat(authException.getErrorCode()).isEqualTo(AuthErrorCode.REFRESH_TOKEN_INVALID);
                });
    }
}
