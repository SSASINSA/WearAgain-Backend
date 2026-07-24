package com.ssasinsa.wearagain.domain.event.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.ssasinsa.wearagain.domain.auth.entity.AdminUser;
import com.ssasinsa.wearagain.domain.auth.entity.User;
import com.ssasinsa.wearagain.domain.event.dto.request.EventApplyRequest;
import com.ssasinsa.wearagain.domain.event.dto.request.EventCancelRequest;
import com.ssasinsa.wearagain.domain.event.dto.response.EventApplicationDetailResponse;
import com.ssasinsa.wearagain.domain.event.dto.response.EventDetailResponse;
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
import com.ssasinsa.wearagain.domain.event.support.CheckinTokenPayload;
import com.ssasinsa.wearagain.global.common.qr.QrTokenStore;
import com.ssasinsa.wearagain.global.common.redis.RedisResourceGuard;
import com.ssasinsa.wearagain.global.common.redis.RedisResourceKey;
import com.ssasinsa.wearagain.global.common.redis.RedisTransactionCallbackRegistrar;
import com.ssasinsa.wearagain.global.exception.CommonErrorCode;
import com.ssasinsa.wearagain.global.exception.CustomException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
    private QrTokenStore<CheckinTokenPayload> eventQrTokenStore;

    @Mock
    private OptionCapacityService optionCapacityService;

    @Mock
    private RedisResourceGuard redisResourceGuard;

    @Mock
    private RedisTransactionCallbackRegistrar redisTransactionCallbackRegistrar;

    @Mock
    private RedisResourceGuard.LockHandle lockHandle;

    private EventUserServiceImpl eventUserService;

    @BeforeEach
    void setUp() {
        eventUserService = new EventUserServiceImpl(
                eventRepository,
                eventOptionRepository,
                eventApplicationRepository,
                null,
                userRepository,
                eventQrTokenStore,
                optionCapacityService,
                redisResourceGuard,
                redisTransactionCallbackRegistrar
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
                "화재 예방을 위해 지정된 구역에서만 작업해주세요.",
                3
        );
        ReflectionTestUtils.setField(event, "id", 45L);

        EventOption rootOption = EventOption.create(event, null, "11월 15일", 1, null);
        ReflectionTestUtils.setField(rootOption, "id", 2001L);

        EventOption childOption = EventOption.create(event, rootOption, "오전 세션", 1, null);
        ReflectionTestUtils.setField(childOption, "id", 2002L);

        EventOption leafOption = EventOption.create(event, childOption, "A조", 1, null);
        ReflectionTestUtils.setField(leafOption, "id", 2003L);

        EventApplication application = EventApplication.create(
                user,
                event,
                leafOption,
                EventApplicationStatus.APPLIED, null
        );
        ReflectionTestUtils.setField(application, "id", applicationId);

        when(eventApplicationRepository.findById(applicationId)).thenReturn(Optional.of(application));

        // When
        EventApplicationDetailResponse response = eventUserService.getUserApplicationDetail(applicationId, userId);

        // Then
        assertThat(response.applicationId()).isEqualTo(applicationId);
        assertThat(response.eventId()).isEqualTo(event.getId());
        assertThat(response.eventTitle()).isEqualTo(event.getTitle());
        assertThat(response.applicationStatus()).isEqualTo(application.getStatus().name());
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
                null,
                3
        );
        ReflectionTestUtils.setField(event, "id", 45L);

        EventOption option = EventOption.create(event, null, "옵션", 1, null);
        ReflectionTestUtils.setField(option, "id", 2001L);

        EventApplication application = EventApplication.create(
                owner,
                event,
                option,
                EventApplicationStatus.APPLIED, null
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

    @Test
    void should_return_event_detail_without_user_application_for_authenticated_user() {
        // Given
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
                null,
                null,
                3
        );
        ReflectionTestUtils.setField(event, "id", 101L);

        EventOption dateOption = EventOption.create(event, null, "11월 15일", 1, null);
        ReflectionTestUtils.setField(dateOption, "id", 2001L);
        EventOption timeOption = EventOption.create(event, dateOption, "오전 세션", 1, null);
        ReflectionTestUtils.setField(timeOption, "id", 2002L);
        EventOption groupOption = EventOption.create(event, timeOption, "A조", 1, 10);
        ReflectionTestUtils.setField(groupOption, "id", 2003L);

        when(eventRepository.findWithDetailsById(101L)).thenReturn(Optional.of(event));
        when(eventApplicationRepository.countActiveApplicationsByOptionIds(anySet(), any()))
                .thenReturn(List.of());

        // When
        EventDetailResponse response = eventUserService.getEventDetail(101L, 1L);

        // Then
        assertThat(response.eventId()).isEqualTo(101L);
        assertThat(response.options()).hasSize(1);
        assertThat(response.optionDepth()).isEqualTo(3);
        EventDetailResponse.EventDetailOptionResponse root = response.options().get(0);
        assertThat(root.children()).hasSize(1);
        EventDetailResponse.EventDetailOptionResponse group = root.children().get(0).children().get(0);
        assertThat(group.capacity()).isEqualTo(10);
        assertThat(group.appliedCount()).isEqualTo(0);
        assertThat(group.remainingCount()).isEqualTo(10);
        verify(eventApplicationRepository, never())
                .findTopByUserIdAndEventIdOrderByCreatedAtDescIdDesc(anyLong(), anyLong());
    }

    @Test
    void should_skip_application_lookup_when_user_is_anonymous() {
        // Given
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
                null,
                null,
                3
        );
        ReflectionTestUtils.setField(event, "id", 101L);
        EventOption option = EventOption.create(event, null, "11월 15일", 1, null);
        ReflectionTestUtils.setField(option, "id", 2001L);

        when(eventRepository.findById(101L)).thenReturn(Optional.of(event));
        when(eventApplicationRepository.countActiveApplicationsByOptionIds(anySet(), any()))
                .thenReturn(List.of());

        // When
        EventDetailResponse response = eventUserService.getEventDetail(101L, null);

        // Then
        assertThat(response.optionDepth()).isEqualTo(3);
        verify(eventApplicationRepository, never())
                .findTopByUserIdAndEventIdOrderByCreatedAtDescIdDesc(anyLong(), anyLong());
    }

    @Test
    void should_return_zero_option_depth_when_event_has_no_options() {
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
                null,
                null,
                1
        );
        ReflectionTestUtils.setField(event, "id", 301L);
        ReflectionTestUtils.setField(event, "optionDepth", null);
        ReflectionTestUtils.setField(event, "options", new ArrayList<>());

        when(eventRepository.findWithDetailsById(301L)).thenReturn(Optional.of(event));
        EventDetailResponse response = eventUserService.getEventDetail(301L, 5L);

        assertThat(response.optionDepth()).isEqualTo(0);
        assertThat(response.options()).isEmpty();
    }

    @Test
    void should_throw_when_applying_non_leaf_option() {
        // Given
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
                null,
                null,
                3
        );
        ReflectionTestUtils.setField(event, "id", 101L);

        EventOption parentOption = EventOption.create(event, null, "11월 15일", 1, 30);
        ReflectionTestUtils.setField(parentOption, "id", 2001L);
        EventOption childOption = EventOption.create(event, parentOption, "오전 세션", 1, 10);
        ReflectionTestUtils.setField(childOption, "id", 2002L);

        when(eventRepository.findById(101L)).thenReturn(Optional.of(event));
        when(eventOptionRepository.findByIdAndEventId(2001L, 101L)).thenReturn(Optional.of(parentOption));

        // When & Then
        assertThatThrownBy(() -> eventUserService.apply(
                101L,
                new EventApplyRequest(2001L, null),
                1L
        ))
                .isInstanceOf(EventException.class)
                .extracting(throwable -> ((EventException) throwable).getErrorCode())
                .isEqualTo(EventErrorCode.EVENT_OPTION_NOT_LEAF);
        verify(optionCapacityService, never()).reserve(anyLong(), anyInt());
    }

    @Test
    void should_register_rollback_compensation_after_capacity_reservation() {
        Long eventId = 401L;
        Long optionId = 4001L;
        Long userId = 41L;
        Event event = event(eventId);
        EventOption option = option(event, optionId, 10);
        User user = user(userId);
        RedisResourceKey resourceKey = RedisResourceKey.eventOption(optionId);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventOptionRepository.findByIdAndEventId(optionId, eventId)).thenReturn(Optional.of(option));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(redisResourceGuard.acquireRead(resourceKey)).thenReturn(lockHandle);
        when(optionCapacityService.reserve(optionId, 10))
                .thenReturn(OptionCapacityService.ReserveResult.RESERVED);
        when(redisTransactionCallbackRegistrar.registerRollbackCompensation(
                eq(resourceKey),
                any(Runnable.class),
                any(Runnable.class)
        )).thenReturn(true);
        when(eventApplicationRepository.save(any(EventApplication.class))).thenAnswer(invocation -> {
            EventApplication application = invocation.getArgument(0);
            ReflectionTestUtils.setField(application, "id", 9001L);
            return application;
        });

        eventUserService.apply(eventId, new EventApplyRequest(optionId, null), userId);

        ArgumentCaptor<Runnable> compensation = ArgumentCaptor.forClass(Runnable.class);
        ArgumentCaptor<Runnable> completion = ArgumentCaptor.forClass(Runnable.class);
        verify(redisTransactionCallbackRegistrar).registerRollbackCompensation(
                eq(resourceKey),
                compensation.capture(),
                completion.capture()
        );
        verify(optionCapacityService, never()).release(optionId);
        compensation.getValue().run();
        completion.getValue().run();
        verify(optionCapacityService).release(optionId);
        verify(lockHandle).close();
    }

    @Test
    void should_compensate_immediately_when_transaction_callback_cannot_be_registered() {
        Long eventId = 402L;
        Long optionId = 4002L;
        Long userId = 42L;
        Event event = event(eventId);
        EventOption option = option(event, optionId, 10);
        RedisResourceKey resourceKey = RedisResourceKey.eventOption(optionId);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventOptionRepository.findByIdAndEventId(optionId, eventId)).thenReturn(Optional.of(option));
        when(redisResourceGuard.acquireRead(resourceKey)).thenReturn(lockHandle);
        when(optionCapacityService.reserve(optionId, 10))
                .thenReturn(OptionCapacityService.ReserveResult.RESERVED);

        assertThatThrownBy(() -> eventUserService.apply(
                eventId,
                new EventApplyRequest(optionId, null),
                userId
        ))
                .isInstanceOf(EventException.class)
                .extracting(throwable -> ((EventException) throwable).getErrorCode())
                .isEqualTo(EventErrorCode.EVENT_CAPACITY_UNAVAILABLE);
        verify(optionCapacityService).release(optionId);
        verify(lockHandle).close();
    }

    @Test
    void should_return_unavailable_when_redis_reservation_fails() {
        Long eventId = 403L;
        Long optionId = 4003L;
        Event event = event(eventId);
        EventOption option = option(event, optionId, 10);
        RedisResourceKey resourceKey = RedisResourceKey.eventOption(optionId);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventOptionRepository.findByIdAndEventId(optionId, eventId)).thenReturn(Optional.of(option));
        when(redisResourceGuard.acquireRead(resourceKey)).thenReturn(lockHandle);
        when(optionCapacityService.reserve(optionId, 10))
                .thenReturn(OptionCapacityService.ReserveResult.UNAVAILABLE);

        assertThatThrownBy(() -> eventUserService.apply(
                eventId,
                new EventApplyRequest(optionId, null),
                43L
        ))
                .isInstanceOf(EventException.class)
                .extracting(throwable -> ((EventException) throwable).getErrorCode())
                .isEqualTo(EventErrorCode.EVENT_CAPACITY_UNAVAILABLE);
        verify(optionCapacityService, never()).release(optionId);
        verify(lockHandle).close();
    }

    @Test
    void should_release_capacity_only_after_cancel_commit_callback() {
        Long userId = 44L;
        Event event = event(404L);
        EventOption option = option(event, 4004L, 10);
        User user = user(userId);
        EventApplication application = EventApplication.create(
                user,
                event,
                option,
                EventApplicationStatus.APPLIED,
                null
        );
        ReflectionTestUtils.setField(application, "id", 9404L);
        RedisResourceKey resourceKey = RedisResourceKey.eventOption(option.getId());

        when(eventApplicationRepository.findByIdAndUserIdForUpdate(application.getId(), userId))
                .thenReturn(Optional.of(application));
        when(redisResourceGuard.acquireRead(resourceKey)).thenReturn(lockHandle);
        when(redisTransactionCallbackRegistrar.registerAfterCommit(
                eq(resourceKey),
                any(Runnable.class),
                any(Runnable.class)
        )).thenReturn(true);

        eventUserService.cancel(application.getId(), new EventCancelRequest("일정 변경"), userId);

        ArgumentCaptor<Runnable> action = ArgumentCaptor.forClass(Runnable.class);
        ArgumentCaptor<Runnable> completion = ArgumentCaptor.forClass(Runnable.class);
        verify(redisTransactionCallbackRegistrar).registerAfterCommit(
                eq(resourceKey),
                action.capture(),
                completion.capture()
        );
        assertThat(application.getStatus()).isEqualTo(EventApplicationStatus.CANCELED);
        verify(optionCapacityService, never()).release(option.getId());
        action.getValue().run();
        completion.getValue().run();
        verify(optionCapacityService).release(option.getId());
        verify(lockHandle).close();
    }

    @Test
    void should_skip_redis_when_applying_to_unlimited_option() {
        Long eventId = 405L;
        Long optionId = 4005L;
        Long userId = 45L;
        Event event = event(eventId);
        EventOption option = option(event, optionId, null);
        User user = user(userId);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventOptionRepository.findByIdAndEventId(optionId, eventId)).thenReturn(Optional.of(option));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(eventApplicationRepository.save(any(EventApplication.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        eventUserService.apply(eventId, new EventApplyRequest(optionId, null), userId);

        verifyNoInteractions(optionCapacityService, redisResourceGuard, redisTransactionCallbackRegistrar);
    }

    @Test
    void should_throw_when_request_qr_for_closed_event() {
        // Given
        User user = User.create("user@wearagain.kr", "사용자", null);
        ReflectionTestUtils.setField(user, "id", 1L);

        AdminUser admin = AdminUser.createSuperAdmin("admin@wearagain.kr", "encoded", "관리자");
        ReflectionTestUtils.setField(admin, "id", 10L);

        Event event = Event.create(
                "업사이클링 클래스",
                "업사이클링 수업",
                LocalDate.of(2025, 2, 10),
                LocalDate.of(2025, 2, 11),
                "서울시 마포구",
                EventStatus.CLOSED,
                admin,
                null,
                null,
                3
        );
        ReflectionTestUtils.setField(event, "id", 45L);

        EventOption option = EventOption.create(event, null, "11월 15일", 1, null);
        ReflectionTestUtils.setField(option, "id", 2001L);

        EventApplication application = EventApplication.create(
                user,
                event,
                option,
                EventApplicationStatus.APPLIED, null
        );
        ReflectionTestUtils.setField(application, "id", 5001L);
        ReflectionTestUtils.setField(application, "createdAt", LocalDateTime.now());

        when(eventApplicationRepository.findByIdAndUserId(5001L, 1L)).thenReturn(Optional.of(application));

        // When & Then
        assertThatThrownBy(() -> eventUserService.issueApplicationQr(5001L, 1L))
                .isInstanceOf(EventException.class)
                .extracting(throwable -> ((EventException) throwable).getErrorCode())
                .isEqualTo(EventErrorCode.EVENT_CHECKIN_NOT_AVAILABLE);
        verify(eventQrTokenStore, never()).generateToken();
    }

    private Event event(Long eventId) {
        AdminUser admin = AdminUser.createSuperAdmin("admin@wearagain.kr", "encoded", "관리자");
        ReflectionTestUtils.setField(admin, "id", eventId + 1000);
        Event event = Event.create(
                "업사이클링 클래스",
                "업사이클링 수업",
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                "서울시 마포구",
                EventStatus.OPEN,
                admin,
                null,
                null,
                1
        );
        ReflectionTestUtils.setField(event, "id", eventId);
        return event;
    }

    private EventOption option(Event event, Long optionId, Integer capacity) {
        EventOption option = EventOption.create(event, null, "신청 옵션", 1, capacity);
        ReflectionTestUtils.setField(option, "id", optionId);
        return option;
    }

    private User user(Long userId) {
        User user = User.create("user%s@wearagain.kr".formatted(userId), "사용자", null);
        ReflectionTestUtils.setField(user, "id", userId);
        return user;
    }
}
