package com.ssasinsa.wearagain.global.common.redis;

/**
 * resource별 local lock과 callback log에서 사용하는 식별자다.
 * 실제 Redis key 문자열을 표현하는 객체가 아니라 resource 종류와 DB ID를 묶은 값이다.
 */
public record RedisResourceKey(Type type, long resourceId) {

    /**
     * 상품 재고 resource를 식별한다.
     */
    public static RedisResourceKey storeItem(long itemId) {
        return new RedisResourceKey(Type.STORE_ITEM, itemId);
    }

    /**
     * 행사 옵션 정원 resource를 식별한다.
     */
    public static RedisResourceKey eventOption(long optionId) {
        return new RedisResourceKey(Type.EVENT_OPTION, optionId);
    }

    /**
     * DB 기준으로 Redis counter를 관리하는 resource 종류다.
     */
    public enum Type {
        STORE_ITEM,
        EVENT_OPTION
    }
}
