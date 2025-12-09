package com.ssasinsa.wearagain.domain.event.service;

import com.ssasinsa.wearagain.domain.auth.entity.AdminUser;
import com.ssasinsa.wearagain.domain.auth.entity.User;
import com.ssasinsa.wearagain.domain.event.dto.staff.EventStaffCheckInRequest;
import com.ssasinsa.wearagain.domain.event.dto.staff.EventStaffCheckInResponse;
import com.ssasinsa.wearagain.domain.event.entity.*;
import com.ssasinsa.wearagain.domain.event.exception.EventErrorCode;
import com.ssasinsa.wearagain.domain.event.exception.EventException;
import com.ssasinsa.wearagain.domain.event.repository.EventApplicationRepository;
import com.ssasinsa.wearagain.domain.event.repository.EventRepository;
import com.ssasinsa.wearagain.domain.event.support.CheckinTokenPayload;
import com.ssasinsa.wearagain.global.common.qr.QrTokenStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventStaffServiceImplTest {

    private static final String STAFF_CODE = "023941";
    private static final String QR_TOKEN = "3b3f6e3456d34a84b41ce8a3f7fb16b1";

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventApplicationRepository eventApplicationRepository;

    @Mock
    private QrTokenStore<CheckinTokenPayload> eventQrTokenStore;

    private EventStaffService eventStaffService;

    @BeforeEach
    void setUp() {
        eventStaffService = new EventStaffServiceImpl(eventRepository, eventApplicationRepository, eventQrTokenStore);
    }

    @Test
    void should_check_in_when_token_and_code_are_valid() {
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
                EventStatus.OPEN,
                admin,
                null,
                null,
                3
        );
        ReflectionTestUtils.setField(event, "id", 45L);
        event.updateStaffCode(STAFF_CODE, LocalDateTime.now(ZoneOffset.UTC));

        EventOption option = EventOption.create(event, null, "옵션", 1, null);
        ReflectionTestUtils.setField(option, "id", 2001L);

        EventApplication application = EventApplication.create(
                user,
                event,
                option,
                EventApplicationStatus.APPLIED, null
        );
        ReflectionTestUtils.setField(application, "id", 123L);

        EventStaffCheckInRequest request = new EventStaffCheckInRequest(QR_TOKEN, STAFF_CODE);
        CheckinTokenPayload payload = new CheckinTokenPayload(
                user.getId(),
                application.getId(),
                QR_TOKEN,
                OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(1),
                OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(9)
        );

        when(eventRepository.findByStaffCode(STAFF_CODE)).thenReturn(Optional.of(event));
        when(eventQrTokenStore.getTokenByToken(QR_TOKEN)).thenReturn(Optional.of(payload));
        when(eventApplicationRepository.findById(application.getId())).thenReturn(Optional.of(application));

        // When
        EventStaffCheckInResponse response = eventStaffService.checkIn(request);

        // Then
        assertThat(response.applicationId()).isEqualTo(application.getId());
        assertThat(response.status()).isEqualTo(EventApplicationStatus.CHECKED_IN.name());
        assertThat(response.userDisplayName()).isEqualTo(user.getDisplayName());
        assertThat(application.getStatus()).isEqualTo(EventApplicationStatus.CHECKED_IN);
        verify(eventQrTokenStore).deleteToken(user.getId());
    }

    @Test
    void should_throw_when_staff_code_is_invalid() {
        // Given
        EventStaffCheckInRequest request = new EventStaffCheckInRequest(QR_TOKEN, STAFF_CODE);
        when(eventRepository.findByStaffCode(STAFF_CODE)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> eventStaffService.checkIn(request))
                .isInstanceOf(EventException.class)
                .extracting(throwable -> ((EventException) throwable).getErrorCode())
                .isEqualTo(EventErrorCode.EVENT_STAFF_CODE_INVALID);
    }

    @Test
    void should_throw_when_token_not_found_in_redis() {
        // Given
        User user = User.create("user@wearagain.kr", "사용자", null);
        ReflectionTestUtils.setField(user, "id", 1L);

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
        event.updateStaffCode(STAFF_CODE, LocalDateTime.now(ZoneOffset.UTC));

        EventOption option = EventOption.create(event, null, "옵션", 1, null);
        ReflectionTestUtils.setField(option, "id", 2001L);

        EventApplication application = EventApplication.create(
                user,
                event,
                option,
                EventApplicationStatus.APPLIED, null
        );
        ReflectionTestUtils.setField(application, "id", 123L);

        EventStaffCheckInRequest request = new EventStaffCheckInRequest(QR_TOKEN, STAFF_CODE);

        when(eventRepository.findByStaffCode(STAFF_CODE)).thenReturn(Optional.of(event));
        when(eventQrTokenStore.getTokenByToken(QR_TOKEN)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> eventStaffService.checkIn(request))
                .isInstanceOf(EventException.class)
                .extracting(throwable -> ((EventException) throwable).getErrorCode())
                .isEqualTo(EventErrorCode.EVENT_CHECKIN_TOKEN_NOT_FOUND);
    }

    @Test
    void should_throw_when_event_is_closed() {
        // Given
        User user = User.create("user@wearagain.kr", "사용자", null);
        ReflectionTestUtils.setField(user, "id", 1L);

        AdminUser admin = AdminUser.createSuperAdmin("admin@wearagain.kr", "encoded", "관리자");
        ReflectionTestUtils.setField(admin, "id", 10L);

        Event event = Event.create(
                "업사이클링 클래스",
                "업사이클링 수업",
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                "서울시 마포구",
                EventStatus.CLOSED,
                admin,
                null,
                null,
                3
        );
        ReflectionTestUtils.setField(event, "id", 45L);
        event.updateStaffCode(STAFF_CODE, LocalDateTime.now(ZoneOffset.UTC));

        EventStaffCheckInRequest request = new EventStaffCheckInRequest(QR_TOKEN, STAFF_CODE);

        when(eventRepository.findByStaffCode(STAFF_CODE)).thenReturn(Optional.of(event));

        // When & Then
        assertThatThrownBy(() -> eventStaffService.checkIn(request))
                .isInstanceOf(EventException.class)
                .extracting(throwable -> ((EventException) throwable).getErrorCode())
                .isEqualTo(EventErrorCode.EVENT_CHECKIN_NOT_AVAILABLE);
    }

    @Test
    void should_throw_when_application_already_processed() {
        // Given
        User user = User.create("user@wearagain.kr", "사용자", null);
        ReflectionTestUtils.setField(user, "id", 1L);

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
        event.updateStaffCode(STAFF_CODE, LocalDateTime.now(ZoneOffset.UTC));

        EventOption option = EventOption.create(event, null, "옵션", 1, null);
        ReflectionTestUtils.setField(option, "id", 2001L);

        EventApplication application = EventApplication.create(
                user,
                event,
                option,
                EventApplicationStatus.CHECKED_IN, null
        );
        ReflectionTestUtils.setField(application, "id", 123L);

        EventStaffCheckInRequest request = new EventStaffCheckInRequest(QR_TOKEN, STAFF_CODE);
        CheckinTokenPayload payload = new CheckinTokenPayload(
                user.getId(),
                application.getId(),
                QR_TOKEN,
                OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(1),
                OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(9)
        );

        when(eventRepository.findByStaffCode(STAFF_CODE)).thenReturn(Optional.of(event));
        when(eventQrTokenStore.getTokenByToken(QR_TOKEN)).thenReturn(Optional.of(payload));
        when(eventApplicationRepository.findById(application.getId())).thenReturn(Optional.of(application));

        // When & Then
        assertThatThrownBy(() -> eventStaffService.checkIn(request))
                .isInstanceOf(EventException.class)
                .extracting(throwable -> ((EventException) throwable).getErrorCode())
                .isEqualTo(EventErrorCode.EVENT_APPLICATION_ALREADY_PROCESSED);
    }

    @Test
    void should_throw_when_application_was_canceled() {
        // Given
        User user = User.create("user@wearagain.kr", "사용자", null);
        ReflectionTestUtils.setField(user, "id", 1L);

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
        event.updateStaffCode(STAFF_CODE, LocalDateTime.now(ZoneOffset.UTC));

        EventOption option = EventOption.create(event, null, "옵션", 1, null);
        ReflectionTestUtils.setField(option, "id", 2001L);

        EventApplication application = EventApplication.create(
                user,
                event,
                option,
                EventApplicationStatus.CANCELED, null
        );
        ReflectionTestUtils.setField(application, "id", 123L);

        EventStaffCheckInRequest request = new EventStaffCheckInRequest(QR_TOKEN, STAFF_CODE);
        CheckinTokenPayload payload = new CheckinTokenPayload(
                user.getId(),
                application.getId(),
                QR_TOKEN,
                OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(1),
                OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(9)
        );

        when(eventRepository.findByStaffCode(STAFF_CODE)).thenReturn(Optional.of(event));
        when(eventQrTokenStore.getTokenByToken(QR_TOKEN)).thenReturn(Optional.of(payload));
        when(eventApplicationRepository.findById(application.getId())).thenReturn(Optional.of(application));

        // When & Then
        assertThatThrownBy(() -> eventStaffService.checkIn(request))
                .isInstanceOf(EventException.class)
                .extracting(throwable -> ((EventException) throwable).getErrorCode())
                .isEqualTo(EventErrorCode.EVENT_APPLICATION_CANCELED);
    }

    @Test
    void should_throw_when_application_was_rejected() {
        // Given
        User user = User.create("user@wearagain.kr", "사용자", null);
        ReflectionTestUtils.setField(user, "id", 1L);

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
        event.updateStaffCode(STAFF_CODE, LocalDateTime.now(ZoneOffset.UTC));

        EventOption option = EventOption.create(event, null, "옵션", 1, null);
        ReflectionTestUtils.setField(option, "id", 2001L);

        EventApplication application = EventApplication.create(
                user,
                event,
                option,
                EventApplicationStatus.REJECTED, null
        );
        ReflectionTestUtils.setField(application, "id", 123L);

        EventStaffCheckInRequest request = new EventStaffCheckInRequest(QR_TOKEN, STAFF_CODE);
        CheckinTokenPayload payload = new CheckinTokenPayload(
                user.getId(),
                application.getId(),
                QR_TOKEN,
                OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(1),
                OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(9)
        );

        when(eventRepository.findByStaffCode(STAFF_CODE)).thenReturn(Optional.of(event));
        when(eventQrTokenStore.getTokenByToken(QR_TOKEN)).thenReturn(Optional.of(payload));
        when(eventApplicationRepository.findById(application.getId())).thenReturn(Optional.of(application));

        // When & Then
        assertThatThrownBy(() -> eventStaffService.checkIn(request))
                .isInstanceOf(EventException.class)
                .extracting(throwable -> ((EventException) throwable).getErrorCode())
                .isEqualTo(EventErrorCode.EVENT_APPLICATION_REJECTED);
    }
}
