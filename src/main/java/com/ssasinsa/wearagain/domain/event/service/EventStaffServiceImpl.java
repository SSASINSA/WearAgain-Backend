package com.ssasinsa.wearagain.domain.event.service;

import com.ssasinsa.wearagain.domain.event.dto.staff.EventStaffCheckInRequest;
import com.ssasinsa.wearagain.domain.event.dto.staff.EventStaffCheckInResponse;
import com.ssasinsa.wearagain.domain.event.entity.Event;
import com.ssasinsa.wearagain.domain.event.entity.EventApplication;
import com.ssasinsa.wearagain.domain.event.entity.EventApplicationStatus;
import com.ssasinsa.wearagain.domain.event.exception.EventErrorCode;
import com.ssasinsa.wearagain.domain.event.exception.EventException;
import com.ssasinsa.wearagain.domain.event.repository.EventApplicationRepository;
import com.ssasinsa.wearagain.domain.event.repository.EventRepository;
import com.ssasinsa.wearagain.domain.event.support.CheckinTokenPayload;
import com.ssasinsa.wearagain.global.common.qr.QrTokenStore;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
@RequiredArgsConstructor
public class EventStaffServiceImpl implements EventStaffService {

    private final EventRepository eventRepository;
    private final EventApplicationRepository eventApplicationRepository;
    @Qualifier("eventQrTokenStore")
    private final QrTokenStore<CheckinTokenPayload> eventQrTokenStore;

    @Override
    public EventStaffCheckInResponse checkIn(EventStaffCheckInRequest request) {
        String code = request.code().trim();
        String qrToken = request.qrToken().trim();

        Event event = eventRepository.findByStaffCode(code)
                .orElseThrow(() -> new EventException(EventErrorCode.EVENT_STAFF_CODE_INVALID));

        if (!StringUtils.hasText(event.getStaffCode())) {
            throw new EventException(EventErrorCode.EVENT_STAFF_CODE_NOT_ISSUED);
        }

        CheckinTokenPayload payload = eventQrTokenStore.getTokenByToken(qrToken)
                .orElseThrow(() -> new EventException(EventErrorCode.EVENT_CHECKIN_TOKEN_NOT_FOUND));

        EventApplication application = eventApplicationRepository.findById(payload.applicationId())
                .orElseThrow(() -> new EventException(EventErrorCode.EVENT_APPLICATION_NOT_FOUND));

        if (!Objects.equals(payload.userId(), application.getUser().getId())) {
            throw new EventException(EventErrorCode.EVENT_CHECKIN_TOKEN_INVALID);
        }

        if (!Objects.equals(application.getEvent().getId(), event.getId())) {
            throw new EventException(EventErrorCode.EVENT_CHECKIN_TOKEN_INVALID);
        }

        if (application.getStatus() != EventApplicationStatus.APPLIED) {
            throw new EventException(EventErrorCode.EVENT_APPLICATION_ALREADY_PROCESSED);
        }

        OffsetDateTime checkedInAt = OffsetDateTime.now(ZoneOffset.UTC);
        application.checkIn(checkedInAt.toLocalDateTime());
        eventQrTokenStore.deleteToken(payload.userId());

        return new EventStaffCheckInResponse(
                application.getId(),
                application.getStatus().name(),
                checkedInAt,
                application.getUser().getDisplayName(),
                event.getTitle()
        );
    }
}
