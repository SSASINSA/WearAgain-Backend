package com.ssasinsa.wearagain.domain.ticket.service;

public record TicketBalanceChangeResult(
        int ticketCountBefore,
        int ticketCountAfter
) {
}
