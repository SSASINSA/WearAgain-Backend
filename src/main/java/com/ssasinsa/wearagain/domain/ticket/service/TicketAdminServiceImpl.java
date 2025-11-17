package com.ssasinsa.wearagain.domain.ticket.service;

import com.ssasinsa.wearagain.domain.ticket.dto.TicketChargeRequest;
import com.ssasinsa.wearagain.domain.ticket.dto.TicketChargeResponse;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TicketAdminServiceImpl implements TicketAdminService {

    private final TicketBalanceService ticketBalanceService;

    @Override
    public TicketChargeResponse chargeTicket(TicketChargeRequest request, Long adminId) {
        String reason = buildReason(adminId, request.reason());
        TicketBalanceChangeResult result = ticketBalanceService.chargeTicket(
                request.userId(),
                request.amount(),
                reason,
                null
        );
        OffsetDateTime chargedAt = OffsetDateTime.now(ZoneOffset.UTC);
        log.info("Ticket charged for user {} by admin {} (amount: {})", request.userId(), adminId, request.amount());
        return new TicketChargeResponse(result.ticketCountBefore(), result.ticketCountAfter(), chargedAt);
    }

    private String buildReason(Long adminId, String requestReason) {
        String trimmedReason = requestReason == null ? "" : requestReason.trim();
        String combined = String.format("ADMIN_CHARGE:%d:%s", adminId, trimmedReason);
        if (combined.length() > 255) {
            return combined.substring(0, 255);
        }
        return combined;
    }
}
