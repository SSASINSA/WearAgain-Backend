package com.ssasinsa.wearagain.domain.store.docs;

import com.ssasinsa.wearagain.global.docs.annotation.ApiDoc;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public final class StoreAdminApiDocs {

    public static final String TAG_NAME = "Store Admin";
    public static final String TAG_DESCRIPTION = "스토어 관리자 API";

    private StoreAdminApiDocs() {
    }

    @SecurityRequirement(name = "adminJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "스토어 상품 이미지 업로드",
            description = "이미지를 업로드하고 접근 가능한 URL을 반환합니다."
    )
    public @interface UploadItemImage {
    }

    @SecurityRequirement(name = "adminJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "관리자 스토어 상품 등록",
            description = "관리자 페이지에서 스토어 상품을 등록합니다. 픽업 장소 목록을 반드시 포함해야 합니다.",
            requestExample = StoreExamples.ADMIN_STORE_ITEM_CREATE_REQUEST,
            responseExample = StoreExamples.ADMIN_STORE_ITEM_CREATE_RESPONSE
    )
    public @interface CreateItem {
    }

    @SecurityRequirement(name = "adminJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "관리자 스토어 상품 목록 조회",
            description = "상태, 카테고리, 키워드, 정렬 조건으로 구성된 관리자 상품 목록을 페이지 단위로 조회합니다."
    )
    public @interface GetAdminItems {
    }

    @SecurityRequirement(name = "adminJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "관리자 스토어 상품 상세 조회",
            description = "상품 기본 정보와 이미지, 픽업 정보 등을 조회합니다."
    )
    public @interface GetAdminItemDetail {
    }

    @SecurityRequirement(name = "adminJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "관리자 스토어 상품 수정",
            description = "상품 기본 정보, 가격, 재고, 상태, 픽업 장소, 이미지 순서를 수정합니다.",
            requestExample = StoreExamples.ADMIN_STORE_ITEM_UPDATE_REQUEST,
            responseExample = StoreExamples.ADMIN_STORE_ITEM_UPDATE_RESPONSE
    )
    public @interface UpdateItem {
    }

    @SecurityRequirement(name = "adminJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "관리자 스토어 상품 상태 변경",
            description = "상품의 상태를 변경합니다."
    )
    public @interface UpdateItemStatus {
    }

    @SecurityRequirement(name = "adminJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "관리자 스토어 상품 삭제",
            description = "스토어 상품을 비활성화하고 상태를 삭제로 변경합니다."
    )
    public @interface DeleteItem {
    }

    @SecurityRequirement(name = "adminJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "관리자 주문 내역 조회",
            description = "상태, 키워드, 정렬 기준으로 어드민 주문 목록을 페이지 단위로 조회합니다."
    )
    public @interface GetAdminOrders {
    }

    @SecurityRequirement(name = "adminJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "관리자 주문 취소",
            description = "특정 주문을 강제 취소하고 재고/크레딧을 롤백합니다."
    )
    public @interface CancelAdminOrder {
    }
}
