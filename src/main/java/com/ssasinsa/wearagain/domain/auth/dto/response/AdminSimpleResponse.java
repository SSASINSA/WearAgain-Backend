package com.ssasinsa.wearagain.domain.auth.dto.response;

public record AdminSimpleResponse(String message) {

    public static AdminSimpleResponse of(String message) {
        return new AdminSimpleResponse(message);
    }
}
