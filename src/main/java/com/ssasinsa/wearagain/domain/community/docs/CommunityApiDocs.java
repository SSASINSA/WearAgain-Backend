package com.ssasinsa.wearagain.domain.community.docs;

import com.ssasinsa.wearagain.domain.community.dto.response.CommentsResponse;
import com.ssasinsa.wearagain.domain.community.dto.response.CommunityImageUploadResponse;
import com.ssasinsa.wearagain.domain.community.dto.response.KeywordsResponse;
import com.ssasinsa.wearagain.domain.community.dto.response.PostDetailResponse;
import com.ssasinsa.wearagain.domain.community.dto.response.PostLikeResponse;
import com.ssasinsa.wearagain.domain.community.dto.response.PostsResponse;
import com.ssasinsa.wearagain.global.docs.annotation.ApiDoc;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public final class CommunityApiDocs {

    private CommunityApiDocs() {
    }

    public static final String TAG_NAME = "Community API";
    public static final String TAG_DESCRIPTION = "커뮤니티 게시글 CRUD API";

    @SecurityRequirement(name = "userJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "게시글 리스트 조회",
            description = """
                    커서 기반 페이지네이션으로 게시글 목록을 조회합니다.
                    키워드(카테고리) 필터와 좋아요 여부(isLiked)를 함께 제공합니다.
                    """,
            responseSchema = PostsResponse.class,
            responseExample = CommunityExamples.POSTS_RESPONSE
    )
    public @interface GetPosts {
    }

    @SecurityRequirement(name = "userJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "게시글 상세 조회",
            description = """
                    게시글 ID로 상세 정보를 조회합니다.
                    작성자 정보, 본문, 좋아요 여부, 키워드, 최신 댓글 수를 함께 반환합니다.
                    """,
            responseSchema = PostDetailResponse.class,
            responseExample = CommunityExamples.POST_DETAIL_RESPONSE
    )
    public @interface GetPostDetail {
    }

    @SecurityRequirement(name = "userJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "게시글 등록",
            description = """
                    새로운 게시글을 등록합니다.
                    제목, 내용, 키워드, 이미지 URL 목록을 전달하면 저장됩니다.
                    """,
            requestExample = CommunityExamples.POST_CREATE_REQUEST,
            responseSchema = Void.class
    )
    public @interface CreatePost {
    }

    @SecurityRequirement(name = "userJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "게시글 수정",
            description = """
                    게시글을 수정합니다.
                    작성자만 수정할 수 있으며 제목, 내용, 키워드를 변경합니다.
                    """,
            requestExample = CommunityExamples.POST_UPDATE_REQUEST,
            responseSchema = Void.class
    )
    public @interface UpdatePost {
    }

    @SecurityRequirement(name = "userJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "게시글 삭제",
            description = """
                    게시글을 삭제합니다.
                    작성자만 삭제할 수 있으며 soft delete(status=INACTIVE)로 처리됩니다.
                    """,
            responseSchema = Void.class
    )
    public @interface DeletePost {
    }

    @SecurityRequirement(name = "userJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "게시글 신고",
            description = """
                    게시글을 신고합니다.
                    신고 사유를 함께 전달하며 중복 신고가 들어오면 상태를 REPORTED로 변경합니다.
                    """,
            requestExample = CommunityExamples.REPORT_POST_REQUEST,
            responseSchema = Void.class
    )
    public @interface ReportPost {
    }

    @SecurityRequirement(name = "userJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "댓글 작성",
            description = """
                    게시글에 댓글을 작성합니다.
                    댓글 본문을 전달하면 등록됩니다.
                    """,
            requestExample = CommunityExamples.COMMENT_CREATE_REQUEST,
            responseSchema = Void.class
    )
    public @interface CreateComment {
    }

    @SecurityRequirement(name = "userJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "댓글 수정",
            description = """
                    댓글을 수정합니다.
                    작성자만 수정할 수 있습니다.
                    """,
            requestExample = CommunityExamples.COMMENT_UPDATE_REQUEST,
            responseSchema = Void.class
    )
    public @interface UpdateComment {
    }

    @SecurityRequirement(name = "userJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "댓글 삭제",
            description = """
                    댓글을 삭제합니다.
                    작성자만 삭제할 수 있으며 soft delete(active=false)로 처리됩니다.
                    """,
            responseSchema = Void.class
    )
    public @interface DeleteComment {
    }

    @SecurityRequirement(name = "userJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "댓글 리스트 조회",
            description = """
                    커서 기반 페이지네이션으로 댓글 목록을 조회합니다.
                    로그인 사용자가 작성한 댓글은 isMine=true 로 표시됩니다.
                    """,
            responseSchema = CommentsResponse.class,
            responseExample = CommunityExamples.COMMENTS_RESPONSE
    )
    public @interface GetComments {
    }

    @SecurityRequirement(name = "userJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "게시글 이미지 업로드",
            description = """
                    멀티파트 파일을 업로드해 게시글 이미지를 사전 업로드합니다.
                    서버 `/data/uploads` 경로에 저장한 뒤 imageName/imageUrl 정보를 반환합니다.
                    """,
            responseSchema = CommunityImageUploadResponse.class,
            responseExample = CommunityExamples.IMAGE_UPLOAD_RESPONSE
    )
    public @interface UploadImage {
    }

    @SecurityRequirement(name = "userJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "게시글 키워드 목록 조회",
            description = """
                    게시글 작성 시 사용할 수 있는 전체 키워드 목록을 조회합니다.
                    자주 사용되는 순서로 정렬된 리스트를 반환합니다.
                    """,
            responseSchema = KeywordsResponse.class,
            responseExample = CommunityExamples.KEYWORDS_RESPONSE
    )
    public @interface GetKeywords {
    }

    @SecurityRequirement(name = "userJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "게시글 좋아요 토글",
            description = """
                    게시글 좋아요를 토글합니다.
                    이미 좋아요한 상태면 취소하고, 아니라면 추가합니다.
                    처리 결과로 최신 좋아요 여부와 개수를 반환합니다.
                    """,
            responseSchema = PostLikeResponse.class,
            responseExample = CommunityExamples.POST_LIKE_RESPONSE
    )
    public @interface ToggleLike {
    }
}
