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

    private static final String USER_KEY_PREFIX = "event:qr:user:";
    private static final String TOKEN_KEY_PREFIX = "event:qr:token:";

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public void saveToken(Long userId, CheckinTokenPayload payload, Duration ttl) {
        String userKey = userKey(userId);
        String previousToken = redisTemplate.opsForValue().get(userKey);
        if (previousToken != null) {
            redisTemplate.delete(tokenKey(previousToken));
        }
        try {
            String json = objectMapper.writeValueAsString(payload);
            redisTemplate.opsForValue().set(tokenKey(payload.token()), json, ttl);
            redisTemplate.opsForValue().set(userKey, payload.token(), ttl);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize check-in token payload", exception);
        } catch (DataAccessException exception) {
            throw new IllegalStateException("Failed to store check-in token in Redis", exception);
        }
    }

    public Optional<CheckinTokenPayload> getTokenByToken(String token) {
        String json = redisTemplate.opsForValue().get(tokenKey(token));
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
        String userKey = userKey(userId);
        String token = redisTemplate.opsForValue().get(userKey);
        if (token != null) {
            redisTemplate.delete(tokenKey(token));
        }
        redisTemplate.delete(userKey);
    }

    private String userKey(Long userId) {
        return USER_KEY_PREFIX + userId;
    }

    private String tokenKey(String token) {
        return TOKEN_KEY_PREFIX + token;
    }
}
