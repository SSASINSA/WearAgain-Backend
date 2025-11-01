package com.ssasinsa.wearagain.domain.auth.service;

import com.ssasinsa.wearagain.domain.auth.config.AdminSuperAdminProperties;
import com.ssasinsa.wearagain.domain.auth.entity.AdminRole;
import com.ssasinsa.wearagain.domain.auth.entity.AdminUser;
import com.ssasinsa.wearagain.domain.auth.repository.AdminUserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminSuperAdminInitializer {

    private final AdminSuperAdminProperties superAdminProperties;
    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    @Transactional
    public void initializeSuperAdmin() {
        if (!StringUtils.hasText(superAdminProperties.email()) || !StringUtils.hasText(superAdminProperties.password())) {
            log.warn("Super admin 초기화가 건너뛰어졌습니다. email 또는 password 환경변수가 설정되지 않았습니다.");
            return;
        }

        String email = superAdminProperties.email().trim().toLowerCase();
        ResolvedPassword resolvedPassword = resolvePassword(superAdminProperties.password());
        String encodedPassword = resolvedPassword.value();
        String rawInput = resolvedPassword.raw();
        boolean inputWasEncoded = resolvedPassword.inputWasEncoded();
        String name = StringUtils.hasText(superAdminProperties.name()) ? superAdminProperties.name() : "Super Admin";

        AdminUser existing = adminUserRepository.findByEmail(email).orElse(null);
        if (existing == null) {
            AdminUser superAdmin = AdminUser.createSuperAdmin(email, encodedPassword, name);
            adminUserRepository.save(superAdmin);
            log.info("Super admin 계정이 생성되었습니다: {}", email);
            return;
        }

        if (existing.getRole() != AdminRole.SUPER_ADMIN) {
            existing.changeRole(AdminRole.SUPER_ADMIN);
        }
        existing.activate();
        boolean samePassword = inputWasEncoded
                ? existing.getPassword().equals(encodedPassword)
                : passwordEncoder.matches(rawInput, existing.getPassword());
        if (!samePassword) {
            existing.changePassword(encodedPassword, false);
        }
        log.info("Super admin 계정이 확인되었습니다: {}", email);
    }

    private ResolvedPassword resolvePassword(String rawValue) {
        String trimmed = rawValue.trim();
        if (trimmed.startsWith("$2a$") || trimmed.startsWith("$2b$") || trimmed.startsWith("$2y$")) {
            return new ResolvedPassword(trimmed, trimmed, true);
        }
        return new ResolvedPassword(passwordEncoder.encode(trimmed), trimmed, false);
    }

    private record ResolvedPassword(String value, String raw, boolean inputWasEncoded) {
    }
}
