package com.ssasinsa.wearagain.domain.event.entity;

public enum EventStatus {
    DRAFT, //승인 대기중
    APPROVAL, // 승인됨
    OPEN, // 현재 진행중
    REJECTED, // 승인 거부됨
    CLOSED, // 행사 종료
    ARCHIVED // 삭제됨
}
