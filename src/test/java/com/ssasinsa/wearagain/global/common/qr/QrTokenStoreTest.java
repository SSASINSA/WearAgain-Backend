package com.ssasinsa.wearagain.global.common.qr;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ssasinsa.wearagain.domain.ticket.support.TicketQrTokenPayload;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

class QrTokenStoreTest {
    //  해당 테스트 실행시 redis가 전부 비워지도록 해놓았으므로, 운영환경 등 민감한 곳에서는 이 테스트 금지
    private static final String TICKET_USER_KEY_PREFIX = "ticket:qr:user:";
    private static final String TICKET_TOKEN_KEY_PREFIX = "ticket:qr:token:";

    private StringRedisTemplate redisTemplate;
    private QrTokenStore<TicketQrTokenPayload> qrTokenStore;

    @BeforeEach
    void setUp() {
        RedisConnectionFactory connectionFactory = new LettuceConnectionFactory("localhost", 6379);
        ((LettuceConnectionFactory) connectionFactory).afterPropertiesSet();
        redisTemplate = new StringRedisTemplate(connectionFactory);
        redisTemplate.afterPropertiesSet();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        qrTokenStore = new QrTokenStore<>(
                redisTemplate,
                objectMapper,
                TICKET_USER_KEY_PREFIX,
                TICKET_TOKEN_KEY_PREFIX,
                TicketQrTokenPayload.class,
                TicketQrTokenPayload::token,
                TicketQrTokenPayload::userId
        );
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();
    }

    @AfterEach
    void tearDown() {
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();
    }

    @Test
    void consume_ticket_only_once() {
        Long userId = 1L;
        String token = "ticket-token";
        OffsetDateTime issuedAt = OffsetDateTime.now(ZoneOffset.UTC);
        TicketQrTokenPayload payload = new TicketQrTokenPayload(
                userId,
                token,
                3,
                issuedAt,
                issuedAt.plusMinutes(15)
        );

        qrTokenStore.saveToken(userId, payload, Duration.ofMinutes(15));

        var firstConsume = qrTokenStore.consumeTokenByToken(token);
        var secondConsume = qrTokenStore.consumeTokenByToken(token);

        assertThat(firstConsume).isPresent();
        assertThat(firstConsume.get()).isEqualTo(payload);
        assertThat(secondConsume).isEmpty();
        assertThat(redisTemplate.opsForValue().get(TICKET_TOKEN_KEY_PREFIX + token)).isNull();
        assertThat(redisTemplate.opsForValue().get(TICKET_USER_KEY_PREFIX + userId)).isNull();
    }
}
