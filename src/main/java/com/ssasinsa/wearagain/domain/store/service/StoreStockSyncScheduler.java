package com.ssasinsa.wearagain.domain.store.service;

import com.ssasinsa.wearagain.domain.store.entity.StoreItem;
import com.ssasinsa.wearagain.domain.store.entity.StoreItemStatus;
import com.ssasinsa.wearagain.domain.store.repository.StoreItemRepository;
import java.util.List;
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

    private final StoreItemRepository storeItemRepository;
    private final StoreStockService storeStockService;

    @EventListener(ApplicationReadyEvent.class)
    public void syncOnStartup() {
        syncStocks();
    }

    // 매 5분마다 재동기화
    @Scheduled(cron = "0 0/5 * * * *")
    public void syncStocks() {
        List<StoreItem> items = storeItemRepository.findByStatusIn(List.of(StoreItemStatus.ACTIVE));
        if (items.isEmpty()) {
            return;
        }
        for (StoreItem item : items) {
            storeStockService.reset(item.getId(), item.getStock());
        }
        log.info("[StoreStockSync] synced {} items", items.size());
    }
}
