package com.ssasinsa.wearagain.domain.event.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ssasinsa.wearagain.domain.auth.entity.AdminRole;
import com.ssasinsa.wearagain.domain.auth.entity.AdminUser;
import com.ssasinsa.wearagain.domain.event.entity.Event;
import com.ssasinsa.wearagain.domain.event.entity.EventStatus;
import com.ssasinsa.wearagain.domain.event.repository.EventRepository;
import com.ssasinsa.wearagain.domain.growth.service.TicketScissorGrantService;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class EventStatusSchedulerTest {

    @Mock
    private EventRepository eventRepository;
    @Mock
    private TicketScissorGrantService ticketScissorGrantService;

    @InjectMocks
    private EventStatusScheduler eventStatusScheduler;

    private Event event;

    @BeforeEach
    void setUp() {
        AdminUser organizer = AdminUser.createApproved("manager@wearagain.kr", "encoded", "매니저", AdminRole.MANAGER);
        ReflectionTestUtils.setField(organizer, "id", 10L);
        event = Event.create(
                "업사이클링 워크숍",
                "단순 설명",
                LocalDate.now(),
                LocalDate.now().plusDays(3),
                "서울 성동구",
                EventStatus.APPROVAL,
                organizer,
                "유의사항",
                "주의사항",
                1
        );
    }

    @Test
    void should_open_events_when_start_date_arrives() {
        when(eventRepository.findApprovedEventsToOpen(eq(EventStatus.APPROVAL), any(LocalDate.class)))
                .thenReturn(List.of(event));

        eventStatusScheduler.openApprovedEvents();

        assertThat(event.getStatus()).isEqualTo(EventStatus.OPEN);
        verify(eventRepository).findApprovedEventsToOpen(eq(EventStatus.APPROVAL), any(LocalDate.class));
    }

    @Test
    void should_skip_when_no_events_need_to_open() {
        when(eventRepository.findApprovedEventsToOpen(eq(EventStatus.APPROVAL), any(LocalDate.class)))
                .thenReturn(List.of());

        eventStatusScheduler.openApprovedEvents();

        assertThat(event.getStatus()).isEqualTo(EventStatus.APPROVAL);
        verify(eventRepository).findApprovedEventsToOpen(eq(EventStatus.APPROVAL), any(LocalDate.class));
    }

    @Test
    void should_execute_grant_job_after_opening_events() {
        when(eventRepository.findApprovedEventsToOpen(eq(EventStatus.APPROVAL), any(LocalDate.class)))
                .thenReturn(List.of());

        eventStatusScheduler.runDailyEventStatusJob();

        verify(eventRepository).findApprovedEventsToOpen(eq(EventStatus.APPROVAL), any(LocalDate.class));
        verify(ticketScissorGrantService).grantScissorsForClosedEvents();
    }
}
