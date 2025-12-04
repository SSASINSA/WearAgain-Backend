package com.ssasinsa.wearagain.domain.event.dto.manager;

public enum ManagerEventParticipantSort {
    LATEST,
    OLDEST,
    BY_EVENT,
    BY_USER;

    public static ManagerEventParticipantSort from(String value) {
        if (value == null || value.isBlank()) {
            return LATEST;
        }
        try {
            return ManagerEventParticipantSort.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return LATEST;
        }
    }
}
