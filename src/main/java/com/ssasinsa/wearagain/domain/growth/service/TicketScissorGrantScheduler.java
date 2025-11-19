package com.ssasinsa.wearagain.domain.growth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TicketScissorGrantScheduler {

    private final TicketScissorGrantService ticketScissorGrantService;

    @Scheduled(cron = "0 0 2 * * *")
    public void runNightlyGrantJob() {
        ticketScissorGrantService.grantScissorsForClosedEvents();
    }
}
