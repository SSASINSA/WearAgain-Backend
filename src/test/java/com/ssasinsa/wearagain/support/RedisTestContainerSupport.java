package com.ssasinsa.wearagain.support;

import java.util.Arrays;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public abstract class RedisTestContainerSupport {

    private static final int REDIS_PORT = 6379;

    private static final GenericContainer<?> REDIS_CONTAINER = new GenericContainer<>(
            DockerImageName.parse("redis:7.0.8-alpine")
    ).withExposedPorts(REDIS_PORT);

    static {
        REDIS_CONTAINER.start();
    }

    @DynamicPropertySource
    static void registerRedisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port", REDIS_CONTAINER::getFirstMappedPort);
        registry.add("spring.data.redis.password", () -> "");
    }

    protected LettuceConnectionFactory createRedisConnectionFactory() {
        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(
                REDIS_CONTAINER.getHost(),
                REDIS_CONTAINER.getFirstMappedPort()
        );
        connectionFactory.afterPropertiesSet();
        return connectionFactory;
    }

    protected StringRedisTemplate createRedisTemplate(LettuceConnectionFactory connectionFactory) {
        StringRedisTemplate redisTemplate = new StringRedisTemplate(connectionFactory);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    protected void deleteKeys(StringRedisTemplate redisTemplate, String... keys) {
        redisTemplate.delete(Arrays.asList(keys));
    }
}
