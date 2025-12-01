package com.ssasinsa.wearagain.domain.store.entity;

import org.springframework.data.domain.Sort;

public enum StoreAdminOrderSortType {
    LATEST,
    OLDEST;

    public Sort toSort() {
        return switch (this) {
            case LATEST -> Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
            case OLDEST -> Sort.by(Sort.Order.asc("createdAt"), Sort.Order.asc("id"));
        };
    }
}
