package com.ssasinsa.wearagain.domain.growth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ssasinsa.wearagain.domain.auth.entity.AdminUser;
import com.ssasinsa.wearagain.domain.auth.entity.User;
import com.ssasinsa.wearagain.domain.auth.repository.UserRepository;
import com.ssasinsa.wearagain.domain.event.entity.Event;
import com.ssasinsa.wearagain.domain.event.entity.EventStatus;
import com.ssasinsa.wearagain.domain.event.repository.EventRepository;
import com.ssasinsa.wearagain.domain.finance.repository.TicketHistoryRepository;
import com.ssasinsa.wearagain.domain.finance.repository.TicketHistoryRepository.TicketChargeSummary;
import com.ssasinsa.wearagain.domain.growth.entity.UserGrowth;
import com.ssasinsa.wearagain.domain.growth.repository.UserGrowthRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TicketScissorGrantServiceTest {

    @Mock
    private EventRepository eventRepository;
    @Mock
    private TicketHistoryRepository ticketHistoryRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserGrowthRepository userGrowthRepository;
    @Mock
    private GrowthInitializer growthInitializer;

    private TicketScissorGrantService ticketScissorGrantService;

    @BeforeEach
    void setUp() {
        ticketScissorGrantService = new TicketScissorGrantService(
                eventRepository,
                ticketHistoryRepository,
                userRepository,
                userGrowthRepository,
                growthInitializer
        );
    }

    @Test
    void should_grant_scissors_and_mark_event_completed() {
        Event event = createClosedEvent(1L);
        when(eventRepository.findByStatusAndScissorGrantedFalse(EventStatus.CLOSED)).thenReturn(List.of(event));

        TicketChargeSummary summary = new TestTicketChargeSummary(10L, 3L);
        when(ticketHistoryRepository.calculateChargedTicketsByEvent(1L)).thenReturn(List.of(summary));

        User user = createUser(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        UserGrowth userGrowth = UserGrowth.create(user);
        when(userGrowthRepository.findByUserId(10L)).thenReturn(Optional.of(userGrowth));

        ticketScissorGrantService.grantScissorsForClosedEvents();

        assertThat(userGrowth.getMagicScissorCount()).isEqualTo(3);
        assertThat(event.isScissorGranted()).isTrue();
        assertThat(event.getScissorGrantedAt()).isNotNull();
        verify(growthInitializer).initialize(user);
    }

    @Test
    void should_mark_event_even_when_ticket_history_missing() {
        Event event = createClosedEvent(2L);
        when(eventRepository.findByStatusAndScissorGrantedFalse(EventStatus.CLOSED)).thenReturn(List.of(event));
        when(ticketHistoryRepository.calculateChargedTicketsByEvent(2L)).thenReturn(List.of());

        ticketScissorGrantService.grantScissorsForClosedEvents();

        assertThat(event.isScissorGranted()).isTrue();
        verify(userRepository, never()).findById(anyLong());
    }

    private Event createClosedEvent(Long id) {
        AdminUser admin = AdminUser.createSuperAdmin("admin@example.com", "password", "관리자");
        Event event = Event.create(
                "이벤트",
                "설명",
                LocalDate.now().minusDays(2),
                LocalDate.now().minusDays(1),
                "서울",
                EventStatus.CLOSED,
                admin,
                null,
                null
        );
        ReflectionTestUtils.setField(event, "id", id);
        return event;
    }

    private User createUser(Long id) {
        User user = User.create("user@example.com", "사용자", null);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private static final class TestTicketChargeSummary implements TicketChargeSummary {

        private final Long userId;
        private final Long totalCharged;

        private TestTicketChargeSummary(Long userId, Long totalCharged) {
            this.userId = userId;
            this.totalCharged = totalCharged;
        }

        @Override
        public Long getUserId() {
            return userId;
        }

        @Override
        public Long getTotalCharged() {
            return totalCharged;
        }
    }
}
