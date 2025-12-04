package com.ssasinsa.wearagain.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ssasinsa.wearagain.domain.auth.entity.User;
import com.ssasinsa.wearagain.domain.auth.repository.UserRepository;
import com.ssasinsa.wearagain.domain.auth.infrastructure.RefreshTokenRedisKeyManager;
import com.ssasinsa.wearagain.domain.event.repository.EventApplicationRepository;
import com.ssasinsa.wearagain.domain.finance.repository.CreditHistoryRepository;
import com.ssasinsa.wearagain.domain.finance.repository.ImpactAnalyticsRepository;
import com.ssasinsa.wearagain.domain.finance.repository.TicketHistoryRepository;
import com.ssasinsa.wearagain.domain.growth.repository.UserGrowthRepository;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminParticipantDetailResponse;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminParticipantListResponse;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminParticipantStatsResponse;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminParticipantSuspensionRequest;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminParticipantUpdateRequest;
import com.ssasinsa.wearagain.domain.user.exception.UserErrorCode;
import com.ssasinsa.wearagain.domain.user.exception.UserException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ImpactAnalyticsRepository impactAnalyticsRepository;

    @Mock
    private UserGrowthRepository userGrowthRepository;

    @Mock
    private EventApplicationRepository eventApplicationRepository;

    @Mock
    private TicketHistoryRepository ticketHistoryRepository;

    @Mock
    private CreditHistoryRepository creditHistoryRepository;
    @Mock
    private RefreshTokenRedisKeyManager refreshTokenRedisKeyManager;
    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @InjectMocks
    private UserAdminServiceImpl userAdminService;

    @Test
    void should_return_participant_list_without_filter() {
        // Given
        User user1 = createUser(1L, "user1@wearagain.kr", "user1", 5, 100);
        User user2 = createUser(2L, "user2@wearagain.kr", "user2", 3, 50);
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(user1, user2), pageable, 2));

        // When
        AdminParticipantListResponse response = userAdminService.getParticipants(
                null,
                "CREATED_DESC",
                null,
                null,
                pageable
        );

        // Then
        assertThat(response.content()).hasSize(2);
        assertThat(response.totalElements()).isEqualTo(2);
        assertThat(response.content().get(0).participantId()).isEqualTo(1L);
        assertThat(response.content().get(1).participantId()).isEqualTo(2L);
    }

    @Test
    void should_throw_invalid_request_when_sort_is_invalid() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When & Then
        assertThatThrownBy(() -> userAdminService.getParticipants(null, "UNKNOWN", null, null, pageable))
                .isInstanceOf(UserException.class)
                .extracting(ex -> ((UserException) ex).getErrorCode())
                .isEqualTo(UserErrorCode.INVALID_REQUEST);
    }

    @Test
    void should_throw_not_found_when_detail_missing() {
        // Given
        when(userRepository.findById(100L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userAdminService.getParticipantDetail(100L))
                .isInstanceOf(UserException.class)
                .extracting(ex -> ((UserException) ex).getErrorCode())
                .isEqualTo(UserErrorCode.USER_NOT_FOUND);
    }

    @Test
    void should_return_feature_not_available_when_update_requested() {
        // Given
        AdminParticipantUpdateRequest request = new AdminParticipantUpdateRequest(
                "new-name",
                "https://img.wearagain.kr/profile/new.png",
                7,
                15,
                true
        );

        // When & Then
        assertThatThrownBy(() -> userAdminService.updateParticipant(5L, request))
                .isInstanceOf(UserException.class)
                .extracting(ex -> ((UserException) ex).getErrorCode())
                .isEqualTo(UserErrorCode.FEATURE_NOT_AVAILABLE);
    }

    @Test
    void should_return_feature_not_available_when_update_requested_with_valid_ticket() {
        // Given
        AdminParticipantUpdateRequest request = new AdminParticipantUpdateRequest(
                null,
                null,
                0,
                0,
                false
        );

        // When & Then
        assertThatThrownBy(() -> userAdminService.updateParticipant(7L, request))
                .isInstanceOf(UserException.class)
                .extracting(ex -> ((UserException) ex).getErrorCode())
                .isEqualTo(UserErrorCode.FEATURE_NOT_AVAILABLE);
    }

    @Test
    void should_update_suspension() {
        // Given
        User user = createUser(8L, "user8@wearagain.kr", "user8", 0, 0);
        when(userRepository.findById(8L)).thenReturn(Optional.of(user));
        AdminParticipantSuspensionRequest request = new AdminParticipantSuspensionRequest(false);

        // When
        AdminParticipantDetailResponse response = userAdminService.updateSuspension(8L, request);

        // Then
        assertThat(response.suspended()).isFalse();
    }

    @Test
    void should_suspend_user_when_requested() {
        // Given
        User user = createUser(9L, "user9@wearagain.kr", "user9", 0, 0);
        when(userRepository.findById(9L)).thenReturn(Optional.of(user));
        when(refreshTokenRedisKeyManager.userRefreshTokenKey(9L)).thenReturn("auth:user:9");
        AdminParticipantSuspensionRequest request = new AdminParticipantSuspensionRequest(true);

        // When
        AdminParticipantDetailResponse response = userAdminService.updateSuspension(9L, request);

        // Then
        assertThat(response.suspended()).isTrue();
        verify(redisTemplate).delete("auth:user:9");
    }

    @Test
    void should_return_stats() {
        // Given
        when(userRepository.count()).thenReturn(3L);
        when(userRepository.sumTicketBalance()).thenReturn(12L);
        when(userRepository.sumCreditBalance()).thenReturn(30L);

        // When
        AdminParticipantStatsResponse response = userAdminService.getParticipantStats();

        // Then
        assertThat(response.totalParticipants()).isEqualTo(3);
        assertThat(response.totalTickets()).isEqualTo(12);
        assertThat(response.totalCredits()).isEqualTo(30);
    }

    private User createUser(Long id, String email, String name, int ticket, int credit) {
        User user = User.create(email, name, null);
        ReflectionTestUtils.setField(user, "id", id);
        ReflectionTestUtils.setField(user, "ticketBalance", ticket);
        ReflectionTestUtils.setField(user, "creditBalance", credit);
        ReflectionTestUtils.setField(user, "suspended", false);
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        ReflectionTestUtils.setField(user, "createdAt", now);
        ReflectionTestUtils.setField(user, "updatedAt", now);
        return user;
    }
}
