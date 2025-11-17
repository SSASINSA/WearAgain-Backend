package com.ssasinsa.wearagain.domain.ticket.service;

import com.ssasinsa.wearagain.domain.auth.entity.User;
import com.ssasinsa.wearagain.domain.auth.repository.UserRepository;
import com.ssasinsa.wearagain.domain.event.entity.Event;
import com.ssasinsa.wearagain.domain.finance.entity.TicketHistory;
import com.ssasinsa.wearagain.domain.finance.repository.TicketHistoryRepository;
import com.ssasinsa.wearagain.domain.ticket.exception.TicketErrorCode;
import com.ssasinsa.wearagain.domain.ticket.exception.TicketException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TicketBalanceServiceImpl implements TicketBalanceService {

    private final UserRepository userRepository;
    private final TicketHistoryRepository ticketHistoryRepository;

    @Override
    public TicketBalanceChangeResult useTicket(Long userId, int amount, String reason, Event relatedEvent) {
        return adjust(userId, amount, reason, relatedEvent, true);
    }

    @Override
    public TicketBalanceChangeResult chargeTicket(Long userId, int amount, String reason, Event relatedEvent) {
        return adjust(userId, amount, reason, relatedEvent, false);
    }

    private TicketBalanceChangeResult adjust(Long userId, int amount, String reason, Event relatedEvent, boolean deduct) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new TicketException(TicketErrorCode.TICKET_DATA_NOT_FOUND));

        int ticketCountBefore = user.getTicketBalance();
        int ticketCountAfter;

        if (deduct) {
            if (ticketCountBefore < amount) {
                throw new TicketException(TicketErrorCode.TICKET_ALREADY_USED_OR_EMPTY);
            }
            ticketCountAfter = user.decreaseTicketBalance(amount);
        } else {
            ticketCountAfter = user.increaseTicketBalance(amount);
        }

        int changeAmount = deduct ? -amount : amount;
        TicketHistory history = TicketHistory.create(user, relatedEvent, changeAmount, reason);
        try {
            ticketHistoryRepository.save(history);
        } catch (RuntimeException exception) {
            throw new TicketException(TicketErrorCode.TICKET_BALANCE_PROCESSING_FAILED, exception);
        }

        return new TicketBalanceChangeResult(ticketCountBefore, ticketCountAfter);
    }
}
