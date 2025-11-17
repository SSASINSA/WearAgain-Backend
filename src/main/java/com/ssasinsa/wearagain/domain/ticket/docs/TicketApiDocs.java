package com.ssasinsa.wearagain.domain.ticket.docs;

import com.ssasinsa.wearagain.domain.ticket.dto.TicketChargeResponse;
import com.ssasinsa.wearagain.domain.ticket.dto.TicketQrResponse;
import com.ssasinsa.wearagain.domain.ticket.dto.TicketUseResponse;
import com.ssasinsa.wearagain.global.docs.annotation.ApiDoc;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public final class TicketApiDocs {

    public static final String USER_TAG_NAME = "티켓";
    public static final String USER_TAG_DESCRIPTION = "교환 티켓 관련 사용자 API";
    public static final String STAFF_TAG_NAME = "티켓 스태프";
    public static final String STAFF_TAG_DESCRIPTION = "교환 티켓 현장 사용 API";
    public static final String ADMIN_TAG_NAME = "티켓 관리자";
    public static final String ADMIN_TAG_DESCRIPTION = "교환 티켓 관리 및 충전 API";

    private TicketApiDocs() {
    }

    @SecurityRequirement(name = "userJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "교환 티켓 QR 발급",
            description = "사용자가 보유한 교환 티켓 잔여 수량과 QR 토큰을 반환합니다. TTL 15분, TTL 30초 이상 남은 경우 기존 토큰을 재사용하며, 잔여량이 0이어도 QR은 발급됩니다.",
            responseSchema = TicketQrResponse.class,
            responseExample = TicketExamples.USER_TICKET_QR_RESPONSE
    )
    public @interface GetTicketQr {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "교환 티켓 사용",
            description = "스태프 코드와 QR 토큰을 검증해 교환 티켓을 차감하고, 차감 결과와 처리 시각을 반환합니다.",
            requestExample = TicketExamples.STAFF_TICKET_USE_REQUEST,
            responseSchema = TicketUseResponse.class,
            responseExample = TicketExamples.STAFF_TICKET_USE_RESPONSE
    )
    public @interface StaffUseTicket {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "교환 티켓 충전",
            description = "스태프 코드와 QR 토큰을 검증해 교환 티켓 잔여량을 증가시킵니다.",
            requestExample = TicketExamples.STAFF_TICKET_CHARGE_REQUEST,
            responseSchema = TicketChargeResponse.class,
            responseExample = TicketExamples.STAFF_TICKET_CHARGE_RESPONSE
    )
    public @interface StaffChargeTicket {
    }

    @SecurityRequirement(name = "adminJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "교환 티켓 충전",
            description = "관리자가 사용자 티켓 잔여량을 증가시키고, 증감 이력을 기록합니다.",
            requestExample = TicketExamples.ADMIN_TICKET_CHARGE_REQUEST,
            responseSchema = TicketChargeResponse.class,
            responseExample = TicketExamples.ADMIN_TICKET_CHARGE_RESPONSE
    )
    public @interface AdminChargeTicket {
    }
}
