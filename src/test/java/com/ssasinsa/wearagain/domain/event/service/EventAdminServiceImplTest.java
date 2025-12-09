package com.ssasinsa.wearagain.domain.event.service;

import com.ssasinsa.wearagain.domain.auth.entity.AdminRole;
import com.ssasinsa.wearagain.domain.auth.entity.AdminUser;
import com.ssasinsa.wearagain.domain.auth.entity.User;
import com.ssasinsa.wearagain.domain.auth.repository.AdminUserRepository;
import com.ssasinsa.wearagain.domain.event.dto.admin.*;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventAdminUpdateRequest.EventAdminImageRequest;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventAdminUpdateRequest.EventAdminOptionRequest;
import com.ssasinsa.wearagain.domain.event.dto.response.EventCreateResponse;
import com.ssasinsa.wearagain.domain.event.entity.*;
import com.ssasinsa.wearagain.domain.event.exception.EventErrorCode;
import com.ssasinsa.wearagain.domain.event.exception.EventException;
import com.ssasinsa.wearagain.domain.event.repository.*;
import com.ssasinsa.wearagain.domain.finance.repository.ImpactAnalyticsRepository;
import com.ssasinsa.wearagain.domain.growth.dto.ImpactSummary;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;

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

    @Mock
    private EventApprovalRequestRepository eventApprovalRequestRepository;

    @Mock
    private ImpactAnalyticsRepository impactAnalyticsRepository;

    @InjectMocks
    private EventAdminServiceImpl eventAdminService;

    private Event event;
    private EventOption option;
    private AdminUser adminUser;
    private EventAdminCreateRequest validCreateRequest;
    private LocalDate defaultStartDate;
    private LocalDate defaultEndDate;

    @BeforeEach
    void setUp() {
        defaultStartDate = LocalDate.now().plusDays(30);
        defaultEndDate = defaultStartDate.plusDays(15);
        adminUser = AdminUser.createSuperAdmin("admin@wearagain.kr", "encoded", "운영자");
        ReflectionTestUtils.setField(adminUser, "id", 11L);
        when(adminUserRepository.findById(11L)).thenReturn(java.util.Optional.of(adminUser));

        event = Event.create(
                "지속가능 패션 워크숍",
                "웨어어게인과 함께하는 리폼 클래스",
                defaultStartDate,
                defaultEndDate,
                "서울시 마포구 연남동 223-14 2F",
                EventStatus.OPEN,
                adminUser,
                "준비물은 개인 텀블러를 지참해주세요.",
                "화재 예방을 위해 지정된 구역에서만 작업해주세요."
        );
        ReflectionTestUtils.setField(event, "id", 101L);

        EventImage image = EventImage.create(event, "https://cdn.wearagain.kr/events/101/main.jpg", "대표", 1);
        ReflectionTestUtils.setField(image, "id", 1001L);

        option = EventOption.create(event, null, "11월 15일", 1, null);
        ReflectionTestUtils.setField(option, "id", 2001L);
        EventOption child = EventOption.create(event, option, "A조", 1, 30);
        ReflectionTestUtils.setField(child, "id", 2003L);

        validCreateRequest = createValidCreateRequest();
    }

    @Test
    void should_create_event_when_request_is_valid() {
        Event persisted = buildPersistedEvent(validCreateRequest);
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        when(eventRepository.save(eventCaptor.capture())).thenReturn(persisted);

        EventCreateResponse response = eventAdminService.createEvent(validCreateRequest, 11L, AdminRole.MANAGER);

        verify(eventRepository).save(any(Event.class));
        verify(eventApprovalRequestRepository).save(any(EventApprovalRequest.class));
        assertThat(eventCaptor.getValue().getStatus()).isEqualTo(EventStatus.DRAFT);
        assertThat(response.eventId()).isEqualTo(1L);
        assertThat(response.organizerAdminId()).isEqualTo(11L);
        assertThat(response.organizerAdminEmail()).isEqualTo("admin@wearagain.kr");
        assertThat(response.organizerAdminName()).isEqualTo("운영자");
        assertThat(response.organizerName()).isEqualTo(adminUser.getName());
        assertThat(response.organizerContact()).isEqualTo(adminUser.getEmail());
        assertThat(response.usageGuide()).isEqualTo(validCreateRequest.usageGuide());
        assertThat(response.precautions()).isEqualTo(validCreateRequest.precautions());
        assertThat(response.images()).hasSize(2);
        assertThat(response.options()).hasSize(2);
        assertThat(response.status()).isEqualTo(EventStatus.DRAFT.name());
    }

    @Test
    void should_fail_when_non_leaf_has_capacity() {
        EventAdminCreateRequest.EventAdminCreateOptionRequest parentOption = new EventAdminCreateRequest.EventAdminCreateOptionRequest(
                "상위 옵션",
                1,
                50,
                List.of(new EventAdminCreateRequest.EventAdminCreateOptionRequest(
                        "리프 옵션",
                        1,
                        20,
                        List.of()
                ))
        );
        EventAdminCreateRequest request = new EventAdminCreateRequest(
                "지속가능 패션 행사",
                "재사용 패션 실습을 진행합니다.",
                "개인 텀블러를 지참해주세요.",
                "발화성 물질 반입 금지",
                "서울시 마포구 연남동",
                defaultStartDate,
                defaultEndDate,
                List.of(new EventAdminCreateRequest.EventAdminCreateImageRequest("https://wearagain.kr/1.jpg", "대표", 1)),
                List.of(parentOption)
        );

        assertThatThrownBy(() -> eventAdminService.createEvent(request, 11L, AdminRole.MANAGER))
                .isInstanceOf(EventException.class)
                .hasFieldOrPropertyWithValue("errorCode", EventErrorCode.INVALID_OPTION_STRUCTURE);
    }

    @Test
    void should_auto_approve_event_when_created_by_admin() {
        Event persisted = buildPersistedEvent(validCreateRequest);
        persisted.changeStatus(EventStatus.APPROVAL);
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        when(eventRepository.save(eventCaptor.capture())).thenReturn(persisted);

        EventCreateResponse response = eventAdminService.createEvent(validCreateRequest, 11L, AdminRole.ADMIN);

        assertThat(eventCaptor.getValue().getStatus()).isEqualTo(EventStatus.APPROVAL);
        assertThat(response.status()).isEqualTo(EventStatus.APPROVAL.name());
        verify(eventApprovalRequestRepository, never()).save(any());
    }

    @Test
    void should_fail_create_when_end_date_is_before_start_date() {
        EventAdminCreateRequest request = new EventAdminCreateRequest(
                "테스트 행사",
                "행사 설명입니다.",
                null,
                null,
                "서울시 마포구",
                LocalDate.now(),
                LocalDate.now().minusDays(1),
                List.of(new EventAdminCreateRequest.EventAdminCreateImageRequest("https://example.com/1.png", "대표", 1)),
                List.of()
        );

        assertThatThrownBy(() -> eventAdminService.createEvent(request, 11L, AdminRole.MANAGER))
                .isInstanceOf(EventException.class)
                .hasFieldOrPropertyWithValue("errorCode", EventErrorCode.INVALID_EVENT_PERIOD);
    }

    @Test
    void should_fail_create_when_option_depth_exceeds_limit() {
        EventAdminCreateRequest.EventAdminCreateOptionRequest depth4Option = new EventAdminCreateRequest.EventAdminCreateOptionRequest(
                "1차",
                1,
                null,
                List.of(
                        new EventAdminCreateRequest.EventAdminCreateOptionRequest(
                                "2차",
                                1,
                                null,
                                List.of(
                                        new EventAdminCreateRequest.EventAdminCreateOptionRequest(
                                                "3차",
                                                1,
                                                null,
                                                List.of(
                                                        new EventAdminCreateRequest.EventAdminCreateOptionRequest(
                                                                "4차",
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

        EventAdminCreateRequest request = new EventAdminCreateRequest(
                validCreateRequest.title(),
                validCreateRequest.description(),
                validCreateRequest.usageGuide(),
                validCreateRequest.precautions(),
                validCreateRequest.location(),
                validCreateRequest.startDate(),
                validCreateRequest.endDate(),
                validCreateRequest.images(),
                List.of(depth4Option)
        );

        assertThatThrownBy(() -> eventAdminService.createEvent(request, 11L, AdminRole.MANAGER))
                .isInstanceOf(EventException.class)
                .hasFieldOrPropertyWithValue("errorCode", EventErrorCode.OPTION_DEPTH_LIMIT_EXCEEDED);
    }

    @Test
    void should_fail_create_when_admin_not_found() {
        when(adminUserRepository.findById(999L)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> eventAdminService.createEvent(validCreateRequest, 999L, AdminRole.MANAGER))
                .isInstanceOf(EventException.class)
                .hasFieldOrPropertyWithValue("errorCode", EventErrorCode.EVENT_ADMIN_NOT_FOUND);
    }

    @Test
    void should_issue_staff_code_when_request_by_organizer() {
        when(eventRepository.findWithDetailsById(101L)).thenReturn(java.util.Optional.of(event));
        when(eventRepository.findById(101L)).thenReturn(java.util.Optional.of(event));

        EventStaffCodeResponse response = eventAdminService.issueStaffCode(101L, 11L);

        assertThat(response.eventId()).isEqualTo(101L);
        assertThat(response.staffCode()).matches("\\d{6}");
        assertThat(response.issuedAt()).isNotNull();
        assertThat(event.getStaffCode()).isEqualTo(response.staffCode());
        assertThat(event.getStaffCodeIssuedAt()).isNotNull();
    }

    @Test
    void should_fail_issue_staff_code_when_not_organizer() {
        when(eventRepository.findById(101L)).thenReturn(java.util.Optional.of(event));

        assertThatThrownBy(() -> eventAdminService.issueStaffCode(101L, 999L))
                .isInstanceOf(EventException.class)
                .hasFieldOrPropertyWithValue("errorCode", EventErrorCode.EVENT_STAFF_CODE_FORBIDDEN);
    }

    @Test
    void should_get_staff_code_when_exists() {
        event.updateStaffCode("123456", LocalDateTime.now(ZoneOffset.UTC));
        when(eventRepository.findById(101L)).thenReturn(java.util.Optional.of(event));

        EventStaffCodeResponse response = eventAdminService.getStaffCode(101L, 11L);

        assertThat(response.staffCode()).isEqualTo("123456");
        assertThat(response.issuedAt()).isNotNull();
    }

    @Test
    void should_fail_get_staff_code_when_not_issued() {
        when(eventRepository.findById(101L)).thenReturn(java.util.Optional.of(event));

        assertThatThrownBy(() -> eventAdminService.getStaffCode(101L, 11L))
                .isInstanceOf(EventException.class)
                .hasFieldOrPropertyWithValue("errorCode", EventErrorCode.EVENT_STAFF_CODE_NOT_ISSUED);
    }

    @Test
    void should_list_events_with_statistics() {
        PageImpl<Event> pageResult = new PageImpl<>(List.of(event), PageRequest.of(0, 10), 20);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        when(eventRepository.findAll(any(Specification.class), pageableCaptor.capture()))
                .thenReturn(pageResult);
        when(eventOptionRepository.sumCapacityByEventIds(anyCollection()))
                .thenReturn(List.of(new EventCapacitySummary(101L, 30L)));
        when(eventApplicationRepository.countActiveApplicationsByEventIds(anyCollection(), anyCollection()))
                .thenReturn(List.of(new EventApplicationEventCount(101L, 20L)));

        EventAdminListResponse response = eventAdminService.getEvents(null, 0, 10, null, 11L, AdminRole.ADMIN, null, null);

        assertThat(response.events()).hasSize(1);
        EventAdminSummaryResponse summary = response.events().get(0);
        assertThat(summary.eventId()).isEqualTo(101L);
        assertThat(summary.totalCapacity()).isEqualTo(30L);
        assertThat(summary.appliedCount()).isEqualTo(20L);
        assertThat(summary.remainingCount()).isEqualTo(10L);
        assertThat(summary.organizerAdminId()).isEqualTo(11L);
        assertThat(summary.organizerAdminEmail()).isEqualTo("admin@wearagain.kr");
        assertThat(summary.organizerAdminName()).isEqualTo("운영자");
        assertThat(summary.organizerName()).isEqualTo(adminUser.getName());
        assertThat(summary.organizerContact()).isEqualTo(adminUser.getEmail());
        assertThat(response.page()).isEqualTo(0);
        assertThat(response.size()).isEqualTo(10);
        assertThat(response.totalElements()).isEqualTo(20);
        assertThat(response.totalPages()).isEqualTo(2);
        assertThat(response.hasNext()).isTrue();

        Pageable pageable = pageableCaptor.getValue();
        Sort.Order createdAtOrder = pageable.getSort().getOrderFor("createdAt");
        assertThat(createdAtOrder).isNotNull();
        assertThat(createdAtOrder.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void should_throw_invalid_query_when_keyword_scope_is_invalid() {
        assertThatThrownBy(() -> eventAdminService.getEvents("OPEN", 0, 10, null, 11L, AdminRole.ADMIN, "sale", "invalid"))
                .isInstanceOf(EventException.class)
                .hasFieldOrPropertyWithValue("errorCode", EventErrorCode.INVALID_EVENT_QUERY);
    }

    @Test
    void should_apply_sort_parameter_when_provided() {
        PageImpl<Event> pageResult = new PageImpl<>(List.of(event), PageRequest.of(0, 10), 1);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        when(eventRepository.findAll(any(Specification.class), pageableCaptor.capture()))
                .thenReturn(pageResult);
        when(eventOptionRepository.sumCapacityByEventIds(anyCollection())).thenReturn(List.of());
        when(eventApplicationRepository.countActiveApplicationsByEventIds(anyCollection(), anyCollection()))
                .thenReturn(List.of());

        eventAdminService.getEvents(null, 0, 10, "TITLE_ASC", 11L, AdminRole.ADMIN, null, null);

        Pageable pageable = pageableCaptor.getValue();
        Sort.Order titleOrder = pageable.getSort().getOrderFor("title");
        assertThat(titleOrder).isNotNull();
        assertThat(titleOrder.getDirection()).isEqualTo(Sort.Direction.ASC);
        Sort.Order idOrder = pageable.getSort().getOrderFor("id");
        assertThat(idOrder).isNotNull();
        assertThat(idOrder.getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void should_throw_invalid_query_when_sort_is_invalid() {
        assertThatThrownBy(() -> eventAdminService.getEvents(null, 0, 10, "WRONG", 11L, AdminRole.ADMIN, null, null))
                .isInstanceOf(EventException.class)
                .hasFieldOrPropertyWithValue("errorCode", EventErrorCode.INVALID_EVENT_QUERY);
    }

    @Test
    void should_apply_manager_and_keyword_filters_together() {
        PageImpl<Event> pageResult = new PageImpl<>(List.of(event), PageRequest.of(0, 5), 5);
        ArgumentCaptor<Specification<Event>> specCaptor = ArgumentCaptor.forClass(Specification.class);
        when(eventRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(pageResult);
        when(eventOptionRepository.sumCapacityByEventIds(anyCollection()))
                .thenReturn(List.of(new EventCapacitySummary(101L, 30L)));
        when(eventApplicationRepository.countActiveApplicationsByEventIds(anyCollection(), anyCollection()))
                .thenReturn(List.of(new EventApplicationEventCount(101L, 10L)));

        eventAdminService.getEvents("OPEN", 0, 5, null, 11L, AdminRole.MANAGER, "Sale%", "TITLE");

        verify(eventRepository).findAll(specCaptor.capture(), any(Pageable.class));
        Specification<Event> captured = specCaptor.getValue();

        CriteriaBuilder builder = mock(CriteriaBuilder.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        jakarta.persistence.criteria.Root<Event> root = mock(jakarta.persistence.criteria.Root.class);
        Path statusPath = mock(Path.class);
        Path organizerPath = mock(Path.class);
        Path organizerIdPath = mock(Path.class);
        Path titlePath = mock(Path.class);
        Expression<String> lowerTitle = mock(Expression.class);
        Predicate statusPredicate = mock(Predicate.class);
        Predicate organizerPredicate = mock(Predicate.class);
        Predicate keywordPredicate = mock(Predicate.class);
        Predicate combinedPredicate = mock(Predicate.class);

        when(root.get("status")).thenReturn(statusPath);
        when(statusPath.in(anyCollection())).thenReturn(statusPredicate);
        when(root.get("organizerAdmin")).thenReturn(organizerPath);
        when(organizerPath.get("id")).thenReturn(organizerIdPath);
        when(builder.equal(organizerIdPath, 11L)).thenReturn(organizerPredicate);
        when(root.get("title")).thenReturn(titlePath);
        when(builder.lower((Expression<String>) titlePath)).thenReturn(lowerTitle);
        when(builder.like(lowerTitle, "%sale\\%%", '\\')).thenReturn(keywordPredicate);
        when(builder.and(any(Predicate.class), any(Predicate.class))).thenReturn(combinedPredicate);
        when(builder.and(any(Predicate.class), any(Predicate.class), any(Predicate.class))).thenReturn(combinedPredicate);
        when(builder.and(any(Predicate[].class))).thenReturn(combinedPredicate);

        Predicate predicate = captured.toPredicate(root, query, builder);

        assertThat(predicate).isNotNull();
        verify(statusPath).in(anyCollection());
        verify(builder).equal(organizerIdPath, 11L);
        verify(builder, atLeastOnce()).like(lowerTitle, "%sale\\%%", '\\');
    }

    @Test
    void should_get_event_detail_with_options_and_applications() {
        event.updateStaffCode("999888", LocalDateTime.now(ZoneOffset.UTC));
        when(eventRepository.findWithDetailsById(101L)).thenReturn(java.util.Optional.of(event));
        when(eventRepository.findById(101L)).thenReturn(java.util.Optional.of(event));
        when(eventOptionRepository.sumCapacityByEventIds(anyCollection()))
                .thenReturn(List.of(new EventCapacitySummary(101L, 30L)));
        when(eventApplicationRepository.countActiveApplicationsByEventIds(anyCollection(), anyCollection()))
                .thenReturn(List.of(new EventApplicationEventCount(101L, 12L)));
        when(eventApplicationRepository.countActiveApplicationsByOptionIds(anyCollection(), anyCollection()))
                .thenReturn(List.of(new EventOptionApplicationCount(2003L, 12L)));

        User user = User.create("user@wearagain.kr", "사용자", null);
        ReflectionTestUtils.setField(user, "id", 10L);
        EventApplication application = EventApplication.create(user, event, option.getChildOptions().get(0), EventApplicationStatus.APPLIED, null);
        ReflectionTestUtils.setField(application, "id", 5001L);
        when(eventApplicationRepository.findAllWithUserByEventId(101L)).thenReturn(List.of(application));

        EventAdminDetailResponse response = eventAdminService.getEventDetail(101L, 11L, AdminRole.ADMIN);

        assertThat(response.eventId()).isEqualTo(101L);
        assertThat(response.usageGuide()).isEqualTo("준비물은 개인 텀블러를 지참해주세요.");
        assertThat(response.precautions()).isEqualTo("화재 예방을 위해 지정된 구역에서만 작업해주세요.");
        assertThat(response.organizerName()).isEqualTo(adminUser.getName());
        assertThat(response.organizerContact()).isEqualTo(adminUser.getEmail());
        assertThat(response.organizerAdminId()).isEqualTo(11L);
        assertThat(response.organizerAdminEmail()).isEqualTo("admin@wearagain.kr");
        assertThat(response.organizerAdminName()).isEqualTo("운영자");
        assertThat(response.totalCapacity()).isEqualTo(30L);
        assertThat(response.appliedCount()).isEqualTo(12L);
        assertThat(response.remainingCount()).isEqualTo(18L);
        assertThat(response.images()).hasSize(1);
        assertThat(response.options()).hasSize(1);
        assertThat(response.applications()).hasSize(1);
        assertThat(response.staffCode()).isEqualTo("999888");
        assertThat(response.staffCodeIssuedAt()).isNotNull();
        assertThat(response.impactAnalytics().available()).isFalse();
        assertThat(response.impactAnalytics().message()).isEqualTo("행사 종료 후 집계 예정입니다.");
        verify(impactAnalyticsRepository, never()).aggregateByEventId(101L);
    }

    @Test
    void should_include_impact_analytics_when_event_finished() {
        LocalDate pastStart = LocalDate.now().minusDays(10);
        LocalDate pastEnd = LocalDate.now().minusDays(5);
        event.updatePeriod(pastStart, pastEnd);
        event.changeStatus(EventStatus.CLOSED);
        when(eventRepository.findWithDetailsById(101L)).thenReturn(java.util.Optional.of(event));
        when(eventRepository.findById(101L)).thenReturn(java.util.Optional.of(event));
        when(eventOptionRepository.sumCapacityByEventIds(anyCollection())).thenReturn(List.of());
        when(eventApplicationRepository.countActiveApplicationsByEventIds(anyCollection(), anyCollection()))
                .thenReturn(List.of());
        when(eventApplicationRepository.countActiveApplicationsByOptionIds(anyCollection(), anyCollection()))
                .thenReturn(List.of());
        when(eventApplicationRepository.findAllWithUserByEventId(101L)).thenReturn(List.of());
        ImpactSummary summary = new ImpactSummary(
                BigDecimal.valueOf(1.23),
                BigDecimal.valueOf(45.6),
                BigDecimal.valueOf(7.89)
        );
        when(impactAnalyticsRepository.aggregateByEventId(101L)).thenReturn(summary);

        EventAdminDetailResponse response = eventAdminService.getEventDetail(101L, 11L, AdminRole.ADMIN);

        assertThat(response.impactAnalytics().available()).isTrue();
        assertThat(response.impactAnalytics().co2Saved()).isEqualByComparingTo("1.23");
        assertThat(response.impactAnalytics().waterSaved()).isEqualByComparingTo("45.6");
        assertThat(response.impactAnalytics().energySaved()).isEqualByComparingTo("7.89");
        assertThat(response.impactAnalytics().message()).isNull();
        verify(impactAnalyticsRepository).aggregateByEventId(101L);
    }

    @Test
    void should_update_event_basic_info_and_replace_structures() {
        when(eventRepository.findById(101L)).thenReturn(java.util.Optional.of(event));
        when(eventRepository.findWithDetailsById(101L)).thenReturn(java.util.Optional.of(event));
        when(eventOptionRepository.sumCapacityByEventIds(anyCollection())).thenReturn(List.<EventCapacitySummary>of());
        when(eventApplicationRepository.countActiveApplicationsByEventIds(anyCollection(), anyCollection()))
                .thenReturn(List.<EventApplicationEventCount>of());
        when(eventApplicationRepository.countActiveApplicationsByOptionIds(anyCollection(), anyCollection()))
                .thenReturn(List.<EventOptionApplicationCount>of());
        when(eventApplicationRepository.findAllWithUserByEventId(101L)).thenReturn(List.<EventApplication>of());

        LocalDate updatedStartDate = defaultStartDate.plusDays(2);
        LocalDate updatedEndDate = updatedStartDate.plusDays(10);
        EventAdminUpdateRequest request = new EventAdminUpdateRequest(
                "워크숍 업데이트",
                "설명 업데이트입니다.",
                "업데이트된 이용 방법",
                "업데이트된 주의 사항",
                "서울시 성동구 왕십리로 32",
                updatedStartDate,
                updatedEndDate,
                EventStatus.OPEN,
                List.of(new EventAdminImageRequest("https://cdn.wearagain.kr/events/101/main.jpg", "대표", 1)),
                List.of(new EventAdminOptionRequest(
                        "11월 20일",
                        1,
                        null,
                        List.of(new EventAdminOptionRequest(
                                "오전 세션",
                                1,
                                null,
                                List.of(new EventAdminOptionRequest(
                                        "A조",
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
        assertThat(event.getStartDate()).isEqualTo(updatedStartDate);
        assertThat(event.getUsageGuide()).isEqualTo("업데이트된 이용 방법");
        assertThat(event.getPrecautions()).isEqualTo("업데이트된 주의 사항");
        assertThat(response.options()).hasSize(1);
        assertThat(response.options().get(0).children().get(0).children()).hasSize(1);
    }

    @Test
    void should_reset_existing_approval_request_when_manager_updates_event() {
        when(eventRepository.findById(101L)).thenReturn(java.util.Optional.of(event), java.util.Optional.of(event));
        when(eventRepository.findWithDetailsById(101L)).thenReturn(java.util.Optional.of(event));
        when(eventOptionRepository.sumCapacityByEventIds(anyCollection())).thenReturn(List.of());
        when(eventApplicationRepository.countActiveApplicationsByEventIds(anyCollection(), anyCollection()))
                .thenReturn(List.of());
        when(eventApplicationRepository.countActiveApplicationsByOptionIds(anyCollection(), anyCollection()))
                .thenReturn(List.of());
        when(eventApplicationRepository.findAllWithUserByEventId(101L)).thenReturn(List.<EventApplication>of());

        AdminUser previousRequester = AdminUser.createApproved("prev@wearagain.kr", "encoded", "이전 신청자", AdminRole.MANAGER);
        ReflectionTestUtils.setField(previousRequester, "id", 77L);
        EventApprovalRequest approvalRequest = EventApprovalRequest.create(event, previousRequester);
        AdminUser reviewer = AdminUser.createSuperAdmin("reviewer@wearagain.kr", "encoded", "검토 관리자");
        ReflectionTestUtils.setField(reviewer, "id", 55L);
        approvalRequest.approve(reviewer, LocalDateTime.now().minusDays(1), EventStatus.OPEN);
        when(eventApprovalRequestRepository.findByEvent_Id(101L)).thenReturn(java.util.Optional.of(approvalRequest));

        EventAdminUpdateRequest request = new EventAdminUpdateRequest(
                "매니저 수정",
                "운영자가 내용을 수정합니다.",
                "이용 안내 문구",
                "주의 사항 문구",
                "서울특별시 종로구 1",
                defaultStartDate.plusDays(1),
                defaultEndDate.plusDays(2),
                null,
                null,
                null
        );

        EventAdminDetailResponse response = eventAdminService.updateEvent(101L, request, 11L, AdminRole.MANAGER);

        assertThat(event.getStatus()).isEqualTo(EventStatus.DRAFT);
        assertThat(response.status()).isEqualTo(EventStatus.DRAFT);
        assertThat(approvalRequest.getProcessedAt()).isNull();
        assertThat(approvalRequest.getProcessedByAdmin()).isNull();
        assertThat(approvalRequest.getRequestingAdmin()).isEqualTo(adminUser);
        verify(eventApprovalRequestRepository, never()).save(any(EventApprovalRequest.class));
    }

    @Test
    void should_create_new_approval_request_when_manager_updates_event_without_existing_request() {
        when(eventRepository.findById(101L)).thenReturn(java.util.Optional.of(event), java.util.Optional.of(event));
        when(eventRepository.findWithDetailsById(101L)).thenReturn(java.util.Optional.of(event));
        when(eventOptionRepository.sumCapacityByEventIds(anyCollection())).thenReturn(List.of());
        when(eventApplicationRepository.countActiveApplicationsByEventIds(anyCollection(), anyCollection()))
                .thenReturn(List.of());
        when(eventApplicationRepository.countActiveApplicationsByOptionIds(anyCollection(), anyCollection()))
                .thenReturn(List.of());
        when(eventApplicationRepository.findAllWithUserByEventId(101L)).thenReturn(List.<EventApplication>of());

        ArgumentCaptor<EventApprovalRequest> captor = ArgumentCaptor.forClass(EventApprovalRequest.class);
        when(eventApprovalRequestRepository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        EventAdminUpdateRequest request = new EventAdminUpdateRequest(
                "매니저 신규 수정",
                "승인을 다시 요청합니다.",
                "변경된 이용 안내",
                "변경된 주의 사항",
                "서울특별시 강남구 1",
                defaultStartDate.plusDays(3),
                defaultEndDate.plusDays(4),
                null,
                null,
                null
        );

        EventAdminDetailResponse response = eventAdminService.updateEvent(101L, request, 11L, AdminRole.MANAGER);

        assertThat(event.getStatus()).isEqualTo(EventStatus.DRAFT);
        assertThat(response.status()).isEqualTo(EventStatus.DRAFT);
        verify(eventApprovalRequestRepository).save(any(EventApprovalRequest.class));
        EventApprovalRequest savedRequest = captor.getValue();
        assertThat(savedRequest.getEvent()).isEqualTo(event);
        assertThat(savedRequest.getRequestingAdmin()).isEqualTo(adminUser);
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
        when(eventRepository.findWithDetailsById(101L)).thenReturn(java.util.Optional.of(event));
        when(eventOptionRepository.sumCapacityByEventIds(anyCollection())).thenReturn(List.of());
        when(eventApplicationRepository.countActiveApplicationsByEventIds(anyCollection(), anyCollection()))
                .thenReturn(List.of());
        when(eventApplicationRepository.countActiveApplicationsByOptionIds(anyCollection(), anyCollection()))
                .thenReturn(List.of());
        when(eventApplicationRepository.findAllWithUserByEventId(101L)).thenReturn(List.<EventApplication>of());

        EventAdminDetailResponse response = eventAdminService.updateEventStatus(101L, EventStatus.CLOSED, AdminRole.SUPER_ADMIN);

        assertThat(event.getStatus()).isEqualTo(EventStatus.CLOSED);
        assertThat(response.status()).isEqualTo(EventStatus.CLOSED);
    }

    @Test
    void should_forbid_manager_status_change() {
        when(eventRepository.findById(101L)).thenReturn(java.util.Optional.of(event));

        assertThatThrownBy(() -> eventAdminService.updateEventStatus(101L, EventStatus.CLOSED, AdminRole.MANAGER))
                .isInstanceOf(EventException.class)
                .hasFieldOrPropertyWithValue("errorCode", EventErrorCode.EVENT_STATUS_UPDATE_FORBIDDEN);
    }

    @Test
    void should_reject_invalid_status_transition() {
        event.changeStatus(EventStatus.ARCHIVED);
        when(eventRepository.findById(101L)).thenReturn(java.util.Optional.of(event));

        assertThatThrownBy(() -> eventAdminService.updateEventStatus(101L, EventStatus.OPEN, AdminRole.ADMIN))
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
    void should_fail_archive_when_already_archived() {
        event.changeStatus(EventStatus.ARCHIVED);
        when(eventRepository.findById(101L)).thenReturn(java.util.Optional.of(event));

        assertThatThrownBy(() -> eventAdminService.archiveEvent(101L))
                .isInstanceOf(EventException.class)
                .hasFieldOrPropertyWithValue("errorCode", EventErrorCode.EVENT_ALREADY_ARCHIVED);
        verify(eventApplicationRepository, never()).countActiveApplicationsByEventIds(anyCollection(), anyCollection());
    }

    private EventAdminCreateRequest createValidCreateRequest() {
        List<EventAdminCreateRequest.EventAdminCreateImageRequest> images = List.of(
                new EventAdminCreateRequest.EventAdminCreateImageRequest("https://wearagain.kr/1.jpg", "대표", 1),
                new EventAdminCreateRequest.EventAdminCreateImageRequest("https://wearagain.kr/2.jpg", "설명", 2)
        );

        List<EventAdminCreateRequest.EventAdminCreateOptionRequest> options = List.of(
                new EventAdminCreateRequest.EventAdminCreateOptionRequest(
                        "11월 15일",
                        1,
                        null,
                        List.of(
                                new EventAdminCreateRequest.EventAdminCreateOptionRequest(
                                        "오전 세션",
                                        1,
                                        null,
                                        List.of(
                                                new EventAdminCreateRequest.EventAdminCreateOptionRequest(
                                                        "A조",
                                                        1,
                                                        10,
                                                        List.of()
                                                )
                                        )
                                )
                        )
                ),
                new EventAdminCreateRequest.EventAdminCreateOptionRequest(
                        "11월 22일",
                        2,
                        null,
                        List.of()
                )
        );

        return new EventAdminCreateRequest(
                "지속가능 패션 행사",
                "재사용 패션 실습을 진행합니다.",
                "개인 텀블러를 지참해주세요.",
                "발화성 물질 반입 금지",
                "서울시 마포구 연남동",
                defaultStartDate,
                defaultEndDate,
                images,
                options
        );
    }

    private Event buildPersistedEvent(EventAdminCreateRequest request) {
        Event event = Event.create(
                request.title(),
                request.description(),
                request.startDate(),
                request.endDate(),
                request.location(),
                EventStatus.DRAFT,
                adminUser,
                request.usageGuide(),
                request.precautions()
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
            EventAdminCreateRequest.EventAdminCreateOptionRequest request,
            long baseId
    ) {
        EventOption option = EventOption.create(
                event,
                parent,
                request.name(),
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
