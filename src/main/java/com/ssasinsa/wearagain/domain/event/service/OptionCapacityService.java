package com.ssasinsa.wearagain.domain.event.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OptionCapacityService {

    private static final String KEY_FORMAT = "event:option:used:%d";

    private static final DefaultRedisScript<Long> RESERVE_SCRIPT = new DefaultRedisScript<>(
            "local used = redis.call('GET', KEYS[1]) or '0'\n"
                    + "if tonumber(used) >= tonumber(ARGV[1]) then return -1 end\n"
                    + "local newUsed = redis.call('INCR', KEYS[1])\n"
                    + "if tonumber(newUsed) > tonumber(ARGV[1]) then redis.call('DECR', KEYS[1]); return -1 end\n"
                    + "return newUsed",
            Long.class
    );

    private static final DefaultRedisScript<Long> RELEASE_SCRIPT = new DefaultRedisScript<>(
            "local used = redis.call('GET', KEYS[1]) or '0'\n"
                    + "if tonumber(used) <= 0 then return 0 end\n"
                    + "return redis.call('DECR', KEYS[1])",
            Long.class
    );

    private final StringRedisTemplate redisTemplate;

    public boolean reserve(Long optionId, int capacity) {
        if (optionId == null || capacity <= 0) {
            return true;
        }
        try {
            Long result = redisTemplate.execute(
                    RESERVE_SCRIPT,
                    List.of(key(optionId)),
                    String.valueOf(capacity)
            );
            return result != null && result >= 0;
        } catch (DataAccessException exception) {
            return false;
        }
    }

    public void release(Long optionId) {
        if (optionId == null) {
            return;
        }
        try {
            redisTemplate.execute(RELEASE_SCRIPT, List.of(key(optionId)));
        } catch (DataAccessException ignored) {
            // fallback 없음: 불일치 시 재동기화에서 회복
        }
    }

    public void reset(Long optionId, long used) {
        if (optionId == null || used < 0) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(key(optionId), String.valueOf(used));
        } catch (DataAccessException ignored) {
            // 재동기화 시 기록만 남기고 넘어간다
        }
    }

    private String key(Long optionId) {
        return KEY_FORMAT.formatted(optionId);
    }
}
