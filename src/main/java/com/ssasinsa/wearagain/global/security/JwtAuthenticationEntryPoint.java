package com.ssasinsa.wearagain.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssasinsa.wearagain.domain.auth.exception.AuthErrorCode;
import com.ssasinsa.wearagain.global.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        AuthErrorCode errorCode = AuthErrorCode.ACCESS_TOKEN_EXPIRED;
        ErrorResponse errorResponse = ErrorResponse.of(errorCode, errorCode.getMessage(), request.getRequestURI());
        response.setStatus(errorCode.getStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
