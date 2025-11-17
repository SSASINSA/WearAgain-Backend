package com.ssasinsa.wearagain.domain.ticket.service;

import com.ssasinsa.wearagain.domain.ticket.dto.TicketChargeResponse;
import com.ssasinsa.wearagain.domain.ticket.dto.TicketStaffChargeRequest;
import com.ssasinsa.wearagain.domain.ticket.dto.TicketUseRequest;
import com.ssasinsa.wearagain.domain.ticket.dto.TicketUseResponse;

public interface TicketStaffService {

    TicketUseResponse useTicket(TicketUseRequest request);

    TicketChargeResponse chargeTicket(TicketStaffChargeRequest request);
}
