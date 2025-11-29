package com.ssasinsa.wearagain.domain.user.dto.admin;

public record AdminParticipantUpdateRequest(
        String name,
        String avatarUrl,
        Integer ticketBalance,
        Integer creditBalance,
        Boolean suspended
) {
}
