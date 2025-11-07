package com.ssasinsa.wearagain.global.security;

import com.ssasinsa.wearagain.domain.auth.entity.User;
import com.ssasinsa.wearagain.domain.auth.infrastructure.jwt.JwtTokenProvider;
import com.ssasinsa.wearagain.domain.auth.infrastructure.jwt.JwtTokenProvider.AccessTokenClaims;
import com.ssasinsa.wearagain.domain.auth.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final SecurityWhitelist securityWhitelist;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String token = resolveToken(request);
            if (!StringUtils.hasText(token)) {
                filterChain.doFilter(request, response);
                return;
            }

            AccessTokenClaims claims = jwtTokenProvider.parseAccessToken(token);
            Optional<User> userOptional = userRepository.findById(claims.userId());
            if (userOptional.isEmpty()) {
                throw new JwtAuthenticationException("사용자를 찾을 수 없습니다.");
            }
            User user = userOptional.get();

            AuthenticatedUser principal = new AuthenticatedUser(user.getId(), user.getEmail(), user.getDisplayName());
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    Collections.emptyList()
            );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);
        } catch (JwtAuthenticationException exception) {
            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(request, response, exception);
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException exception) {
            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(request, response, new JwtAuthenticationException("인증 토큰이 유효하지 않습니다.", exception));
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(authorization) || !authorization.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return authorization.substring(BEARER_PREFIX.length());
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        if (requestUri != null && requestUri.startsWith("/api/v1/admin")) {
            return true;
        }
        return securityWhitelist.isPublic(request);
    }
}
