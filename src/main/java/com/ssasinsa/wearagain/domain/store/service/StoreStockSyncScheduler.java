package com.ssasinsa.wearagain.domain.store.service;

import com.ssasinsa.wearagain.domain.store.entity.StoreItem;
import com.ssasinsa.wearagain.domain.store.entity.StoreItemStatus;
import com.ssasinsa.wearagain.domain.store.repository.StoreItemRepository;
import com.ssasinsa.wearagain.global.common.redis.RedisResourceGuard;
import com.ssasinsa.wearagain.global.common.redis.RedisResourceKey;
import java.time.Duration;
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
public class StoreStockSyncScheduler {

    private static final Duration SYNC_LOCK_TIMEOUT = Duration.ofMillis(100);

    private final StoreItemRepository storeItemRepository;
    private final StoreStockService storeStockService;
    private final RedisResourceGuard redisResourceGuard;

    @EventListener(ApplicationReadyEvent.class)
    public void syncOnStartup() {
        syncStocks();
    }

    /**
     * 5분 주기 DB 기준 상품 재고 동기화 메서드.
     */
    @Scheduled(cron = "0 0/5 * * * *")
    public void syncStocks() {
        List<StoreItem> items = storeItemRepository.findByStatusIn(List.of(StoreItemStatus.ACTIVE));
        if (items.isEmpty()) {
            return;
        }
        int syncedCount = 0;
        for (StoreItem item : items) {
            if (syncStock(item.getId())) {
                syncedCount++;
            }
            if (Thread.currentThread().isInterrupted()) {
                break;
            }
        }
        log.info("[StoreStockSync] 대상={}, 동기화={}", items.size(), syncedCount);
    }

    /**
     * 단일 상품 DB 재고 동기화 메서드.
     */
    private boolean syncStock(Long itemId) {
        RedisResourceKey resourceKey = RedisResourceKey.storeItem(itemId);
        Optional<RedisResourceGuard.LockHandle> lockHandle;
        try {
            lockHandle = redisResourceGuard.tryAcquireWrite(resourceKey, SYNC_LOCK_TIMEOUT);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            log.warn("[StoreStockSync] lock 대기 중 중단되었습니다. itemId={}", itemId, exception);
            return false;
        }

        if (lockHandle.isEmpty()) {
            log.warn("[StoreStockSync] 진행 중인 요청으로 동기화를 건너뜁니다. itemId={}", itemId);
            return false;
        }

        try (RedisResourceGuard.LockHandle ignored = lockHandle.get()) {
            Optional<StoreItem> currentItem = storeItemRepository.findById(itemId);
            if (currentItem.isEmpty() || currentItem.get().getStatus() != StoreItemStatus.ACTIVE) {
                return false;
            }
            StoreItem item = currentItem.get();
            return storeStockService.reset(item.getId(), item.getStock())
                    == StoreStockService.ResetResult.RESET;
        } catch (RuntimeException exception) {
            log.error("[StoreStockSync] 상품 재고 동기화에 실패했습니다. itemId={}", itemId, exception);
            return false;
        }
    }
}
