package com.ssasinsa.wearagain.domain.store.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.ssasinsa.wearagain.support.RedisTestContainerSupport;
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
        storeStockService.reset(RESERVE_ITEM_ID, stock);

        var executor = Executors.newFixedThreadPool(20);
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();
        for (int i = 0; i < requesters; i++) {
            futures.add(CompletableFuture.supplyAsync(
                    () -> storeStockService.reserve(RESERVE_ITEM_ID, 1),
                    executor
            ));
        }

        List<Boolean> results = futures.stream().map(CompletableFuture::join).toList();

        long success = results.stream().filter(Boolean::booleanValue).count();
        long remaining = Long.parseLong(redisTemplate.opsForValue().get(STOCK_KEY_PREFIX + RESERVE_ITEM_ID));

        assertThat(success).isEqualTo(stock);
        assertThat(remaining).isZero();

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
    }

    @Test
    void should_release_stock_on_cancel() {
        storeStockService.reset(RELEASE_ITEM_ID, 3);

        assertThat(storeStockService.reserve(RELEASE_ITEM_ID, 3)).isTrue();
        assertThat(storeStockService.reserve(RELEASE_ITEM_ID, 1)).isFalse();

        storeStockService.release(RELEASE_ITEM_ID, 2);

        assertThat(storeStockService.reserve(RELEASE_ITEM_ID, 2)).isTrue();
    }

    private void deleteTestKeys() {
        deleteKeys(
                redisTemplate,
                STOCK_KEY_PREFIX + RESERVE_ITEM_ID,
                STOCK_KEY_PREFIX + RELEASE_ITEM_ID
        );
    }
}
