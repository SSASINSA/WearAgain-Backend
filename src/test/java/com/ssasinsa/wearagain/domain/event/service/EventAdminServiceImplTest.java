package com.ssasinsa.wearagain.domain.event.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ssasinsa.wearagain.domain.auth.entity.AdminRole;
import com.ssasinsa.wearagain.domain.auth.entity.AdminUser;
import com.ssasinsa.wearagain.domain.auth.entity.User;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventAdminDetailResponse;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventAdminListResponse;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventAdminSummaryResponse;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventAdminUpdateRequest;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventAdminUpdateRequest.EventAdminImageRequest;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventAdminUpdateRequest.EventAdminOptionRequest;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventApplicationRejectRequest;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventApplicationRejectResponse;
import com.ssasinsa.wearagain.domain.event.entity.Event;
import com.ssasinsa.wearagain.domain.event.entity.EventApplication;
import com.ssasinsa.wearagain.domain.event.entity.EventApplicationStatus;
import com.ssasinsa.wearagain.domain.event.entity.EventImage;
import com.ssasinsa.wearagain.domain.event.entity.EventOption;
import com.ssasinsa.wearagain.domain.event.entity.EventStatus;
import com.ssasinsa.wearagain.domain.event.exception.EventErrorCode;
import com.ssasinsa.wearagain.domain.event.exception.EventException;
import com.ssasinsa.wearagain.domain.event.repository.EventApplicationEventCount;
import com.ssasinsa.wearagain.domain.event.repository.EventApplicationRepository;
import com.ssasinsa.wearagain.domain.event.repository.EventCapacitySummary;
import com.ssasinsa.wearagain.domain.event.repository.EventOptionApplicationCount;
import com.ssasinsa.wearagain.domain.event.repository.EventOptionRepository;
import com.ssasinsa.wearagain.domain.event.repository.EventRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EventAdminServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventOptionRepository eventOptionRepository;

    @Mock
    private EventApplicationRepository eventApplicationRepository;

    @InjectMocks
    private EventAdminServiceImpl eventAdminService;

    private Event event;
    private EventOption option;
    private AdminUser adminUser;

    @BeforeEach
    void setUp() {
        adminUser = AdminUser.createSuperAdmin("admin@wearagain.kr", "encoded", "운영자");
        ReflectionTestUtils.setField(adminUser, "id", 11L);

        event = Event.create(
                "지속가능 패션 워크숍",
                "웨어어게인과 함께하는 리폼 클래스",
                "웨어어게인 운영팀",
                "02-0000-0000",
                LocalDate.of(2025, 11, 10),
                LocalDate.of(2025, 11, 30),
                "서울시 마포구 연남동 223-14 2F",
                EventStatus.OPEN,
                adminUser
        );
        ReflectionTestUtils.setField(event, "id", 101L);

        EventImage image = EventImage.create(event, "https://cdn.wearagain.kr/events/101/main.jpg", "대표", 1);
        ReflectionTestUtils.setField(image, "id", 1001L);

        option = EventOption.create(event, null, "11월 15일", "DATE", 1, null);
        ReflectionTestUtils.setField(option, "id", 2001L);
        EventOption child = EventOption.create(event, option, "A조", "GROUP", 1, 30);
        ReflectionTestUtils.setField(child, "id", 2003L);
    }

    @Test
    void should_list_events_with_statistics() {
        PageImpl<Event> pageResult = new PageImpl<>(List.of(event), PageRequest.of(0, 10), 20);
        when(eventRepository.findByStatusIn(anyCollection(), any(Pageable.class))).thenReturn(pageResult);
        when(eventOptionRepository.sumCapacityByEventIds(anyCollection()))
                .thenReturn(List.of(new EventCapacitySummary(101L, 30L)));
        when(eventApplicationRepository.countActiveApplicationsByEventIds(anyCollection(), anyCollection()))
                .thenReturn(List.of(new EventApplicationEventCount(101L, 20L)));

        EventAdminListResponse response = eventAdminService.getEvents(null, 0, 10);

        assertThat(response.events()).hasSize(1);
        EventAdminSummaryResponse summary = response.events().get(0);
        assertThat(summary.eventId()).isEqualTo(101L);
        assertThat(summary.totalCapacity()).isEqualTo(30L);
        assertThat(summary.appliedCount()).isEqualTo(20L);
        assertThat(summary.remainingCount()).isEqualTo(10L);
        assertThat(summary.organizerAdminId()).isEqualTo(11L);
        assertThat(summary.organizerAdminEmail()).isEqualTo("admin@wearagain.kr");
        assertThat(summary.organizerAdminName()).isEqualTo("운영자");
        assertThat(summary.organizerName()).isEqualTo("웨어어게인 운영팀");
        assertThat(summary.organizerContact()).isEqualTo("02-0000-0000");
        assertThat(response.page()).isEqualTo(0);
        assertThat(response.size()).isEqualTo(10);
        assertThat(response.totalElements()).isEqualTo(20);
        assertThat(response.totalPages()).isEqualTo(2);
        assertThat(response.hasNext()).isTrue();
    }

    @Test
    void should_get_event_detail_with_options_and_applications() {
        when(eventRepository.findById(101L)).thenReturn(java.util.Optional.of(event));
        when(eventOptionRepository.sumCapacityByEventIds(anyCollection()))
                .thenReturn(List.of(new EventCapacitySummary(101L, 30L)));
        when(eventApplicationRepository.countActiveApplicationsByEventIds(anyCollection(), anyCollection()))
                .thenReturn(List.of(new EventApplicationEventCount(101L, 12L)));
        when(eventApplicationRepository.countActiveApplicationsByOptionIds(anyCollection(), anyCollection()))
                .thenReturn(List.of(new EventOptionApplicationCount(2003L, 12L)));

        User user = User.create("user@wearagain.kr", "사용자", null);
        ReflectionTestUtils.setField(user, "id", 10L);
        EventApplication application = EventApplication.create(user, event, option.getChildOptions().get(0), EventApplicationStatus.APPLIED, null, null);
        ReflectionTestUtils.setField(application, "id", 5001L);
        when(eventApplicationRepository.findAllWithUserByEventId(101L)).thenReturn(List.of(application));

        EventAdminDetailResponse response = eventAdminService.getEventDetail(101L);

        assertThat(response.eventId()).isEqualTo(101L);
        assertThat(response.organizerName()).isEqualTo("웨어어게인 운영팀");
        assertThat(response.organizerContact()).isEqualTo("02-0000-0000");
        assertThat(response.organizerAdminId()).isEqualTo(11L);
        assertThat(response.organizerAdminEmail()).isEqualTo("admin@wearagain.kr");
        assertThat(response.organizerAdminName()).isEqualTo("운영자");
        assertThat(response.totalCapacity()).isEqualTo(30L);
        assertThat(response.appliedCount()).isEqualTo(12L);
        assertThat(response.remainingCount()).isEqualTo(18L);
        assertThat(response.images()).hasSize(1);
        assertThat(response.options()).hasSize(1);
        assertThat(response.applications()).hasSize(1);
    }

    @Test
    void should_update_event_basic_info_and_replace_structures() {
        when(eventRepository.findById(101L)).thenReturn(java.util.Optional.of(event));
        when(eventOptionRepository.sumCapacityByEventIds(anyCollection())).thenReturn(List.<EventCapacitySummary>of());
        when(eventApplicationRepository.countActiveApplicationsByEventIds(anyCollection(), anyCollection()))
                .thenReturn(List.<EventApplicationEventCount>of());
        when(eventApplicationRepository.countActiveApplicationsByOptionIds(anyCollection(), anyCollection()))
                .thenReturn(List.<EventOptionApplicationCount>of());
        when(eventApplicationRepository.findAllWithUserByEventId(101L)).thenReturn(List.<EventApplication>of());

        EventAdminUpdateRequest request = new EventAdminUpdateRequest(
                "워크숍 업데이트",
                "설명 업데이트입니다.",
                "서울시 성동구 왕십리로 32",
                "웨어어게인 주최팀",
                "02-1234-5678",
                LocalDate.of(2025, 11, 12),
                LocalDate.of(2025, 12, 1),
                EventStatus.OPEN,
                List.of(new EventAdminImageRequest("https://cdn.wearagain.kr/events/101/main.jpg", "대표", 1)),
                List.of(new EventAdminOptionRequest(
                        "11월 20일",
                        "DATE",
                        1,
                        null,
                        List.of(new EventAdminOptionRequest(
                                "오전 세션",
                                "TIME",
                                1,
                                null,
                                List.of(new EventAdminOptionRequest(
                                        "A조",
                                        "GROUP",
                                        1,
                                        30,
                                        List.of()
                                ))
                        ))
                ))
        );

        EventAdminDetailResponse response = eventAdminService.updateEvent(101L, request, 11L, AdminRole.SUPER_ADMIN);

        assertThat(event.getTitle()).isEqualTo("워크숍 업데이트");
        assertThat(event.getLocation()).isEqualTo("서울시 성동구 왕십리로 32");
        assertThat(event.getOrganizerName()).isEqualTo("웨어어게인 주최팀");
        assertThat(event.getOrganizerContact()).isEqualTo("02-1234-5678");
        assertThat(event.getStartDate()).isEqualTo(LocalDate.of(2025, 11, 12));
        assertThat(response.options()).hasSize(1);
        assertThat(response.options().get(0).children().get(0).children()).hasSize(1);
    }

    @Test
    void should_forbid_manager_updating_other_admin_event() {
        when(eventRepository.findById(101L)).thenReturn(java.util.Optional.of(event));

        EventAdminUpdateRequest request = new EventAdminUpdateRequest(
                "수정",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        assertThatThrownBy(() -> eventAdminService.updateEvent(101L, request, 77L, AdminRole.MANAGER))
                .isInstanceOf(EventException.class)
                .hasFieldOrPropertyWithValue("errorCode", EventErrorCode.EVENT_UPDATE_FORBIDDEN);
    }

    @Test
    void should_change_status_when_super_admin_requests() {
        when(eventRepository.findById(101L)).thenReturn(
                java.util.Optional.of(event),
                java.util.Optional.of(event)
        );
        when(eventOptionRepository.sumCapacityByEventIds(anyCollection())).thenReturn(List.of());
        when(eventApplicationRepository.countActiveApplicationsByEventIds(anyCollection(), anyCollection()))
                .thenReturn(List.of());
        when(eventApplicationRepository.countActiveApplicationsByOptionIds(anyCollection(), anyCollection()))
                .thenReturn(List.of());
        when(eventApplicationRepository.findAllWithUserByEventId(101L)).thenReturn(List.<EventApplication>of());

        EventAdminDetailResponse response = eventAdminService.updateEventStatus(101L, EventStatus.CLOSED, 11L, AdminRole.SUPER_ADMIN);

        assertThat(event.getStatus()).isEqualTo(EventStatus.CLOSED);
        assertThat(response.status()).isEqualTo(EventStatus.CLOSED);
    }

    @Test
    void should_forbid_manager_status_change() {
        when(eventRepository.findById(101L)).thenReturn(java.util.Optional.of(event));

        assertThatThrownBy(() -> eventAdminService.updateEventStatus(101L, EventStatus.CLOSED, 11L, AdminRole.MANAGER))
                .isInstanceOf(EventException.class)
                .hasFieldOrPropertyWithValue("errorCode", EventErrorCode.EVENT_STATUS_UPDATE_FORBIDDEN);
    }

    @Test
    void should_reject_invalid_status_transition() {
        event.changeStatus(EventStatus.ARCHIVED);
        when(eventRepository.findById(101L)).thenReturn(java.util.Optional.of(event));

        assertThatThrownBy(() -> eventAdminService.updateEventStatus(101L, EventStatus.OPEN, 11L, AdminRole.ADMIN))
                .isInstanceOf(EventException.class)
                .hasFieldOrPropertyWithValue("errorCode", EventErrorCode.EVENT_STATUS_UPDATE_INVALID);
    }

    @Test
    void should_archive_event() {
        when(eventRepository.findById(101L)).thenReturn(java.util.Optional.of(event));
        when(eventApplicationRepository.countActiveApplicationsByEventIds(anyCollection(), anyCollection()))
                .thenReturn(List.of());

        eventAdminService.archiveEvent(101L);

        assertThat(event.getStatus()).isEqualTo(EventStatus.ARCHIVED);
    }

    @Test
    void should_reject_application() {
        User user = User.create("user@wearagain.kr", "사용자", null);
        ReflectionTestUtils.setField(user, "id", 10L);
        EventApplication application = EventApplication.create(user, event, option, EventApplicationStatus.APPLIED, null, null);
        ReflectionTestUtils.setField(application, "id", 5001L);
        when(eventApplicationRepository.findById(5001L)).thenReturn(java.util.Optional.of(application));

        EventApplicationRejectResponse response = eventAdminService.rejectApplication(5001L, new EventApplicationRejectRequest("사유"));

        assertThat(response.status()).isEqualTo(EventApplicationStatus.REJECTED.name());
        verify(eventApplicationRepository).findById(5001L);
    }

    @Test
    void should_fail_archive_when_already_archived() {
        event.changeStatus(EventStatus.ARCHIVED);
        when(eventRepository.findById(101L)).thenReturn(java.util.Optional.of(event));

        assertThatThrownBy(() -> eventAdminService.archiveEvent(101L))
                .isInstanceOf(EventException.class)
                .hasFieldOrPropertyWithValue("errorCode", EventErrorCode.EVENT_ALREADY_ARCHIVED);
        verify(eventApplicationRepository, never()).countActiveApplicationsByEventIds(anyCollection(), anyCollection());
    }
}
