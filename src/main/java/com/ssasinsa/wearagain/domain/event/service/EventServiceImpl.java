package com.ssasinsa.wearagain.domain.event.service;

import com.ssasinsa.wearagain.domain.event.dto.request.EventCreateRequest;
import com.ssasinsa.wearagain.domain.event.dto.request.EventCreateRequest.EventCreateImageRequest;
import com.ssasinsa.wearagain.domain.event.dto.request.EventCreateRequest.EventCreateOptionRequest;
import com.ssasinsa.wearagain.domain.event.dto.response.EventCreateResponse;
import com.ssasinsa.wearagain.domain.event.dto.response.EventCreateResponse.EventCreateImageResponse;
import com.ssasinsa.wearagain.domain.event.dto.response.EventCreateResponse.EventCreateOptionResponse;
import com.ssasinsa.wearagain.domain.auth.entity.AdminUser;
import com.ssasinsa.wearagain.domain.auth.repository.AdminUserRepository;
import com.ssasinsa.wearagain.domain.event.entity.Event;
import com.ssasinsa.wearagain.domain.event.entity.EventImage;
import com.ssasinsa.wearagain.domain.event.entity.EventOption;
import com.ssasinsa.wearagain.domain.event.entity.EventStatus;
import com.ssasinsa.wearagain.domain.event.exception.EventErrorCode;
import com.ssasinsa.wearagain.domain.event.exception.EventException;
import com.ssasinsa.wearagain.domain.event.repository.EventRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private static final Pattern HTTPS_URL_PATTERN = Pattern.compile("^https://.+", Pattern.CASE_INSENSITIVE);
    private static final int MAX_EVENT_DURATION_DAYS = 365;
    private static final int MAX_IMAGE_COUNT = 10;
    private static final int MAX_OPTION_DEPTH = 3;
    private static final int MAX_OPTION_CAPACITY = 999;

    private final EventRepository eventRepository;
    private final AdminUserRepository adminUserRepository;

    @Override
    @Transactional
    public EventCreateResponse createEvent(EventCreateRequest request, Long adminId) {
        AdminUser organizer = adminUserRepository.findById(adminId)
                .orElseThrow(() -> new EventException(EventErrorCode.EVENT_ADMIN_NOT_FOUND));
        validateEventPeriod(request.startDate(), request.endDate());
        List<EventCreateImageRequest> imageRequests = request.images();
        validateImages(imageRequests);

        List<EventCreateOptionRequest> optionRequests = request.options();

        EventStatus status = request.status() == null ? EventStatus.DRAFT : request.status();
        Event event = Event.create(
                request.title().trim(),
                request.description().trim(),
                request.organizerName().trim(),
                request.organizerContact().trim(),
                request.startDate(),
                request.endDate(),
                request.location().trim(),
                status,
                organizer
        );

        List<EventImage> images = buildEventImages(event, imageRequests);
        List<EventOption> options = buildEventOptions(event, optionRequests);

        event.assignImages(images);
        event.assignOptions(options);

        Event savedEvent;
        try {
            savedEvent = eventRepository.save(event);
        } catch (Exception exception) {
            throw new EventException(EventErrorCode.EVENT_REGISTRATION_FAILED, exception);
        }

        return mapToResponse(savedEvent);
    }

    private void validateEventPeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new EventException(EventErrorCode.MISSING_REQUIRED_VALUE);
        }
        if (endDate.isBefore(startDate)) {
            throw new EventException(EventErrorCode.INVALID_EVENT_PERIOD);
        }
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        if (days > MAX_EVENT_DURATION_DAYS) {
            throw new EventException(EventErrorCode.INVALID_EVENT_PERIOD);
        }
    }

    private void validateImages(List<EventCreateImageRequest> images) {
        if (CollectionUtils.isEmpty(images)) {
            throw new EventException(EventErrorCode.INVALID_IMAGE_INFORMATION);
        }
        if (images.size() > MAX_IMAGE_COUNT) {
            throw new EventException(EventErrorCode.INVALID_IMAGE_INFORMATION);
        }

        Set<Integer> orders = new HashSet<>();
        for (EventCreateImageRequest image : images) {
            if (!StringUtils.hasText(image.url())) {
                throw new EventException(EventErrorCode.INVALID_IMAGE_INFORMATION);
            }
            String trimmedUrl = image.url().trim();
            if (!HTTPS_URL_PATTERN.matcher(trimmedUrl).matches()) {
                throw new EventException(EventErrorCode.INVALID_IMAGE_INFORMATION);
            }
            if (image.displayOrder() <= 0 || !orders.add(image.displayOrder())) {
                throw new EventException(EventErrorCode.INVALID_IMAGE_INFORMATION);
            }
        }
        validateSequentialOrder(orders, EventErrorCode.INVALID_IMAGE_INFORMATION);
    }

    private List<EventImage> buildEventImages(Event event, List<EventCreateImageRequest> imageRequests) {
        List<EventImage> images = new ArrayList<>();
        for (EventCreateImageRequest imageRequest : imageRequests) {
            EventImage image = EventImage.create(
                    event,
                    imageRequest.url().trim(),
                    imageRequest.altText() == null ? null : imageRequest.altText().trim(),
                    imageRequest.displayOrder()
            );
            images.add(image);
        }
        images.sort(Comparator.comparingInt(EventImage::getDisplayOrder));
        return images;
    }

    private List<EventOption> buildEventOptions(Event event, List<EventCreateOptionRequest> optionRequests) {
        if (CollectionUtils.isEmpty(optionRequests)) {
            return List.of();
        }
        validateSiblingConstraints(optionRequests, 1);

        List<EventOption> options = new ArrayList<>();
        for (EventCreateOptionRequest optionRequest : optionRequests) {
            EventOption option = createOption(event, null, optionRequest, 1);
            options.add(option);
        }
        options.sort(Comparator.comparingInt(EventOption::getDisplayOrder));
        return options;
    }

    private EventOption createOption(
            Event event,
            EventOption parent,
            EventCreateOptionRequest request,
            int depth
    ) {
        String normalizedType = normalizeType(request.type());
        Integer normalizedCapacity = normalizeCapacity(request.capacity());
        if (depth > MAX_OPTION_DEPTH) {
            throw new EventException(EventErrorCode.OPTION_DEPTH_LIMIT_EXCEEDED);
        }

        EventOption option = EventOption.create(
                event,
                parent,
                request.name().trim(),
                normalizedType,
                request.displayOrder(),
                normalizedCapacity
        );

        List<EventCreateOptionRequest> children = request.children();
        if (!CollectionUtils.isEmpty(children)) {
            validateSiblingConstraints(children, depth + 1);
            List<EventOption> childOptions = new ArrayList<>();
            for (EventCreateOptionRequest child : children) {
                EventOption childOption = createOption(event, option, child, depth + 1);
                childOptions.add(childOption);
            }
            childOptions.sort(Comparator.comparingInt(EventOption::getDisplayOrder));
            option.assignChildren(childOptions);
        }
        return option;
    }

    private void validateSiblingConstraints(
            List<EventCreateOptionRequest> requests,
            int depth
    ) {
        if (depth > MAX_OPTION_DEPTH) {
            throw new EventException(EventErrorCode.OPTION_DEPTH_LIMIT_EXCEEDED);
        }
        Set<Integer> orders = new HashSet<>();
        Set<String> names = new HashSet<>();
        for (EventCreateOptionRequest request : requests) {
            normalizeType(request.type());
            normalizeCapacity(request.capacity());
            String normalizedName = request.name().trim();
            if (!names.add(normalizedName)) {
                throw new EventException(EventErrorCode.DUPLICATE_OPTION);
            }
            if (request.displayOrder() <= 0 || !orders.add(request.displayOrder())) {
                throw new EventException(EventErrorCode.INVALID_OPTION_STRUCTURE);
            }
        }
        validateSequentialOrder(orders, EventErrorCode.INVALID_OPTION_STRUCTURE);
    }

    private void validateSequentialOrder(Set<Integer> orders, EventErrorCode errorCode) {
        List<Integer> sorted = orders.stream().sorted().toList();
        for (int index = 0; index < sorted.size(); index++) {
            int expected = index + 1;
            if (!Objects.equals(sorted.get(index), expected)) {
                throw new EventException(errorCode);
            }
        }
    }

    private String normalizeType(String type) {
        if (!StringUtils.hasText(type)) {
            throw new EventException(EventErrorCode.INVALID_OPTION_STRUCTURE);
        }
        return type.trim();
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


    private EventCreateResponse mapToResponse(Event event) {
        List<EventCreateImageResponse> imageResponses = event.getImages().stream()
                .sorted(Comparator.comparingInt(EventImage::getDisplayOrder))
                .map(image -> new EventCreateImageResponse(
                        image.getId(),
                        image.getUrl(),
                        image.getAltText(),
                        image.getDisplayOrder()
                ))
                .toList();

        List<EventCreateOptionResponse> optionResponses = event.getOptions().stream()
                .sorted(Comparator.comparingInt(EventOption::getDisplayOrder))
                .map(this::mapOptionToResponse)
                .toList();

        AdminUser organizerAdmin = event.getOrganizerAdmin();

        return new EventCreateResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getLocation(),
                event.getOrganizerName(),
                event.getOrganizerContact(),
                organizerAdmin == null ? null : organizerAdmin.getId(),
                organizerAdmin == null ? null : organizerAdmin.getEmail(),
                organizerAdmin == null ? null : organizerAdmin.getName(),
                event.getStartDate(),
                event.getEndDate(),
                event.getStatus().name(),
                imageResponses,
                optionResponses,
                toOffsetDateTime(event.getCreatedAt())
        );
    }

    private EventCreateOptionResponse mapOptionToResponse(EventOption option) {
        List<EventCreateOptionResponse> childResponses = option.getChildOptions().stream()
                .sorted(Comparator.comparingInt(EventOption::getDisplayOrder))
                .map(this::mapOptionToResponse)
                .toList();

        return new EventCreateOptionResponse(
                option.getId(),
                option.getName(),
                option.getType(),
                option.getDisplayOrder(),
                option.getCapacity(),
                childResponses
        );
    }

    private OffsetDateTime toOffsetDateTime(LocalDateTime createdAt) {
        return createdAt == null ? null : createdAt.atOffset(ZoneOffset.UTC);
    }
}
