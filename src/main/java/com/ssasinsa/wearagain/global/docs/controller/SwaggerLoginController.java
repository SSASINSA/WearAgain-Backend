package com.ssasinsa.wearagain.global.docs.controller;

import com.ssasinsa.wearagain.domain.auth.dto.request.AdminLoginRequest;
import com.ssasinsa.wearagain.domain.auth.dto.response.AdminAuthTokenResponse;
import com.ssasinsa.wearagain.domain.auth.service.AdminAuthService;
import com.ssasinsa.wearagain.global.docs.config.SwaggerTestLoginProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/swagger-login")
@Profile({"local", "dev"})
public class SwaggerLoginController {

    private final AdminAuthService adminAuthService;
    private final SwaggerTestLoginProperties properties;

    @PostMapping("/admin/token")
    public ResponseEntity<AdminAuthTokenResponse> issueAdminToken() {
        SwaggerTestLoginProperties.Admin admin = properties.getAdmin();
        if (admin == null || !StringUtils.hasText(admin.getEmail()) || !StringUtils.hasText(admin.getPassword())) {
            log.warn("Swagger 테스트 로그인용 관리자 계정 정보가 설정되지 않았습니다.");
            throw new ResponseStatusException(BAD_REQUEST, "관리자 테스트 계정이 설정되지 않았습니다.");
        }

        AdminLoginRequest request = new AdminLoginRequest(admin.getEmail(), admin.getPassword());
        AdminAuthTokenResponse response = adminAuthService.login(request);
        return ResponseEntity.ok(response);
    }
}
