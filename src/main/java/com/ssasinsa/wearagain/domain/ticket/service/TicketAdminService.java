package com.ssasinsa.wearagain.domain.ticket.service;

import com.ssasinsa.wearagain.domain.ticket.dto.TicketChargeRequest;
import com.ssasinsa.wearagain.domain.ticket.dto.TicketChargeResponse;

public interface TicketAdminService {

    TicketChargeResponse chargeTicket(TicketChargeRequest request, Long adminId);
}
