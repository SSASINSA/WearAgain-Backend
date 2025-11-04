package com.ssasinsa.wearagain.domain.event.service;

import com.ssasinsa.wearagain.domain.auth.entity.User;
import com.ssasinsa.wearagain.domain.auth.repository.UserRepository;
import com.ssasinsa.wearagain.domain.event.dto.request.EventApplyRequest;
import com.ssasinsa.wearagain.domain.event.dto.request.EventCancelRequest;
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
import com.ssasinsa.wearagain.domain.event.repository.EventRepository;
import com.ssasinsa.wearagain.global.exception.CommonErrorCode;
import com.ssasinsa.wearagain.global.exception.CustomException;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
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
    private static final EnumSet<EventApplicationStatus> ACTIVE_APPLICATION_STATUSES =
            EnumSet.of(EventApplicationStatus.APPLIED, EventApplicationStatus.CHECKED_IN);
    private static final Comparator<EventImage> IMAGE_ORDER = Comparator.comparingInt(EventImage::getDisplayOrder);
    private static final Comparator<EventOption> OPTION_ORDER = Comparator.comparingInt(EventOption::getDisplayOrder);

    private final EventRepository eventRepository;
    private final EventOptionRepository eventOptionRepository;
    private final EventApplicationRepository eventApplicationRepository;
    private final UserRepository userRepository;

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

        List<EventSummaryResponse> events = limited.stream()
                .map(this::mapToSummary)
                .toList();

        String nextCursor = hasNext && !limited.isEmpty()
                ? String.valueOf(limited.get(limited.size() - 1).getId())
                : null;

        return new EventListResponse(events, nextCursor, hasNext);
    }

    @Override
    @Transactional(readOnly = true)
    public EventDetailResponse getEventDetail(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventException(EventErrorCode.EVENT_NOT_FOUND));

        if (isHiddenFromUser(event.getStatus())) {
            throw new EventException(EventErrorCode.EVENT_NOT_FOUND);
        }

        List<EventOption> rootOptions = event.getOptions()
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

        if (event.getStatus() != EventStatus.OPEN) {
            throw new EventException(EventErrorCode.EVENT_NOT_OPEN);
        }

        EventOption option = eventOptionRepository.findByIdAndEventId(request.optionId(), eventId)
                .orElseThrow(() -> new EventException(EventErrorCode.EVENT_OPTION_NOT_FOUND));

        if (eventApplicationRepository.existsByUserIdAndEventIdAndStatusIn(
                userId, eventId, ACTIVE_APPLICATION_STATUSES
        )) {
            throw new EventException(EventErrorCode.EVENT_ALREADY_APPLIED);
        }

        if (option.getCapacity() != null) {
            long appliedCount = eventApplicationRepository.countByEventOptionIdAndStatusIn(
                    option.getId(),
                    ACTIVE_APPLICATION_STATUSES
            );
            if (appliedCount >= option.getCapacity()) {
                throw new EventException(EventErrorCode.EVENT_CAPACITY_EXCEEDED);
            }
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.UNAUTHORIZED));

        EventApplication application = EventApplication.create(
                user,
                event,
                option,
                EventApplicationStatus.APPLIED,
                StringUtils.hasText(request.memo()) ? request.memo().trim() : null,
                null
        );

        EventApplication saved = eventApplicationRepository.save(application);
        return new EventApplyResponse(saved.getId(), saved.getStatus().name());
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

        return new EventCancelResponse(application.getId(), application.getStatus().name());
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

    private EventSummaryResponse mapToSummary(Event event) {
        String thumbnailUrl = event.getImages()
                .stream()
                .sorted(IMAGE_ORDER)
                .map(EventImage::getUrl)
                .findFirst()
                .orElse(null);

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

    private EventDetailResponse mapToDetail(Event event, List<EventOption> rootOptions, Map<Long, Long> counts) {
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

        return new EventDetailResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getLocation(),
                event.getStartDate(),
                event.getEndDate(),
                event.getStatus().name(),
                images,
                options
        );
    }

    private EventDetailOptionResponse mapOption(EventOption option, Map<Long, Long> counts) {
        long applied = counts.getOrDefault(option.getId(), 0L);
        Integer appliedCount = safeToInteger(applied);
        Integer capacity = option.getCapacity();
        Integer remaining = capacity == null ? null : Math.max(0, capacity - appliedCount);

        List<EventDetailOptionResponse> children = option.getChildOptions()
                .stream()
                .sorted(OPTION_ORDER)
                .map(child -> mapOption(child, counts))
                .toList();

        return new EventDetailOptionResponse(
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

    private Integer safeToInteger(long value) {
        if (value > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) value;
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
            option.getChildOptions().forEach(stack::push);
        }
        return ids;
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
}
