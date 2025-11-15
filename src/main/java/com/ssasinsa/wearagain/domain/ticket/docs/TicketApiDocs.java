package com.ssasinsa.wearagain.domain.ticket.docs;

import com.ssasinsa.wearagain.domain.ticket.dto.TicketQrResponse;
import com.ssasinsa.wearagain.global.docs.annotation.ApiDoc;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public final class TicketApiDocs {

    public static final String USER_TAG_NAME = "티켓";
    public static final String USER_TAG_DESCRIPTION = "교환 티켓 관련 사용자 API";

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
}
