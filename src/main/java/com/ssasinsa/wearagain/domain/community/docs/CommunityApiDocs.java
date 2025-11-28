package com.ssasinsa.wearagain.domain.community.docs;

import com.ssasinsa.wearagain.domain.community.dto.request.CommentCreateRequest;
import com.ssasinsa.wearagain.domain.community.dto.request.CommentUpdateRequest;
import com.ssasinsa.wearagain.domain.community.dto.request.PostCreateRequest;
import com.ssasinsa.wearagain.domain.community.dto.request.PostUpdateRequest;
import com.ssasinsa.wearagain.domain.community.dto.request.ReportRequest;
import com.ssasinsa.wearagain.domain.community.dto.response.CommentsResponse;
import com.ssasinsa.wearagain.domain.community.dto.response.KeywordsResponse;
import com.ssasinsa.wearagain.domain.community.dto.response.PostDetailResponse;
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
                    커서 기반 페이지네이션으로 게시글 리스트를 조회합니다.
                    키워드 필터링이 가능하며, 토큰이 있으면 좋아요 여부를 포함합니다.
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
                    작성자 정보, 이미지, 좋아요 수, 댓글 수, 키워드(카테고리), 내 게시물 여부, 내 좋아요 게시물 여부를 포함합니다.
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
                    제목, 내용, 키워드(카테고리), 이미지 URL 목록을 입력받습니다.
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
                    작성자만 수정할 수 있으며, 제공된 필드만 업데이트됩니다.
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
                    작성자만 삭제할 수 있으며, 실제로는 soft delete(status=INACTIVE)로 처리됩니다.
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
                    게시글 ID와 신고 사유를 입력받습니다.
                    중복 신고는 불가능하며, 신고 시 게시글 상태가 REPORTED로 변경됩니다.
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
                    댓글 내용을 입력받습니다.
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
                    작성자만 삭제할 수 있으며, 실제로는 soft delete(active=false)로 처리됩니다.
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
                    커서 기반 페이지네이션을 사용하여 댓글 목록을 조회합니다.
                    인증 토큰이 없어도 접근 가능하며, 토큰이 유효하면 `isMine` 필드를 통해 현재 사용자가 작성한 댓글인지 여부를 알 수 있습니다.
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
            summary = "게시글 키워드(카테고리) 목록 조회",
            description = """
                    게시글 작성 시 사용할 수 있는 모든 키워드(카테고리) 목록을 조회합니다.
                    인증 토큰이 없어도 접근 가능합니다.
                    """,
            responseSchema = KeywordsResponse.class,
            responseExample = CommunityExamples.KEYWORDS_RESPONSE
    )
    public @interface GetKeywords {
    }
}

