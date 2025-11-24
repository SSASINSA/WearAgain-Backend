package com.ssasinsa.wearagain.domain.growth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ssasinsa.wearagain.domain.auth.entity.AdminUser;
import com.ssasinsa.wearagain.domain.auth.entity.User;
import com.ssasinsa.wearagain.domain.auth.repository.UserRepository;
import com.ssasinsa.wearagain.domain.event.entity.Event;
import com.ssasinsa.wearagain.domain.event.entity.EventStatus;
import com.ssasinsa.wearagain.domain.event.repository.EventRepository;
import com.ssasinsa.wearagain.domain.finance.config.ImpactProperties;
import com.ssasinsa.wearagain.domain.finance.entity.ImpactAnalytics;
import com.ssasinsa.wearagain.domain.finance.repository.ImpactAnalyticsRepository;
import com.ssasinsa.wearagain.domain.finance.repository.TicketHistoryRepository;
import com.ssasinsa.wearagain.domain.finance.repository.TicketHistoryRepository.TicketChargeSummary;
import com.ssasinsa.wearagain.domain.growth.entity.UserGrowth;
import com.ssasinsa.wearagain.domain.growth.repository.UserGrowthRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
    @Mock
    private GrowthCommandService growthCommandService;
    @Mock
    private ImpactAnalyticsRepository impactAnalyticsRepository;

    private ImpactProperties impactProperties;

    private TicketScissorGrantService ticketScissorGrantService;

    @BeforeEach
    void setUp() {
        impactProperties = new ImpactProperties(
                new BigDecimal("1.50"),
                new BigDecimal("2.00"),
                new BigDecimal("3.00")
        );
        ticketScissorGrantService = new TicketScissorGrantService(
                eventRepository,
                ticketHistoryRepository,
                userRepository,
                userGrowthRepository,
                growthInitializer,
                growthCommandService,
                impactAnalyticsRepository,
                impactProperties
        );
    }

    @Test
    void should_grant_scissors_and_mark_event_completed() {
        Event event = createEventNeedingClosure(1L);
        when(eventRepository.findEventsToClose(eq(EventStatus.CLOSED), any(LocalDate.class))).thenReturn(List.of(event));
        when(eventRepository.findByStatusAndScissorGrantedFalse(EventStatus.CLOSED)).thenReturn(List.of(event));

        TicketChargeSummary summary = new TestTicketChargeSummary(10L, 3L);
        when(ticketHistoryRepository.calculateChargedTicketsByEvent(1L)).thenReturn(List.of(summary));

        User user = createUser(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(impactAnalyticsRepository.existsByUserIdAndEventId(10L, 1L)).thenReturn(false);

        UserGrowth userGrowth = UserGrowth.create(user);
        when(userGrowthRepository.findByUserId(10L)).thenReturn(Optional.of(userGrowth));

        ticketScissorGrantService.grantScissorsForClosedEvents();

        assertThat(userGrowth.getMagicScissorCount()).isEqualTo(3);
        assertThat(event.getStatus()).isEqualTo(EventStatus.CLOSED);
        assertThat(event.isScissorGranted()).isTrue();
        assertThat(event.getScissorGrantedAt()).isNotNull();
        verify(growthInitializer).initialize(user);
        verify(growthCommandService).recordGrant(user, userGrowth, event, 3, "EVENT_GRANT");
        ArgumentCaptor<ImpactAnalytics> captor = ArgumentCaptor.forClass(ImpactAnalytics.class);
        verify(impactAnalyticsRepository).save(captor.capture());
        ImpactAnalytics saved = captor.getValue();
        assertThat(saved.getCo2Saved()).isEqualByComparingTo("4.500");
        assertThat(saved.getWaterSaved()).isEqualByComparingTo("6.000");
        assertThat(saved.getEnergySaved()).isEqualByComparingTo("9.000");
    }

    @Test
    void should_mark_event_even_when_ticket_history_missing() {
        Event event = createEventNeedingClosure(2L);
        when(eventRepository.findEventsToClose(eq(EventStatus.CLOSED), any(LocalDate.class))).thenReturn(List.of(event));
        when(eventRepository.findByStatusAndScissorGrantedFalse(EventStatus.CLOSED)).thenReturn(List.of(event));
        when(ticketHistoryRepository.calculateChargedTicketsByEvent(2L)).thenReturn(List.of());

        ticketScissorGrantService.grantScissorsForClosedEvents();

        assertThat(event.getStatus()).isEqualTo(EventStatus.CLOSED);
        assertThat(event.isScissorGranted()).isTrue();
        verify(userRepository, never()).findById(anyLong());
        verify(impactAnalyticsRepository, never()).save(any());
    }

    @Test
    void should_skip_saving_impact_when_already_exists() {
        Event event = createEventNeedingClosure(3L);
        when(eventRepository.findEventsToClose(eq(EventStatus.CLOSED), any(LocalDate.class))).thenReturn(List.of(event));
        when(eventRepository.findByStatusAndScissorGrantedFalse(EventStatus.CLOSED)).thenReturn(List.of(event));

        TicketChargeSummary summary = new TestTicketChargeSummary(11L, 5L);
        when(ticketHistoryRepository.calculateChargedTicketsByEvent(3L)).thenReturn(List.of(summary));

        User user = createUser(11L);
        when(userRepository.findById(11L)).thenReturn(Optional.of(user));
        when(userGrowthRepository.findByUserId(11L)).thenReturn(Optional.of(UserGrowth.create(user)));
        when(impactAnalyticsRepository.existsByUserIdAndEventId(11L, 3L)).thenReturn(true);

        ticketScissorGrantService.grantScissorsForClosedEvents();

        verify(impactAnalyticsRepository, never()).save(any());
        assertThat(event.isScissorGranted()).isTrue();
    }

    @Test
    void should_save_zero_impact_when_unit_is_zero() {
        // 단가 0일 때도 저장되지만 값은 0이어야 한다.
        impactProperties = new ImpactProperties(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        ticketScissorGrantService = new TicketScissorGrantService(
                eventRepository,
                ticketHistoryRepository,
                userRepository,
                userGrowthRepository,
                growthInitializer,
                growthCommandService,
                impactAnalyticsRepository,
                impactProperties
        );

        Event event = createEventNeedingClosure(4L);
        when(eventRepository.findEventsToClose(eq(EventStatus.CLOSED), any(LocalDate.class))).thenReturn(List.of(event));
        when(eventRepository.findByStatusAndScissorGrantedFalse(EventStatus.CLOSED)).thenReturn(List.of(event));

        TicketChargeSummary summary = new TestTicketChargeSummary(12L, 2L);
        when(ticketHistoryRepository.calculateChargedTicketsByEvent(4L)).thenReturn(List.of(summary));

        User user = createUser(12L);
        when(userRepository.findById(12L)).thenReturn(Optional.of(user));
        when(userGrowthRepository.findByUserId(12L)).thenReturn(Optional.of(UserGrowth.create(user)));
        when(impactAnalyticsRepository.existsByUserIdAndEventId(12L, 4L)).thenReturn(false);

        ticketScissorGrantService.grantScissorsForClosedEvents();

        ArgumentCaptor<ImpactAnalytics> captor = ArgumentCaptor.forClass(ImpactAnalytics.class);
        verify(impactAnalyticsRepository).save(captor.capture());
        ImpactAnalytics saved = captor.getValue();
        assertThat(saved.getCo2Saved()).isEqualByComparingTo("0.000");
        assertThat(saved.getWaterSaved()).isEqualByComparingTo("0.000");
        assertThat(saved.getEnergySaved()).isEqualByComparingTo("0.000");
    }

    private Event createEventNeedingClosure(Long id) {
        AdminUser admin = AdminUser.createSuperAdmin("admin@example.com", "password", "관리자");
        Event event = Event.create(
                "이벤트",
                "설명",
                LocalDate.now().minusDays(2),
                LocalDate.now().minusDays(1),
                "서울",
                EventStatus.OPEN,
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
