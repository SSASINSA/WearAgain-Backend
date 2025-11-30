package com.ssasinsa.wearagain.domain.store.entity;

import org.springframework.data.domain.Sort;

public enum StoreAdminItemSortType {
    LATEST,
    OLDEST,
    TITLE_ASC;

    public Sort toSort() {
        return switch (this) {
            case LATEST -> Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
            case OLDEST -> Sort.by(Sort.Order.asc("createdAt"), Sort.Order.asc("id"));
            case TITLE_ASC -> Sort.by(Sort.Order.asc("name"), Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
        };
    }
}
