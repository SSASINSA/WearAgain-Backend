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
import com.ssasinsa.wearagain.domain.auth.repository.AdminUserRepository;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventAdminDetailResponse;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventAdminListResponse;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventAdminSummaryResponse;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventAdminUpdateRequest;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventAdminUpdateRequest.EventAdminImageRequest;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventAdminUpdateRequest.EventAdminOptionRequest;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventApplicationRejectRequest;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventApplicationRejectResponse;
import com.ssasinsa.wearagain.domain.event.dto.request.EventCreateRequest;
import com.ssasinsa.wearagain.domain.event.dto.request.EventCreateRequest.EventCreateImageRequest;
import com.ssasinsa.wearagain.domain.event.dto.request.EventCreateRequest.EventCreateOptionRequest;
import com.ssasinsa.wearagain.domain.event.dto.response.EventCreateResponse;
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

    @Mock
    private AdminUserRepository adminUserRepository;

    @InjectMocks
    private EventAdminServiceImpl eventAdminService;

    private Event event;
    private EventOption option;
    private AdminUser adminUser;
    private EventCreateRequest validCreateRequest;

    @BeforeEach
    void setUp() {
        adminUser = AdminUser.createSuperAdmin("admin@wearagain.kr", "encoded", "운영자");
        ReflectionTestUtils.setField(adminUser, "id", 11L);
        when(adminUserRepository.findById(11L)).thenReturn(java.util.Optional.of(adminUser));

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

        validCreateRequest = createValidCreateRequest();
    }

    @Test
    void should_create_event_when_request_is_valid() {
        Event persisted = buildPersistedEvent(validCreateRequest);
        when(eventRepository.save(any(Event.class))).thenReturn(persisted);

        EventCreateResponse response = eventAdminService.createEvent(validCreateRequest, 11L);

        verify(eventRepository).save(any(Event.class));
        assertThat(response.eventId()).isEqualTo(1L);
        assertThat(response.organizerAdminId()).isEqualTo(11L);
        assertThat(response.organizerAdminEmail()).isEqualTo("admin@wearagain.kr");
        assertThat(response.organizerAdminName()).isEqualTo("운영자");
        assertThat(response.organizerName()).isEqualTo(validCreateRequest.organizerName());
        assertThat(response.organizerContact()).isEqualTo(validCreateRequest.organizerContact());
        assertThat(response.images()).hasSize(2);
        assertThat(response.options()).hasSize(2);
        assertThat(response.status()).isEqualTo(EventStatus.DRAFT.name());
    }

    @Test
    void should_fail_create_when_end_date_is_before_start_date() {
        EventCreateRequest request = new EventCreateRequest(
                "테스트 행사",
                "행사 설명입니다.",
                "서울시 마포구",
                "운영자",
                "02-0000-0000",
                LocalDate.now(),
                LocalDate.now().minusDays(1),
                null,
                List.of(new EventCreateImageRequest("https://example.com/1.png", "대표", 1)),
                List.of()
        );

        assertThatThrownBy(() -> eventAdminService.createEvent(request, 11L))
                .isInstanceOf(EventException.class)
                .hasFieldOrPropertyWithValue("errorCode", EventErrorCode.INVALID_EVENT_PERIOD);
    }

    @Test
    void should_fail_create_when_option_depth_exceeds_limit() {
        EventCreateOptionRequest depth4Option = new EventCreateOptionRequest(
                "1차",
                "DATE",
                1,
                null,
                List.of(
                        new EventCreateOptionRequest(
                                "2차",
                                "TIME",
                                1,
                                null,
                                List.of(
                                        new EventCreateOptionRequest(
                                                "3차",
                                                "GROUP",
                                                1,
                                                10,
                                                List.of(
                                                        new EventCreateOptionRequest(
                                                                "4차",
                                                                "GROUP",
                                                                1,
                                                                10,
                                                                List.of()
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );

        EventCreateRequest request = new EventCreateRequest(
                validCreateRequest.title(),
                validCreateRequest.description(),
                validCreateRequest.location(),
                validCreateRequest.organizerName(),
                validCreateRequest.organizerContact(),
                validCreateRequest.startDate(),
                validCreateRequest.endDate(),
                validCreateRequest.status(),
                validCreateRequest.images(),
                List.of(depth4Option)
        );

        assertThatThrownBy(() -> eventAdminService.createEvent(request, 11L))
                .isInstanceOf(EventException.class)
                .hasFieldOrPropertyWithValue("errorCode", EventErrorCode.OPTION_DEPTH_LIMIT_EXCEEDED);
    }

    @Test
    void should_fail_create_when_admin_not_found() {
        when(adminUserRepository.findById(999L)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> eventAdminService.createEvent(validCreateRequest, 999L))
                .isInstanceOf(EventException.class)
                .hasFieldOrPropertyWithValue("errorCode", EventErrorCode.EVENT_ADMIN_NOT_FOUND);
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

    private EventCreateRequest createValidCreateRequest() {
        List<EventCreateImageRequest> images = List.of(
                new EventCreateImageRequest("https://wearagain.kr/1.jpg", "대표", 1),
                new EventCreateImageRequest("https://wearagain.kr/2.jpg", "설명", 2)
        );

        List<EventCreateOptionRequest> options = List.of(
                new EventCreateOptionRequest(
                        "11월 15일",
                        "DATE",
                        1,
                        null,
                        List.of(
                                new EventCreateOptionRequest(
                                        "오전 세션",
                                        "TIME",
                                        1,
                                        null,
                                        List.of(
                                                new EventCreateOptionRequest(
                                                        "A조",
                                                        "GROUP",
                                                        1,
                                                        10,
                                                        List.of()
                                                )
                                        )
                                )
                        )
                ),
                new EventCreateOptionRequest(
                        "11월 22일",
                        "DATE",
                        2,
                        null,
                        List.of()
                )
        );

        return new EventCreateRequest(
                "지속가능 패션 행사",
                "재사용 패션 실습을 진행합니다.",
                "서울시 마포구 연남동",
                "웨어어게인 운영팀",
                "02-0000-0000",
                LocalDate.of(2025, 11, 10),
                LocalDate.of(2025, 11, 30),
                EventStatus.DRAFT,
                images,
                options
        );
    }

    private Event buildPersistedEvent(EventCreateRequest request) {
        Event event = Event.create(
                request.title(),
                request.description(),
                request.organizerName(),
                request.organizerContact(),
                request.startDate(),
                request.endDate(),
                request.location(),
                request.status() == null ? EventStatus.DRAFT : request.status(),
                adminUser
        );
        ReflectionTestUtils.setField(event, "id", 1L);

        List<EventImage> images = request.images().stream()
                .map(imageRequest -> {
                    EventImage image = EventImage.create(event, imageRequest.url(), imageRequest.altText(), imageRequest.displayOrder());
                    ReflectionTestUtils.setField(image, "id", image.getDisplayOrder() == 1 ? 1001L : 1002L);
                    return image;
                })
                .toList();

        List<EventOption> options = request.options().stream()
                .map(optionRequest -> buildPersistedOptionTree(event, null, optionRequest, 2000L))
                .toList();

        event.assignImages(images);
        event.assignOptions(options);
        return event;
    }

    private EventOption buildPersistedOptionTree(
            Event event,
            EventOption parent,
            EventCreateOptionRequest request,
            long baseId
    ) {
        EventOption option = EventOption.create(
                event,
                parent,
                request.name(),
                request.type(),
                request.displayOrder(),
                request.capacity()
        );
        ReflectionTestUtils.setField(option, "id", baseId + request.displayOrder());

        if (request.children() != null && !request.children().isEmpty()) {
            List<EventOption> children = request.children().stream()
                    .map(child -> buildPersistedOptionTree(event, option, child, baseId + 10))
                    .toList();
            option.assignChildren(children);
        }
        return option;
    }
}
