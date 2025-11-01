package com.ssasinsa.wearagain.global.security;

public record AuthenticatedUser(
        Long userId,
        String email,
        String displayName
) {
}
