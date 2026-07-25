package com.ssasinsa.wearagain.domain.event.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ssasinsa.wearagain.domain.auth.entity.AdminRole;
import com.ssasinsa.wearagain.domain.auth.entity.AdminUser;
import com.ssasinsa.wearagain.domain.auth.entity.User;
import com.ssasinsa.wearagain.domain.auth.infrastructure.security.AdminAuthenticatedUser;
import com.ssasinsa.wearagain.domain.event.dto.manager.ManagerEventParticipantCancelRequest;
import com.ssasinsa.wearagain.domain.event.dto.manager.ManagerEventParticipantKeywordScope;
import com.ssasinsa.wearagain.domain.event.dto.manager.ManagerEventParticipantListResponse;
import com.ssasinsa.wearagain.domain.event.dto.manager.ManagerEventParticipantSort;
import com.ssasinsa.wearagain.domain.event.entity.Event;
import com.ssasinsa.wearagain.domain.event.entity.EventApplication;
import com.ssasinsa.wearagain.domain.event.entity.EventApplicationStatus;
import com.ssasinsa.wearagain.domain.event.entity.EventOption;
import com.ssasinsa.wearagain.domain.event.entity.EventStatus;
import com.ssasinsa.wearagain.domain.event.exception.EventErrorCode;
import com.ssasinsa.wearagain.domain.event.exception.EventException;
import com.ssasinsa.wearagain.domain.event.repository.EventApplicationRepository;
import com.ssasinsa.wearagain.domain.event.repository.EventRepository;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminParticipantDetailResponse;
import com.ssasinsa.wearagain.domain.user.service.UserAdminService;
import com.ssasinsa.wearagain.global.common.redis.RedisResourceGuard;
import com.ssasinsa.wearagain.global.common.redis.RedisResourceKey;
import com.ssasinsa.wearagain.global.common.redis.RedisTransactionCallbackRegistrar;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class EventParticipantManagerServiceImplTest {

    @Mock
    private EventApplicationRepository eventApplicationRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserAdminService userAdminService;

    @Mock
    private OptionCapacityService optionCapacityService;

    @Mock
    private RedisResourceGuard redisResourceGuard;

    @Mock
    private RedisTransactionCallbackRegistrar redisTransactionCallbackRegistrar;

    @Mock
    private RedisResourceGuard.LockHandle lockHandle;

    @InjectMocks
    private EventParticipantManagerServiceImpl service;

    private AdminAuthenticatedUser principal;
    private EventApplication application;
    private Event event;

    @BeforeEach
    void setUp() {
        AdminUser organizer = AdminUser.createApproved("manager@wearagain.kr", "secure", "매니저", AdminRole.MANAGER);
        ReflectionTestUtils.setField(organizer, "id", 10L);
        principal = new AdminAuthenticatedUser(
                organizer.getId(),
                organizer.getEmail(),
                organizer.getName(),
                AdminRole.MANAGER
        );

        event = Event.create(
                "플리마켓",
                "설명",
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                "서울",
                EventStatus.OPEN,
                organizer,
                null,
                null,
                1
        );
        ReflectionTestUtils.setField(event, "id", 100L);
        ReflectionTestUtils.setField(event, "staffCode", "ABC123");

        EventOption option = EventOption.create(event, null, "셀렉트", 1, 20);
        ReflectionTestUtils.setField(option, "id", 200L);
        User user = User.create("user@wearagain.kr", "참가자", null);
        ReflectionTestUtils.setField(user, "id", 500L);

        application = EventApplication.create(user, event, option, EventApplicationStatus.APPLIED, null);
        ReflectionTestUtils.setField(application, "id", 1000L);
    }

    @Test
    void should_getParticipants_when_filtersProvided() {
        when(eventApplicationRepository.findAll(
                ArgumentMatchers.<Specification<EventApplication>>any(),
                any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(application), PageRequest.of(0, 20), 1));
        when(eventApplicationRepository.findAllWithAssociationsByEventIdAndIdIn(anyLong(), any()))
                .thenReturn(List.of(application));
        when(eventApplicationRepository.count(
                ArgumentMatchers.<Specification<EventApplication>>any()
        )).thenReturn(1L);

        ManagerEventParticipantListResponse response = service.getParticipants(
                principal,
                event.getId(),
                EventApplicationStatus.APPLIED,
                null,
                null,
                ManagerEventParticipantKeywordScope.ALL,
                0,
                20,
                ManagerEventParticipantSort.LATEST
        );

        assertThat(response.content()).hasSize(1);
        assertThat(response.summary().totalApplications()).isEqualTo(1);
        assertThat(response.summary().events()).hasSize(1);
    }

    @Test
    void should_getParticipantDetail_when_authorized() {
        when(eventApplicationRepository.findById(application.getId()))
                .thenReturn(java.util.Optional.of(application));
        when(userAdminService.getParticipantDetail(application.getUser().getId()))
                .thenReturn(new AdminParticipantDetailResponse(
                        application.getUser().getId(),
                        application.getUser().getDisplayName(),
                        application.getUser().getEmail(),
                        application.getUser().getProfileImageUrl(),
                        application.getUser().getTicketBalance(),
                        application.getUser().getCreditBalance(),
                        application.getUser().isSuspended(),
                        null,
                        null,
                        null,
                        null,
                        List.of()
                ));

        AdminParticipantDetailResponse response =
                service.getParticipantDetail(event.getId(), application.getId(), principal);

        assertThat(response.participantId()).isEqualTo(application.getUser().getId());
    }

    @Test
    void should_cancelApplication_when_managerOwnsEvent() {
        when(eventApplicationRepository.findByIdForUpdate(application.getId()))
                .thenReturn(java.util.Optional.of(application));
        RedisResourceKey resourceKey = RedisResourceKey.eventOption(application.getEventOption().getId());
        when(redisResourceGuard.acquireRead(resourceKey)).thenReturn(lockHandle);
        when(redisTransactionCallbackRegistrar.registerAfterCommit(
                eq(resourceKey),
                any(Runnable.class),
                any(Runnable.class)
        )).thenReturn(true);

        ManagerEventParticipantCancelRequest request = new ManagerEventParticipantCancelRequest("중복 예약");
        service.cancelApplication(event.getId(), application.getId(), request, principal);

        assertThat(application.getStatus()).isEqualTo(EventApplicationStatus.REJECTED);
        assertThat(application.getReason()).isEqualTo("중복 예약");

        ArgumentCaptor<Runnable> action = ArgumentCaptor.forClass(Runnable.class);
        ArgumentCaptor<Runnable> completion = ArgumentCaptor.forClass(Runnable.class);
        verify(redisTransactionCallbackRegistrar).registerAfterCommit(
                eq(resourceKey),
                action.capture(),
                completion.capture()
        );
        verify(optionCapacityService, never()).release(application.getEventOption().getId());
        action.getValue().run();
        completion.getValue().run();
        verify(optionCapacityService).release(application.getEventOption().getId());
        verify(lockHandle).close();
    }

    @Test
    void should_not_reject_when_transaction_callback_cannot_be_registered() {
        when(eventApplicationRepository.findByIdForUpdate(application.getId()))
                .thenReturn(java.util.Optional.of(application));
        RedisResourceKey resourceKey = RedisResourceKey.eventOption(application.getEventOption().getId());
        when(redisResourceGuard.acquireRead(resourceKey)).thenReturn(lockHandle);

        assertThatThrownBy(() -> service.cancelApplication(
                event.getId(),
                application.getId(),
                new ManagerEventParticipantCancelRequest("중복 예약"),
                principal
        ))
                .isInstanceOf(EventException.class)
                .extracting(throwable -> ((EventException) throwable).getErrorCode())
                .isEqualTo(EventErrorCode.EVENT_CAPACITY_UNAVAILABLE);
        assertThat(application.getStatus()).isEqualTo(EventApplicationStatus.APPLIED);
        verify(optionCapacityService, never()).release(application.getEventOption().getId());
        verify(lockHandle).close();
    }
}
