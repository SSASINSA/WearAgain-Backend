package com.ssasinsa.wearagain.domain.auth.infrastructure.security;

import com.ssasinsa.wearagain.domain.auth.entity.AdminStatus;
import com.ssasinsa.wearagain.domain.auth.entity.AdminUser;
import com.ssasinsa.wearagain.domain.auth.infrastructure.jwt.AdminJwtTokenProvider;
import com.ssasinsa.wearagain.domain.auth.infrastructure.jwt.AdminJwtTokenProvider.AccessTokenClaims;
import com.ssasinsa.wearagain.domain.auth.repository.AdminUserRepository;
import com.ssasinsa.wearagain.global.security.JwtAuthenticationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminJwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String ADMIN_PREFIX = "/api/v1/admin";
    private static final Set<String> PUBLIC_POST_ENDPOINTS = Set.of(
            "/api/v1/admin/auth/login",
            "/api/v1/admin/auth/refresh",
            "/api/v1/admin/auth/logout",
            "/api/v1/admin/auth/signup-requests"
    );
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final AdminJwtTokenProvider adminJwtTokenProvider;
    private final AdminUserRepository adminUserRepository;
    private final AdminJwtAuthenticationEntryPoint authenticationEntryPoint;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!isAdminProtectedEndpoint(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = resolveToken(request);
            if (!StringUtils.hasText(token)) {
                throw new JwtAuthenticationException("관리자 인증 토큰이 필요합니다.");
            }

            AccessTokenClaims claims = adminJwtTokenProvider.parseAccessToken(token);
            AdminUser adminUser = adminUserRepository.findById(claims.adminId())
                    .orElseThrow(() -> new JwtAuthenticationException("관리자 계정을 찾을 수 없습니다."));

            if (adminUser.getStatus() != AdminStatus.ACTIVE) {
                throw new JwtAuthenticationException("승인되지 않은 관리자 계정입니다.");
            }

            AdminAuthenticatedUser principal = new AdminAuthenticatedUser(
                    adminUser.getId(),
                    adminUser.getEmail(),
                    adminUser.getName(),
                    claims.role()
            );

            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + claims.role().name()))
            );
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            filterChain.doFilter(request, response);
        } catch (JwtAuthenticationException exception) {
            log.debug("Admin authentication failed: {}", exception.getMessage());
            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(request, response, exception);
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException exception) {
            log.debug("Admin token parsing failed", exception);
            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(request, response, new JwtAuthenticationException("관리자 인증 토큰이 유효하지 않습니다.", exception));
        }
    }

    private boolean isAdminProtectedEndpoint(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        if (!requestUri.startsWith(ADMIN_PREFIX)) {
            return false;
        }
        if (HttpMethod.POST.matches(request.getMethod()) &&
                PUBLIC_POST_ENDPOINTS.stream().anyMatch(pattern -> PATH_MATCHER.match(pattern, requestUri))) {
            return false;
        }
        return true;
    }

    private String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
            return null;
        }
        return authorization.substring("Bearer ".length());
    }
}
