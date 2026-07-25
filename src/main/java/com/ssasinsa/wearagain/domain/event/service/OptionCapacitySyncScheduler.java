package com.ssasinsa.wearagain.domain.event.service;

import com.ssasinsa.wearagain.domain.event.entity.EventApplicationStatus;
import com.ssasinsa.wearagain.domain.event.entity.EventOption;
import com.ssasinsa.wearagain.domain.event.repository.EventApplicationRepository;
import com.ssasinsa.wearagain.domain.event.repository.EventOptionRepository;
import com.ssasinsa.wearagain.global.common.redis.RedisResourceGuard;
import com.ssasinsa.wearagain.global.common.redis.RedisResourceKey;
import java.time.Duration;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OptionCapacitySyncScheduler {

    private static final EnumSet<EventApplicationStatus> ACTIVE_STATUSES =
            EnumSet.of(EventApplicationStatus.APPLIED, EventApplicationStatus.CHECKED_IN);
    private static final Duration SYNC_LOCK_TIMEOUT = Duration.ofMillis(100);

    private final EventOptionRepository eventOptionRepository;
    private final EventApplicationRepository eventApplicationRepository;
    private final OptionCapacityService optionCapacityService;
    private final RedisResourceGuard redisResourceGuard;

    @EventListener(ApplicationReadyEvent.class)
    public void syncOnStartup() {
        syncOptionCapacities();
    }

    /**
     * 5분 주기 DB 기준 행사 옵션 사용 인원 동기화 메서드.
     */
    @Scheduled(cron = "0 0/5 * * * *")
    public void syncOptionCapacities() {
        List<Long> optionIds = eventOptionRepository.findIdsWithCapacityNotNull();
        if (optionIds.isEmpty()) {
            return;
        }

        int syncedCount = 0;
        for (Long optionId : optionIds) {
            if (syncOptionCapacity(optionId)) {
                syncedCount++;
            }
            if (Thread.currentThread().isInterrupted()) {
                break;
            }
        }
        log.info("[OptionCapacitySync] 대상={}, 동기화={}", optionIds.size(), syncedCount);
    }

    /**
     * 단일 행사 옵션 DB 사용 인원 동기화 메서드.
     */
    private boolean syncOptionCapacity(Long optionId) {
        RedisResourceKey resourceKey = RedisResourceKey.eventOption(optionId);
        Optional<RedisResourceGuard.LockHandle> lockHandle;
        try {
            lockHandle = redisResourceGuard.tryAcquireWrite(resourceKey, SYNC_LOCK_TIMEOUT);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            log.warn("[OptionCapacitySync] lock 대기 중 중단되었습니다. optionId={}", optionId, exception);
            return false;
        }

        if (lockHandle.isEmpty()) {
            log.warn("[OptionCapacitySync] 진행 중인 요청으로 동기화를 건너뜁니다. optionId={}", optionId);
            return false;
        }

        try (RedisResourceGuard.LockHandle ignored = lockHandle.get()) {
            Optional<EventOption> currentOption = eventOptionRepository.findById(optionId);
            if (currentOption.isEmpty() || currentOption.get().getCapacity() == null) {
                return false;
            }
            long used = eventApplicationRepository.countByEventOptionIdAndStatusIn(optionId, ACTIVE_STATUSES);
            return optionCapacityService.reset(optionId, used) == OptionCapacityService.ResetResult.RESET;
        } catch (RuntimeException exception) {
            log.error("[OptionCapacitySync] 행사 옵션 사용 인원 동기화에 실패했습니다. optionId={}", optionId, exception);
            return false;
        }
    }
}
