package com.ssasinsa.wearagain.domain.event.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ssasinsa.wearagain.domain.event.dto.request.EventCreateRequest;
import com.ssasinsa.wearagain.domain.event.dto.request.EventCreateRequest.EventCreateImageRequest;
import com.ssasinsa.wearagain.domain.event.dto.request.EventCreateRequest.EventCreateOptionRequest;
import com.ssasinsa.wearagain.domain.event.dto.response.EventCreateResponse;
import com.ssasinsa.wearagain.domain.event.entity.Event;
import com.ssasinsa.wearagain.domain.event.entity.EventImage;
import com.ssasinsa.wearagain.domain.event.entity.EventOption;
import com.ssasinsa.wearagain.domain.event.entity.EventStatus;
import com.ssasinsa.wearagain.domain.event.exception.EventErrorCode;
import com.ssasinsa.wearagain.domain.event.exception.EventException;
import com.ssasinsa.wearagain.domain.event.repository.EventRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventServiceImpl eventService;

    private EventCreateRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = createValidRequest();
    }

    @Test
    void should_create_event_when_request_is_valid() {
        Event event = buildPersistedEvent();
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        EventCreateResponse response = eventService.createEvent(validRequest);

        verify(eventRepository).save(any(Event.class));
        assertThat(response.eventId()).isEqualTo(1L);
        assertThat(response.images()).hasSize(2);
        assertThat(response.options()).hasSize(2);
        assertThat(response.status()).isEqualTo(EventStatus.DRAFT.name());
    }

    @Test
    void should_fail_when_end_date_is_before_start_date() {
        EventCreateRequest request = new EventCreateRequest(
                "테스트 행사",
                "행사 설명입니다.",
                "서울시 마포구",
                LocalDate.now(),
                LocalDate.now().minusDays(1),
                null,
                List.of(new EventCreateImageRequest("https://example.com/1.png", "대표", 1)),
                List.of()
        );

        assertThatThrownBy(() -> eventService.createEvent(request))
                .isInstanceOf(EventException.class)
                .hasFieldOrPropertyWithValue("errorCode", EventErrorCode.INVALID_EVENT_PERIOD);
    }

    @Test
    void should_fail_when_option_depth_exceeds_limit() {
        EventCreateOptionRequest depth4Option = new EventCreateOptionRequest(
                "1차",
                "DATE",
                1,
                null,
                List.of(
                        new EventCreateOptionRequest(
                                "2차",
                                "TIME",
                                1,
                                null,
                                List.of(
                                                new EventCreateOptionRequest(
                                                        "3차",
                                                        "GROUP",
                                                        1,
                                                        10,
                                                        List.of(
                                                                new EventCreateOptionRequest(
                                                                        "4차",
                                                                        "GROUP",
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

        EventCreateRequest request = new EventCreateRequest(
                validRequest.title(),
                validRequest.description(),
                validRequest.location(),
                validRequest.startDate(),
                validRequest.endDate(),
                validRequest.status(),
                validRequest.images(),
                List.of(depth4Option)
        );

        assertThatThrownBy(() -> eventService.createEvent(request))
                .isInstanceOf(EventException.class)
                .hasFieldOrPropertyWithValue("errorCode", EventErrorCode.OPTION_DEPTH_LIMIT_EXCEEDED);
    }

    private EventCreateRequest createValidRequest() {
        List<EventCreateImageRequest> images = List.of(
                new EventCreateImageRequest("https://wearagain.kr/1.jpg", "대표", 1),
                new EventCreateImageRequest("https://wearagain.kr/2.jpg", "설명", 2)
        );

        List<EventCreateOptionRequest> options = List.of(
                new EventCreateOptionRequest(
                        "11월 15일",
                        "DATE",
                        1,
                        null,
                        List.of(
                                new EventCreateOptionRequest(
                                        "오전 세션",
                                        "TIME",
                                        1,
                                        null,
                                        List.of(
                                                new EventCreateOptionRequest(
                                                        "A조",
                                                        "GROUP",
                                                        1,
                                                        10,
                                                        List.of()
                                                )
                                        )
                                )
                        )
                ),
                new EventCreateOptionRequest(
                        "11월 22일",
                        "DATE",
                        2,
                        null,
                        List.of()
                )
        );

        return new EventCreateRequest(
                "지속가능 패션 행사",
                "재사용 패션 실습을 진행합니다.",
                "서울시 마포구 연남동",
                LocalDate.of(2025, 11, 10),
                LocalDate.of(2025, 11, 30),
                EventStatus.DRAFT,
                images,
                options
        );
    }

    private Event buildPersistedEvent() {
        Event event = Event.create(
                validRequest.title(),
                validRequest.description(),
                validRequest.startDate(),
                validRequest.endDate(),
                validRequest.location(),
                EventStatus.DRAFT
        );

        ReflectionTestUtils.setField(event, "id", 1L);

        List<EventImage> images = validRequest.images().stream()
                .map(imageRequest -> {
                    EventImage image = EventImage.create(event, imageRequest.url(), imageRequest.altText(), imageRequest.displayOrder());
                    ReflectionTestUtils.setField(image, "id", image.getDisplayOrder() == 1 ? 1001L : 1002L);
                    return image;
                })
                .toList();

        List<EventOption> options = validRequest.options().stream()
                .map(optionRequest -> buildPersistedOptionTree(event, null, optionRequest, 2000L))
                .toList();

        event.assignImages(images);
        event.assignOptions(options);

        return event;
    }

    private EventOption buildPersistedOptionTree(
            Event event,
            EventOption parent,
            EventCreateOptionRequest request,
            long baseId
    ) {
        EventOption option = EventOption.create(
                event,
                parent,
                request.name(),
                request.type(),
                request.displayOrder(),
                request.capacity()
        );
        ReflectionTestUtils.setField(option, "id", baseId + request.displayOrder());

        if (request.children() != null) {
            List<EventOption> children = request.children().stream()
                    .map(child -> buildPersistedOptionTree(event, option, child, baseId + 10))
                    .toList();
            option.assignChildren(children);
        }
        return option;
    }
}
