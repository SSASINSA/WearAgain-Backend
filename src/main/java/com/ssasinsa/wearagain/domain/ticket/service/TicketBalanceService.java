package com.ssasinsa.wearagain.domain.ticket.service;

import com.ssasinsa.wearagain.domain.event.entity.Event;

public interface TicketBalanceService {

    TicketBalanceChangeResult useTicket(Long userId, int amount, String reason, Event relatedEvent);

    TicketBalanceChangeResult chargeTicket(Long userId, int amount, String reason, Event relatedEvent);
}
