package com.ssasinsa.wearagain.domain.ticket.service;

import com.ssasinsa.wearagain.domain.auth.entity.User;
import com.ssasinsa.wearagain.domain.auth.repository.UserRepository;
import com.ssasinsa.wearagain.domain.ticket.dto.TicketQrResponse;
import com.ssasinsa.wearagain.domain.ticket.exception.TicketErrorCode;
import com.ssasinsa.wearagain.domain.ticket.exception.TicketException;
import com.ssasinsa.wearagain.domain.ticket.support.TicketQrTokenPayload;
import com.ssasinsa.wearagain.global.exception.CommonErrorCode;
import com.ssasinsa.wearagain.global.exception.CustomException;
import com.ssasinsa.wearagain.global.common.qr.QrTokenStore;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private static final Duration QR_TOKEN_TTL = Duration.ofMinutes(15);
    private static final long MIN_REUSE_TTL_SECONDS = 30L;

    private final UserRepository userRepository;
    private final QrTokenStore<TicketQrTokenPayload> ticketQrTokenStore;

    @Override
    public TicketQrResponse getTicketQr(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.UNAUTHORIZED));

        int ticketCount = user.getTicketBalance();
        if (ticketCount <= 0) {
            throw new TicketException(TicketErrorCode.TICKET_BALANCE_EMPTY);
        }

        Optional<TicketQrTokenPayload> existingPayload = getExistingPayload(userId);
        if (existingPayload.isPresent()) {
            String token = existingPayload.get().token();
            Optional<Long> remainingTtl = getRemainingTtl(token);
            if (remainingTtl.isPresent() && remainingTtl.get() >= MIN_REUSE_TTL_SECONDS) {
                return new TicketQrResponse(ticketCount, token, remainingTtl.get().intValue());
            }
        }

        String token = ticketQrTokenStore.generateToken();
        OffsetDateTime issuedAt = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime expiresAt = issuedAt.plusSeconds(QR_TOKEN_TTL.getSeconds());
        TicketQrTokenPayload payload = new TicketQrTokenPayload(userId, token, ticketCount, issuedAt, expiresAt);
        try {
            ticketQrTokenStore.saveToken(userId, payload, QR_TOKEN_TTL);
        } catch (IllegalStateException exception) {
            throw new TicketException(TicketErrorCode.TICKET_QR_TOKEN_STORE_FAILED, exception);
        }
        return new TicketQrResponse(ticketCount, token, (int) QR_TOKEN_TTL.getSeconds());
    }

    private Optional<TicketQrTokenPayload> getExistingPayload(Long userId) {
        try {
            return ticketQrTokenStore.getTokenByUser(userId);
        } catch (IllegalStateException exception) {
            throw new TicketException(TicketErrorCode.TICKET_QR_TOKEN_STORE_FAILED, exception);
        }
    }

    private Optional<Long> getRemainingTtl(String token) {
        try {
            return ticketQrTokenStore.getRemainingTtlByToken(token);
        } catch (IllegalStateException exception) {
            throw new TicketException(TicketErrorCode.TICKET_QR_TOKEN_STORE_FAILED, exception);
        }
    }
}
