package com.ssasinsa.wearagain.domain.event.service;

import com.ssasinsa.wearagain.domain.event.entity.EventApplicationStatus;
import com.ssasinsa.wearagain.domain.event.repository.EventApplicationRepository;
import com.ssasinsa.wearagain.domain.event.repository.EventOptionApplicationCount;
import com.ssasinsa.wearagain.domain.event.repository.EventOptionRepository;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OptionCapacitySyncScheduler {

    private static final EnumSet<EventApplicationStatus> ACTIVE_STATUSES =
            EnumSet.of(EventApplicationStatus.APPLIED, EventApplicationStatus.CHECKED_IN);

    private final EventOptionRepository eventOptionRepository;
    private final EventApplicationRepository eventApplicationRepository;
    private final OptionCapacityService optionCapacityService;

    // 매 5분마다 동기화 (크론: 초 분 시 일 월 요일)
    @Scheduled(cron = "0 0/5 * * * *")
    public void syncOptionCapacities() {
        List<Long> optionIds = eventOptionRepository.findIdsWithCapacityNotNull();
        if (optionIds.isEmpty()) {
            return;
        }

        Map<Long, Long> usage = eventApplicationRepository.countActiveApplicationsByOptionIds(
                        optionIds,
                        ACTIVE_STATUSES
                )
                .stream()
                .collect(Collectors.toMap(
                        EventOptionApplicationCount::eventOptionId,
                        EventOptionApplicationCount::appliedCount
                ));

        for (Long optionId : optionIds) {
            long used = usage.getOrDefault(optionId, 0L);
            optionCapacityService.reset(optionId, used);
        }

        log.info("[OptionCapacitySync] synced {} options", optionIds.size());
    }
}
