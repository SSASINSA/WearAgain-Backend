package com.ssasinsa.wearagain.domain.community.docs;

import com.ssasinsa.wearagain.global.docs.annotation.ApiDoc;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public final class PostAdminApiDocs {

    public static final String TAG_NAME = "Community Admin";
    public static final String TAG_DESCRIPTION = "관리자 커뮤니티 게시글 관리";

    private PostAdminApiDocs() {
    }

    @SecurityRequirement(name = "adminJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "게시글 목록 조회",
            description = "page/size/status/keywordScope 파라미터로 게시글을 필터링한다.",
            responseExample = PostAdminExamples.POST_LIST_RESPONSE
    )
    public @interface GetPosts {
    }

    @SecurityRequirement(name = "adminJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "게시글 상세 조회",
            description = "게시글 본문, 이미지, 댓글, 신고 현황을 조회한다.",
            responseExample = PostAdminExamples.POST_DETAIL_RESPONSE
    )
    public @interface GetPostDetail {
    }

    @SecurityRequirement(name = "adminJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "게시글 삭제",
            description = """
                    게시글을 삭제합니다.
                    관리자는 모든 게시글을 삭제할 수 있으며, 실제로는 soft delete(status=INACTIVE)로 처리됩니다.
                    """,
            responseSchema = Void.class
    )
    public @interface DeletePost {
    }

    @SecurityRequirement(name = "adminJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "게시글 활성화",
            description = """
                    게시글 상태를 ACTIVE로 변경합니다.
                    관리자는 모든 게시글을 활성화할 수 있습니다.
                    """,
            responseSchema = Void.class
    )
    public @interface ActivatePost {
    }
}
