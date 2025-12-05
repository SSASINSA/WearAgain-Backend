package com.ssasinsa.wearagain.domain.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.ssasinsa.wearagain.domain.auth.entity.AdminRole;
import com.ssasinsa.wearagain.domain.dashboard.entity.DashboardSnapshot;
import com.ssasinsa.wearagain.domain.dashboard.repository.DashboardSnapshotRepository;
import com.ssasinsa.wearagain.domain.event.entity.EventStatus;
import com.ssasinsa.wearagain.domain.event.repository.EventApplicationRepository;
import com.ssasinsa.wearagain.domain.event.repository.EventRepository;
import com.ssasinsa.wearagain.domain.finance.repository.ImpactAnalyticsRepository;
import com.ssasinsa.wearagain.domain.finance.repository.TicketHistoryRepository;
import com.ssasinsa.wearagain.domain.growth.dto.ImpactSummary;
import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DashboardAggregationServiceTest {

    @Mock
    private EventRepository eventRepository;
    @Mock
    private EventApplicationRepository eventApplicationRepository;
    @Mock
    private TicketHistoryRepository ticketHistoryRepository;
    @Mock
    private ImpactAnalyticsRepository impactAnalyticsRepository;
    @Mock
    private DashboardSnapshotRepository dashboardSnapshotRepository;

    @InjectMocks
    private DashboardAggregationService dashboardAggregationService;

    @BeforeEach
    void setUpMocks() {
        when(eventRepository.countByStatusIn(EnumSet.of(EventStatus.OPEN, EventStatus.CLOSED))).thenReturn(10L);
        when(eventRepository.countByOrganizerRole(AdminRole.MANAGER)).thenReturn(4L);
        when(eventApplicationRepository.countCheckedIn()).thenReturn(25L);
        when(ticketHistoryRepository.sumPositiveAmounts()).thenReturn(Optional.of(100L));
        when(ticketHistoryRepository.sumNegativeAmountsAbs()).thenReturn(Optional.of(40L));
        when(impactAnalyticsRepository.aggregateTotalImpact()).thenReturn(new ImpactSummary(
                new BigDecimal("12.5"),
                new BigDecimal("3.2"),
                new BigDecimal("7.8")
        ));
        when(dashboardSnapshotRepository.save(any(DashboardSnapshot.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @DisplayName("집계 시 스냅샷이 생성되고 계산 값이 반영된다")
    @Test
    void should_aggregate_snapshot() {
        DashboardSnapshot snapshot = dashboardAggregationService.aggregate();

        assertThat(snapshot.getEventTotalOpenClosed()).isEqualTo(10L);
        assertThat(snapshot.getEventManagerHosted()).isEqualTo(4L);
        assertThat(snapshot.getParticipantsCheckedIn()).isEqualTo(25L);
        assertThat(snapshot.getTicketsCharged()).isEqualTo(100L);
        assertThat(snapshot.getTicketsUsed()).isEqualTo(40L);
        assertThat(snapshot.getExchangeRate()).isEqualTo(new BigDecimal("0.4000"));
        assertThat(snapshot.getImpactCo2Saved()).isEqualTo(new BigDecimal("12.5"));
        assertThat(snapshot.getImpactWaterSaved()).isEqualTo(new BigDecimal("3.2"));
        assertThat(snapshot.getImpactEnergySaved()).isEqualTo(new BigDecimal("7.8"));
    }
}
