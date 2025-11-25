package com.ssasinsa.wearagain.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.ssasinsa.wearagain.domain.auth.entity.User;
import com.ssasinsa.wearagain.domain.auth.repository.UserRepository;
import com.ssasinsa.wearagain.domain.finance.repository.TicketHistoryRepository;
import com.ssasinsa.wearagain.domain.user.dto.UserSummaryResponse;
import com.ssasinsa.wearagain.global.exception.CommonErrorCode;
import com.ssasinsa.wearagain.global.exception.CustomException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserSummaryServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TicketHistoryRepository ticketHistoryRepository;

    private UserSummaryService userSummaryService;

    @BeforeEach
    void setUp() {
        userSummaryService = new UserSummaryServiceImpl(userRepository, ticketHistoryRepository);
    }

    @Test
    void should_return_ticket_and_credit_balance() {
        // Given
        User user = User.create("user@wearagain.kr", "사용자", null);
        ReflectionTestUtils.setField(user, "id", 1L);
        ReflectionTestUtils.setField(user, "ticketBalance", 5);
        ReflectionTestUtils.setField(user, "creditBalance", 200);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(ticketHistoryRepository.sumChangeAmountByUserId(1L)).thenReturn(15L);

        // When
        UserSummaryResponse response = userSummaryService.getUserSummary(1L);

        // Then
        assertThat(response.displayName()).isEqualTo("사용자");
        assertThat(response.ticketBalance()).isEqualTo(5);
        assertThat(response.creditBalance()).isEqualTo(200);
        assertThat(response.totalTicketChangeAmount()).isEqualTo(15);
    }

    @Test
    void should_throw_when_user_not_found() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userSummaryService.getUserSummary(1L))
                .isInstanceOf(CustomException.class)
                .extracting(throwable -> ((CustomException) throwable).getErrorCode())
                .isEqualTo(CommonErrorCode.UNAUTHORIZED);
    }
}
