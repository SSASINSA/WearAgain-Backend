package com.ssasinsa.wearagain.domain.store.docs;

import com.ssasinsa.wearagain.global.docs.annotation.ApiDoc;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public final class StoreApiDocs {

    public static final String TAG_NAME = "Store";
    public static final String TAG_DESCRIPTION = "스토어 상품 관리 및 조회 API";

    private StoreApiDocs() {
    }

    @SecurityRequirement(name = "adminJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "스토어 상품 이미지 업로드",
            description = "이미지 파일을 업로드하고 저장 경로를 반환합니다."
    )
    public @interface UploadItemImage {
    }

    @SecurityRequirement(name = "adminJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "관리자 스토어 상품 등록",
            description = "관리자 페이지에서 스토어 상품과 이미지를 등록합니다."
    )
    public @interface CreateItem {
    }

    @SecurityRequirement(name = "adminJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "관리자 스토어 상품 목록 조회",
            description = "상태, 카테고리, 키워드로 필터링하여 페이지네이션된 상품 목록을 조회합니다."
    )
    public @interface GetAdminItems {
    }

    @SecurityRequirement(name = "adminJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "관리자 스토어 상품 상세 조회",
            description = "상품 기본 정보와 이미지 목록을 조회합니다."
    )
    public @interface GetAdminItemDetail {
    }

    @SecurityRequirement(name = "adminJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "관리자 스토어 상품 수정",
            description = "상품 기본 정보, 가격, 재고, 상태, 이미지를 수정합니다."
    )
    public @interface UpdateItem {
    }

    @SecurityRequirement(name = "adminJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "관리자 스토어 상품 상태 변경",
            description = "상품의 전시 상태를 변경합니다."
    )
    public @interface UpdateItemStatus {
    }

    @SecurityRequirement(name = "adminJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "관리자 스토어 상품 삭제",
            description = "스토어 상품을 비활성/삭제 상태로 전환합니다."
    )
    public @interface DeleteItem {
    }

    @SecurityRequirement(name = "adminJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "스토어 상품 목록 조회",
            description = "사용자용 스토어 상품 목록을 조회합니다."
    )
    public @interface GetItems {
    }

    @SecurityRequirement(name = "adminJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "스토어 상품 상세 조회",
            description = "사용자용 스토어 상품 상세 정보를 조회합니다."
    )
    public @interface GetItemDetail {
    }
}
