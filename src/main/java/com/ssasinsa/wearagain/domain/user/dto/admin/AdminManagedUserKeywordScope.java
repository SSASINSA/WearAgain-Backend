package com.ssasinsa.wearagain.domain.user.dto.admin;

public enum AdminManagedUserKeywordScope {
    ALL,
    EMAIL,
    NAME;

    public static AdminManagedUserKeywordScope from(String value) {
        if (value == null || value.isBlank()) {
            return ALL;
        }
        try {
            return AdminManagedUserKeywordScope.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            return ALL;
        }
    }
}
