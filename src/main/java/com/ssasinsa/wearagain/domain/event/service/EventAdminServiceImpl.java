package com.ssasinsa.wearagain.domain.event.service;

import com.ssasinsa.wearagain.domain.auth.entity.AdminRole;
import com.ssasinsa.wearagain.domain.auth.entity.AdminUser;
import com.ssasinsa.wearagain.domain.auth.repository.AdminUserRepository;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventAdminCreateRequest;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventAdminCreateRequest.EventAdminCreateImageRequest;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventAdminCreateRequest.EventAdminCreateOptionRequest;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventAdminDetailResponse;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventAdminDetailResponse.EventAdminApplicationResponse;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventAdminDetailResponse.EventAdminImageResponse;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventAdminDetailResponse.EventAdminOptionResponse;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventAdminListResponse;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventAdminSummaryResponse;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventAdminUpdateRequest;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventAdminUpdateRequest.EventAdminImageRequest;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventAdminUpdateRequest.EventAdminOptionRequest;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventApplicationRejectRequest;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventApplicationRejectResponse;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventStaffCodeResponse;
import com.ssasinsa.wearagain.domain.event.dto.response.EventCreateResponse;
import com.ssasinsa.wearagain.domain.event.dto.response.EventCreateResponse.EventCreateImageResponse;
import com.ssasinsa.wearagain.domain.event.dto.response.EventCreateResponse.EventCreateOptionResponse;
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
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.EnumSet;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class EventAdminServiceImpl implements EventAdminService {

    private static final Pattern HTTPS_URL_PATTERN = Pattern.compile("^(https?://|/|file:).+", Pattern.CASE_INSENSITIVE);
    private static final int MAX_EVENT_DURATION_DAYS = 365;
    private static final int MAX_IMAGE_COUNT = 10;
    private static final int MAX_OPTION_DEPTH = 3;
    private static final int MAX_OPTION_CAPACITY = 999;

    private static final Comparator<EventImage> IMAGE_ORDER = Comparator.comparingInt(EventImage::getDisplayOrder);
    private static final Comparator<EventOption> OPTION_ORDER = Comparator.comparingInt(EventOption::getDisplayOrder);

    private static final EnumSet<EventApplicationStatus> ACTIVE_APPLICATION_STATUSES =
            EnumSet.of(EventApplicationStatus.APPLIED, EventApplicationStatus.CHECKED_IN);

    private static final Map<EventStatus, EnumSet<EventStatus>> ALLOWED_STATUS_TRANSITIONS = createStatusTransitions();
    private static final SecureRandom STAFF_CODE_RANDOM = new SecureRandom();
    private static final int STAFF_CODE_LENGTH = 6;

    private final EventRepository eventRepository;
    private final EventOptionRepository eventOptionRepository;
    private final EventApplicationRepository eventApplicationRepository;
    private final AdminUserRepository adminUserRepository;

    @Override
    @Transactional
    public EventCreateResponse createEvent(EventAdminCreateRequest request, Long adminId) {
        AdminUser organizer = adminUserRepository.findById(adminId)
                .orElseThrow(() -> new EventException(EventErrorCode.EVENT_ADMIN_NOT_FOUND));

        validateEventPeriod(request.startDate(), request.endDate());

        EventStatus status = EventStatus.DRAFT;
        Event event = Event.create(
                request.title().trim(),
                request.description().trim(),
                request.startDate(),
                request.endDate(),
                request.location().trim(),
                status,
                organizer,
                normalizeText(request.usageGuide()),
                normalizeText(request.precautions())
        );

        List<EventAdminCreateImageRequest> createImages = request.images();
        if (CollectionUtils.isEmpty(createImages)) {
            throw new EventException(EventErrorCode.INVALID_IMAGE_INFORMATION);
        }
        List<EventAdminImageRequest> imageRequests = createImages.stream()
                .map(image -> new EventAdminImageRequest(
                        image.url(),
                        image.altText(),
                        Integer.valueOf(image.displayOrder())
                ))
                .toList();
        List<EventImage> images = buildEventImages(event, imageRequests);
        event.assignImages(images);

        List<EventAdminOptionRequest> optionRequests = convertCreateOptions(request.options());
        List<EventOption> options = buildEventOptions(event, optionRequests);
        event.assignOptions(options);

        Event savedEvent;
        try {
            savedEvent = eventRepository.save(event);
        } catch (Exception exception) {
            throw new EventException(EventErrorCode.EVENT_REGISTRATION_FAILED, exception);
        }

        return mapToCreateResponse(savedEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public EventAdminListResponse getEvents(String status, int page, int size) {
        if (page < 0 || size <= 0 || size > 50) {
            throw new EventException(EventErrorCode.INVALID_EVENT_QUERY);
        }
        EnumSet<EventStatus> statuses = resolveStatuses(status);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "startDate").and(Sort.by("id")));
        Page<Event> result = eventRepository.findByStatusIn(statuses, pageable);

        List<Event> events = result.getContent();

        Map<Long, Long> capacityMap = loadCapacityByEventIds(events);
        Map<Long, Long> appliedMap = loadAppliedCountByEventIds(events);

        List<EventAdminSummaryResponse> summaries = events.stream()
                .map(event -> mapToSummary(event, capacityMap, appliedMap))
                .toList();

        return new EventAdminListResponse(
                summaries,
                page,
                size,
                result.getTotalElements(),
                result.getTotalPages(),
                result.hasNext()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public EventAdminDetailResponse getEventDetail(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventException(EventErrorCode.EVENT_NOT_FOUND));

        List<EventOption> rootOptions = event.getOptions()
                .stream()
                .filter(option -> option.getParentOption() == null)
                .sorted(OPTION_ORDER)
                .toList();

        Map<Long, Long> capacityMap = loadCapacityByEventIds(List.of(event));
        Map<Long, Long> appliedMap = loadAppliedCountByEventIds(List.of(event));
        Map<Long, Long> optionAppliedMap = loadApplicationCounts(rootOptions);

        List<EventAdminImageResponse> images = event.getImages()
                .stream()
                .sorted(IMAGE_ORDER)
                .map(image -> new EventAdminImageResponse(
                        image.getId(),
                        image.getUrl(),
                        image.getAltText(),
                        image.getDisplayOrder()
                ))
                .toList();

        List<EventAdminOptionResponse> options = rootOptions.stream()
                .map(option -> mapOption(option, optionAppliedMap))
                .toList();

        List<EventAdminApplicationResponse> applications = eventApplicationRepository.findAllWithUserByEventId(eventId)
                .stream()
                .map(this::mapApplication)
                .toList();

        Long totalCapacity = capacityMap.getOrDefault(event.getId(), null);
        Long appliedCount = appliedMap.getOrDefault(event.getId(), 0L);
        Long remaining = totalCapacity == null ? null : Math.max(0L, totalCapacity - appliedCount);
        AdminUser organizerAdmin = event.getOrganizerAdmin();
        String organizerName = organizerAdmin == null ? null : organizerAdmin.getName();
        String organizerEmail = organizerAdmin == null ? null : organizerAdmin.getEmail();

        return new EventAdminDetailResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getUsageGuide(),
                event.getPrecautions(),
                event.getLocation(),
                organizerName,
                organizerEmail,
                organizerAdmin == null ? null : organizerAdmin.getId(),
                organizerAdmin == null ? null : organizerAdmin.getEmail(),
                organizerAdmin == null ? null : organizerAdmin.getName(),
                event.getStartDate(),
                event.getEndDate(),
                event.getStatus(),
                totalCapacity,
                appliedCount,
                remaining,
                event.getStaffCode(),
                toOffset(event.getStaffCodeIssuedAt()),
                toOffset(event.getCreatedAt()),
                toOffset(event.getUpdatedAt()),
                images,
                options,
                applications
        );
    }

    @Override
    @Transactional
    public EventAdminDetailResponse updateEvent(Long eventId, EventAdminUpdateRequest request, Long adminId, AdminRole role) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventException(EventErrorCode.EVENT_NOT_FOUND));

        enforceUpdatePermission(event, adminId, role);

        LocalDate startDate = request.startDate() != null ? request.startDate() : event.getStartDate();
        LocalDate endDate = request.endDate() != null ? request.endDate() : event.getEndDate();
        validateEventPeriod(startDate, endDate);

        if (StringUtils.hasText(request.title())) {
            event.updateTitle(request.title().trim());
        }
        if (StringUtils.hasText(request.description())) {
            event.updateDescription(request.description().trim());
        }
        if (request.usageGuide() != null) {
            event.updateUsageGuide(normalizeText(request.usageGuide()));
        }
        if (request.precautions() != null) {
            event.updatePrecautions(normalizeText(request.precautions()));
        }
        if (StringUtils.hasText(request.location())) {
            event.updateLocation(request.location().trim());
        }
        event.updatePeriod(startDate, endDate);

        if (request.status() != null) {
            event.changeStatus(request.status());
        }

        if (request.images() != null) {
            List<EventImage> images = buildEventImages(event, request.images());
            event.assignImages(images);
        }

        if (request.options() != null) {
            List<EventOption> options = buildEventOptions(event, request.options());
            event.assignOptions(options);
        }

        return getEventDetail(eventId);
    }

    @Override
    @Transactional
    public EventAdminDetailResponse updateEventStatus(Long eventId, EventStatus status, AdminRole role) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventException(EventErrorCode.EVENT_NOT_FOUND));

        enforceStatusChangePermission(role);
        validateStatusTransition(event.getStatus(), status);
        if (event.getStatus() != status) {
            event.changeStatus(status);
        }
        return getEventDetail(eventId);
    }

    @Override
    @Transactional
    public void archiveEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventException(EventErrorCode.EVENT_NOT_FOUND));
        if (event.getStatus() == EventStatus.ARCHIVED) {
            throw new EventException(EventErrorCode.EVENT_ALREADY_ARCHIVED);
        }
        boolean hasActiveApplications = eventApplicationRepository.countActiveApplicationsByEventIds(
                List.of(eventId), ACTIVE_APPLICATION_STATUSES
        ).stream().anyMatch(count -> count.eventId().equals(eventId) && count.appliedCount() > 0);
        if (hasActiveApplications) {
            throw new EventException(EventErrorCode.EVENT_APPLICATION_ALREADY_PROCESSED);
        }
        event.changeStatus(EventStatus.ARCHIVED);
    }

    @Override
    @Transactional
    public EventApplicationRejectResponse rejectApplication(Long applicationId, EventApplicationRejectRequest request) {
        EventApplication application = eventApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new EventException(EventErrorCode.EVENT_APPLICATION_NOT_FOUND));
        if (application.getStatus() != EventApplicationStatus.APPLIED) {
            throw new EventException(EventErrorCode.EVENT_APPLICATION_ALREADY_PROCESSED);
        }
        String trimmedReason = request.reason().trim();
        application.reject(LocalDateTime.now(), trimmedReason);
        return new EventApplicationRejectResponse(application.getId(), application.getStatus().name());
    }

    @Override
    @Transactional
    public EventStaffCodeResponse issueStaffCode(Long eventId, Long adminId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventException(EventErrorCode.EVENT_NOT_FOUND));
        enforceStaffCodePermission(event, adminId);

        String staffCode = generateStaffCode();
        LocalDateTime issuedAt = LocalDateTime.now(ZoneOffset.UTC);
        event.updateStaffCode(staffCode, issuedAt);

        return EventStaffCodeResponse.of(event.getId(), staffCode, toOffset(issuedAt));
    }

    @Override
    @Transactional(readOnly = true)
    public EventStaffCodeResponse getStaffCode(Long eventId, Long adminId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventException(EventErrorCode.EVENT_NOT_FOUND));
        enforceStaffCodePermission(event, adminId);

        if (!StringUtils.hasText(event.getStaffCode())) {
            throw new EventException(EventErrorCode.EVENT_STAFF_CODE_NOT_ISSUED);
        }

        return EventStaffCodeResponse.of(
                event.getId(),
                event.getStaffCode(),
                toOffset(event.getStaffCodeIssuedAt())
        );
    }

    private Map<Long, Long> loadCapacityByEventIds(Collection<Event> events) {
        if (events.isEmpty()) {
            return Map.of();
        }
        List<Long> ids = events.stream().map(Event::getId).toList();
        return eventOptionRepository.sumCapacityByEventIds(ids)
                .stream()
                .collect(Collectors.toMap(EventCapacitySummary::eventId, EventCapacitySummary::totalCapacity));
    }

    private String normalizeText(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private Map<Long, Long> loadAppliedCountByEventIds(Collection<Event> events) {
        if (events.isEmpty()) {
            return Map.of();
        }
        List<Long> ids = events.stream().map(Event::getId).toList();
        return eventApplicationRepository.countActiveApplicationsByEventIds(ids, ACTIVE_APPLICATION_STATUSES)
                .stream()
                .collect(Collectors.toMap(EventApplicationEventCount::eventId, EventApplicationEventCount::appliedCount));
    }

    private Map<Long, Long> loadApplicationCounts(Collection<EventOption> roots) {
        Set<Long> optionIds = collectOptionIds(roots);
        if (optionIds.isEmpty()) {
            return Map.of();
        }
        return eventApplicationRepository.countActiveApplicationsByOptionIds(optionIds, ACTIVE_APPLICATION_STATUSES)
                .stream()
                .collect(Collectors.toMap(EventOptionApplicationCount::eventOptionId, EventOptionApplicationCount::appliedCount));
    }

    private EventAdminSummaryResponse mapToSummary(
            Event event,
            Map<Long, Long> capacityMap,
            Map<Long, Long> appliedMap
    ) {
        Long totalCapacity = capacityMap.getOrDefault(event.getId(), null);
        Long applied = appliedMap.getOrDefault(event.getId(), 0L);
        Long remaining = totalCapacity == null ? null : Math.max(0L, totalCapacity - applied);
        AdminUser organizerAdmin = event.getOrganizerAdmin();
        String organizerName = organizerAdmin == null ? null : organizerAdmin.getName();
        String organizerEmail = organizerAdmin == null ? null : organizerAdmin.getEmail();

        return new EventAdminSummaryResponse(
                event.getId(),
                event.getTitle(),
                event.getStatus().name(),
                event.getStartDate(),
                event.getEndDate(),
                event.getLocation(),
                totalCapacity,
                applied,
                remaining,
                organizerName,
                organizerEmail,
                organizerAdmin == null ? null : organizerAdmin.getId(),
                organizerAdmin == null ? null : organizerAdmin.getEmail(),
                organizerAdmin == null ? null : organizerAdmin.getName()
        );
    }

    private EventCreateResponse mapToCreateResponse(Event event) {
        List<EventCreateImageResponse> imageResponses = event.getImages().stream()
                .sorted(IMAGE_ORDER)
                .map(image -> new EventCreateImageResponse(
                        image.getId(),
                        image.getUrl(),
                        image.getAltText(),
                        image.getDisplayOrder()
                ))
                .toList();

        List<EventCreateOptionResponse> optionResponses = event.getOptions().stream()
                .filter(option -> option.getParentOption() == null)
                .sorted(OPTION_ORDER)
                .map(this::mapOptionToCreateResponse)
                .toList();

        AdminUser organizerAdmin = event.getOrganizerAdmin();
        String organizerName = organizerAdmin == null ? null : organizerAdmin.getName();
        String organizerEmail = organizerAdmin == null ? null : organizerAdmin.getEmail();

        return new EventCreateResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getUsageGuide(),
                event.getPrecautions(),
                event.getLocation(),
                organizerName,
                organizerEmail,
                organizerAdmin == null ? null : organizerAdmin.getId(),
                organizerAdmin == null ? null : organizerAdmin.getEmail(),
                organizerAdmin == null ? null : organizerAdmin.getName(),
                event.getStartDate(),
                event.getEndDate(),
                event.getStatus().name(),
                imageResponses,
                optionResponses,
                toOffset(event.getCreatedAt())
        );
    }

    private EventCreateOptionResponse mapOptionToCreateResponse(EventOption option) {
        List<EventCreateOptionResponse> children = option.getChildOptions().stream()
                .sorted(OPTION_ORDER)
                .map(this::mapOptionToCreateResponse)
                .toList();

        return new EventCreateOptionResponse(
                option.getId(),
                option.getName(),
                option.getType(),
                option.getDisplayOrder(),
                option.getCapacity(),
                children
        );
    }

    private EventAdminOptionResponse mapOption(EventOption option, Map<Long, Long> counts) {
        Long optionId = option.getId();
        long applied = optionId == null ? 0L : counts.getOrDefault(optionId, 0L);
        Integer appliedCount = safeToInteger(applied);
        Integer capacity = option.getCapacity();
        Integer remaining = capacity == null ? null : Math.max(0, capacity - appliedCount);

        List<EventAdminOptionResponse> children = option.getChildOptions()
                .stream()
                .sorted(OPTION_ORDER)
                .map(child -> mapOption(child, counts))
                .toList();

        return new EventAdminOptionResponse(
                option.getId(),
                option.getName(),
                option.getType(),
                option.getDisplayOrder(),
                capacity,
                appliedCount,
                remaining,
                children
        );
    }

    private EventAdminApplicationResponse mapApplication(EventApplication application) {
        return new EventAdminApplicationResponse(
                application.getId(),
                application.getUser().getEmail(),
                application.getUser().getDisplayName(),
                application.getEventOption().getId(),
                application.getStatus().name(),
                toOffset(application.getCreatedAt()),
                application.getReason()
        );
    }

    private void validateEventPeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new EventException(EventErrorCode.MISSING_REQUIRED_VALUE);
        }
        if (endDate.isBefore(startDate)) {
            throw new EventException(EventErrorCode.INVALID_EVENT_PERIOD);
        }
        long days = Math.abs(endDate.toEpochDay() - startDate.toEpochDay());
        if (days > MAX_EVENT_DURATION_DAYS) {
            throw new EventException(EventErrorCode.INVALID_EVENT_PERIOD);
        }
    }

    private List<EventImage> buildEventImages(Event event, List<EventAdminImageRequest> requests) {
        if (requests.size() > MAX_IMAGE_COUNT) {
            throw new EventException(EventErrorCode.INVALID_IMAGE_INFORMATION);
        }
        Set<Integer> orders = new HashSet<>();
        List<EventImage> images = new ArrayList<>();
        for (EventAdminImageRequest request : requests) {
            if (request.displayOrder() == null || request.displayOrder() <= 0 || !orders.add(request.displayOrder())) {
                throw new EventException(EventErrorCode.INVALID_IMAGE_INFORMATION);
            }
            if (!StringUtils.hasText(request.url())) {
                throw new EventException(EventErrorCode.INVALID_IMAGE_INFORMATION);
            }
            String trimmedUrl = request.url().trim();
            if (!HTTPS_URL_PATTERN.matcher(trimmedUrl).matches()) {
                throw new EventException(EventErrorCode.INVALID_IMAGE_INFORMATION);
            }
            EventImage image = EventImage.create(
                    event,
                    trimmedUrl,
                    request.altText() == null ? null : request.altText().trim(),
                    request.displayOrder()
            );
            images.add(image);
        }
        images.sort(Comparator.comparingInt(EventImage::getDisplayOrder));
        return images;
    }

    private List<EventAdminOptionRequest> convertCreateOptions(List<EventAdminCreateOptionRequest> requests) {
        if (CollectionUtils.isEmpty(requests)) {
            return List.of();
        }
        List<EventAdminOptionRequest> converted = new ArrayList<>();
        for (EventAdminCreateOptionRequest request : requests) {
            List<EventAdminOptionRequest> children = convertCreateOptions(request.children());
            converted.add(new EventAdminOptionRequest(
                    request.name(),
                    request.type(),
                    request.displayOrder(),
                    request.capacity(),
                    children
            ));
        }
        return converted;
    }

    private List<EventOption> buildEventOptions(Event event, List<EventAdminOptionRequest> requests) {
        if (CollectionUtils.isEmpty(requests)) {
            return List.of();
        }
        validateSiblingConstraints(requests, 1);
        List<EventOption> options = new ArrayList<>();
        for (EventAdminOptionRequest request : requests) {
            EventOption option = createOption(event, null, request, 1);
            options.add(option);
        }
        options.sort(OPTION_ORDER);
        return options;
    }

    private EventOption createOption(
            Event event,
            EventOption parent,
            EventAdminOptionRequest request,
            int depth
    ) {
        if (depth > MAX_OPTION_DEPTH) {
            throw new EventException(EventErrorCode.OPTION_DEPTH_LIMIT_EXCEEDED);
        }

        String normalizedName = request.name() == null ? null : request.name().trim();
        String normalizedType = request.type() == null ? null : request.type().trim();
        if (!StringUtils.hasText(normalizedName) || !StringUtils.hasText(normalizedType)) {
            throw new EventException(EventErrorCode.INVALID_OPTION_STRUCTURE);
        }
        Integer displayOrder = request.displayOrder();
        if (displayOrder == null || displayOrder <= 0) {
            throw new EventException(EventErrorCode.INVALID_OPTION_STRUCTURE);
        }

        Integer capacity = normalizeCapacity(request.capacity());

        EventOption option = EventOption.create(
                event,
                parent,
                normalizedName,
                normalizedType,
                displayOrder,
                capacity
        );

        List<EventAdminOptionRequest> children = request.children();
        if (!CollectionUtils.isEmpty(children)) {
            validateSiblingConstraints(children, depth + 1);
            List<EventOption> childOptions = new ArrayList<>();
            for (EventAdminOptionRequest child : children) {
                EventOption childOption = createOption(event, option, child, depth + 1);
                childOptions.add(childOption);
            }
            childOptions.sort(OPTION_ORDER);
            option.assignChildren(childOptions);
        }
        return option;
    }

    private void validateSiblingConstraints(List<EventAdminOptionRequest> requests, int depth) {
        if (depth > MAX_OPTION_DEPTH) {
            throw new EventException(EventErrorCode.OPTION_DEPTH_LIMIT_EXCEEDED);
        }
        Set<Integer> orders = new HashSet<>();
        Set<String> names = new HashSet<>();
        for (EventAdminOptionRequest request : requests) {
            String normalizedName = request.name() == null ? null : request.name().trim();
            String normalizedType = request.type() == null ? null : request.type().trim();
            if (!StringUtils.hasText(normalizedName) || !StringUtils.hasText(normalizedType)) {
                throw new EventException(EventErrorCode.INVALID_OPTION_STRUCTURE);
            }
            if (!names.add(normalizedName)) {
                throw new EventException(EventErrorCode.DUPLICATE_OPTION);
            }
            Integer displayOrder = request.displayOrder();
            if (displayOrder == null || displayOrder <= 0 || !orders.add(displayOrder)) {
                throw new EventException(EventErrorCode.INVALID_OPTION_STRUCTURE);
            }
            normalizeCapacity(request.capacity());
        }
        validateSequentialOrder(orders, EventErrorCode.INVALID_OPTION_STRUCTURE);
    }

    private Integer normalizeCapacity(Integer capacity) {
        if (capacity == null) {
            return null;
        }
        if (capacity <= 0 || capacity > MAX_OPTION_CAPACITY) {
            throw new EventException(EventErrorCode.INVALID_OPTION_STRUCTURE);
        }
        return capacity;
    }

    private void validateSequentialOrder(Set<Integer> orders, EventErrorCode errorCode) {
        int expected = 1;
        for (int order : orders.stream().sorted().toList()) {
            if (order != expected) {
                throw new EventException(errorCode);
            }
            expected++;
        }
    }

    private void enforceStaffCodePermission(Event event, Long adminId) {
        if (adminId == null) {
            throw new EventException(EventErrorCode.EVENT_STAFF_CODE_FORBIDDEN);
        }
        AdminUser organizer = event.getOrganizerAdmin();
        if (organizer == null || organizer.getId() == null || !organizer.getId().equals(adminId)) {
            throw new EventException(EventErrorCode.EVENT_STAFF_CODE_FORBIDDEN);
        }
    }

    private void enforceUpdatePermission(Event event, Long adminId, AdminRole role) {
        if (role == null || adminId == null) {
            throw new EventException(EventErrorCode.EVENT_UPDATE_FORBIDDEN);
        }
        if (role == AdminRole.SUPER_ADMIN || role == AdminRole.ADMIN) {
            return;
        }
        if (role == AdminRole.MANAGER) {
            AdminUser organizer = event.getOrganizerAdmin();
            if (organizer == null || organizer.getId() == null || !organizer.getId().equals(adminId)) {
                throw new EventException(EventErrorCode.EVENT_UPDATE_FORBIDDEN);
            }
            return;
        }
        throw new EventException(EventErrorCode.EVENT_UPDATE_FORBIDDEN);
    }

    private void enforceStatusChangePermission(AdminRole role) {
        if (role == null || role == AdminRole.MANAGER) {
            throw new EventException(EventErrorCode.EVENT_STATUS_UPDATE_FORBIDDEN);
        }
    }

    private void validateStatusTransition(EventStatus current, EventStatus target) {
        if (target == null) {
            throw new EventException(EventErrorCode.EVENT_STATUS_UPDATE_INVALID);
        }
        EnumSet<EventStatus> allowed = ALLOWED_STATUS_TRANSITIONS.getOrDefault(current, EnumSet.noneOf(EventStatus.class));
        if (!allowed.contains(target)) {
            throw new EventException(EventErrorCode.EVENT_STATUS_UPDATE_INVALID);
        }
    }

    private static Map<EventStatus, EnumSet<EventStatus>> createStatusTransitions() {
        EnumMap<EventStatus, EnumSet<EventStatus>> transitions = new EnumMap<>(EventStatus.class);
        transitions.put(EventStatus.DRAFT, EnumSet.of(EventStatus.DRAFT, EventStatus.OPEN, EventStatus.ARCHIVED));
        transitions.put(EventStatus.OPEN, EnumSet.of(EventStatus.OPEN, EventStatus.CLOSED, EventStatus.ARCHIVED));
        transitions.put(EventStatus.CLOSED, EnumSet.of(EventStatus.CLOSED, EventStatus.ARCHIVED));
        transitions.put(EventStatus.ARCHIVED, EnumSet.of(EventStatus.ARCHIVED));
        return transitions;
    }

    private EnumSet<EventStatus> resolveStatuses(String param) {
        if (!StringUtils.hasText(param)) {
            return EnumSet.allOf(EventStatus.class);
        }
        EnumSet<EventStatus> statuses = EnumSet.noneOf(EventStatus.class);
        StringTokenizer tokenizer = new StringTokenizer(param, ",");
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken().trim();
            if (!StringUtils.hasText(token)) {
                continue;
            }
            try {
                EventStatus status = EventStatus.valueOf(token.toUpperCase());
                statuses.add(status);
            } catch (IllegalArgumentException exception) {
                throw new EventException(EventErrorCode.INVALID_EVENT_QUERY, exception);
            }
        }
        if (statuses.isEmpty()) {
            throw new EventException(EventErrorCode.INVALID_EVENT_QUERY);
        }
        return statuses;
    }

    private Set<Long> collectOptionIds(Collection<EventOption> roots) {
        Set<Long> ids = new HashSet<>();
        Deque<EventOption> stack = new ArrayDeque<>(roots);
        while (!stack.isEmpty()) {
            EventOption option = stack.pop();
            if (option.getId() != null) {
                ids.add(option.getId());
            }
            option.getChildOptions().forEach(stack::push);
        }
        return ids;
    }

    private Integer safeToInteger(long value) {
        if (value > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) value;
    }

    private OffsetDateTime toOffset(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.atOffset(ZoneOffset.UTC);
    }

    private String generateStaffCode() {
        int value = STAFF_CODE_RANDOM.nextInt(1_000_000);
        return String.format("%06d", value);
    }
}
