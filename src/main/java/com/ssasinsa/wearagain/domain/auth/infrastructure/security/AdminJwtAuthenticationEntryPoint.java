package com.ssasinsa.wearagain.domain.auth.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssasinsa.wearagain.domain.auth.exception.AdminAuthErrorCode;
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
public class AdminJwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        AdminAuthErrorCode errorCode = AdminAuthErrorCode.TOKEN_INVALID;
        ErrorResponse errorResponse = ErrorResponse.of(errorCode, errorCode.getMessage(), request.getRequestURI());

        response.setStatus(errorCode.getStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
