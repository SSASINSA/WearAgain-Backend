package com.ssasinsa.wearagain.domain.ticket.service;

import com.ssasinsa.wearagain.domain.event.entity.Event;
import com.ssasinsa.wearagain.domain.event.entity.EventApplicationStatus;
import com.ssasinsa.wearagain.domain.event.repository.EventApplicationRepository;
import com.ssasinsa.wearagain.domain.event.repository.EventRepository;
import com.ssasinsa.wearagain.domain.ticket.dto.TicketChargeResponse;
import com.ssasinsa.wearagain.domain.ticket.dto.TicketStaffChargeRequest;
import com.ssasinsa.wearagain.domain.ticket.dto.TicketUseRequest;
import com.ssasinsa.wearagain.domain.ticket.dto.TicketUseResponse;
import com.ssasinsa.wearagain.domain.ticket.exception.TicketErrorCode;
import com.ssasinsa.wearagain.domain.ticket.exception.TicketException;
import com.ssasinsa.wearagain.domain.ticket.support.TicketQrTokenPayload;
import com.ssasinsa.wearagain.global.common.qr.QrTokenStore;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TicketStaffServiceImpl implements TicketStaffService {

    private static final Set<EventApplicationStatus> PARTICIPANT_STATUSES = EnumSet.of(
            EventApplicationStatus.APPLIED,
            EventApplicationStatus.CHECKED_IN
    );

    private final EventRepository eventRepository;
    private final EventApplicationRepository eventApplicationRepository;
    private final TicketBalanceService ticketBalanceService;
    private final QrTokenStore<TicketQrTokenPayload> ticketQrTokenStore;

    @Override
    public TicketUseResponse useTicket(TicketUseRequest request) {
        String code = trim(request.code());
        String qrToken = trim(request.qrToken());

        Event event = eventRepository.findByStaffCode(code)
                .orElseThrow(() -> new TicketException(TicketErrorCode.TICKET_STAFF_CODE_INVALID));

        if (!StringUtils.hasText(event.getStaffCode())) {
            throw new TicketException(TicketErrorCode.TICKET_STAFF_CODE_INVALID);
        }

        TicketQrTokenPayload payload = getToken(qrToken);
        ensureParticipant(event.getId(), payload.userId());
        TicketBalanceChangeResult result = ticketBalanceService.useTicket(
                payload.userId(),
                request.amount(),
                buildStaffUseReason(event.getId(), code),
                event
        );

        deleteToken(payload.userId());
        OffsetDateTime checkedInAt = OffsetDateTime.now(ZoneOffset.UTC);
        log.info("Ticket used by staff code {} for user {}", code, payload.userId());
        return new TicketUseResponse(result.ticketCountBefore(), result.ticketCountAfter(), checkedInAt);
    }

    @Override
    public TicketChargeResponse chargeTicket(TicketStaffChargeRequest request) {
        String code = trim(request.code());
        String qrToken = trim(request.qrToken());

        Event event = eventRepository.findByStaffCode(code)
                .orElseThrow(() -> new TicketException(TicketErrorCode.TICKET_STAFF_CODE_INVALID));

        if (!StringUtils.hasText(event.getStaffCode())) {
            throw new TicketException(TicketErrorCode.TICKET_STAFF_CODE_INVALID);
        }

        TicketQrTokenPayload payload = getToken(qrToken);
        ensureParticipant(event.getId(), payload.userId());
        TicketBalanceChangeResult result = ticketBalanceService.chargeTicket(
                payload.userId(),
                request.amount(),
                buildStaffChargeReason(event.getId(), code),
                event
        );

        OffsetDateTime chargedAt = OffsetDateTime.now(ZoneOffset.UTC);
        log.info("Ticket charged by staff code {} for user {} (amount: {})", code, payload.userId(), request.amount());
        return new TicketChargeResponse(result.ticketCountBefore(), result.ticketCountAfter(), chargedAt);
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private TicketQrTokenPayload getToken(String qrToken) {
        try {
            return ticketQrTokenStore.getTokenByToken(qrToken)
                    .orElseThrow(() -> new TicketException(TicketErrorCode.TICKET_QR_TOKEN_NOT_FOUND));
        } catch (IllegalStateException exception) {
            throw new TicketException(TicketErrorCode.TICKET_BALANCE_PROCESSING_FAILED, exception);
        }
    }

    private void deleteToken(Long userId) {
        try {
            ticketQrTokenStore.deleteToken(userId);
        } catch (IllegalStateException exception) {
            throw new TicketException(TicketErrorCode.TICKET_BALANCE_PROCESSING_FAILED, exception);
        }
    }

    private void ensureParticipant(Long eventId, Long userId) {
        boolean exists = eventApplicationRepository.existsByUserIdAndEventIdAndStatusIn(
                userId,
                eventId,
                PARTICIPANT_STATUSES
        );
        if (!exists) {
            throw new TicketException(TicketErrorCode.TICKET_USER_NOT_PARTICIPANT);
        }
    }

    private String buildStaffUseReason(Long eventId, String code) {
        return String.format("STAFF_USE:%d:%s", eventId, code);
    }

    private String buildStaffChargeReason(Long eventId, String code) {
        return String.format("STAFF_CHARGE:%d:%s", eventId, code);
    }
}
