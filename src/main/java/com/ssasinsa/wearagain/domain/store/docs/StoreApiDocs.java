package com.ssasinsa.wearagain.domain.store.docs;

import com.ssasinsa.wearagain.domain.store.docs.StoreExamples;
import com.ssasinsa.wearagain.global.docs.annotation.ApiDoc;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public final class StoreApiDocs {

    public static final String TAG_NAME = "Store";
    public static final String TAG_DESCRIPTION = "스토어 상품/주문 API (사용자)";

    private StoreApiDocs() {
    }

    @SecurityRequirement(name = "userJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "스토어 상품 목록 조회",
            description = "사용자용 스토어 상품 목록을 조회합니다."
    )
    public @interface GetItems {
    }

    @SecurityRequirement(name = "userJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "스토어 상품 상세 조회",
            description = "사용자용 스토어 상품 상세 정보를 조회합니다."
    )
    public @interface GetItemDetail {
    }

    @SecurityRequirement(name = "userJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "스토어 주문 생성",
            description = "사용자용 스토어 주문을 생성합니다.",
            requestExample = StoreExamples.USER_STORE_ORDER_CREATE_REQUEST,
            responseExample = StoreExamples.USER_STORE_ORDER_CREATE_RESPONSE,
            successStatus = "201"
    )
    public @interface CreateOrder {
    }

    @SecurityRequirement(name = "userJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "스토어 주문 취소",
            description = "사용자 소유의 스토어 주문을 취소합니다.",
            responseExample = StoreExamples.USER_STORE_ORDER_CANCEL_RESPONSE
    )
    public @interface CancelOrder {
    }

    @SecurityRequirement(name = "userJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "스토어 주문 목록 조회",
            description = "사용자용 스토어 주문 목록을 커서 기반으로 조회합니다."
    )
    public @interface GetOrders {
    }

    @SecurityRequirement(name = "userJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "스토어 주문 상세 조회",
            description = "사용자용 스토어 주문 상세 정보를 조회합니다."
    )
    public @interface GetOrderDetail {
    }
}
