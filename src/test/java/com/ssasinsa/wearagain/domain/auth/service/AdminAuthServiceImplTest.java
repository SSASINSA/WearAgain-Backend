package com.ssasinsa.wearagain.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ssasinsa.wearagain.domain.auth.config.AdminJwtProperties;
import com.ssasinsa.wearagain.domain.auth.dto.request.AdminLoginRequest;
import com.ssasinsa.wearagain.domain.auth.dto.request.AdminSignupRequestCreateRequest;
import com.ssasinsa.wearagain.domain.auth.dto.response.AdminAuthTokenResponse;
import com.ssasinsa.wearagain.domain.auth.dto.response.AdminSignupApprovalResponse;
import com.ssasinsa.wearagain.domain.auth.dto.response.AdminSignupRequestListResponse;
import com.ssasinsa.wearagain.domain.auth.entity.AdminRole;
import com.ssasinsa.wearagain.domain.auth.entity.AdminSignupRequest;
import com.ssasinsa.wearagain.domain.auth.entity.AdminSignupRequestStatus;
import com.ssasinsa.wearagain.domain.auth.entity.AdminStatus;
import com.ssasinsa.wearagain.domain.auth.entity.AdminUser;
import com.ssasinsa.wearagain.domain.auth.exception.AdminAuthErrorCode;
import com.ssasinsa.wearagain.domain.auth.exception.AdminAuthException;
import com.ssasinsa.wearagain.domain.auth.infrastructure.AdminRefreshTokenKeyManager;
import com.ssasinsa.wearagain.domain.auth.infrastructure.jwt.AdminJwtTokenProvider;
import com.ssasinsa.wearagain.domain.auth.infrastructure.jwt.JwtToken;
import com.ssasinsa.wearagain.domain.auth.repository.AdminSignupRequestRepository;
import com.ssasinsa.wearagain.domain.auth.repository.AdminUserRepository;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AdminAuthServiceImplTest {

    @Mock
    private AdminUserRepository adminUserRepository;
    @Mock
    private AdminSignupRequestRepository adminSignupRequestRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AdminJwtTokenProvider adminJwtTokenProvider;
    @Mock
    private AdminRefreshTokenKeyManager adminRefreshTokenKeyManager;
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private AdminAuthServiceImpl adminAuthService;

    private AdminJwtProperties adminJwtProperties;

    @BeforeEach
    void setUp() {
        adminJwtProperties = new AdminJwtProperties(
                "wearagain-admin",
                new AdminJwtProperties.TokenProperties("access-secret", 1800000L),
                new AdminJwtProperties.TokenProperties("refresh-secret", 604800000L)
        );
        adminAuthService = new AdminAuthServiceImpl(
                adminUserRepository,
                adminSignupRequestRepository,
                passwordEncoder,
                adminJwtTokenProvider,
                adminRefreshTokenKeyManager,
                redisTemplate,
                adminJwtProperties
        );
    }

    @Test
    @DisplayName("관리자 회원가입 요청을 생성한다")
    void should_create_signup_request_when_input_valid() {
        AdminSignupRequestCreateRequest request = new AdminSignupRequestCreateRequest(
                "manager@wearagain.kr",
                "P@ssw0rd1234",
                "홍길동",
                AdminRole.MANAGER,
                "행사 운영 담당"
        );

        when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");
        when(adminUserRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(adminSignupRequestRepository.existsByEmailAndStatusIn(eq(request.email()), any())).thenReturn(false);
        when(adminSignupRequestRepository.findTopByEmailOrderByCreatedAtDesc(request.email())).thenReturn(Optional.empty());
        when(adminSignupRequestRepository.save(any(AdminSignupRequest.class))).thenAnswer(invocation -> {
            AdminSignupRequest signupRequest = invocation.getArgument(0);
            ReflectionTestUtils.setField(signupRequest, "id", 1L);
            return signupRequest;
        });

        var response = adminAuthService.createSignupRequest(request);

        assertThat(response.signupRequestId()).isEqualTo(1L);
        assertThat(response.status()).isEqualTo(AdminSignupRequestStatus.PENDING);
        assertThat(response.message()).contains("접수");
    }

    @Test
    @DisplayName("기존 관리자 이메일로 신청 시 예외가 발생한다")
    void should_throw_exception_when_email_already_exists() {
        AdminSignupRequestCreateRequest request = new AdminSignupRequestCreateRequest(
                "admin@wearagain.kr",
                "P@ssw0rd1234",
                "홍길동",
                AdminRole.ADMIN,
                null
        );

        AdminUser existing = AdminUser.createApproved(request.email(), "encoded", "홍길동", AdminRole.ADMIN);
        when(adminUserRepository.findByEmail(request.email())).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> adminAuthService.createSignupRequest(request))
                .isInstanceOf(AdminAuthException.class)
                .extracting("errorCode")
                .isEqualTo(AdminAuthErrorCode.EMAIL_ALREADY_REGISTERED);
    }

    @Test
    @DisplayName("대기중인 요청을 승인하면 관리자 계정을 생성한다")
    void should_approve_signup_request_when_pending() {
        Long requestId = 10L;
        Long reviewerId = 1L;

        AdminSignupRequest signupRequest = AdminSignupRequest.createPending(
                "manager@wearagain.kr",
                "encoded-password",
                "홍길동",
                AdminRole.ADMIN,
                "이벤트 운영"
        );
        ReflectionTestUtils.setField(signupRequest, "id", requestId);

        AdminUser reviewer = AdminUser.createSuperAdmin("super@wearagain.kr", "encoded-super", "Super");
        ReflectionTestUtils.setField(reviewer, "id", reviewerId);

        when(adminSignupRequestRepository.findById(requestId)).thenReturn(Optional.of(signupRequest));
        when(adminUserRepository.existsByEmail(signupRequest.getEmail())).thenReturn(false);
        when(adminUserRepository.findById(reviewerId)).thenReturn(Optional.of(reviewer));
        when(adminUserRepository.save(any(AdminUser.class))).thenAnswer(invocation -> {
            AdminUser saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 200L);
            return saved;
        });

        AdminSignupApprovalResponse response = adminAuthService.approveSignupRequest(requestId, reviewerId);

        assertThat(response.adminUserId()).isEqualTo(200L);
        assertThat(response.role()).isEqualTo(AdminRole.ADMIN);
        assertThat(response.status()).isEqualTo(AdminStatus.ACTIVE.name());
    }

    @Test
    @DisplayName("정상 로그인 시 토큰을 발급한다")
    void should_issue_tokens_when_login_successful() {
        AdminUser adminUser = AdminUser.createApproved("admin@wearagain.kr", "encoded", "관리자", AdminRole.ADMIN);
        ReflectionTestUtils.setField(adminUser, "id", 5L);

        when(adminUserRepository.findByEmail(adminUser.getEmail())).thenReturn(Optional.of(adminUser));
        when(passwordEncoder.matches("password", adminUser.getPassword())).thenReturn(true);
        when(adminJwtTokenProvider.createAccessToken(adminUser)).thenReturn(new JwtToken("access", Instant.now(), null));
        when(adminJwtTokenProvider.createRefreshToken(adminUser)).thenReturn(new JwtToken("refresh", Instant.now(), UUID.randomUUID()));
        when(adminRefreshTokenKeyManager.adminRefreshTokenKey(adminUser.getId())).thenReturn("auth:refresh-token:admin:" + adminUser.getId());
        when(adminRefreshTokenKeyManager.rotationDetectorKey(any())).thenAnswer(invocation -> "rotation:" + invocation.getArgument(0));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        AdminAuthTokenResponse response = adminAuthService.login(new AdminLoginRequest(adminUser.getEmail(), "password"));

        assertThat(response.role()).isEqualTo(AdminRole.ADMIN);
        assertThat(response.accessToken()).isEqualTo("access");
        assertThat(response.refreshToken()).isEqualTo("refresh");
    }

    @Test
    @DisplayName("SUPER_ADMIN이 상태별 요청 목록을 조회하고 만료 처리한다")
    void should_return_signup_requests_by_status() {
        AdminSignupRequest pending = AdminSignupRequest.createPending(
                "pending@wearagain.kr",
                "encoded-password",
                "만료 대상 관리자",
                AdminRole.ADMIN,
                "담당 부서 지정 요청"
        );
        ReflectionTestUtils.setField(pending, "id", 50L);
        ReflectionTestUtils.setField(pending, "createdAt", LocalDateTime.now().minusDays(8));
        ReflectionTestUtils.setField(pending, "updatedAt", LocalDateTime.now().minusDays(8));

        Page<AdminSignupRequest> page = new PageImpl<>(List.of(pending), PageRequest.of(0, 20), 1);
        when(adminSignupRequestRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        AdminSignupRequestListResponse response = adminAuthService.getSignupRequests(
                AdminSignupRequestStatus.PENDING,
                null,
                null,
                0,
                20,
                "LATEST"
        );

        assertThat(response.items()).isEmpty();
        assertThat(pending.getStatus()).isEqualTo(AdminSignupRequestStatus.EXPIRED);
        assertThat(response.totalElements()).isEqualTo(1);
        assertThat(response.page()).isEqualTo(0);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(adminSignupRequestRepository).findAll(any(Specification.class), pageableCaptor.capture());
        Pageable usedPageable = pageableCaptor.getValue();
        assertThat(usedPageable.getSort()).isEqualTo(Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")));
    }

    @Test
    @DisplayName("상태 필터 없이 조회하면 전체 요청을 반환한다")
    void should_return_all_signup_requests_when_status_not_provided() {
        AdminSignupRequest pending = AdminSignupRequest.createPending(
                "pending2@wearagain.kr",
                "encoded",
                "대기자2",
                AdminRole.MANAGER,
                "커뮤니티 지원"
        );
        ReflectionTestUtils.setField(pending, "id", 60L);
        ReflectionTestUtils.setField(pending, "createdAt", LocalDateTime.now().minusDays(1));
        ReflectionTestUtils.setField(pending, "updatedAt", LocalDateTime.now().minusDays(1));

        AdminUser reviewer = AdminUser.createSuperAdmin("super@wearagain.kr", "encoded-super", "슈퍼관리자");
        ReflectionTestUtils.setField(reviewer, "id", 1L);

        AdminSignupRequest approved = AdminSignupRequest.createPending(
                "approved@wearagain.kr",
                "encoded-2",
                "승인자",
                AdminRole.ADMIN,
                "행사 총괄"
        );
        ReflectionTestUtils.setField(approved, "id", 55L);
        ReflectionTestUtils.setField(approved, "createdAt", LocalDateTime.now().minusDays(2));
        ReflectionTestUtils.setField(approved, "updatedAt", LocalDateTime.now().minusDays(2));
        approved.markApproved(reviewer, LocalDateTime.now().minusDays(1));

        Page<AdminSignupRequest> page = new PageImpl<>(List.of(approved, pending), PageRequest.of(1, 10), 2);
        when(adminSignupRequestRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        AdminSignupRequestListResponse response = adminAuthService.getSignupRequests(
                null,
                null,
                null,
                1,
                10,
                "OLDEST"
        );

        assertThat(response.items()).hasSize(2);
        assertThat(response.items().get(0).signupRequestId()).isEqualTo(55L);
        assertThat(response.items().get(0).reviewer()).isNotNull();
        assertThat(response.items().get(0).reviewer().adminId()).isEqualTo(1L);
        assertThat(response.items().get(1).signupRequestId()).isEqualTo(60L);
        assertThat(response.page()).isEqualTo(1);
        assertThat(response.totalPages()).isEqualTo(1);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(adminSignupRequestRepository).findAll(any(Specification.class), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getSort()).isEqualTo(Sort.by(Sort.Order.asc("createdAt"), Sort.Order.asc("id")));
    }

    @Test
    @DisplayName("KEYWORD로 요청 목록을 검색한다")
    void should_filter_signup_requests_by_keyword() {
        AdminSignupRequest pending = AdminSignupRequest.createPending(
                "FindMe@wearagain.kr",
                "encoded",
                "검색될 관리자",
                AdminRole.ADMIN,
                "검색 키워드 포함"
        );
        ReflectionTestUtils.setField(pending, "id", 70L);
        ReflectionTestUtils.setField(pending, "createdAt", LocalDateTime.now().minusDays(2));
        ReflectionTestUtils.setField(pending, "updatedAt", LocalDateTime.now().minusDays(2));

        Page<AdminSignupRequest> page = new PageImpl<>(List.of(pending), PageRequest.of(0, 20), 1);
        when(adminSignupRequestRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        AdminSignupRequestListResponse response = adminAuthService.getSignupRequests(
                null,
                "  ADMIN ",
                null,
                0,
                20,
                "LATEST"
        );

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).signupRequestId()).isEqualTo(70L);
        verify(adminSignupRequestRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("키워드 범위가 잘못되면 INVALID_INPUT 예외를 던진다")
    void should_throw_when_keyword_scope_invalid() {
        assertThatThrownBy(() -> adminAuthService.getSignupRequests(null, "admin", "unknown", 0, 20, "LATEST"))
                .isInstanceOf(AdminAuthException.class)
                .extracting("errorCode")
                .isEqualTo(AdminAuthErrorCode.INVALID_INPUT);
    }

    @Test
    @DisplayName("키워드 스코프가 EMAIL이면 이메일로만 필터링한다")
    void should_filter_by_email_scope_only() {
        AdminSignupRequest pending = AdminSignupRequest.createPending(
                "findemail@wearagain.kr",
                "encoded",
                "이메일 검색 관리자",
                AdminRole.ADMIN,
                "검색 대상"
        );
        ReflectionTestUtils.setField(pending, "id", 71L);
        ReflectionTestUtils.setField(pending, "createdAt", LocalDateTime.now().minusDays(1));
        ReflectionTestUtils.setField(pending, "updatedAt", LocalDateTime.now().minusDays(1));

        Page<AdminSignupRequest> page = new PageImpl<>(List.of(pending), PageRequest.of(0, 20), 1);
        when(adminSignupRequestRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        AdminSignupRequestListResponse response = adminAuthService.getSignupRequests(
                null,
                " find ",
                "EMAIL",
                0,
                20,
                "LATEST"
        );

        assertThat(response.items()).hasSize(1);
        verify(adminSignupRequestRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("페이지 파라미터가 유효하지 않으면 예외가 발생한다")
    void should_throw_when_page_invalid() {
        assertThatThrownBy(() -> adminAuthService.getSignupRequests(null, null, null, -1, 10, "LATEST"))
                .isInstanceOf(AdminAuthException.class)
                .extracting("errorCode")
                .isEqualTo(AdminAuthErrorCode.INVALID_INPUT);
    }

    @Test
    @DisplayName("정렬 파라미터가 잘못되면 예외가 발생한다")
    void should_throw_when_sort_invalid() {
        assertThatThrownBy(() -> adminAuthService.getSignupRequests(null, null, null, 0, 10, "UNKNOWN"))
                .isInstanceOf(AdminAuthException.class)
                .extracting("errorCode")
                .isEqualTo(AdminAuthErrorCode.INVALID_INPUT);
    }
}

