package com.ssasinsa.wearagain.global.dto;

public record MessageResponse(String message) {

    public static MessageResponse of(String message) {
        return new MessageResponse(message);
    }
}
