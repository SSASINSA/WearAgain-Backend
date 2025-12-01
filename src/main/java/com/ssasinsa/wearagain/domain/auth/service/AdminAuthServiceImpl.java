package com.ssasinsa.wearagain.domain.auth.service;

import com.ssasinsa.wearagain.domain.auth.config.AdminJwtProperties;
import com.ssasinsa.wearagain.domain.auth.dto.request.AdminLoginRequest;
import com.ssasinsa.wearagain.domain.auth.dto.request.AdminLogoutRequest;
import com.ssasinsa.wearagain.domain.auth.dto.request.AdminSignupRequestCreateRequest;
import com.ssasinsa.wearagain.domain.auth.dto.request.AdminTokenRefreshRequest;
import com.ssasinsa.wearagain.domain.auth.dto.response.*;
import com.ssasinsa.wearagain.domain.auth.entity.*;
import com.ssasinsa.wearagain.domain.auth.exception.AdminAuthErrorCode;
import com.ssasinsa.wearagain.domain.auth.exception.AdminAuthException;
import com.ssasinsa.wearagain.domain.auth.infrastructure.AdminRefreshTokenKeyManager;
import com.ssasinsa.wearagain.domain.auth.infrastructure.jwt.AdminJwtTokenProvider;
import com.ssasinsa.wearagain.domain.auth.infrastructure.jwt.AdminJwtTokenProvider.RefreshTokenClaims;
import com.ssasinsa.wearagain.domain.auth.infrastructure.jwt.JwtToken;
import com.ssasinsa.wearagain.domain.auth.repository.AdminSignupRequestRepository;
import com.ssasinsa.wearagain.domain.auth.repository.AdminSignupRequestSpecifications;
import com.ssasinsa.wearagain.domain.auth.repository.AdminUserRepository;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminAuthServiceImpl implements AdminAuthService {

    private static final long SIGNUP_REQUEST_EXPIRY_HOURS = 24L * 7;

    private final AdminUserRepository adminUserRepository;
    private final AdminSignupRequestRepository adminSignupRequestRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminJwtTokenProvider adminJwtTokenProvider;
    private final AdminRefreshTokenKeyManager adminRefreshTokenKeyManager;
    private final RedisTemplate<String, String> redisTemplate;
    private final AdminJwtProperties adminJwtProperties;

    @Override
    @Transactional
    public AdminAuthTokenResponse login(AdminLoginRequest request) {
        boolean pendingSignupExists = adminSignupRequestRepository.existsByEmailAndStatusIn(
                request.email(),
                EnumSet.of(AdminSignupRequestStatus.PENDING)
        );
        if (pendingSignupExists) {
            throw new AdminAuthException(AdminAuthErrorCode.SIGNUP_PENDING);
        }

        AdminUser admin = adminUserRepository.findByEmail(request.email())
                .orElseThrow(() -> new AdminAuthException(AdminAuthErrorCode.INVALID_CREDENTIAL));

        if (admin.getStatus() != AdminStatus.ACTIVE) {
            throw new AdminAuthException(AdminAuthErrorCode.ACCOUNT_NOT_APPROVED);
        }

        if (!passwordEncoder.matches(request.password(), admin.getPassword())) {
            throw new AdminAuthException(AdminAuthErrorCode.INVALID_CREDENTIAL);
        }

        admin.recordSuccessfulLogin(LocalDateTime.now());

        JwtToken accessToken = adminJwtTokenProvider.createAccessToken(admin);
        JwtToken refreshToken = adminJwtTokenProvider.createRefreshToken(admin);
        storeRefreshToken(admin.getId(), refreshToken);

        return AdminAuthTokenResponse.of(
                admin.getRole(),
                admin.getName(),
                admin.isMustChangePassword(),
                accessToken,
                refreshToken,
                adminJwtProperties.accessToken().validity()
        );
    }

    @Override
    @Transactional
    public AdminAuthTokenResponse refresh(AdminTokenRefreshRequest request) {
         if (!StringUtils.hasText(request.refreshToken())) {
             throw new AdminAuthException(AdminAuthErrorCode.INVALID_INPUT);
         }

         RefreshTokenClaims claims;
         try {
            claims = adminJwtTokenProvider.parseRefreshToken(request.refreshToken());
         } catch (JwtException | IllegalArgumentException exception) {
            throw new AdminAuthException(AdminAuthErrorCode.REFRESH_TOKEN_INVALID, exception);
         }

         Long adminId = claims.adminId();
         UUID tokenId = claims.tokenId();

         String refreshKey = adminRefreshTokenKeyManager.adminRefreshTokenKey(adminId);
         String storedToken = redisTemplate.opsForValue().get(refreshKey);
         if (!StringUtils.hasText(storedToken) || !Objects.equals(storedToken, request.refreshToken())) {
            throw new AdminAuthException(AdminAuthErrorCode.REFRESH_TOKEN_INVALID);
         }

         String rotationKey = adminRefreshTokenKeyManager.rotationDetectorKey(tokenId.toString());
         Boolean deleted = redisTemplate.delete(rotationKey);
         if (!Boolean.TRUE.equals(deleted)) {
            throw new AdminAuthException(AdminAuthErrorCode.REFRESH_TOKEN_INVALID);
         }

        AdminUser admin = adminUserRepository.findById(adminId)
                .orElseThrow(() -> new AdminAuthException(AdminAuthErrorCode.REFRESH_TOKEN_INVALID));

        if (admin.getStatus() != AdminStatus.ACTIVE) {
            throw new AdminAuthException(AdminAuthErrorCode.ACCOUNT_NOT_APPROVED);
        }

        JwtToken newAccessToken = adminJwtTokenProvider.createAccessToken(admin);
        JwtToken newRefreshToken = adminJwtTokenProvider.createRefreshToken(admin);
        storeRefreshToken(admin.getId(), newRefreshToken);

        return AdminAuthTokenResponse.of(
                admin.getRole(),
                admin.getName(),
                admin.isMustChangePassword(),
                newAccessToken,
                newRefreshToken,
                adminJwtProperties.accessToken().validity()
        );
    }

    @Override
    @Transactional
    public AdminSimpleResponse logout(AdminLogoutRequest request) {
        if (!StringUtils.hasText(request.refreshToken())) {
            throw new AdminAuthException(AdminAuthErrorCode.INVALID_INPUT);
        }

        RefreshTokenClaims claims;
        try {
            claims = adminJwtTokenProvider.parseRefreshToken(request.refreshToken());
        } catch (JwtException | IllegalArgumentException exception) {
            throw new AdminAuthException(AdminAuthErrorCode.INVALID_INPUT, exception);
        }

        String refreshKey = adminRefreshTokenKeyManager.adminRefreshTokenKey(claims.adminId());
        redisTemplate.delete(refreshKey);

        String rotationKey = adminRefreshTokenKeyManager.rotationDetectorKey(claims.tokenId().toString());
        redisTemplate.delete(rotationKey);

        return AdminSimpleResponse.of("로그아웃이 완료되었습니다.");
    }

    @Override
    @Transactional
    public AdminSignupRequestResponse createSignupRequest(AdminSignupRequestCreateRequest request) {
        validateSignupInput(request);

        String encodedPassword = passwordEncoder.encode(request.password());
        AdminSignupRequest signupRequest = AdminSignupRequest.createPending(
                request.email(),
                encodedPassword,
                request.name(),
                normalizeRequestedRole(request.requestedRole()),
                request.reason()
        );

        AdminSignupRequest saved = adminSignupRequestRepository.save(signupRequest);
        return AdminSignupRequestResponse.pending(saved);
    }

    @Override
    @Transactional
    public AdminSignupApprovalResponse approveSignupRequest(Long requestId, Long reviewerId) {
        AdminSignupRequest signupRequest = loadPendingRequest(requestId);

        if (adminUserRepository.existsByEmail(signupRequest.getEmail())) {
            signupRequest.markExpired();
            throw new AdminAuthException(AdminAuthErrorCode.EMAIL_ALREADY_REGISTERED);
        }

        AdminUser reviewer = adminUserRepository.findById(reviewerId)
                .orElseThrow(() -> new AdminAuthException(AdminAuthErrorCode.INSUFFICIENT_PERMISSION));

        AdminRole role = normalizeRequestedRole(signupRequest.getRequestedRole());
        AdminUser adminUser = AdminUser.createApproved(signupRequest.getEmail(), signupRequest.getPassword(), signupRequest.getName(), role);
        adminUserRepository.save(adminUser);

        signupRequest.markApproved(reviewer, LocalDateTime.now());

        return AdminSignupApprovalResponse.of(adminUser);
    }

    @Override
    @Transactional
    public AdminSimpleResponse rejectSignupRequest(Long requestId, Long reviewerId) {
        AdminSignupRequest signupRequest = loadPendingRequest(requestId);
        AdminUser reviewer = adminUserRepository.findById(reviewerId)
                .orElseThrow(() -> new AdminAuthException(AdminAuthErrorCode.INSUFFICIENT_PERMISSION));

        signupRequest.markRejected(reviewer, LocalDateTime.now());
        return AdminSimpleResponse.of("가입 신청이 거절되었습니다.");
    }

    @Override
    @Transactional
    public AdminSignupRequestListResponse getSignupRequests(
            AdminSignupRequestStatus status,
            String keyword,
            String keywordScope,
            int page,
            int size,
            String sort
    ) {
        validatePageRequest(page, size);
        String normalizedKeyword = normalizeKeyword(keyword);
        AdminSignupRequestKeywordScope scope = resolveKeywordScope(keywordScope);

        Specification<AdminSignupRequest> spec = AdminSignupRequestSpecifications.statusEquals(status);
        Specification<AdminSignupRequest> keywordSpec = AdminSignupRequestSpecifications.keywordMatches(normalizedKeyword, scope);
        if (keywordSpec != null) {
            spec = spec == null ? keywordSpec : spec.and(keywordSpec);
        }

        Pageable pageable = PageRequest.of(page, size, resolveSort(sort));
        Page<AdminSignupRequest> result = adminSignupRequestRepository.findAll(spec, pageable);
        List<AdminSignupRequest> requests = result.getContent();

        LocalDateTime now = LocalDateTime.now();
        requests.stream()
                .filter(request -> request.isExpired(now, SIGNUP_REQUEST_EXPIRY_HOURS))
                .forEach(AdminSignupRequest::markExpired);

        if (status != null) {
            requests = requests.stream()
                    .filter(request -> request.getStatus() == status)
                    .toList();
        }

        List<AdminSignupRequestSummaryResponse> summaries = requests.stream()
                .map(AdminSignupRequestSummaryResponse::from)
                .toList();

        return AdminSignupRequestListResponse.of(
                summaries,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.hasNext()
        );
    }

    @Override
    public AdminRoleResponse getMyRole(Long adminId) {
        AdminUser admin = adminUserRepository.findById(adminId)
                .orElseThrow(() -> new AdminAuthException(AdminAuthErrorCode.INVALID_INPUT));
        return AdminRoleResponse.of(admin.getRole());
    }

    private String normalizeKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        return keyword.trim().toLowerCase();
    }

    private void validatePageRequest(int page, int size) {
        if (page < 0 || size <= 0 || size > 50) {
            throw new AdminAuthException(AdminAuthErrorCode.INVALID_INPUT);
        }
    }

    private Sort resolveSort(String param) {
        AdminSignupRequestSortType sortType = resolveSortType(param);
        return switch (sortType) {
            case LATEST -> Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
            case OLDEST -> Sort.by(Sort.Order.asc("createdAt"), Sort.Order.asc("id"));
            case NAME_ASC -> Sort.by(Sort.Order.asc("name"), Sort.Order.asc("id"));
            case NAME_DESC -> Sort.by(Sort.Order.desc("name"), Sort.Order.desc("id"));
        };
    }

    private AdminSignupRequestSortType resolveSortType(String param) {
        if (!StringUtils.hasText(param)) {
            return AdminSignupRequestSortType.LATEST;
        }
        try {
            return AdminSignupRequestSortType.valueOf(param.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new AdminAuthException(AdminAuthErrorCode.INVALID_INPUT, exception);
        }
    }

    private AdminSignupRequestKeywordScope resolveKeywordScope(String keywordScope) {
        if (!StringUtils.hasText(keywordScope)) {
            return AdminSignupRequestKeywordScope.ALL;
        }
        try {
            return AdminSignupRequestKeywordScope.valueOf(keywordScope.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new AdminAuthException(AdminAuthErrorCode.INVALID_INPUT, exception);
        }
    }

    private AdminSignupRequest loadPendingRequest(Long requestId) {
        AdminSignupRequest signupRequest = adminSignupRequestRepository.findById(requestId)
                .orElseThrow(() -> new AdminAuthException(AdminAuthErrorCode.INVALID_INPUT));

        if (!signupRequest.isPending()) {
            throw new AdminAuthException(AdminAuthErrorCode.REQUEST_ALREADY_PROCESSED);
        }

        if (signupRequest.isExpired(LocalDateTime.now(), SIGNUP_REQUEST_EXPIRY_HOURS)) {
            signupRequest.markExpired();
            throw new AdminAuthException(AdminAuthErrorCode.SIGNUP_REQUEST_EXPIRED);
        }

        return signupRequest;
    }

    private void validateSignupInput(AdminSignupRequestCreateRequest request) {
        AdminRole requestedRole = normalizeRequestedRole(request.requestedRole());
        if (requestedRole == AdminRole.SUPER_ADMIN) {
            throw new AdminAuthException(AdminAuthErrorCode.INVALID_INPUT);
        }

        Optional<AdminUser> existingUser = adminUserRepository.findByEmail(request.email());
        if (existingUser.isPresent()) {
            AdminUser user = existingUser.get();
            if (user.getStatus() == AdminStatus.SUSPENDED) {
                adminUserRepository.delete(user);
            } else {
                throw new AdminAuthException(AdminAuthErrorCode.EMAIL_ALREADY_REGISTERED);
            }
        }

        boolean pendingExists = adminSignupRequestRepository.existsByEmailAndStatusIn(
                request.email(),
                EnumSet.of(AdminSignupRequestStatus.PENDING)
        );
        if (pendingExists) {
            throw new AdminAuthException(AdminAuthErrorCode.SIGNUP_ALREADY_REQUESTED);
        }

        Optional<AdminSignupRequest> latestRequest = adminSignupRequestRepository.findTopByEmailOrderByCreatedAtDesc(request.email());
        if (latestRequest.isPresent() && latestRequest.get().isExpired(LocalDateTime.now(), SIGNUP_REQUEST_EXPIRY_HOURS)) {
            latestRequest.get().markExpired();
        }
    }

    private AdminRole normalizeRequestedRole(AdminRole role) {
        if (role == null) {
            return AdminRole.MANAGER;
        }
        if (role == AdminRole.SUPER_ADMIN) {
            return AdminRole.ADMIN;
        }
        return role;
    }

    private void storeRefreshToken(Long adminId, JwtToken refreshToken) {
        Duration validity = Duration.ofMillis(adminJwtProperties.refreshToken().validity());
        String refreshKey = adminRefreshTokenKeyManager.adminRefreshTokenKey(adminId);
        redisTemplate.opsForValue().set(refreshKey, refreshToken.value(), validity);

        if (refreshToken.tokenId() != null) {
            String rotationKey = adminRefreshTokenKeyManager.rotationDetectorKey(refreshToken.tokenId().toString());
            redisTemplate.opsForValue().set(rotationKey, adminId.toString(), validity);
        }
    }
}
