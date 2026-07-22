package com.ssasinsa.wearagain.domain.store.service;

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
public class StoreStockService {

    private static final String KEY_FORMAT = "store:stock:item:%d";
    private static final long STOCK_SHORTAGE = -1L;
    private static final long CACHE_MISS = -2L;

    private static final RedisScript<Long> RESERVE_SCRIPT = RedisScript.of(
            new ClassPathResource("redis/store/reserve-stock.lua"),
            Long.class
    );

    private static final RedisScript<Long> RELEASE_SCRIPT = RedisScript.of(
            new ClassPathResource("redis/store/release-stock.lua"),
            Long.class
    );

    private final StringRedisTemplate redisTemplate;

    /**
     * 상품 재고 선점 메서드.
     */
    public ReserveResult reserve(Long itemId, int quantity) {
        if (itemId == null || quantity <= 0) {
            return ReserveResult.UNAVAILABLE;
        }
        try {
            Long result = redisTemplate.execute(
                    RESERVE_SCRIPT,
                    List.of(key(itemId)),
                    String.valueOf(quantity)
            );
            if (result == null) {
                log.error("[StoreStock] reserve 결과가 없습니다. itemId={}", itemId);
                return ReserveResult.UNAVAILABLE;
            }
            if (result >= 0) {
                return ReserveResult.RESERVED;
            }
            if (result == STOCK_SHORTAGE) {
                return ReserveResult.STOCK_SHORTAGE;
            }
            if (result == CACHE_MISS) {
                log.warn("[StoreStock] reserve 대상 key가 없습니다. itemId={}", itemId);
                return ReserveResult.CACHE_MISS;
            }
            log.error("[StoreStock] reserve 결과가 올바르지 않습니다. itemId={}, result={}", itemId, result);
            return ReserveResult.UNAVAILABLE;
        } catch (DataAccessException exception) {
            log.error("[StoreStock] reserve 실행에 실패했습니다. itemId={}", itemId, exception);
            return ReserveResult.UNAVAILABLE;
        }
    }

    /**
     * 상품 재고 반환 메서드.
     */
    public ReleaseResult release(Long itemId, int quantity) {
        if (itemId == null || quantity <= 0) {
            return ReleaseResult.UNAVAILABLE;
        }
        try {
            Long result = redisTemplate.execute(
                    RELEASE_SCRIPT,
                    List.of(key(itemId)),
                    String.valueOf(quantity)
            );
            if (result == null) {
                log.error("[StoreStock] release 결과가 없습니다. itemId={}", itemId);
                return ReleaseResult.UNAVAILABLE;
            }
            if (result >= 0) {
                return ReleaseResult.RELEASED;
            }
            if (result == CACHE_MISS) {
                log.warn("[StoreStock] release 대상 key가 없습니다. itemId={}", itemId);
                return ReleaseResult.CACHE_MISS;
            }
            log.error("[StoreStock] release 결과가 올바르지 않습니다. itemId={}, result={}", itemId, result);
            return ReleaseResult.UNAVAILABLE;
        } catch (DataAccessException exception) {
            log.error("[StoreStock] release 실행에 실패했습니다. itemId={}", itemId, exception);
            return ReleaseResult.UNAVAILABLE;
        }
    }

    /**
     * DB 기준 상품 재고 반영 메서드.
     */
    public ResetResult reset(Long itemId, int stock) {
        if (itemId == null || stock < 0) {
            return ResetResult.UNAVAILABLE;
        }
        try {
            redisTemplate.opsForValue().set(key(itemId), String.valueOf(stock));
            return ResetResult.RESET;
        } catch (DataAccessException exception) {
            log.error("[StoreStock] reset 실행에 실패했습니다. itemId={}", itemId, exception);
            return ResetResult.UNAVAILABLE;
        }
    }

    private String key(Long itemId) {
        return KEY_FORMAT.formatted(itemId);
    }

    public enum ReserveResult {
        RESERVED,
        STOCK_SHORTAGE,
        CACHE_MISS,
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
