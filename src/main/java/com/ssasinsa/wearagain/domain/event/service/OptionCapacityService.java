package com.ssasinsa.wearagain.domain.event.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class OptionCapacityService {

    private static final String KEY_FORMAT = "event:option:used:%d";
    private static final long CAPACITY_EXCEEDED = -1L;
    private static final long CACHE_MISS = -2L;

    private static final RedisScript<Long> RESERVE_SCRIPT = RedisScript.of(
            new ClassPathResource("redis/event/reserve-option-capacity.lua"),
            Long.class
    );

    private static final RedisScript<Long> RELEASE_SCRIPT = RedisScript.of(
            new ClassPathResource("redis/event/release-option-capacity.lua"),
            Long.class
    );

    private final StringRedisTemplate redisTemplate;

    /**
     * 행사 옵션 정원 선점 메서드.
     */
    public ReserveResult reserve(Long optionId, int capacity) {
        if (optionId == null || capacity <= 0) {
            return ReserveResult.UNAVAILABLE;
        }
        try {
            Long result = redisTemplate.execute(
                    RESERVE_SCRIPT,
                    List.of(key(optionId)),
                    String.valueOf(capacity)
            );
            if (result == null) {
                log.error("[OptionCapacity] reserve 결과가 없습니다. optionId={}", optionId);
                return ReserveResult.UNAVAILABLE;
            }
            if (result >= 0) {
                return ReserveResult.RESERVED;
            }
            if (result == CAPACITY_EXCEEDED) {
                return ReserveResult.CAPACITY_EXCEEDED;
            }
            log.error("[OptionCapacity] reserve 결과가 올바르지 않습니다. optionId={}, result={}", optionId, result);
            return ReserveResult.UNAVAILABLE;
        } catch (DataAccessException exception) {
            log.error("[OptionCapacity] reserve 실행에 실패했습니다. optionId={}", optionId, exception);
            return ReserveResult.UNAVAILABLE;
        }
    }

    /**
     * 행사 옵션 정원 반환 메서드.
     */
    public ReleaseResult release(Long optionId) {
        if (optionId == null) {
            return ReleaseResult.UNAVAILABLE;
        }
        try {
            Long result = redisTemplate.execute(RELEASE_SCRIPT, List.of(key(optionId)));
            if (result == null) {
                log.error("[OptionCapacity] release 결과가 없습니다. optionId={}", optionId);
                return ReleaseResult.UNAVAILABLE;
            }
            if (result >= 0) {
                return ReleaseResult.RELEASED;
            }
            if (result == CACHE_MISS) {
                log.warn("[OptionCapacity] release 대상 key가 없습니다. optionId={}", optionId);
                return ReleaseResult.CACHE_MISS;
            }
            log.error("[OptionCapacity] release 결과가 올바르지 않습니다. optionId={}, result={}", optionId, result);
            return ReleaseResult.UNAVAILABLE;
        } catch (DataAccessException exception) {
            log.error("[OptionCapacity] release 실행에 실패했습니다. optionId={}", optionId, exception);
            return ReleaseResult.UNAVAILABLE;
        }
    }

    /**
     * DB 기준 행사 옵션 사용 인원 반영 메서드.
     */
    public ResetResult reset(Long optionId, long used) {
        if (optionId == null || used < 0) {
            return ResetResult.UNAVAILABLE;
        }
        try {
            redisTemplate.opsForValue().set(key(optionId), String.valueOf(used));
            return ResetResult.RESET;
        } catch (DataAccessException exception) {
            log.error("[OptionCapacity] reset 실행에 실패했습니다. optionId={}", optionId, exception);
            return ResetResult.UNAVAILABLE;
        }
    }

    private String key(Long optionId) {
        return KEY_FORMAT.formatted(optionId);
    }

    public enum ReserveResult {
        RESERVED,
        CAPACITY_EXCEEDED,
        UNAVAILABLE
    }

    public enum ReleaseResult {
        RELEASED,
        CACHE_MISS,
        UNAVAILABLE
    }

    public enum ResetResult {
        RESET,
        UNAVAILABLE
    }
}
