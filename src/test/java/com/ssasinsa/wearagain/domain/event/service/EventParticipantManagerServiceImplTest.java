package com.ssasinsa.wearagain.domain.event.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import com.ssasinsa.wearagain.domain.auth.entity.AdminRole;
import com.ssasinsa.wearagain.domain.auth.entity.AdminUser;
import com.ssasinsa.wearagain.domain.auth.entity.User;
import com.ssasinsa.wearagain.domain.auth.infrastructure.security.AdminAuthenticatedUser;
import com.ssasinsa.wearagain.domain.event.dto.manager.ManagerEventParticipantKeywordScope;
import com.ssasinsa.wearagain.domain.event.dto.manager.ManagerEventParticipantListResponse;
import com.ssasinsa.wearagain.domain.event.dto.manager.ManagerEventParticipantSort;
import com.ssasinsa.wearagain.domain.event.dto.manager.ManagerEventParticipantCancelRequest;
import com.ssasinsa.wearagain.domain.event.entity.Event;
import com.ssasinsa.wearagain.domain.event.entity.EventApplication;
import com.ssasinsa.wearagain.domain.event.entity.EventApplicationStatus;
import com.ssasinsa.wearagain.domain.event.entity.EventOption;
import com.ssasinsa.wearagain.domain.event.entity.EventStatus;
import com.ssasinsa.wearagain.domain.event.repository.EventApplicationRepository;
import com.ssasinsa.wearagain.domain.event.repository.EventRepository;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminParticipantDetailResponse;
import com.ssasinsa.wearagain.domain.user.service.UserAdminService;
import java.time.LocalDate;
import java.util.List;
import org.mockito.ArgumentMatchers;
import org.springframework.data.jpa.domain.Specification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class EventParticipantManagerServiceImplTest {

    @Mock
    private EventApplicationRepository eventApplicationRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserAdminService userAdminService;

    @InjectMocks
    private EventParticipantManagerServiceImpl service;

    private AdminAuthenticatedUser principal;
    private EventApplication application;
    private Event event;

    @BeforeEach
    void setUp() {
        AdminUser organizer = AdminUser.createApproved("manager@wearagain.kr", "secure", "매니저", AdminRole.MANAGER);
        ReflectionTestUtils.setField(organizer, "id", 10L);
        principal = new AdminAuthenticatedUser(organizer.getId(), organizer.getEmail(), organizer.getName(), AdminRole.MANAGER);

        event = Event.create(
                "플리마켓",
                "설명",
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                "서울",
                EventStatus.OPEN,
                organizer,
                null,
                null
        );
        ReflectionTestUtils.setField(event, "id", 100L);
        ReflectionTestUtils.setField(event, "staffCode", "ABC123");

        EventOption option = EventOption.create(event, null, "셀렉트", 1, 20);
        User user = User.create("user@wearagain.kr", "참가자", null);
        ReflectionTestUtils.setField(user, "id", 500L);

        application = EventApplication.create(user, event, option, EventApplicationStatus.APPLIED, null, null);
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
        when(eventApplicationRepository.count(any(Specification.class))).thenReturn(1L);

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
        when(eventApplicationRepository.findById(application.getId()))
                .thenReturn(java.util.Optional.of(application));

        ManagerEventParticipantCancelRequest request = new ManagerEventParticipantCancelRequest("중복 예약");
        service.cancelApplication(event.getId(), application.getId(), request, principal);

        assertThat(application.getStatus()).isEqualTo(EventApplicationStatus.REJECTED);
        assertThat(application.getReason()).isEqualTo("중복 예약");
    }
}
