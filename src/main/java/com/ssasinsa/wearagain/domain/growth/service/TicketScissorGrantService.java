package com.ssasinsa.wearagain.domain.growth.service;

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
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketScissorGrantService {

    private final EventRepository eventRepository;
    private final TicketHistoryRepository ticketHistoryRepository;
    private final UserRepository userRepository;
    private final UserGrowthRepository userGrowthRepository;
    private final GrowthInitializer growthInitializer;
    private final GrowthCommandService growthCommandService;

    @Transactional
    public void grantScissorsForClosedEvents() {
        closeExpiredEvents();

        List<Event> closedEvents = eventRepository.findByStatusAndScissorGrantedFalse(EventStatus.CLOSED);
        if (closedEvents.isEmpty()) {
            return;
        }

        closedEvents.forEach(this::processEvent);
    }

    private void closeExpiredEvents() {
        List<Event> eventsToClose = eventRepository.findEventsToClose(EventStatus.CLOSED, LocalDate.now());
        eventsToClose.forEach(event -> event.changeStatus(EventStatus.CLOSED));
    }

    private void processEvent(Event event) {
        List<TicketChargeSummary> charges = ticketHistoryRepository.calculateChargedTicketsByEvent(event.getId());

        if (charges.isEmpty()) {
            event.markScissorGrantCompleted(LocalDateTime.now());
            return;
        }

        for (TicketChargeSummary charge : charges) {
            int grantAmount = toPositiveAmount(charge.getTotalCharged());
            if (grantAmount <= 0) {
                continue;
            }

            Long userId = charge.getUserId();
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalStateException("User not found for ticket history aggregation: " + userId));

            growthInitializer.initialize(user);
            UserGrowth userGrowth = userGrowthRepository.findByUserId(userId)
                    .orElseThrow(() -> new IllegalStateException("Growth record missing after initialization for user: " + userId));

            userGrowth.addScissors(grantAmount);
            growthCommandService.recordGrant(user, userGrowth, event, grantAmount, "EVENT_GRANT");
            log.debug("Granted {} magic scissors to user {} for event {}", grantAmount, userId, event.getId());
        }

        event.markScissorGrantCompleted(LocalDateTime.now());
    }

    private int toPositiveAmount(Long totalCharged) {
        if (totalCharged == null) {
            return 0;
        }
        if (totalCharged > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return totalCharged.intValue();
    }
}
