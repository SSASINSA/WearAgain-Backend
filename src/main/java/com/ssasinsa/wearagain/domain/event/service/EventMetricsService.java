package com.ssasinsa.wearagain.domain.event.service;

import com.ssasinsa.wearagain.domain.event.dto.manager.EventMetricResponse;
import com.ssasinsa.wearagain.domain.event.entity.Event;
import com.ssasinsa.wearagain.domain.event.entity.EventStatus;
import com.ssasinsa.wearagain.domain.event.repository.EventApplicationMetrics;
import com.ssasinsa.wearagain.domain.event.repository.EventApplicationRepository;
import com.ssasinsa.wearagain.domain.event.repository.EventRepository;
import com.ssasinsa.wearagain.domain.finance.repository.TicketHistoryEventSum;
import com.ssasinsa.wearagain.domain.finance.repository.TicketHistoryRepository;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventMetricsService {

    private static final EnumSet<EventStatus> TARGET_STATUSES = EnumSet.of(EventStatus.OPEN, EventStatus.CLOSED);

    private final EventRepository eventRepository;
    private final EventApplicationRepository eventApplicationRepository;
    private final TicketHistoryRepository ticketHistoryRepository;

    @Transactional(readOnly = true)
    public List<EventMetricResponse> getEventMetrics(LocalDate fromDate) {
        List<Event> events = eventRepository.findByStartDateGreaterThanEqualAndStatusIn(fromDate, TARGET_STATUSES);
        if (events.isEmpty()) {
            return List.of();
        }
        List<Long> eventIds = events.stream().map(Event::getId).toList();

        Map<Long, Long> participants = eventApplicationRepository.countCheckedInByEventIds(eventIds)
                .stream()
                .collect(Collectors.toMap(EventApplicationMetrics::eventId, EventApplicationMetrics::checkedInCount));

        Map<Long, Long> donated = ticketHistoryRepository.sumPositiveAmountsByEventIds(eventIds)
                .stream()
                .collect(Collectors.toMap(TicketHistoryEventSum::eventId, TicketHistoryEventSum::amount));

        Map<Long, Long> exchanged = ticketHistoryRepository.sumNegativeAmountsAbsByEventIds(eventIds)
                .stream()
                .collect(Collectors.toMap(TicketHistoryEventSum::eventId, TicketHistoryEventSum::amount));

        return events.stream()
                .sorted(Comparator.comparing(Event::getStartDate).thenComparing(Event::getId))
                .map(event -> new EventMetricResponse(
                        event.getId(),
                        event.getTitle(),
                        event.getStartDate(),
                        event.getEndDate(),
                        participants.getOrDefault(event.getId(), 0L),
                        donated.getOrDefault(event.getId(), 0L),
                        exchanged.getOrDefault(event.getId(), 0L)
                ))
                .toList();
    }
}
