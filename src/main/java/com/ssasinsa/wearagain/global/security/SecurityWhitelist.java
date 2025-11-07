package com.ssasinsa.wearagain.global.security;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;

@Component
public class SecurityWhitelist {

    private static final String[] PUBLIC_MATCHERS = {
            "/api/v1/auth/**",
            "/api/v1/sample/public",
            "/resources/**",
            "/static/**",
            "/public/**",
            "/*.html",
            "/",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/api/v1/staff/**"
    };

    private final RequestMatcher publicRequestMatcher;

    public SecurityWhitelist() {
        this.publicRequestMatcher = new OrRequestMatcher(
                Arrays.stream(PUBLIC_MATCHERS)
                        .map(AntPathRequestMatcher::new)
                        .toArray(RequestMatcher[]::new)
        );
    }

    public String[] getPublicMatchers() {
        return Arrays.copyOf(PUBLIC_MATCHERS, PUBLIC_MATCHERS.length);
    }

    public boolean isPublic(HttpServletRequest request) {
        return publicRequestMatcher.matches(request);
    }
}
