package com.ssasinsa.wearagain.domain.event.service;

import com.ssasinsa.wearagain.domain.auth.entity.AdminUser;
import com.ssasinsa.wearagain.domain.auth.entity.User;
import com.ssasinsa.wearagain.domain.auth.repository.UserRepository;
import com.ssasinsa.wearagain.domain.event.dto.request.EventApplyRequest;
import com.ssasinsa.wearagain.domain.event.dto.request.EventCancelRequest;
import com.ssasinsa.wearagain.domain.event.dto.response.EventApplicationDetailResponse;
import com.ssasinsa.wearagain.domain.event.dto.response.EventApplicationListResponse;
import com.ssasinsa.wearagain.domain.event.dto.response.EventApplicationQrResponse;
import com.ssasinsa.wearagain.domain.event.dto.response.EventApplicationSummaryResponse;
import com.ssasinsa.wearagain.domain.event.dto.response.EventApplicationSummaryResponse.EventPeriod;
import com.ssasinsa.wearagain.domain.event.dto.response.EventApplyResponse;
import com.ssasinsa.wearagain.domain.event.dto.response.EventCancelResponse;
import com.ssasinsa.wearagain.domain.event.dto.response.EventDetailResponse;
import com.ssasinsa.wearagain.domain.event.dto.response.EventDetailResponse.EventDetailImageResponse;
import com.ssasinsa.wearagain.domain.event.dto.response.EventDetailResponse.EventDetailOptionResponse;
import com.ssasinsa.wearagain.domain.event.dto.response.EventListResponse;
import com.ssasinsa.wearagain.domain.event.dto.response.EventSummaryResponse;
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
import com.ssasinsa.wearagain.domain.event.repository.EventImageRepository;
import com.ssasinsa.wearagain.domain.event.repository.EventRepository;
import com.ssasinsa.wearagain.domain.event.support.CheckinTokenPayload;
import com.ssasinsa.wearagain.domain.event.support.EventApplicationCursor;
import com.ssasinsa.wearagain.global.common.qr.QrTokenStore;
import com.ssasinsa.wearagain.global.exception.CommonErrorCode;
import com.ssasinsa.wearagain.global.exception.CustomException;
import java.time.Duration;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class EventUserServiceImpl implements EventUserService {

    private static final EnumSet<EventStatus> DEFAULT_VISIBLE_STATUSES = EnumSet.of(EventStatus.OPEN);
    private static final EnumSet<EventStatus> APPLICABLE_EVENT_STATUSES =
            EnumSet.of(EventStatus.OPEN, EventStatus.APPROVAL);
    private static final EnumSet<EventApplicationStatus> ACTIVE_APPLICATION_STATUSES =
            EnumSet.of(EventApplicationStatus.APPLIED, EventApplicationStatus.CHECKED_IN);
    private static final EnumSet<EventApplicationStatus> REJECTED_APPLICATION_STATUSES =
            EnumSet.of(EventApplicationStatus.REJECTED);
    private static final Comparator<EventImage> IMAGE_ORDER = Comparator.comparingInt(EventImage::getDisplayOrder);
    private static final Comparator<EventOption> OPTION_ORDER = Comparator.comparingInt(EventOption::getDisplayOrder);
    private static final Duration QR_TOKEN_TTL = Duration.ofMinutes(10);

    private final EventRepository eventRepository;
    private final EventOptionRepository eventOptionRepository;
    private final EventApplicationRepository eventApplicationRepository;
    private final EventImageRepository eventImageRepository;
    private final UserRepository userRepository;
    private final QrTokenStore<CheckinTokenPayload> eventQrTokenStore;
    private final OptionCapacityService optionCapacityService;

    @Override
    @Transactional(readOnly = true)
    public EventListResponse getEvents(String status, String cursor, int size) {
        if (size <= 0 || size > 50) {
            throw new EventException(EventErrorCode.INVALID_EVENT_QUERY);
        }
        EnumSet<EventStatus> statuses = resolveStatuses(status);
        Long cursorValue = parseCursor(cursor);
        Pageable pageable = PageRequest.of(0, size + 1, Sort.by(Sort.Direction.ASC, "startDate").and(Sort.by("id")));
        List<Event> fetched = eventRepository.findEventsAfterCursor(statuses, cursorValue, pageable);

        boolean hasNext = fetched.size() > size;
        List<Event> limited = hasNext ? fetched.subList(0, size) : fetched;

        if (limited.isEmpty()) {
            return new EventListResponse(List.of(), null, false);
        }

        Map<Long, String> thumbnails = loadThumbnails(limited);

        List<EventSummaryResponse> events = limited.stream()
                .map(event -> mapToSummary(event, thumbnails.get(event.getId())))
                .toList();

        String nextCursor = hasNext && !limited.isEmpty()
                ? String.valueOf(limited.get(limited.size() - 1).getId())
                : null;

        return new EventListResponse(events, nextCursor, hasNext);
    }

    @Override
    @Transactional(readOnly = true)
    public EventDetailResponse getEventDetail(Long eventId, Long userId) {
        Event event = eventRepository.findWithDetailsById(eventId)
                .or(() -> eventRepository.findById(eventId))
                .orElseGet(() -> eventRepository.findById(eventId).orElse(null));
        if (event == null) {
            throw new EventException(EventErrorCode.EVENT_NOT_FOUND);
        }

        if (isHiddenFromUser(event.getStatus())) {
            throw new EventException(EventErrorCode.EVENT_NOT_FOUND);
        }

        List<EventOption> rootOptions = toDistinctOptions(event.getOptions())
                .stream()
                .filter(option -> option.getParentOption() == null)
                .sorted(OPTION_ORDER)
                .toList();

        Set<Long> optionIds = collectOptionIds(rootOptions);
        Map<Long, Long> counts = loadApplicationCounts(optionIds);

        return mapToDetail(event, rootOptions, counts);
    }

    @Override
    @Transactional
    public EventApplyResponse apply(Long eventId, EventApplyRequest request, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventException(EventErrorCode.EVENT_NOT_FOUND));

        if (!APPLICABLE_EVENT_STATUSES.contains(event.getStatus())) {
            throw new EventException(EventErrorCode.EVENT_NOT_OPEN);
        }

        EventOption option = eventOptionRepository.findByIdAndEventId(request.optionId(), eventId)
                .orElseThrow(() -> new EventException(EventErrorCode.EVENT_OPTION_NOT_FOUND));

        if (!option.isSelectable()) {
            throw new EventException(EventErrorCode.EVENT_OPTION_NOT_LEAF);
        }

        if (eventApplicationRepository.existsByUserIdAndEventOptionIdAndStatusIn(
                userId, option.getId(), ACTIVE_APPLICATION_STATUSES
        )) {
            throw new EventException(EventErrorCode.EVENT_ALREADY_APPLIED);
        }
        if (eventApplicationRepository.existsByUserIdAndEventOptionIdAndStatusIn(
                userId, option.getId(), REJECTED_APPLICATION_STATUSES
        )) {
            throw new EventException(EventErrorCode.EVENT_REJECTED_CANNOT_REAPPLY);
        }

        if (!reserveCapacity(option)) {
            throw new EventException(EventErrorCode.EVENT_CAPACITY_EXCEEDED);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.UNAUTHORIZED));

        try {
            EventApplication application = EventApplication.create(
                    user,
                    event,
                    option,
                    EventApplicationStatus.APPLIED,
                    StringUtils.hasText(request.memo()) ? request.memo().trim() : null
            );

            EventApplication saved = eventApplicationRepository.save(application);
            return new EventApplyResponse(saved.getId(), saved.getStatus().name());
        } catch (RuntimeException exception) {
            releaseCapacity(option);
            throw exception;
        }
    }

    @Override
    @Transactional
    public EventCancelResponse cancel(Long applicationId, EventCancelRequest request, Long userId) {
        EventApplication application = eventApplicationRepository.findByIdAndUserId(applicationId, userId)
                .orElseThrow(() -> new EventException(EventErrorCode.EVENT_APPLICATION_NOT_FOUND));

        if (application.getStatus() != EventApplicationStatus.APPLIED) {
            throw new EventException(EventErrorCode.EVENT_APPLICATION_NOT_CANCELABLE);
        }

        String reason = StringUtils.hasText(request.reason()) ? request.reason().trim() : null;
        application.cancel(LocalDateTime.now(), reason);

        releaseCapacity(application.getEventOption());

        return new EventCancelResponse(application.getId(), application.getStatus().name());
    }

    @Override
    @Transactional(readOnly = true)
    public EventApplicationListResponse getUserApplications(
            Long userId,
            EventApplicationStatus[] statuses,
            LocalDate from,
            LocalDate to,
            String cursor,
            int size
    ) {
        if (size <= 0 || size > 50) {
            throw new EventException(EventErrorCode.INVALID_EVENT_QUERY);
        }

        EventApplicationCursor.Cursor decodedCursor = EventApplicationCursor.decode(cursor).orElse(null);
        LocalDateTime cursorCreatedAt = decodedCursor == null ? null : decodedCursor.createdAt();
        Long cursorId = decodedCursor == null ? null : decodedCursor.applicationId();

        LocalDateTime fromDateTime = from == null ? null : from.atStartOfDay();
        LocalDateTime toDateTime = to == null ? null : to.plusDays(1).atStartOfDay();

        Collection<EventApplicationStatus> targetStatuses = resolveApplicationStatuses(statuses);

        Pageable pageable = PageRequest.of(0, size + 1);
        List<Long> fetchedIds = eventApplicationRepository.findApplicationIdsForUser(
                userId,
                targetStatuses,
                fromDateTime,
                toDateTime,
                cursorCreatedAt,
                cursorId,
                pageable
        );

        boolean hasNext = fetchedIds.size() > size;
        List<Long> limitedIds = hasNext ? fetchedIds.subList(0, size) : fetchedIds;

        if (limitedIds.isEmpty()) {
            return new EventApplicationListResponse(List.of(), null, false);
        }

        List<EventApplication> applications = eventApplicationRepository.findByIdsWithEventAndImages(limitedIds);
        Map<Long, EventApplication> applicationMap = applications.stream()
                .collect(Collectors.toMap(
                        EventApplication::getId,
                        application -> application,
                        (existing, duplicate) -> existing
                ));

        List<EventApplicationSummaryResponse> items = limitedIds.stream()
                .map(id -> {
                    EventApplication application = applicationMap.get(id);
                    if (application == null) {
                        throw new EventException(EventErrorCode.EVENT_APPLICATION_NOT_FOUND);
                    }
                    return mapToApplicationSummary(application);
                })
                .toList();

        EventApplication lastApplication = applicationMap.get(limitedIds.get(limitedIds.size() - 1));
        String nextCursor = hasNext && lastApplication != null
                ? EventApplicationCursor.encode(lastApplication)
                : null;

        return new EventApplicationListResponse(items, nextCursor, hasNext);
    }

    private Collection<EventApplicationStatus> resolveApplicationStatuses(EventApplicationStatus[] statuses) {
        if (statuses == null || statuses.length == 0) {
            return EnumSet.complementOf(EnumSet.of(EventApplicationStatus.CANCELED, EventApplicationStatus.REJECTED));
        }
        EnumSet<EventApplicationStatus> set = EnumSet.noneOf(EventApplicationStatus.class);
        for (EventApplicationStatus status : statuses) {
            if (status != null) {
                set.add(status);
            }
        }
        if (set.isEmpty()) {
            return EnumSet.complementOf(EnumSet.of(EventApplicationStatus.CANCELED, EventApplicationStatus.REJECTED));
        }
        return set;
    }

    @Override
    @Transactional(readOnly = true)
    public EventApplicationDetailResponse getUserApplicationDetail(Long applicationId, Long userId) {
        EventApplication application = eventApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new EventException(EventErrorCode.EVENT_APPLICATION_NOT_FOUND));

        if (!Objects.equals(application.getUser().getId(), userId)) {
            throw new CustomException(CommonErrorCode.FORBIDDEN);
        }

        Event event = application.getEvent();
        EventApplicationDetailResponse.EventPeriod period = new EventApplicationDetailResponse.EventPeriod(
                event.getStartDate(),
                event.getEndDate()
        );

        List<EventApplicationDetailResponse.OptionTrailResponse> optionTrail = buildOptionTrail(
                application.getEventOption()
        );

        return new EventApplicationDetailResponse(
                application.getId(),
                event.getId(),
                event.getTitle(),
                event.getStatus().name(),
                application.getStatus().name(),
                period,
                event.getLocation(),
                event.getDescription(),
                event.getUsageGuide(),
                event.getPrecautions(),
                optionTrail
        );
    }

    @Override
    @Transactional
    public EventApplicationQrResponse issueApplicationQr(Long applicationId, Long userId) {
        EventApplication application = eventApplicationRepository.findByIdAndUserId(applicationId, userId)
                .orElseThrow(() -> new EventException(EventErrorCode.EVENT_APPLICATION_NOT_FOUND));

        if (application.getEvent().getStatus() == EventStatus.CLOSED) {
            throw new EventException(EventErrorCode.EVENT_CHECKIN_NOT_AVAILABLE);
        }

        if (application.getStatus() != EventApplicationStatus.APPLIED) {
            throw new EventException(EventErrorCode.EVENT_APPLICATION_ALREADY_PROCESSED);
        }

        String token = eventQrTokenStore.generateToken();
        OffsetDateTime issuedAt = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime expiresAt = issuedAt.plusSeconds(QR_TOKEN_TTL.getSeconds());

        CheckinTokenPayload payload = new CheckinTokenPayload(userId, application.getId(), token, issuedAt, expiresAt);
        eventQrTokenStore.saveToken(userId, payload, QR_TOKEN_TTL);

        return new EventApplicationQrResponse(token, (int) QR_TOKEN_TTL.getSeconds());
    }

    private EnumSet<EventStatus> resolveStatuses(String param) {
        if (!StringUtils.hasText(param)) {
            return EnumSet.copyOf(DEFAULT_VISIBLE_STATUSES);
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

    private boolean isHiddenFromUser(EventStatus status) {
        return status == EventStatus.DRAFT || status == EventStatus.ARCHIVED;
    }

    private Long parseCursor(String cursor) {
        if (!StringUtils.hasText(cursor)) {
            return null;
        }
        try {
            long value = Long.parseLong(cursor);
            if (value < 0) {
                throw new NumberFormatException();
            }
            return value;
        } catch (NumberFormatException exception) {
            throw new EventException(EventErrorCode.INVALID_EVENT_QUERY, exception);
        }
    }

    private List<EventApplicationDetailResponse.OptionTrailResponse> buildOptionTrail(EventOption option) {
        return buildOptionTrail(option, step -> new EventApplicationDetailResponse.OptionTrailResponse(
                step.getId(),
                step.getName()
        ));
    }

    private <T> List<T> buildOptionTrail(EventOption option, Function<EventOption, T> mapper) {
        if (option == null) {
            return List.of();
        }
        Deque<EventOption> stack = new ArrayDeque<>();
        EventOption current = option;
        while (current != null) {
            stack.push(current);
            current = current.getParentOption();
        }
        List<T> trail = new ArrayList<>(stack.size());
        while (!stack.isEmpty()) {
            trail.add(mapper.apply(stack.pop()));
        }
        return List.copyOf(trail);
    }

    private EventSummaryResponse mapToSummary(Event event, String thumbnailUrl) {

        return new EventSummaryResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getLocation(),
                event.getStartDate(),
                event.getEndDate(),
                event.getStatus().name(),
                thumbnailUrl
        );
    }

    private EventApplicationSummaryResponse mapToApplicationSummary(EventApplication application) {
        Event event = application.getEvent();

        String thumbnailUrl = event.getImages()
                .stream()
                .sorted(IMAGE_ORDER)
                .map(EventImage::getUrl)
                .findFirst()
                .orElse(null);
        EventPeriod period = new EventPeriod(event.getStartDate(), event.getEndDate());
        String eventStatus = event.getStatus().name();

        return new EventApplicationSummaryResponse(
                application.getId(),
                event.getId(),
                event.getTitle(),
                thumbnailUrl,
                event.getDescription(),
                event.getLocation(),
                period,
                eventStatus
        );
    }

    private EventDetailResponse mapToDetail(
            Event event,
            List<EventOption> rootOptions,
            Map<Long, Long> counts
    ) {
        Integer storedDepth = event.getOptionDepth();
        int optionDepth = storedDepth == null || storedDepth <= 0 ? 0 : storedDepth;
        List<EventDetailImageResponse> images = event.getImages()
                .stream()
                .sorted(IMAGE_ORDER)
                .map(image -> new EventDetailImageResponse(
                        image.getId(),
                        image.getUrl(),
                        image.getAltText(),
                        image.getDisplayOrder()
                ))
                .toList();

        List<EventDetailOptionResponse> options = rootOptions.stream()
                .map(option -> mapOption(option, counts))
                .toList();

        AdminUser organizerAdmin = event.getOrganizerAdmin();
        String organizerName = organizerAdmin == null ? null : organizerAdmin.getName();
        String organizerEmail = organizerAdmin == null ? null : organizerAdmin.getEmail();

        return new EventDetailResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getLocation(),
                organizerName,
                organizerEmail,
                event.getStartDate(),
                event.getEndDate(),
                event.getStatus().name(),
                images,
                optionDepth,
                options
        );
    }

    private EventDetailOptionResponse mapOption(EventOption option, Map<Long, Long> counts) {
        long applied = counts.getOrDefault(option.getId(), 0L);
        Integer appliedCount = safeToInteger(applied);
        Integer capacity = option.getCapacity();
        Integer remaining = capacity == null ? null : Math.max(0, capacity - appliedCount);

        List<EventDetailOptionResponse> children = toDistinctOptions(option.getChildOptions())
                .stream()
                .sorted(OPTION_ORDER)
                .map(child -> mapOption(child, counts))
                .toList();

        return new EventDetailOptionResponse(
                option.getId(),
                option.getName(),
                option.getDisplayOrder(),
                capacity,
                appliedCount,
                remaining,
                children
        );
    }

    private Integer safeToInteger(long value) {
        if (value > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) value;
    }

    private boolean reserveCapacity(EventOption option) {
        Integer capacity = option.getCapacity();
        if (capacity == null) {
            return true;
        }
        return optionCapacityService.reserve(option.getId(), capacity);
    }

    private void releaseCapacity(EventOption option) {
        if (option == null) {
            return;
        }
        optionCapacityService.release(option.getId());
    }

    private Set<Long> collectOptionIds(Collection<EventOption> roots) {
        return roots.stream()
                .map(this::collectOptionIds)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private List<Long> collectOptionIds(EventOption root) {
        List<Long> ids = new ArrayList<>();
        Deque<EventOption> stack = new ArrayDeque<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            EventOption option = stack.pop();
            if (option.getId() != null) {
                ids.add(option.getId());
            }
            toDistinctOptions(option.getChildOptions()).forEach(stack::push);
        }
        return ids;
    }

    private List<EventOption> toDistinctOptions(Collection<EventOption> options) {
        if (options == null || options.isEmpty()) {
            return List.of();
        }
        Map<Object, EventOption> byId = new LinkedHashMap<>();
        for (EventOption option : options) {
            Long id = option.getId();
            Object key = (id != null) ? id : option;
            byId.putIfAbsent(key, option);
        }
        return List.copyOf(byId.values());
    }

    private Map<Long, Long> loadApplicationCounts(Set<Long> optionIds) {
        if (optionIds.isEmpty()) {
            return Map.of();
        }
        List<EventOptionApplicationCount> aggregates = eventApplicationRepository.countActiveApplicationsByOptionIds(
                optionIds,
                ACTIVE_APPLICATION_STATUSES
        );
        return aggregates.stream()
                .collect(Collectors.toMap(EventOptionApplicationCount::eventOptionId, EventOptionApplicationCount::appliedCount));
    }

    private Map<Long, String> loadThumbnails(List<Event> events) {
        if (events.isEmpty()) {
            return Map.of();
        }
        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .toList();
        List<EventImage> images = eventImageRepository.findThumbnailsByEventIds(eventIds);
        Map<Long, String> thumbnails = new HashMap<>();
        for (EventImage image : images) {
            Long eventId = image.getEvent() != null ? image.getEvent().getId() : null;
            if (eventId != null && !thumbnails.containsKey(eventId)) {
                thumbnails.put(eventId, image.getUrl());
            }
        }
        return thumbnails;
    }

    private record OptionLevel(EventOption option, int depth) {
    }

}
