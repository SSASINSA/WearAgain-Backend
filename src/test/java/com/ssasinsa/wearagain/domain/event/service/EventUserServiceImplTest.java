package com.ssasinsa.wearagain.domain.event.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.ssasinsa.wearagain.domain.auth.entity.AdminUser;
import com.ssasinsa.wearagain.domain.auth.entity.User;
import com.ssasinsa.wearagain.domain.event.dto.response.EventApplicationDetailResponse;
import com.ssasinsa.wearagain.domain.event.entity.Event;
import com.ssasinsa.wearagain.domain.event.entity.EventApplication;
import com.ssasinsa.wearagain.domain.event.entity.EventApplicationStatus;
import com.ssasinsa.wearagain.domain.event.entity.EventOption;
import com.ssasinsa.wearagain.domain.event.entity.EventStatus;
import com.ssasinsa.wearagain.domain.event.exception.EventErrorCode;
import com.ssasinsa.wearagain.domain.event.exception.EventException;
import com.ssasinsa.wearagain.domain.event.repository.EventApplicationRepository;
import com.ssasinsa.wearagain.domain.event.repository.EventOptionRepository;
import com.ssasinsa.wearagain.domain.event.repository.EventRepository;
import com.ssasinsa.wearagain.domain.event.support.CheckinTokenUtil;
import com.ssasinsa.wearagain.global.exception.CommonErrorCode;
import com.ssasinsa.wearagain.global.exception.CustomException;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
    private com.ssasinsa.wearagain.domain.auth.repository.UserRepository userRepository;

    @Mock
    private CheckinTokenUtil checkinTokenUtil;

    private EventUserServiceImpl eventUserService;

    @BeforeEach
    void setUp() {
        eventUserService = new EventUserServiceImpl(
                eventRepository,
                eventOptionRepository,
                eventApplicationRepository,
                userRepository,
                checkinTokenUtil
        );
    }

    @Test
    void should_return_application_detail_when_owner_requests() {
        // Given
        Long userId = 1L;
        Long applicationId = 100L;

        User user = User.create("user@wearagain.kr", "사용자", null);
        ReflectionTestUtils.setField(user, "id", userId);

        AdminUser admin = AdminUser.createSuperAdmin("admin@wearagain.kr", "encoded", "관리자");
        ReflectionTestUtils.setField(admin, "id", 10L);

        Event event = Event.create(
                "업사이클링 클래스",
                "업사이클링 수업",
                LocalDate.of(2025, 2, 10),
                LocalDate.of(2025, 2, 11),
                "서울시 마포구",
                EventStatus.OPEN,
                admin,
                "현장에는 개인 텀블러를 지참해주세요.",
                "화재 예방을 위해 지정된 구역에서만 작업해주세요."
        );
        ReflectionTestUtils.setField(event, "id", 45L);

        EventOption rootOption = EventOption.create(event, null, "11월 15일", "DATE", 1, null);
        ReflectionTestUtils.setField(rootOption, "id", 2001L);

        EventOption childOption = EventOption.create(event, rootOption, "오전 세션", "TIME", 1, null);
        ReflectionTestUtils.setField(childOption, "id", 2002L);

        EventOption leafOption = EventOption.create(event, childOption, "A조", "GROUP", 1, null);
        ReflectionTestUtils.setField(leafOption, "id", 2003L);

        EventApplication application = EventApplication.create(
                user,
                event,
                leafOption,
                EventApplicationStatus.APPLIED,
                null,
                null
        );
        ReflectionTestUtils.setField(application, "id", applicationId);

        when(eventApplicationRepository.findById(applicationId)).thenReturn(Optional.of(application));

        // When
        EventApplicationDetailResponse response = eventUserService.getUserApplicationDetail(applicationId, userId);

        // Then
        assertThat(response.applicationId()).isEqualTo(applicationId);
        assertThat(response.eventId()).isEqualTo(event.getId());
        assertThat(response.eventTitle()).isEqualTo(event.getTitle());
        assertThat(response.optionTrail())
                .extracting(EventApplicationDetailResponse.OptionTrailResponse::eventOptionId)
                .containsExactly(rootOption.getId(), childOption.getId(), leafOption.getId());
        assertThat(response.usageGuide()).isEqualTo(event.getUsageGuide());
        assertThat(response.precautions()).isEqualTo(event.getPrecautions());
    }

    @Test
    void should_throw_forbidden_when_requester_is_not_owner() {
        // Given
        Long ownerId = 1L;
        Long requesterId = 2L;

        User owner = User.create("owner@wearagain.kr", "사용자", null);
        ReflectionTestUtils.setField(owner, "id", ownerId);

        AdminUser admin = AdminUser.createSuperAdmin("admin@wearagain.kr", "encoded", "관리자");
        ReflectionTestUtils.setField(admin, "id", 10L);

        Event event = Event.create(
                "업사이클링 클래스",
                "업사이클링 수업",
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                "서울시 마포구",
                EventStatus.OPEN,
                admin,
                null,
                null
        );
        ReflectionTestUtils.setField(event, "id", 45L);

        EventOption option = EventOption.create(event, null, "옵션", "DATE", 1, null);
        ReflectionTestUtils.setField(option, "id", 2001L);

        EventApplication application = EventApplication.create(
                owner,
                event,
                option,
                EventApplicationStatus.APPLIED,
                null,
                null
        );
        ReflectionTestUtils.setField(application, "id", 100L);

        when(eventApplicationRepository.findById(100L)).thenReturn(Optional.of(application));

        // When & Then
        assertThatThrownBy(() -> eventUserService.getUserApplicationDetail(100L, requesterId))
                .isInstanceOf(CustomException.class)
                .extracting(throwable -> ((CustomException) throwable).getErrorCode())
                .isEqualTo(CommonErrorCode.FORBIDDEN);
    }

    @Test
    void should_throw_not_found_when_application_is_missing() {
        // Given
        when(eventApplicationRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> eventUserService.getUserApplicationDetail(999L, 1L))
                .isInstanceOf(EventException.class)
                .extracting(throwable -> ((EventException) throwable).getErrorCode())
                .isEqualTo(EventErrorCode.EVENT_APPLICATION_NOT_FOUND);
    }
}
