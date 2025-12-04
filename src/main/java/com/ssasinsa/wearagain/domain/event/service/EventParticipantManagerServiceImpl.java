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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
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
            Set<Long> eventIds,
            Set<String> eventCodes,
            EventApplicationStatus status,
            Boolean suspended,
            String keyword,
            ManagerEventParticipantKeywordScope keywordScope,
            int page,
            int size,
            ManagerEventParticipantSort sort
    ) {
        ensureAuthenticated(principal);
        eventIds = eventIds == null ? Set.of() : eventIds;
        eventCodes = eventCodes == null ? Set.of() : eventCodes;
        keywordScope = keywordScope == null ? ManagerEventParticipantKeywordScope.ALL : keywordScope;
        sort = sort == null ? ManagerEventParticipantSort.LATEST : sort;

        Specification<EventApplication> baseSpec = buildBaseSpecification(
                principal,
                eventIds,
                eventCodes,
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

        List<EventApplication> applications = loadWithAssociations(result.getContent());
        List<ManagerEventParticipantListItemResponse> content = applications.stream()
                .map(this::toListItem)
                .toList();

        ManagerEventParticipantSummaryResponse summary = buildSummary(
                baseSpec,
                principal,
                eventIds,
                eventCodes,
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
    }

    private Specification<EventApplication> buildBaseSpecification(
            AdminAuthenticatedUser principal,
            Set<Long> eventIds,
            Set<String> eventCodes,
            String keyword,
            ManagerEventParticipantKeywordScope keywordScope,
            Boolean suspended
    ) {
        Specification<EventApplication> specification = alwaysTrueSpecification();
        if (isManager(principal)) {
            specification = specification.and(EventApplicationSpecifications.organizerEquals(principal.adminId()));
        }
        specification = specification
                .and(EventApplicationSpecifications.eventIdIn(eventIds))
                .and(EventApplicationSpecifications.eventCodesIn(eventCodes))
                .and(EventApplicationSpecifications.keywordMatches(keyword, keywordScope))
                .and(EventApplicationSpecifications.userSuspended(suspended));
        return specification;
    }

    private ManagerEventParticipantSummaryResponse buildSummary(
            Specification<EventApplication> baseSpec,
            AdminAuthenticatedUser principal,
            Set<Long> eventIds,
            Set<String> eventCodes,
            Page<EventApplication> result,
            List<EventApplication> applications
    ) {
        Map<EventApplicationStatus, Long> statusCounts = countStatuses(baseSpec);
        Set<Long> scopedEventIds = resolveEventScope(principal, eventIds, eventCodes, applications);
        List<ManagerEventParticipantEventSummaryResponse> eventSummaries = buildEventSummaries(
                baseSpec,
                scopedEventIds,
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
            Set<Long> eventIds,
            List<EventApplication> applications
    ) {
        if (eventIds.isEmpty()) {
            return List.of();
        }
        Map<Long, String> eventTitles = new LinkedHashMap<>();
        applications.stream()
                .map(EventApplication::getEvent)
                .filter(Objects::nonNull)
                .forEach(event -> eventTitles.putIfAbsent(event.getId(), event.getTitle()));
        eventRepository.findAllById(eventIds).forEach(event ->
                eventTitles.putIfAbsent(event.getId(), event.getTitle())
        );

        List<ManagerEventParticipantEventSummaryResponse> responses = new ArrayList<>();
        for (Long eventId : eventIds) {
            Specification<EventApplication> spec = baseSpec.and(EventApplicationSpecifications.eventIdEquals(eventId));
            long count = eventApplicationRepository.count(spec);
            responses.add(new ManagerEventParticipantEventSummaryResponse(
                    eventId,
                    eventTitles.get(eventId),
                    count
            ));
        }
        return responses;
    }

    private Set<Long> resolveEventScope(
            AdminAuthenticatedUser principal,
            Set<Long> eventIds,
            Set<String> eventCodes,
            List<EventApplication> applications
    ) {
        if (eventIds != null && !eventIds.isEmpty()) {
            return new LinkedHashSet<>(eventIds);
        }
        if (eventCodes != null && !eventCodes.isEmpty()) {
            return eventRepository.findAllByStaffCodeIn(eventCodes)
                    .stream()
                    .filter(event -> !isManager(principal)
                            || (event.getOrganizerAdmin() != null
                            && Objects.equals(event.getOrganizerAdmin().getId(), principal.adminId())))
                    .map(Event::getId)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }
        return applications.stream()
                .map(EventApplication::getEvent)
                .filter(Objects::nonNull)
                .map(Event::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
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
                toOffset(application.getUpdatedAt()),
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

    private List<EventApplication> loadWithAssociations(List<EventApplication> pageContent) {
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
        Map<Long, EventApplication> fetched = eventApplicationRepository.findAllWithAssociationsByIdIn(ids)
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
