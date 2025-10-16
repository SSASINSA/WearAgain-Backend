package com.ssasinsa.wearagain.auth.service;

import com.ssasinsa.wearagain.auth.dto.request.AppleOAuthLoginRequest;
import com.ssasinsa.wearagain.auth.dto.request.GoogleOAuthLoginRequest;
import com.ssasinsa.wearagain.auth.dto.request.KakaoOAuthLoginRequest;
import com.ssasinsa.wearagain.auth.dto.request.TokenRefreshRequest;
import com.ssasinsa.wearagain.auth.dto.response.OAuthLoginResponse;
import com.ssasinsa.wearagain.auth.dto.response.TokenRefreshResponse;

public interface AuthService {

    OAuthLoginResponse loginWithGoogle(GoogleOAuthLoginRequest request);

    OAuthLoginResponse loginWithKakao(KakaoOAuthLoginRequest request);

    OAuthLoginResponse loginWithApple(AppleOAuthLoginRequest request);

    TokenRefreshResponse refreshToken(TokenRefreshRequest request);

    String generateGoogleAuthorizationUrl();

    String generateKakaoAuthorizationUrl();
}
