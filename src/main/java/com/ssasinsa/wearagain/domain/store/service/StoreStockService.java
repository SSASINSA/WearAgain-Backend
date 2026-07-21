package com.ssasinsa.wearagain.domain.store.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StoreStockService {

    private static final String KEY_FORMAT = "store:stock:item:%d";

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
     * 재고 예약: 재고가 부족하면 false 반환, 성공 시 감소.
     */
    public boolean reserve(Long itemId, int quantity) {
        if (itemId == null || quantity <= 0) {
            return true;
        }
        try {
            Long result = redisTemplate.execute(
                    RESERVE_SCRIPT,
                    List.of(key(itemId)),
                    String.valueOf(quantity)
            );
            return result != null && result >= 0;
        } catch (DataAccessException exception) {
            return false;
        }
    }

    /**
     * 재고 반환: 취소/보상 시 수량을 되돌린다.
     */
    public void release(Long itemId, int quantity) {
        if (itemId == null || quantity <= 0) {
            return;
        }
        try {
            redisTemplate.execute(
                    RELEASE_SCRIPT,
                    List.of(key(itemId)),
                    String.valueOf(quantity)
            );
        } catch (DataAccessException ignored) {
            // 재동기화로 회복
        }
    }

    /**
     * 재고 초기화/재동기화: DB 재고를 캐시에 설정.
     */
    public void reset(Long itemId, int stock) {
        if (itemId == null || stock < 0) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(key(itemId), String.valueOf(stock));
        } catch (DataAccessException ignored) {
            // 재동기화로 회복
        }
    }

    private String key(Long itemId) {
        return KEY_FORMAT.formatted(itemId);
    }
}
