package com.ssasinsa.wearagain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import com.ssasinsa.wearagain.auth.dto.response.OAuthLoginResponse;
import com.ssasinsa.wearagain.auth.exception.AuthErrorCode;
import com.ssasinsa.wearagain.auth.exception.AuthException;
import com.ssasinsa.wearagain.auth.infrastructure.RefreshTokenRedisKeyManager;
import com.ssasinsa.wearagain.auth.infrastructure.client.AppleOAuthClient;
import com.ssasinsa.wearagain.auth.infrastructure.client.AppleOAuthClient.AppleUserInfo;
import com.ssasinsa.wearagain.auth.infrastructure.client.AppleOAuthTokenResponse;
import com.ssasinsa.wearagain.auth.infrastructure.client.GoogleOAuthClient;
import com.ssasinsa.wearagain.auth.infrastructure.client.KakaoOAuthClient;
import com.ssasinsa.wearagain.auth.infrastructure.jwt.JwtToken;
import com.ssasinsa.wearagain.auth.infrastructure.jwt.JwtTokenProvider;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
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
        kakaoOAuthProperties = new KakaoOAuthProperties("kakao-client", "kakao-secret", "kakao-redirect", "kakao-token", "kakao-userinfo");

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

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
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
}
