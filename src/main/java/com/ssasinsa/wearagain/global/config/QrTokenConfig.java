package com.ssasinsa.wearagain.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssasinsa.wearagain.domain.event.support.CheckinTokenPayload;
import com.ssasinsa.wearagain.domain.ticket.support.TicketQrTokenPayload;
import com.ssasinsa.wearagain.global.common.qr.QrTokenStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class QrTokenConfig {

    private static final String EVENT_USER_KEY_PREFIX = "event:qr:user:";
    private static final String EVENT_TOKEN_KEY_PREFIX = "event:qr:token:";
    private static final String TICKET_USER_KEY_PREFIX = "ticket:qr:user:";
    private static final String TICKET_TOKEN_KEY_PREFIX = "ticket:qr:token:";

    @Bean
    public QrTokenStore<CheckinTokenPayload> eventQrTokenStore(
            RedisTemplate<String, String> redisTemplate,
            ObjectMapper objectMapper
    ) {
        return new QrTokenStore<>(
                redisTemplate,
                objectMapper,
                EVENT_USER_KEY_PREFIX,
                EVENT_TOKEN_KEY_PREFIX,
                CheckinTokenPayload.class,
                CheckinTokenPayload::token
        );
    }

    @Bean
    public QrTokenStore<TicketQrTokenPayload> ticketQrTokenStore(
            RedisTemplate<String, String> redisTemplate,
            ObjectMapper objectMapper
    ) {
        return new QrTokenStore<>(
                redisTemplate,
                objectMapper,
                TICKET_USER_KEY_PREFIX,
                TICKET_TOKEN_KEY_PREFIX,
                TicketQrTokenPayload.class,
                TicketQrTokenPayload::token
        );
    }
}
