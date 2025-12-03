package com.ssasinsa.wearagain.domain.community.entity;

import org.springframework.util.StringUtils;

public enum PostAdminKeywordScope {
    ALL,
    TITLE,
    AUTHOR;

    public static PostAdminKeywordScope from(String value) {
        if (!StringUtils.hasText(value)) {
            return ALL;
        }
        try {
            return PostAdminKeywordScope.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            return ALL;
        }
    }
}
