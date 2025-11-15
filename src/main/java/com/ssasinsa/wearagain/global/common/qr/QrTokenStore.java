package com.ssasinsa.wearagain.global.common.qr;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;

@RequiredArgsConstructor
public class QrTokenStore<T> {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final String userKeyPrefix;
    private final String tokenKeyPrefix;
    private final Class<T> payloadType;
    private final Function<T, String> tokenExtractor;

    public String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public void saveToken(Long userId, T payload, Duration ttl) {
        String userKey = userKey(userId);
        String previousToken = redisTemplate.opsForValue().get(userKey);
        if (previousToken != null) {
            redisTemplate.delete(tokenKey(previousToken));
        }
        try {
            String token = tokenExtractor.apply(payload);
            String json = objectMapper.writeValueAsString(payload);
            redisTemplate.opsForValue().set(tokenKey(token), json, ttl);
            redisTemplate.opsForValue().set(userKey, token, ttl);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize QR token payload", exception);
        } catch (DataAccessException exception) {
            throw new IllegalStateException("Failed to store QR token in Redis", exception);
        }
    }

    public Optional<T> getTokenByToken(String token) {
        String json = redisTemplate.opsForValue().get(tokenKey(token));
        if (json == null) {
            return Optional.empty();
        }
        try {
            T payload = objectMapper.readValue(json, payloadType);
            return Optional.of(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to deserialize QR token payload", exception);
        }
    }

    public Optional<T> getTokenByUser(Long userId) {
        String token = redisTemplate.opsForValue().get(userKey(userId));
        if (token == null) {
            return Optional.empty();
        }
        return getTokenByToken(token);
    }

    public Optional<Long> getRemainingTtlByToken(String token) {
        Long ttl = redisTemplate.getExpire(tokenKey(token), TimeUnit.SECONDS);
        if (ttl == null || ttl < 0) {
            return Optional.empty();
        }
        return Optional.of(ttl);
    }

    public Optional<Long> getRemainingTtlByUser(Long userId) {
        String token = redisTemplate.opsForValue().get(userKey(userId));
        if (token == null) {
            return Optional.empty();
        }
        return getRemainingTtlByToken(token);
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
        return userKeyPrefix + userId;
    }

    private String tokenKey(String token) {
        return tokenKeyPrefix + token;
    }
}
