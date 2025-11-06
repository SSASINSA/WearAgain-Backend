package com.ssasinsa.wearagain.domain.event.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CheckinTokenUtil {

    private static final String KEY_PREFIX = "event:qr:";

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public void saveToken(Long userId, CheckinTokenPayload payload, Duration ttl) {
        String key = key(userId);
        redisTemplate.delete(key);
        try {
            String json = objectMapper.writeValueAsString(payload);
            redisTemplate.opsForValue().set(key, json, ttl);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize check-in token payload", exception);
        } catch (DataAccessException exception) {
            throw new IllegalStateException("Failed to store check-in token in Redis", exception);
        }
    }

    public Optional<CheckinTokenPayload> getToken(Long userId) {
        String key = key(userId);
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) {
            return Optional.empty();
        }
        try {
            CheckinTokenPayload payload = objectMapper.readValue(json, CheckinTokenPayload.class);
            return Optional.of(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to deserialize check-in token payload", exception);
        }
    }

    public void deleteToken(Long userId) {
        redisTemplate.delete(key(userId));
    }

    private String key(Long userId) {
        return KEY_PREFIX + userId;
    }
}
