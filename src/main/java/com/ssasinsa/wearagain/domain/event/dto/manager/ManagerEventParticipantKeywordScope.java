package com.ssasinsa.wearagain.domain.event.dto.manager;

public enum ManagerEventParticipantKeywordScope {
    ALL,
    EMAIL,
    NAME;

    public static ManagerEventParticipantKeywordScope from(String value) {
        if (value == null || value.isBlank()) {
            return ALL;
        }
        try {
            return ManagerEventParticipantKeywordScope.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return ALL;
        }
    }
}
