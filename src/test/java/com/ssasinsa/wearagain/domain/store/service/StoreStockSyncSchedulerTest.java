package com.ssasinsa.wearagain.domain.store.service;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ssasinsa.wearagain.domain.store.entity.StoreItem;
import com.ssasinsa.wearagain.domain.store.entity.StoreItemStatus;
import com.ssasinsa.wearagain.domain.store.repository.StoreItemRepository;
import com.ssasinsa.wearagain.global.common.redis.RedisResourceGuard;
import com.ssasinsa.wearagain.global.common.redis.RedisResourceKey;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class StoreStockSyncSchedulerTest {

    @Mock
    private StoreItemRepository storeItemRepository;

    @Mock
    private StoreStockService storeStockService;

    private RedisResourceGuard redisResourceGuard;
    private StoreStockSyncScheduler scheduler;

    @BeforeEach
    void setUp() {
        redisResourceGuard = new RedisResourceGuard();
        scheduler = new StoreStockSyncScheduler(storeItemRepository, storeStockService, redisResourceGuard);
    }

    @Test
    void should_reset_with_fresh_database_stock_after_acquiring_lock() {
        StoreItem initiallyRead = item(1L, 3, StoreItemStatus.ACTIVE);
        StoreItem freshlyRead = item(1L, 7, StoreItemStatus.ACTIVE);
        when(storeItemRepository.findByStatusIn(List.of(StoreItemStatus.ACTIVE)))
                .thenReturn(List.of(initiallyRead));
        when(storeItemRepository.findById(1L)).thenReturn(Optional.of(freshlyRead));
        when(storeStockService.reset(1L, 7)).thenReturn(StoreStockService.ResetResult.RESET);

        scheduler.syncStocks();

        verify(storeStockService).reset(1L, 7);
        verify(storeStockService, never()).reset(1L, 3);
    }

    @Test
    void should_skip_item_when_request_holds_shared_lock() {
        StoreItem item = item(1L, 3, StoreItemStatus.ACTIVE);
        when(storeItemRepository.findByStatusIn(List.of(StoreItemStatus.ACTIVE))).thenReturn(List.of(item));
        RedisResourceKey resourceKey = RedisResourceKey.storeItem(1L);

        try (RedisResourceGuard.LockHandle ignored = redisResourceGuard.acquireRead(resourceKey)) {
            scheduler.syncStocks();
        }

        verify(storeItemRepository, never()).findById(1L);
        verify(storeStockService, never()).reset(1L, 3);
    }

    @Test
    void should_skip_item_that_is_no_longer_active() {
        StoreItem initiallyRead = item(1L, 3, StoreItemStatus.ACTIVE);
        StoreItem freshlyRead = item(1L, 3, StoreItemStatus.INACTIVE);
        when(storeItemRepository.findByStatusIn(List.of(StoreItemStatus.ACTIVE)))
                .thenReturn(List.of(initiallyRead));
        when(storeItemRepository.findById(1L)).thenReturn(Optional.of(freshlyRead));

        scheduler.syncStocks();

        verify(storeStockService, never()).reset(1L, 3);
    }

    @Test
    void should_release_lock_when_reset_throws() throws Exception {
        StoreItem item = item(1L, 3, StoreItemStatus.ACTIVE);
        RedisResourceKey resourceKey = RedisResourceKey.storeItem(1L);
        when(storeItemRepository.findByStatusIn(List.of(StoreItemStatus.ACTIVE))).thenReturn(List.of(item));
        when(storeItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(storeStockService.reset(1L, 3)).thenThrow(new RuntimeException());

        scheduler.syncStocks();

        Optional<RedisResourceGuard.LockHandle> acquired = redisResourceGuard.tryAcquireWrite(
                resourceKey,
                Duration.ZERO
        );
        try (RedisResourceGuard.LockHandle ignored = acquired.orElseThrow()) {
            verify(storeStockService).reset(1L, 3);
        }
    }

    private StoreItem item(Long id, int stock, StoreItemStatus status) {
        StoreItem item = StoreItem.create(
                "name",
                "desc",
                "cat",
                1000,
                stock,
                1,
                status,
                List.of(),
                List.of("강남")
        );
        ReflectionTestUtils.setField(item, "id", id);
        return item;
    }
}
