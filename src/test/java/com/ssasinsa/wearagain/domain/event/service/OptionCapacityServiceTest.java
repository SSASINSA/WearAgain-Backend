package com.ssasinsa.wearagain.domain.event.service;

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

class OptionCapacityServiceTest extends RedisTestContainerSupport {

    private static final Long RESERVE_OPTION_ID = 1L;
    private static final Long RELEASE_OPTION_ID = 2L;
    private static final String CAPACITY_KEY_PREFIX = "event:option:used:";

    private LettuceConnectionFactory connectionFactory;
    private StringRedisTemplate redisTemplate;
    private OptionCapacityService optionCapacityService;

    @BeforeEach
    void setUp() {
        connectionFactory = createRedisConnectionFactory();
        redisTemplate = createRedisTemplate(connectionFactory);
        deleteTestKeys();
        optionCapacityService = new OptionCapacityService(redisTemplate);
    }

    @AfterEach
    void tearDown() {
        deleteTestKeys();
        connectionFactory.destroy();
    }

    @Test
    void should_not_exceed_capacity_under_concurrent_reservations() throws Exception {
        // Given
        int capacity = 10;
        int requesters = 25;

        var executor = Executors.newFixedThreadPool(20);
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();
        for (int i = 0; i < requesters; i++) {
            futures.add(CompletableFuture.supplyAsync(
                    () -> optionCapacityService.reserve(RESERVE_OPTION_ID, capacity), executor
            ));
        }

        // When
        List<Boolean> results = futures.stream().map(CompletableFuture::join).toList();

        // Then
        long success = results.stream().filter(Boolean::booleanValue).count();
        long used = Long.parseLong(redisTemplate.opsForValue().get(CAPACITY_KEY_PREFIX + RESERVE_OPTION_ID));

        assertThat(success).isEqualTo(capacity);
        assertThat(used).isEqualTo(capacity);

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
    }

    @Test
    void should_release_on_cancel() {
        // Given
        int capacity = 3;

        assertThat(optionCapacityService.reserve(RELEASE_OPTION_ID, capacity)).isTrue();
        assertThat(optionCapacityService.reserve(RELEASE_OPTION_ID, capacity)).isTrue();
        assertThat(optionCapacityService.reserve(RELEASE_OPTION_ID, capacity)).isTrue();
        assertThat(optionCapacityService.reserve(RELEASE_OPTION_ID, capacity)).isFalse();

        // When
        optionCapacityService.release(RELEASE_OPTION_ID);

        // Then
        assertThat(optionCapacityService.reserve(RELEASE_OPTION_ID, capacity)).isTrue();
    }

    private void deleteTestKeys() {
        deleteKeys(
                redisTemplate,
                CAPACITY_KEY_PREFIX + RESERVE_OPTION_ID,
                CAPACITY_KEY_PREFIX + RELEASE_OPTION_ID
        );
    }
}
