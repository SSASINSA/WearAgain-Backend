package com.ssasinsa.wearagain.domain.event.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
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

class OptionCapacityServiceTest {

    private StringRedisTemplate redisTemplate;
    private OptionCapacityService optionCapacityService;

    @BeforeEach
    void setUp() {
        RedisConnectionFactory connectionFactory = new LettuceConnectionFactory("localhost", 6379);
        ((LettuceConnectionFactory) connectionFactory).afterPropertiesSet();
        redisTemplate = new StringRedisTemplate(connectionFactory);
        redisTemplate.afterPropertiesSet();
        optionCapacityService = new OptionCapacityService(redisTemplate);
    }

    @AfterEach
    void tearDown() {
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();
    }

    @Test
    void should_not_exceed_capacity_under_concurrent_reservations() throws Exception {
        // Given
        Long optionId = 1L;
        int capacity = 10;
        int requesters = 25;

        var executor = Executors.newFixedThreadPool(20);
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();
        for (int i = 0; i < requesters; i++) {
            futures.add(CompletableFuture.supplyAsync(
                    () -> optionCapacityService.reserve(optionId, capacity), executor
            ));
        }

        // When
        List<Boolean> results = futures.stream().map(CompletableFuture::join).toList();

        // Then
        long success = results.stream().filter(Boolean::booleanValue).count();
        long used = Long.parseLong(redisTemplate.opsForValue().get("event:option:used:" + optionId));

        assertThat(success).isEqualTo(capacity);
        assertThat(used).isEqualTo(capacity);

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
    }

    @Test
    void should_release_on_cancel() {
        // Given
        Long optionId = 2L;
        int capacity = 3;

        assertThat(optionCapacityService.reserve(optionId, capacity)).isTrue();
        assertThat(optionCapacityService.reserve(optionId, capacity)).isTrue();
        assertThat(optionCapacityService.reserve(optionId, capacity)).isTrue();
        assertThat(optionCapacityService.reserve(optionId, capacity)).isFalse();

        // When
        optionCapacityService.release(optionId);

        // Then
        assertThat(optionCapacityService.reserve(optionId, capacity)).isTrue();
    }
}
