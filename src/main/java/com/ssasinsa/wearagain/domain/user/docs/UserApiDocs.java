package com.ssasinsa.wearagain.domain.user.docs;

import com.ssasinsa.wearagain.domain.community.dto.response.PostsResponse;
import com.ssasinsa.wearagain.domain.user.dto.UserDisplayNameResponse;
import com.ssasinsa.wearagain.domain.user.dto.UserSummaryResponse;
import com.ssasinsa.wearagain.global.docs.annotation.ApiDoc;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public final class UserApiDocs {

    public static final String USER_TAG_NAME = "사용자";
    public static final String USER_TAG_DESCRIPTION = "사용자 정보 조회 API";

    private UserApiDocs() {
    }

    @SecurityRequirement(name = "userJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "사용자 잔여 자원 요약 조회",
            description = "로그인한 사용자의 교환 티켓 및 크레딧 잔여량을 함께 반환합니다.",
            responseSchema = UserSummaryResponse.class,
            responseExample = UserExamples.USER_SUMMARY_RESPONSE
    )
    public @interface GetUserSummary {
    }

    @SecurityRequirement(name = "userJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "내가 쓴 게시물 리스트 조회",
            description = """
                    커서 기반 페이지네이션으로 내가 작성한 게시물 리스트를 조회합니다.
                    좋아요 여부를 포함합니다.
                    """,
            responseSchema = PostsResponse.class,
            responseExample = UserExamples.MY_POSTS_RESPONSE
    )
    public @interface GetMyPosts {
    }

    @SecurityRequirement(name = "userJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "내가 댓글을 쓴 게시물 리스트 조회",
            description = """
                    커서 기반 페이지네이션으로 내가 댓글을 작성한 게시물 리스트를 조회합니다.
                    좋아요 여부를 포함합니다.
                    """,
            responseSchema = PostsResponse.class,
            responseExample = UserExamples.MY_COMMENTED_POSTS_RESPONSE
    )
    public @interface GetMyCommentedPosts {
    }

    @SecurityRequirement(name = "userJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "표시 이름 변경",
            description = "로그인한 사용자의 표시 이름을 변경합니다.",
            responseSchema = UserDisplayNameResponse.class,
            responseExample = UserExamples.UPDATE_DISPLAY_NAME_RESPONSE
    )
    public @interface UpdateDisplayName {
    }

    @SecurityRequirement(name = "userJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "회원 탈퇴",
            description = "로그인한 사용자가 모든 개인정보를 삭제하고 서비스 이용을 중단합니다.",
            responseSchema = Void.class,
            successStatus = "204"
    )
    public @interface DeleteAccount {
    }
}
