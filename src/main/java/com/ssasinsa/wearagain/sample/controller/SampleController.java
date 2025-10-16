package com.ssasinsa.wearagain.sample.controller;

import com.ssasinsa.wearagain.global.security.AuthenticatedUser;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sample")
public class SampleController {

    @GetMapping("/public")
    public Map<String, String> publicEndpoint() {
        return Map.of("message", "이 엔드포인트는 인증이 필요 없습니다.");
    }

    @GetMapping("/private")
    public Map<String, Object> privateEndpoint(Authentication authentication) {
        AuthenticatedUser principal = authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser authUser
                ? authUser
                : null;
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("message", "인증이 필요한 엔드포인트에 접근했습니다.");
        result.put("user", principal);
        return result;
    }
}
