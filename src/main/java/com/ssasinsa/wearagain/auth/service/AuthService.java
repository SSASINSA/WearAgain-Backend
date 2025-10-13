package com.ssasinsa.wearagain.auth.service;

import com.ssasinsa.wearagain.auth.dto.request.GoogleOAuthLoginRequest;
import com.ssasinsa.wearagain.auth.dto.request.KakaoOAuthLoginRequest;
import com.ssasinsa.wearagain.auth.dto.response.OAuthLoginResponse;

public interface AuthService {

    OAuthLoginResponse loginWithGoogle(GoogleOAuthLoginRequest request);

    OAuthLoginResponse loginWithKakao(KakaoOAuthLoginRequest request);

    String generateGoogleAuthorizationUrl();

    String generateKakaoAuthorizationUrl();
}
