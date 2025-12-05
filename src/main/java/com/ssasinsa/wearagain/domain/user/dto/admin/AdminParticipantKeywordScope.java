package com.ssasinsa.wearagain.domain.user.dto.admin;

public enum AdminParticipantKeywordScope {
    ALL,
    EMAIL,
    NAME;

    public static AdminParticipantKeywordScope from(String value) {
        if (value == null || value.isBlank()) {
            return ALL;
        }
        try {
            return AdminParticipantKeywordScope.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return ALL;
        }
    }
}
