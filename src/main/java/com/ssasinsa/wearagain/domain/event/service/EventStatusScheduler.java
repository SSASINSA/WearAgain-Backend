package com.ssasinsa.wearagain.domain.event.service;

import com.ssasinsa.wearagain.domain.event.entity.Event;
import com.ssasinsa.wearagain.domain.event.entity.EventStatus;
import com.ssasinsa.wearagain.domain.event.repository.EventRepository;
import com.ssasinsa.wearagain.domain.growth.service.TicketScissorGrantService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventStatusScheduler {

    private final EventRepository eventRepository;
    private final TicketScissorGrantService ticketScissorGrantService;

    @Scheduled(cron = "0 0 2 * * *", zone = "Asia/Seoul")
    public void runDailyEventStatusJob() {
        openApprovedEvents();
        ticketScissorGrantService.grantScissorsForClosedEvents();
    }

    @Transactional
    public void openApprovedEvents() {
        LocalDate today = LocalDate.now();
        List<Event> eventsToOpen = eventRepository.findApprovedEventsToOpen(EventStatus.APPROVAL, today);
        if (eventsToOpen.isEmpty()) {
            return;
        }
        eventsToOpen.forEach(event -> event.changeStatus(EventStatus.OPEN));
        log.info("Opened {} events scheduled for {}", eventsToOpen.size(), today);
    }
}
