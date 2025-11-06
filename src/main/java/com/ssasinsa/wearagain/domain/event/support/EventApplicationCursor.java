package com.ssasinsa.wearagain.domain.event.support;

import com.ssasinsa.wearagain.domain.event.entity.EventApplication;
import com.ssasinsa.wearagain.domain.event.exception.EventErrorCode;
import com.ssasinsa.wearagain.domain.event.exception.EventException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Optional;
import org.springframework.util.StringUtils;

public final class EventApplicationCursor {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    private EventApplicationCursor() {
    }

    public static Optional<Cursor> decode(String cursor) {
        if (!StringUtils.hasText(cursor)) {
            return Optional.empty();
        }
        try {
            String decoded = new String(DECODER.decode(cursor), StandardCharsets.UTF_8);
            String[] parts = decoded.split(":");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid cursor format");
            }
            LocalDateTime createdAt = LocalDateTime.parse(parts[0], FORMATTER);
            Long applicationId = Long.parseLong(parts[1]);
            return Optional.of(new Cursor(createdAt, applicationId));
        } catch (IllegalArgumentException | java.time.DateTimeException exception) {
            throw new EventException(EventErrorCode.INVALID_EVENT_QUERY, exception);
        }
    }

    public static String encode(EventApplication application) {
        return encode(application.getCreatedAt(), application.getId());
    }

    public static String encode(LocalDateTime createdAt, Long applicationId) {
        String raw = FORMATTER.format(createdAt) + ":" + applicationId;
        return ENCODER.encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public record Cursor(LocalDateTime createdAt, Long applicationId) {
    }
}
