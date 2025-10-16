package com.ssasinsa.wearagain.global.security;

import java.util.UUID;

public record AuthenticatedUser(
        UUID userId,
        String email,
        String displayName
) {
}
