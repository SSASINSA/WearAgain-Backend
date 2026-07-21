package com.ssasinsa.wearagain.global.common.qr;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ssasinsa.wearagain.domain.ticket.support.TicketQrTokenPayload;
import com.ssasinsa.wearagain.support.RedisTestContainerSupport;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

class QrTokenStoreTest extends RedisTestContainerSupport {

    private static final String TICKET_USER_KEY_PREFIX = "ticket:qr:user:";
    private static final String TICKET_TOKEN_KEY_PREFIX = "ticket:qr:token:";
    private static final Long USER_ID = 1L;
    private static final String TOKEN = "ticket-token";

    private LettuceConnectionFactory connectionFactory;
    private StringRedisTemplate redisTemplate;
    private QrTokenStore<TicketQrTokenPayload> qrTokenStore;

    @BeforeEach
    void setUp() {
        connectionFactory = createRedisConnectionFactory();
        redisTemplate = createRedisTemplate(connectionFactory);
        deleteTestKeys();

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
    }

    @AfterEach
    void tearDown() {
        deleteTestKeys();
        connectionFactory.destroy();
    }

    @Test
    void should_consume_ticket_only_once() {
        OffsetDateTime issuedAt = OffsetDateTime.now(ZoneOffset.UTC);
        TicketQrTokenPayload payload = new TicketQrTokenPayload(
                USER_ID,
                TOKEN,
                3,
                issuedAt,
                issuedAt.plusMinutes(15)
        );

        qrTokenStore.saveToken(USER_ID, payload, Duration.ofMinutes(15));

        var firstConsume = qrTokenStore.consumeTokenByToken(TOKEN);
        var secondConsume = qrTokenStore.consumeTokenByToken(TOKEN);

        assertThat(firstConsume).isPresent();
        assertThat(firstConsume.get()).isEqualTo(payload);
        assertThat(secondConsume).isEmpty();
        assertThat(redisTemplate.opsForValue().get(TICKET_TOKEN_KEY_PREFIX + TOKEN)).isNull();
        assertThat(redisTemplate.opsForValue().get(TICKET_USER_KEY_PREFIX + USER_ID)).isNull();
    }

    private void deleteTestKeys() {
        deleteKeys(
                redisTemplate,
                TICKET_TOKEN_KEY_PREFIX + TOKEN,
                TICKET_USER_KEY_PREFIX + USER_ID
        );
    }
}
