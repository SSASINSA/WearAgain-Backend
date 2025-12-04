package com.ssasinsa.wearagain.domain.dashboard.service;

import com.ssasinsa.wearagain.domain.dashboard.entity.DashboardSnapshot;
import com.ssasinsa.wearagain.domain.dashboard.repository.DashboardSnapshotRepository;
import com.ssasinsa.wearagain.domain.event.entity.EventStatus;
import com.ssasinsa.wearagain.domain.event.repository.EventApplicationRepository;
import com.ssasinsa.wearagain.domain.event.repository.EventRepository;
import com.ssasinsa.wearagain.domain.finance.repository.ImpactAnalyticsRepository;
import com.ssasinsa.wearagain.domain.finance.repository.TicketHistoryRepository;
import com.ssasinsa.wearagain.domain.auth.entity.AdminRole;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumSet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardAggregationService {

    private static final EnumSet<EventStatus> OPEN_OR_CLOSED = EnumSet.of(EventStatus.OPEN, EventStatus.CLOSED);
    private final EventRepository eventRepository;
    private final EventApplicationRepository eventApplicationRepository;
    private final TicketHistoryRepository ticketHistoryRepository;
    private final ImpactAnalyticsRepository impactAnalyticsRepository;
    private final DashboardSnapshotRepository dashboardSnapshotRepository;

    @Transactional
    public DashboardSnapshot aggregate() {
        long eventTotalOpenClosed = eventRepository.countByStatusIn(OPEN_OR_CLOSED);
        long eventManagerHosted = eventRepository.countByOrganizerRole(AdminRole.MANAGER);
        long participantsCheckedIn = eventApplicationRepository.countCheckedIn();

        long ticketsCharged = ticketHistoryRepository.sumPositiveAmounts().orElse(0L);
        long ticketsUsed = ticketHistoryRepository.sumNegativeAmountsAbs().orElse(0L);
        BigDecimal exchangeRate = computeExchangeRate(ticketsUsed, ticketsCharged);

        var impact = impactAnalyticsRepository.aggregateTotalImpact();
        BigDecimal co2 = impact == null ? BigDecimal.ZERO : impact.co2Saved();
        BigDecimal water = impact == null ? BigDecimal.ZERO : impact.waterSaved();
        BigDecimal energy = impact == null ? BigDecimal.ZERO : impact.energySaved();

        DashboardSnapshot snapshot = DashboardSnapshot.create(
                eventTotalOpenClosed,
                eventManagerHosted,
                participantsCheckedIn,
                ticketsCharged,
                ticketsUsed,
                exchangeRate,
                co2,
                water,
                energy
        );
        return dashboardSnapshotRepository.save(snapshot);
    }

    private BigDecimal computeExchangeRate(long used, long charged) {
        if (charged <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(used)
                .divide(BigDecimal.valueOf(charged), 4, RoundingMode.HALF_UP);
    }
}
