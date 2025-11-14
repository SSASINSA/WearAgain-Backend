package com.ssasinsa.wearagain.domain.ticket.service;

import com.ssasinsa.wearagain.domain.ticket.dto.TicketQrResponse;

public interface TicketService {

    TicketQrResponse getTicketQr(Long userId);
}
