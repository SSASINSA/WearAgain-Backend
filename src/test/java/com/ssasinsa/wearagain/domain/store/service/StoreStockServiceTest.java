package com.ssasinsa.wearagain.domain.store.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.ssasinsa.wearagain.support.RedisTestContainerSupport;
import com.ssasinsa.wearagain.domain.store.service.StoreStockService.ReleaseResult;
import com.ssasinsa.wearagain.domain.store.service.StoreStockService.ReserveResult;
import com.ssasinsa.wearagain.domain.store.service.StoreStockService.ResetResult;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

class StoreStockServiceTest extends RedisTestContainerSupport {

    private static final Long RESERVE_ITEM_ID = 1L;
    private static final Long RELEASE_ITEM_ID = 2L;
    private static final Long MISSING_ITEM_ID = 3L;
    private static final String STOCK_KEY_PREFIX = "store:stock:item:";

    private LettuceConnectionFactory connectionFactory;
    private StringRedisTemplate redisTemplate;
    private StoreStockService storeStockService;

    @BeforeEach
    void setUp() {
        connectionFactory = createRedisConnectionFactory();
        redisTemplate = createRedisTemplate(connectionFactory);
        deleteTestKeys();
        storeStockService = new StoreStockService(redisTemplate);
    }

    @AfterEach
    void tearDown() {
        deleteTestKeys();
        connectionFactory.destroy();
    }

    @Test
    void should_not_exceed_stock_under_concurrent_reservations() throws Exception {
        int stock = 10;
        int requesters = 25;
        assertThat(storeStockService.reset(RESERVE_ITEM_ID, stock)).isEqualTo(ResetResult.RESET);

        var executor = Executors.newFixedThreadPool(20);
        List<CompletableFuture<ReserveResult>> futures = new ArrayList<>();
        for (int i = 0; i < requesters; i++) {
            futures.add(CompletableFuture.supplyAsync(
                    () -> storeStockService.reserve(RESERVE_ITEM_ID, 1),
                    executor
            ));
        }

        List<ReserveResult> results = futures.stream().map(CompletableFuture::join).toList();

        long success = results.stream().filter(ReserveResult.RESERVED::equals).count();
        long remaining = Long.parseLong(redisTemplate.opsForValue().get(STOCK_KEY_PREFIX + RESERVE_ITEM_ID));

        assertThat(success).isEqualTo(stock);
        assertThat(remaining).isZero();

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
    }

    @Test
    void should_release_stock_on_cancel() {
        assertThat(storeStockService.reset(RELEASE_ITEM_ID, 3)).isEqualTo(ResetResult.RESET);

        assertThat(storeStockService.reserve(RELEASE_ITEM_ID, 3)).isEqualTo(ReserveResult.RESERVED);
        assertThat(storeStockService.reserve(RELEASE_ITEM_ID, 1)).isEqualTo(ReserveResult.STOCK_SHORTAGE);

        assertThat(storeStockService.release(RELEASE_ITEM_ID, 2)).isEqualTo(ReleaseResult.RELEASED);

        assertThat(storeStockService.reserve(RELEASE_ITEM_ID, 2)).isEqualTo(ReserveResult.RESERVED);
    }

    @Test
    void should_distinguish_missing_stock_from_shortage() {
        assertThat(storeStockService.reserve(MISSING_ITEM_ID, 1)).isEqualTo(ReserveResult.CACHE_MISS);
    }

    @Test
    void should_not_create_key_when_releasing_missing_stock() {
        assertThat(storeStockService.release(MISSING_ITEM_ID, 1)).isEqualTo(ReleaseResult.CACHE_MISS);
        assertThat(redisTemplate.hasKey(STOCK_KEY_PREFIX + MISSING_ITEM_ID)).isFalse();
    }

    private void deleteTestKeys() {
        deleteKeys(
                redisTemplate,
                STOCK_KEY_PREFIX + RESERVE_ITEM_ID,
                STOCK_KEY_PREFIX + RELEASE_ITEM_ID,
                STOCK_KEY_PREFIX + MISSING_ITEM_ID
        );
    }
}
