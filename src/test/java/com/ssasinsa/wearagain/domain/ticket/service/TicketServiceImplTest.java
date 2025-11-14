package com.ssasinsa.wearagain.domain.ticket.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ssasinsa.wearagain.domain.auth.entity.User;
import com.ssasinsa.wearagain.domain.auth.repository.UserRepository;
import com.ssasinsa.wearagain.domain.ticket.dto.TicketQrResponse;
import com.ssasinsa.wearagain.domain.ticket.exception.TicketErrorCode;
import com.ssasinsa.wearagain.domain.ticket.exception.TicketException;
import com.ssasinsa.wearagain.domain.ticket.support.TicketQrTokenPayload;
import com.ssasinsa.wearagain.global.exception.CommonErrorCode;
import com.ssasinsa.wearagain.global.exception.CustomException;
import com.ssasinsa.wearagain.global.common.qr.QrTokenStore;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private QrTokenStore<TicketQrTokenPayload> ticketQrTokenStore;

    private TicketService ticketService;

    @BeforeEach
    void setUp() {
        ticketService = new TicketServiceImpl(userRepository, ticketQrTokenStore);
    }

    @Test
    void should_return_new_token_when_no_cached_token_exists() {
        // Given
        User user = User.create("user@wearagain.kr", "사용자", null);
        ReflectionTestUtils.setField(user, "id", 1L);
        ReflectionTestUtils.setField(user, "ticketBalance", 3);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(ticketQrTokenStore.getTokenByUser(1L)).thenReturn(Optional.empty());
        when(ticketQrTokenStore.generateToken()).thenReturn("new-token");

        // When
        TicketQrResponse response = ticketService.getTicketQr(1L);

        // Then
        assertThat(response.ticketCount()).isEqualTo(3);
        assertThat(response.ticketToken()).isEqualTo("new-token");
        assertThat(response.ticketTokenExpiresIn()).isEqualTo(900);
    }

    @Test
    void should_reuse_token_when_ttl_is_sufficient() {
        // Given
        User user = User.create("user@wearagain.kr", "사용자", null);
        ReflectionTestUtils.setField(user, "id", 1L);
        ReflectionTestUtils.setField(user, "ticketBalance", 4);

        TicketQrTokenPayload payload = new TicketQrTokenPayload(
                user.getId(),
                "existing-token",
                4,
                OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(1),
                OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(14)
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(ticketQrTokenStore.getTokenByUser(1L)).thenReturn(Optional.of(payload));
        when(ticketQrTokenStore.getRemainingTtlByToken("existing-token")).thenReturn(Optional.of(120L));

        // When
        TicketQrResponse response = ticketService.getTicketQr(1L);

        // Then
        assertThat(response.ticketToken()).isEqualTo("existing-token");
        assertThat(response.ticketTokenExpiresIn()).isEqualTo(120);
        verify(ticketQrTokenStore, never()).saveToken(ArgumentMatchers.anyLong(), ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    void should_throw_when_ticket_balance_is_zero() {
        // Given
        User user = User.create("user@wearagain.kr", "사용자", null);
        ReflectionTestUtils.setField(user, "id", 1L);
        ReflectionTestUtils.setField(user, "ticketBalance", 0);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // When & Then
        assertThatThrownBy(() -> ticketService.getTicketQr(1L))
                .isInstanceOf(TicketException.class)
                .extracting(throwable -> ((TicketException) throwable).getErrorCode())
                .isEqualTo(TicketErrorCode.TICKET_BALANCE_EMPTY);
    }

    @Test
    void should_throw_when_user_not_found() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> ticketService.getTicketQr(1L))
                .isInstanceOf(CustomException.class)
                .extracting(throwable -> ((CustomException) throwable).getErrorCode())
                .isEqualTo(CommonErrorCode.UNAUTHORIZED);
    }

    @Test
    void should_throw_when_store_fails_on_save() {
        // Given
        User user = User.create("user@wearagain.kr", "사용자", null);
        ReflectionTestUtils.setField(user, "id", 1L);
        ReflectionTestUtils.setField(user, "ticketBalance", 2);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(ticketQrTokenStore.getTokenByUser(1L)).thenReturn(Optional.empty());
        when(ticketQrTokenStore.generateToken()).thenReturn("new-token");
        doThrow(new IllegalStateException("redis"))
                .when(ticketQrTokenStore)
                .saveToken(ArgumentMatchers.eq(1L), ArgumentMatchers.any(), ArgumentMatchers.any());

        // When & Then
        assertThatThrownBy(() -> ticketService.getTicketQr(1L))
                .isInstanceOf(TicketException.class)
                .extracting(throwable -> ((TicketException) throwable).getErrorCode())
                .isEqualTo(TicketErrorCode.TICKET_QR_TOKEN_STORE_FAILED);
    }
}
