package com.ssasinsa.wearagain.domain.store.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

class StoreStockServiceTest {

    private StringRedisTemplate redisTemplate;
    private StoreStockService storeStockService;

    @BeforeEach
    void setUp() {
        RedisConnectionFactory connectionFactory = new LettuceConnectionFactory("localhost", 6379);
        ((LettuceConnectionFactory) connectionFactory).afterPropertiesSet();
        redisTemplate = new StringRedisTemplate(connectionFactory);
        redisTemplate.afterPropertiesSet();
        storeStockService = new StoreStockService(redisTemplate);
    }

    @AfterEach
    void tearDown() {
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();
    }

    @Test
    void should_not_exceed_stock_under_concurrent_reservations() throws Exception {
        Long itemId = 1L;
        int stock = 10;
        int requesters = 25;
        storeStockService.reset(itemId, stock);

        var executor = Executors.newFixedThreadPool(20);
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();
        for (int i = 0; i < requesters; i++) {
            futures.add(CompletableFuture.supplyAsync(
                    () -> storeStockService.reserve(itemId, 1),
                    executor
            ));
        }

        List<Boolean> results = futures.stream().map(CompletableFuture::join).toList();

        long success = results.stream().filter(Boolean::booleanValue).count();
        long remaining = Long.parseLong(redisTemplate.opsForValue().get("store:stock:item:" + itemId));

        assertThat(success).isEqualTo(stock);
        assertThat(remaining).isZero();

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
    }

    @Test
    void should_release_stock_on_cancel() {
        Long itemId = 2L;
        storeStockService.reset(itemId, 3);

        assertThat(storeStockService.reserve(itemId, 3)).isTrue();
        assertThat(storeStockService.reserve(itemId, 1)).isFalse();

        storeStockService.release(itemId, 2);

        assertThat(storeStockService.reserve(itemId, 2)).isTrue();
    }
}
