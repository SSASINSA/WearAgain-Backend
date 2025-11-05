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

import com.ssasinsa.wearagain.domain.auth.entity.AdminUser;
import com.ssasinsa.wearagain.domain.auth.entity.User;
import com.ssasinsa.wearagain.domain.auth.repository.UserRepository;
import com.ssasinsa.wearagain.domain.event.dto.request.EventApplyRequest;
import com.ssasinsa.wearagain.domain.event.dto.request.EventCancelRequest;
import com.ssasinsa.wearagain.domain.event.dto.response.EventApplyResponse;
import com.ssasinsa.wearagain.domain.event.dto.response.EventCancelResponse;
import com.ssasinsa.wearagain.domain.event.dto.response.EventDetailResponse;
import com.ssasinsa.wearagain.domain.event.dto.response.EventListResponse;
import com.ssasinsa.wearagain.domain.event.entity.Event;
import com.ssasinsa.wearagain.domain.event.entity.EventApplication;
import com.ssasinsa.wearagain.domain.event.entity.EventApplicationStatus;
import com.ssasinsa.wearagain.domain.event.entity.EventImage;
import com.ssasinsa.wearagain.domain.event.entity.EventOption;
import com.ssasinsa.wearagain.domain.event.entity.EventStatus;
import com.ssasinsa.wearagain.domain.event.exception.EventErrorCode;
import com.ssasinsa.wearagain.domain.event.exception.EventException;
import com.ssasinsa.wearagain.domain.event.repository.EventApplicationRepository;
import com.ssasinsa.wearagain.domain.event.repository.EventOptionApplicationCount;
import com.ssasinsa.wearagain.domain.event.repository.EventOptionRepository;
import com.ssasinsa.wearagain.domain.event.repository.EventRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class EventUserServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventOptionRepository eventOptionRepository;

    @Mock
    private EventApplicationRepository eventApplicationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EventUserServiceImpl eventUserService;

    private Event openEvent;
    private EventOption groupOption;
    private AdminUser adminUser;

    @BeforeEach
    void setUp() {
        adminUser = AdminUser.createSuperAdmin("admin@wearagain.kr", "encoded", "운영자");
        ReflectionTestUtils.setField(adminUser, "id", 9L);

        openEvent = Event.create(
                "지속가능 패션 워크숍",
                "웨어어게인과 함께하는 리폼 클래스",
                LocalDate.of(2025, 11, 10),
                LocalDate.of(2025, 11, 30),
                "서울시 마포구 연남동",
                EventStatus.OPEN,
                adminUser
        );
        ReflectionTestUtils.setField(openEvent, "id", 101L);

        EventImage image = EventImage.create(openEvent, "https://cdn.wearagain.kr/events/101/main.jpg", "대표", 1);
        ReflectionTestUtils.setField(image, "id", 1001L);

        EventOption dateOption = EventOption.create(openEvent, null, "11월 15일", "DATE", 1, null);
        ReflectionTestUtils.setField(dateOption, "id", 2001L);
        groupOption = EventOption.create(openEvent, dateOption, "A조", "GROUP", 1, 10);
        ReflectionTestUtils.setField(groupOption, "id", 2003L);
    }

    @Test
    void should_list_events_with_default_status() {
        when(eventRepository.findEventsAfterCursor(anyCollection(), eq(null), any(Pageable.class)))
                .thenReturn(List.of(openEvent));

        EventListResponse response = eventUserService.getEvents(null, null, 10);

        assertThat(response.events()).hasSize(1);
        assertThat(response.events().get(0).eventId()).isEqualTo(101L);
        assertThat(response.hasNext()).isFalse();
        assertThat(response.nextCursor()).isNull();
        verify(eventRepository).findEventsAfterCursor(anyCollection(), eq(null), any(Pageable.class));
    }

    @Test
    void should_return_next_cursor_when_more_results() {
        Event second = Event.create(
                "환경 보호 토크 콘서트",
                "지속 가능한 일상을 주제로 한 토크 콘서트입니다.",
                LocalDate.of(2025, 11, 20),
                LocalDate.of(2025, 11, 20),
                "서울시 성동구",
                EventStatus.OPEN,
                adminUser
        );
        ReflectionTestUtils.setField(second, "id", 150L);

        when(eventRepository.findEventsAfterCursor(anyCollection(), eq(null), any(Pageable.class)))
                .thenReturn(List.of(openEvent, second));

        EventListResponse response = eventUserService.getEvents(null, null, 1);

        assertThat(response.events()).hasSize(1);
        assertThat(response.hasNext()).isTrue();
        assertThat(response.nextCursor()).isEqualTo("101");
    }

    @Test
    void should_throw_invalid_query_when_status_is_invalid() {
        assertThatThrownBy(() -> eventUserService.getEvents("INVALID", null, 10))
                .isInstanceOf(EventException.class)
                .hasFieldOrPropertyWithValue("errorCode", EventErrorCode.INVALID_EVENT_QUERY);
    }

    @Test
    void should_return_event_detail() {
        when(eventRepository.findById(101L)).thenReturn(Optional.of(openEvent));
        when(eventApplicationRepository.countActiveApplicationsByOptionIds(anyCollection(), anyCollection()))
                .thenReturn(List.of(new EventOptionApplicationCount(2003L, 7L)));

        EventDetailResponse response = eventUserService.getEventDetail(101L);

        assertThat(response.eventId()).isEqualTo(101L);
        assertThat(response.organizerName()).isEqualTo(adminUser.getName());
        assertThat(response.organizerContact()).isEqualTo(adminUser.getEmail());
        assertThat(response.images()).hasSize(1);
        assertThat(response.options()).hasSize(1);
        EventDetailResponse.EventDetailOptionResponse leaf = response.options().get(0).children().get(0);
        assertThat(leaf.appliedCount()).isEqualTo(7);
        assertThat(leaf.remainingCount()).isEqualTo(3);
    }

    @Test
    void should_throw_not_found_when_event_hidden() {
        Event draft = Event.create(
                "Draft",
                "숨김",
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                "서울시",
                EventStatus.DRAFT,
                adminUser
        );
        ReflectionTestUtils.setField(draft, "id", 999L);
        when(eventRepository.findById(999L)).thenReturn(Optional.of(draft));

        assertThatThrownBy(() -> eventUserService.getEventDetail(999L))
                .isInstanceOf(EventException.class)
                .hasFieldOrPropertyWithValue("errorCode", EventErrorCode.EVENT_NOT_FOUND);
        verify(eventApplicationRepository, never()).countActiveApplicationsByOptionIds(anyCollection(), anyCollection());
    }

    @Test
    void should_apply_when_user_and_capacity_available() {
        User user = User.create("user@wearagain.kr", "사용자", null);
        ReflectionTestUtils.setField(user, "id", 10L);

        when(eventRepository.findById(101L)).thenReturn(Optional.of(openEvent));
        when(eventOptionRepository.findByIdAndEventId(2003L, 101L)).thenReturn(Optional.of(groupOption));
        when(eventApplicationRepository.existsByUserIdAndEventIdAndStatusIn(anyLong(), anyLong(), anyCollection()))
                .thenReturn(false);
        when(eventApplicationRepository.countByEventOptionIdAndStatusIn(anyLong(), anyCollection())).thenReturn(5L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        EventApplication saved = EventApplication.create(user, openEvent, groupOption, EventApplicationStatus.APPLIED, null, null);
        ReflectionTestUtils.setField(saved, "id", 5001L);
        when(eventApplicationRepository.save(any(EventApplication.class))).thenReturn(saved);

        EventApplyResponse response = eventUserService.apply(101L, new EventApplyRequest(2003L, " 메모 "), 10L);

        assertThat(response.applicationId()).isEqualTo(5001L);
        assertThat(response.status()).isEqualTo(EventApplicationStatus.APPLIED.name());
        verify(eventApplicationRepository).save(any(EventApplication.class));
    }

    @Test
    void should_fail_apply_when_already_applied() {
        when(eventRepository.findById(101L)).thenReturn(Optional.of(openEvent));
        when(eventOptionRepository.findByIdAndEventId(2003L, 101L)).thenReturn(Optional.of(groupOption));
        when(eventApplicationRepository.existsByUserIdAndEventIdAndStatusIn(anyLong(), anyLong(), anyCollection()))
                .thenReturn(true);

        assertThatThrownBy(() -> eventUserService.apply(101L, new EventApplyRequest(2003L, null), 10L))
                .isInstanceOf(EventException.class)
                .hasFieldOrPropertyWithValue("errorCode", EventErrorCode.EVENT_ALREADY_APPLIED);
    }

    @Test
    void should_cancel_application() {
        User user = User.create("user@wearagain.kr", "사용자", null);
        ReflectionTestUtils.setField(user, "id", 10L);

        EventApplication application = EventApplication.create(
                user,
                openEvent,
                groupOption,
                EventApplicationStatus.APPLIED,
                null,
                null
        );
        ReflectionTestUtils.setField(application, "id", 5001L);

        when(eventApplicationRepository.findByIdAndUserId(5001L, 10L)).thenReturn(Optional.of(application));

        EventCancelResponse response = eventUserService.cancel(5001L, new EventCancelRequest(" 사유 "), 10L);

        assertThat(response.status()).isEqualTo(EventApplicationStatus.CANCELED.name());
        assertThat(application.getStatus()).isEqualTo(EventApplicationStatus.CANCELED);
        assertThat(application.getReason()).isEqualTo("사유");
    }

    @Test
    void should_fail_cancel_when_not_applied() {
        User user = User.create("user@wearagain.kr", "사용자", null);
        EventApplication application = EventApplication.create(
                user,
                openEvent,
                groupOption,
                EventApplicationStatus.CANCELED,
                null,
                null
        );
        ReflectionTestUtils.setField(application, "id", 5001L);
        when(eventApplicationRepository.findByIdAndUserId(5001L, 10L)).thenReturn(Optional.of(application));

        assertThatThrownBy(() -> eventUserService.cancel(5001L, new EventCancelRequest(null), 10L))
                .isInstanceOf(EventException.class)
                .hasFieldOrPropertyWithValue("errorCode", EventErrorCode.EVENT_APPLICATION_NOT_CANCELABLE);
    }
}
