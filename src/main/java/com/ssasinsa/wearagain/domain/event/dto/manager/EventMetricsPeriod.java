package com.ssasinsa.wearagain.domain.event.dto.manager;

import java.time.LocalDate;

public enum EventMetricsPeriod {
    MONTH_1,
    MONTH_3,
    YEAR_1;

    public LocalDate fromDate(LocalDate now) {
        return switch (this) {
            case MONTH_1 -> now.minusMonths(1);
            case MONTH_3 -> now.minusMonths(3);
            case YEAR_1 -> now.minusYears(1);
        };
    }

    public static EventMetricsPeriod from(String value) {
        if (value == null) {
            return MONTH_1;
        }
        try {
            return EventMetricsPeriod.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            return MONTH_1;
        }
    }
}
