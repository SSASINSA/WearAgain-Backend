package com.ssasinsa.wearagain.domain.event.service;

import com.ssasinsa.wearagain.domain.auth.entity.AdminRole;
import com.ssasinsa.wearagain.domain.auth.infrastructure.security.AdminAuthenticatedUser;
import com.ssasinsa.wearagain.domain.event.dto.manager.ManagerEventParticipantEventSummaryResponse;
import com.ssasinsa.wearagain.domain.event.dto.manager.ManagerEventParticipantKeywordScope;
import com.ssasinsa.wearagain.domain.event.dto.manager.ManagerEventParticipantListItemResponse;
import com.ssasinsa.wearagain.domain.event.dto.manager.ManagerEventParticipantListResponse;
import com.ssasinsa.wearagain.domain.event.dto.manager.ManagerEventParticipantListItemResponse.EventPeriod;
import com.ssasinsa.wearagain.domain.event.dto.manager.ManagerEventParticipantSort;
import com.ssasinsa.wearagain.domain.event.dto.manager.ManagerEventParticipantSummaryResponse;
import com.ssasinsa.wearagain.domain.event.dto.manager.ManagerEventParticipantCancelRequest;
import com.ssasinsa.wearagain.domain.event.entity.Event;
import com.ssasinsa.wearagain.domain.event.entity.EventApplication;
import com.ssasinsa.wearagain.domain.event.entity.EventApplicationStatus;
import com.ssasinsa.wearagain.domain.event.entity.EventOption;
import com.ssasinsa.wearagain.domain.event.exception.EventErrorCode;
import com.ssasinsa.wearagain.domain.event.exception.EventException;
import com.ssasinsa.wearagain.domain.event.repository.EventApplicationRepository;
import com.ssasinsa.wearagain.domain.event.repository.EventApplicationSpecifications;
import com.ssasinsa.wearagain.domain.event.repository.EventRepository;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminParticipantDetailResponse;
import com.ssasinsa.wearagain.domain.user.service.UserAdminService;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EventParticipantManagerServiceImpl implements EventParticipantManagerService {

    private static final List<EventApplicationStatus> SUMMARY_STATUSES = List.of(
            EventApplicationStatus.APPLIED,
            EventApplicationStatus.CHECKED_IN,
            EventApplicationStatus.CANCELED,
            EventApplicationStatus.REJECTED
    );

    private final EventApplicationRepository eventApplicationRepository;
    private final EventRepository eventRepository;
    private final UserAdminService userAdminService;

    @Override
    public ManagerEventParticipantListResponse getParticipants(
            AdminAuthenticatedUser principal,
            Long eventId,
            EventApplicationStatus status,
            Boolean suspended,
            String keyword,
            ManagerEventParticipantKeywordScope keywordScope,
            int page,
            int size,
            ManagerEventParticipantSort sort
    ) {
        ensureAuthenticated(principal);
        keywordScope = keywordScope == null ? ManagerEventParticipantKeywordScope.ALL : keywordScope;
        sort = sort == null ? ManagerEventParticipantSort.LATEST : sort;

        Specification<EventApplication> baseSpec = buildBaseSpecification(
                principal,
                eventId,
                keyword,
                keywordScope,
                suspended
        );
        Specification<EventApplication> listSpec = status == null
                ? baseSpec
                : baseSpec.and(EventApplicationSpecifications.statusEquals(status));
        Pageable pageable = PageRequest.of(page, size, resolveSort(sort));

        Page<EventApplication> result = eventApplicationRepository.findAll(listSpec, pageable);
        if (result.isEmpty()) {
            return ManagerEventParticipantListResponse.empty(size);
        }

        List<EventApplication> applications = loadWithAssociations(eventId, result.getContent());
        List<ManagerEventParticipantListItemResponse> content = applications.stream()
                .map(this::toListItem)
                .toList();

        ManagerEventParticipantSummaryResponse summary = buildSummary(
                baseSpec,
                eventId,
                result,
                applications
        );

        return new ManagerEventParticipantListResponse(
                content,
                result.getTotalElements(),
                result.getTotalPages(),
                result.getSize(),
                result.getNumber(),
                result.hasNext(),
                result.hasPrevious(),
                summary
        );
    }

    @Override
    public AdminParticipantDetailResponse getParticipantDetail(
            Long eventId,
            Long applicationId,
            AdminAuthenticatedUser principal
    ) {
        ensureAuthenticated(principal);
        EventApplication application = eventApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new EventException(EventErrorCode.EVENT_APPLICATION_NOT_FOUND));

        if (application.getEvent() == null
                || !Objects.equals(application.getEvent().getId(), eventId)
                || (isManager(principal) && (application.getEvent().getOrganizerAdmin() == null
                || !Objects.equals(application.getEvent().getOrganizerAdmin().getId(), principal.adminId())))) {
            throw new EventException(EventErrorCode.EVENT_APPLICATION_NOT_FOUND);
        }

        return userAdminService.getParticipantDetail(application.getUser().getId());
    }

    @Override
    @Transactional
    public void cancelApplication(
            Long eventId,
            Long applicationId,
            ManagerEventParticipantCancelRequest request,
            AdminAuthenticatedUser principal
    ) {
        ensureAuthenticated(principal);
        EventApplication application = eventApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new EventException(EventErrorCode.EVENT_APPLICATION_NOT_FOUND));

        if (application.getEvent() == null
                || !Objects.equals(application.getEvent().getId(), eventId)
                || (isManager(principal) && (application.getEvent().getOrganizerAdmin() == null
                || !Objects.equals(application.getEvent().getOrganizerAdmin().getId(), principal.adminId())))) {
            throw new EventException(EventErrorCode.EVENT_APPLICATION_NOT_FOUND);
        }

        if (application.getStatus() != EventApplicationStatus.APPLIED
                && application.getStatus() != EventApplicationStatus.CHECKED_IN) {
            throw new EventException(EventErrorCode.EVENT_APPLICATION_NOT_CANCELABLE);
        }

        String reason = request == null ? null : request.reason().trim();
        application.reject(LocalDateTime.now(), reason);
        log.info("[Event] action=ADMIN_CANCEL adminId={} role={} eventId={} applicationId={} targetUserId={}",
                principal.adminId(),
                principal.role(),
                eventId,
                applicationId,
                application.getUser() != null ? application.getUser().getId() : null);
    }

    private Specification<EventApplication> buildBaseSpecification(
            AdminAuthenticatedUser principal,
            Long eventId,
            String keyword,
            ManagerEventParticipantKeywordScope keywordScope,
            Boolean suspended
    ) {
        Specification<EventApplication> specification = alwaysTrueSpecification();
        if (isManager(principal)) {
            specification = specification.and(EventApplicationSpecifications.organizerEquals(principal.adminId()));
        }
        specification = specification
                .and(EventApplicationSpecifications.eventIdEquals(eventId))
                .and(EventApplicationSpecifications.keywordMatches(keyword, keywordScope))
                .and(EventApplicationSpecifications.userSuspended(suspended));
        return specification;
    }

    private ManagerEventParticipantSummaryResponse buildSummary(
            Specification<EventApplication> baseSpec,
            Long eventId,
            Page<EventApplication> result,
            List<EventApplication> applications
    ) {
        Map<EventApplicationStatus, Long> statusCounts = countStatuses(baseSpec);
        List<ManagerEventParticipantEventSummaryResponse> eventSummaries = buildEventSummaries(
                baseSpec,
                eventId,
                applications
        );

        return new ManagerEventParticipantSummaryResponse(
                result.getTotalElements(),
                statusCounts.getOrDefault(EventApplicationStatus.APPLIED, 0L),
                statusCounts.getOrDefault(EventApplicationStatus.CHECKED_IN, 0L),
                statusCounts.getOrDefault(EventApplicationStatus.CANCELED, 0L),
                statusCounts.getOrDefault(EventApplicationStatus.REJECTED, 0L),
                eventSummaries
        );
    }

    private Map<EventApplicationStatus, Long> countStatuses(Specification<EventApplication> baseSpec) {
        Map<EventApplicationStatus, Long> counts = new EnumMap<>(EventApplicationStatus.class);
        for (EventApplicationStatus candidate : SUMMARY_STATUSES) {
            Specification<EventApplication> spec = baseSpec.and(EventApplicationSpecifications.statusEquals(candidate));
            counts.put(candidate, eventApplicationRepository.count(spec));
        }
        return counts;
    }

    private List<ManagerEventParticipantEventSummaryResponse> buildEventSummaries(
            Specification<EventApplication> baseSpec,
            Long eventId,
            List<EventApplication> applications
    ) {
        if (eventId == null) {
            return List.of();
        }
        String eventTitle = applications.stream()
                .map(EventApplication::getEvent)
                .filter(Objects::nonNull)
                .filter(event -> Objects.equals(event.getId(), eventId))
                .map(Event::getTitle)
                .findFirst()
                .orElseGet(() -> eventRepository.findById(eventId)
                        .map(Event::getTitle)
                        .orElse(null));
        long count = eventApplicationRepository.count(baseSpec);
        return List.of(new ManagerEventParticipantEventSummaryResponse(eventId, eventTitle, count));
    }

    private ManagerEventParticipantListItemResponse toListItem(EventApplication application) {
        Event event = application.getEvent();
        return new ManagerEventParticipantListItemResponse(
                application.getId(),
                event == null ? null : event.getId(),
                event == null ? null : event.getTitle(),
                toEventPeriod(event),
                application.getUser() == null ? null : application.getUser().getId(),
                application.getUser() == null ? null : application.getUser().getDisplayName(),
                application.getUser() == null ? null : application.getUser().getEmail(),
                buildOptionPath(application.getEventOption()),
                application.getStatus() == null ? null : application.getStatus().name(),
                toOffset(application.getCreatedAt()),
                toOffset(application.getCheckedInAt()),
                application.getUser() != null && application.getUser().isSuspended()
        );
    }

    private EventPeriod toEventPeriod(Event event) {
        if (event == null || event.getStartDate() == null || event.getEndDate() == null) {
            return null;
        }
        return new EventPeriod(event.getStartDate(), event.getEndDate());
    }

    private String buildOptionPath(EventOption option) {
        if (option == null) {
            return null;
        }
        List<String> names = new ArrayList<>();
        EventOption current = option;
        while (current != null) {
            if (StringUtils.hasText(current.getName())) {
                names.add(current.getName());
            }
            current = current.getParentOption();
        }
        if (names.isEmpty()) {
            return null;
        }
        List<String> reversed = new ArrayList<>();
        for (int i = names.size() - 1; i >= 0; i--) {
            reversed.add(names.get(i));
        }
        return String.join(" > ", reversed);
    }

    private OffsetDateTime toOffset(java.time.LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.atOffset(ZoneOffset.UTC);
    }

    private Sort resolveSort(ManagerEventParticipantSort sort) {
        return switch (sort) {
            case OLDEST -> Sort.by(Sort.Order.asc("createdAt"), Sort.Order.asc("id"));
            case BY_EVENT -> Sort.by(Sort.Order.asc("event.title"), Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
            case BY_USER -> Sort.by(Sort.Order.asc("user.displayName"), Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
            case LATEST -> Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
        };
    }

    private void ensureAuthenticated(AdminAuthenticatedUser principal) {
        if (principal == null || principal.role() == null) {
            throw new EventException(EventErrorCode.EVENT_ADMIN_NOT_FOUND);
        }
    }

    private boolean isManager(AdminAuthenticatedUser principal) {
        return principal != null && principal.role() == AdminRole.MANAGER;
    }

    private List<EventApplication> loadWithAssociations(Long eventId, List<EventApplication> pageContent) {
        if (pageContent.isEmpty()) {
            return List.of();
        }
        List<Long> ids = pageContent.stream()
                .map(EventApplication::getId)
                .filter(Objects::nonNull)
                .toList();
        if (ids.isEmpty()) {
            return pageContent;
        }
        Map<Long, EventApplication> fetched = eventApplicationRepository.findAllWithAssociationsByEventIdAndIdIn(eventId, ids)
                .stream()
                .collect(Collectors.toMap(EventApplication::getId, Function.identity()));
        return ids.stream()
                .map(fetched::get)
                .filter(Objects::nonNull)
                .toList();
    }

    private Specification<EventApplication> alwaysTrueSpecification() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
    }
}
