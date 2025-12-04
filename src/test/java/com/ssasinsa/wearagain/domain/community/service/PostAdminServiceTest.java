package com.ssasinsa.wearagain.domain.community.service;

import com.ssasinsa.wearagain.domain.auth.entity.User;
import com.ssasinsa.wearagain.domain.community.dto.admin.PostAdminDetailResponse;
import com.ssasinsa.wearagain.domain.community.dto.admin.PostAdminListRequest;
import com.ssasinsa.wearagain.domain.community.dto.admin.PostAdminListResponse;
import com.ssasinsa.wearagain.domain.community.entity.CommunityCategory;
import com.ssasinsa.wearagain.domain.community.entity.CommunityPost;
import com.ssasinsa.wearagain.domain.community.entity.PostComment;
import com.ssasinsa.wearagain.domain.community.entity.PostStatus;
import com.ssasinsa.wearagain.domain.community.exception.CommunityErrorCode;
import com.ssasinsa.wearagain.domain.community.exception.CommunityException;
import com.ssasinsa.wearagain.domain.community.repository.CommunityPostRepository;
import com.ssasinsa.wearagain.domain.community.repository.PostCommentRepository;
import com.ssasinsa.wearagain.domain.community.repository.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostAdminServiceTest {

    @Mock
    private CommunityPostRepository communityPostRepository;

    @Mock
    private PostCommentRepository postCommentRepository;

    @Mock
    private ReportRepository reportRepository;

    @InjectMocks
    private PostAdminServiceImpl postAdminService;

    private CommunityPost post;

    @BeforeEach
    void setUp() {
        User author = User.create("author@example.com", "Author", null);
        ReflectionTestUtils.setField(author, "id", 5L);

        CommunityCategory category = CommunityCategory.create("eco");
        ReflectionTestUtils.setField(category, "id", 2L);

        post = CommunityPost.create(author, category, "title", "content", List.of());
        ReflectionTestUtils.setField(post, "id", 101L);
        ReflectionTestUtils.setField(post, "createdAt", LocalDateTime.of(2025, 12, 3, 10, 0));
        ReflectionTestUtils.setField(post, "updatedAt", LocalDateTime.of(2025, 12, 3, 11, 0));
    }

    @Test
    void should_return_page_response() {
        Page<CommunityPost> page = new PageImpl<>(List.of(post));
        List<Object[]> commentCountResults = new ArrayList<>();
        commentCountResults.add(new Object[]{101L, 2L});
        List<Object[]> reportCountResults = new ArrayList<>();
        reportCountResults.add(new Object[]{101L, 1L});
        when(communityPostRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);
        when(communityPostRepository.countActiveCommentsByPostIds(List.of(101L)))
                .thenReturn(commentCountResults);
        when(reportRepository.countReportsByTargetTypeAndTargetIds("POST", List.of(101L)))
                .thenReturn(reportCountResults);

        PostAdminListRequest request = PostAdminListRequest.of(0, 20, null, null, null, null);

        PostAdminListResponse response = postAdminService.getPosts(request);

        assertThat(response.posts()).hasSize(1);
        PostAdminListResponse.PostAdminSummary summary = response.posts().get(0);
        assertThat(summary.postId()).isEqualTo(101L);
        assertThat(summary.commentCount()).isEqualTo(2);
        assertThat(summary.reportCount()).isEqualTo(1);
    }

    @Test
    void should_return_detail_response() {
        when(communityPostRepository.findByIdAndStatusNot(101L, PostStatus.INACTIVE))
                .thenReturn(Optional.of(post));

        PostComment comment = PostComment.create(post, post.getUser(), "good");
        ReflectionTestUtils.setField(comment, "id", 301L);
        comment.deactivate();
        when(postCommentRepository.findAllByPostIdForAdmin(101L))
                .thenReturn(List.of(comment));
        when(reportRepository.countByTargetTypeAndTargetId("POST", 101L)).thenReturn(1L);

        PostAdminDetailResponse detail = postAdminService.getPostDetail(101L);

        assertThat(detail.postId()).isEqualTo(101L);
        assertThat(detail.comments()).hasSize(1);
        assertThat(detail.commentCount()).isEqualTo(0);
        assertThat(detail.reportCount()).isEqualTo(1);
    }

    @Test
    void should_throw_exception_when_post_not_found() {
        when(communityPostRepository.findByIdAndStatusNot(101L, PostStatus.INACTIVE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> postAdminService.getPostDetail(101L))
                .isInstanceOf(CommunityException.class)
                .extracting(exception -> ((CommunityException) exception).getErrorCode())
                .isEqualTo(CommunityErrorCode.POST_NOT_FOUND);
    }

    @Test
    void should_delete_post_when_post_exists() {
        when(communityPostRepository.findByIdAndActiveTrue(101L))
                .thenReturn(Optional.of(post));

        postAdminService.deletePost(101L);

        assertThat(post.getStatus()).isEqualTo(PostStatus.INACTIVE);
        verify(communityPostRepository).findByIdAndActiveTrue(101L);
    }

    @Test
    void should_throw_exception_when_delete_post_not_found() {
        when(communityPostRepository.findByIdAndActiveTrue(101L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> postAdminService.deletePost(101L))
                .isInstanceOf(CommunityException.class)
                .extracting(exception -> ((CommunityException) exception).getErrorCode())
                .isEqualTo(CommunityErrorCode.POST_NOT_FOUND);
    }

    @Test
    void should_activate_post_when_post_exists() {
        post.deactivate();
        assertThat(post.getStatus()).isEqualTo(PostStatus.INACTIVE);

        when(communityPostRepository.findById(101L))
                .thenReturn(Optional.of(post));

        postAdminService.activatePost(101L);

        assertThat(post.getStatus()).isEqualTo(PostStatus.ACTIVE);
        verify(communityPostRepository).findById(101L);
    }

    @Test
    void should_activate_post_when_post_is_reported() {
        post.report();
        assertThat(post.getStatus()).isEqualTo(PostStatus.REPORTED);

        when(communityPostRepository.findById(101L))
                .thenReturn(Optional.of(post));

        postAdminService.activatePost(101L);

        assertThat(post.getStatus()).isEqualTo(PostStatus.ACTIVE);
        verify(communityPostRepository).findById(101L);
    }

    @Test
    void should_throw_exception_when_activate_post_not_found() {
        when(communityPostRepository.findById(101L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> postAdminService.activatePost(101L))
                .isInstanceOf(CommunityException.class)
                .extracting(exception -> ((CommunityException) exception).getErrorCode())
                .isEqualTo(CommunityErrorCode.POST_NOT_FOUND);
    }
}
