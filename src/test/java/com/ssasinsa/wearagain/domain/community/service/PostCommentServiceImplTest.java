package com.ssasinsa.wearagain.domain.community.service;

import com.ssasinsa.wearagain.domain.auth.entity.User;
import com.ssasinsa.wearagain.domain.auth.repository.UserRepository;
import com.ssasinsa.wearagain.domain.community.dto.request.CommentCreateRequest;
import com.ssasinsa.wearagain.domain.community.dto.request.CommentUpdateRequest;
import com.ssasinsa.wearagain.domain.community.dto.request.CommentsRequest;
import com.ssasinsa.wearagain.domain.community.dto.response.CommentsResponse;
import com.ssasinsa.wearagain.domain.community.entity.CommentStatus;
import com.ssasinsa.wearagain.domain.community.entity.CommunityCategory;
import com.ssasinsa.wearagain.domain.community.entity.CommunityPost;
import com.ssasinsa.wearagain.domain.community.entity.PostComment;
import com.ssasinsa.wearagain.domain.community.exception.CommunityErrorCode;
import com.ssasinsa.wearagain.domain.community.exception.CommunityException;
import com.ssasinsa.wearagain.domain.community.repository.CommunityPostRepository;
import com.ssasinsa.wearagain.domain.community.repository.PostCommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostCommentServiceImplTest {

    @Mock
    private PostCommentRepository postCommentRepository;

    @Mock
    private CommunityPostRepository communityPostRepository;

    @Mock
    private UserRepository userRepository;

    private PostCommentServiceImpl postCommentService;

    @BeforeEach
    void setUp() {
        postCommentService = new PostCommentServiceImpl(
                postCommentRepository,
                communityPostRepository,
                userRepository
        );
    }

    @Test
    void should_create_comment_when_valid_request() {
        // Given
        Long postId = 1L;
        Long userId = 1L;
        String content = "좋은 후기네요! 저도 다녀왔어요 😄";

        User user = User.create("user@wearagain.kr", "사용자", null);
        ReflectionTestUtils.setField(user, "id", userId);

        CommunityCategory category = CommunityCategory.create("review");
        ReflectionTestUtils.setField(category, "id", 1L);

        CommunityPost post = CommunityPost.create(user, category, "제목", "내용", null);
        ReflectionTestUtils.setField(post, "id", postId);

        CommentCreateRequest request = new CommentCreateRequest(content);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(communityPostRepository.findByIdAndActiveTrue(postId)).thenReturn(Optional.of(post));
        when(postCommentRepository.save(any(PostComment.class))).thenAnswer(invocation -> {
            PostComment saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 1L);
            return saved;
        });

        // When
        postCommentService.createComment(postId, request, userId);

        // Then
        ArgumentCaptor<PostComment> commentCaptor = ArgumentCaptor.forClass(PostComment.class);
        verify(postCommentRepository).save(commentCaptor.capture());
        PostComment savedComment = commentCaptor.getValue();

        assertThat(savedComment.getContent()).isEqualTo(content);
        assertThat(savedComment.getUser().getId()).isEqualTo(userId);
        assertThat(savedComment.getPost().getId()).isEqualTo(postId);
        assertThat(savedComment.getStatus()).isEqualTo(CommentStatus.ACTIVE);
    }

    @Test
    void should_throw_exception_when_post_not_found_on_create() {
        // Given
        Long postId = 999L;
        Long userId = 1L;
        User user = User.create("user@wearagain.kr", "사용자", null);
        ReflectionTestUtils.setField(user, "id", userId);

        CommentCreateRequest request = new CommentCreateRequest("내용");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(communityPostRepository.findByIdAndActiveTrue(postId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> postCommentService.createComment(postId, request, userId))
                .isInstanceOf(CommunityException.class)
                .extracting(throwable -> ((CommunityException) throwable).getErrorCode())
                .isEqualTo(CommunityErrorCode.POST_NOT_FOUND);

        verify(postCommentRepository, never()).save(any(PostComment.class));
    }

    @Test
    void should_throw_exception_when_user_not_found_on_create() {
        // Given
        Long postId = 1L;
        Long userId = 999L;
        CommentCreateRequest request = new CommentCreateRequest("내용");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> postCommentService.createComment(postId, request, userId))
                .isInstanceOf(CommunityException.class)
                .extracting(throwable -> ((CommunityException) throwable).getErrorCode())
                .isEqualTo(CommunityErrorCode.INVALID_POST_DATA);

        verify(communityPostRepository, never()).findByIdAndActiveTrue(anyLong());
        verify(postCommentRepository, never()).save(any(PostComment.class));
    }

    @Test
    void should_update_comment_when_user_is_author() {
        // Given
        Long postId = 1L;
        Long commentId = 100L;
        Long userId = 1L;
        String newContent = "수정된 댓글 내용";

        User user = User.create("user@wearagain.kr", "사용자", null);
        ReflectionTestUtils.setField(user, "id", userId);

        CommunityCategory category = CommunityCategory.create("review");
        ReflectionTestUtils.setField(category, "id", 1L);

        CommunityPost post = CommunityPost.create(user, category, "제목", "내용", null);
        ReflectionTestUtils.setField(post, "id", postId);

        PostComment comment = PostComment.create(post, user, "원래 댓글 내용");
        ReflectionTestUtils.setField(comment, "id", commentId);

        CommentUpdateRequest request = new CommentUpdateRequest(newContent);

        when(communityPostRepository.findByIdAndActiveTrue(postId)).thenReturn(Optional.of(post));
        when(postCommentRepository.findByIdAndActiveTrue(commentId)).thenReturn(Optional.of(comment));

        // When
        postCommentService.updateComment(postId, commentId, request, userId);

        // Then
        assertThat(comment.getContent()).isEqualTo(newContent);
    }

    @Test
    void should_throw_exception_when_comment_not_found_on_update() {
        // Given
        Long postId = 1L;
        Long commentId = 999L;
        Long userId = 1L;

        User user = User.create("user@wearagain.kr", "사용자", null);
        ReflectionTestUtils.setField(user, "id", userId);

        CommunityCategory category = CommunityCategory.create("review");
        ReflectionTestUtils.setField(category, "id", 1L);

        CommunityPost post = CommunityPost.create(user, category, "제목", "내용", null);
        ReflectionTestUtils.setField(post, "id", postId);

        CommentUpdateRequest request = new CommentUpdateRequest("내용");

        when(communityPostRepository.findByIdAndActiveTrue(postId)).thenReturn(Optional.of(post));
        when(postCommentRepository.findByIdAndActiveTrue(commentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> postCommentService.updateComment(postId, commentId, request, userId))
                .isInstanceOf(CommunityException.class)
                .extracting(throwable -> ((CommunityException) throwable).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMENT_NOT_FOUND);
    }

    @Test
    void should_throw_exception_when_user_is_not_author_on_update() {
        // Given
        Long postId = 1L;
        Long commentId = 100L;
        Long authorId = 1L;
        Long requesterId = 2L;

        User author = User.create("author@wearagain.kr", "작성자", null);
        ReflectionTestUtils.setField(author, "id", authorId);

        User requester = User.create("requester@wearagain.kr", "요청자", null);
        ReflectionTestUtils.setField(requester, "id", requesterId);

        CommunityCategory category = CommunityCategory.create("review");
        ReflectionTestUtils.setField(category, "id", 1L);

        CommunityPost post = CommunityPost.create(author, category, "제목", "내용", null);
        ReflectionTestUtils.setField(post, "id", postId);

        PostComment comment = PostComment.create(post, author, "댓글 내용");
        ReflectionTestUtils.setField(comment, "id", commentId);

        CommentUpdateRequest request = new CommentUpdateRequest("내용");

        when(communityPostRepository.findByIdAndActiveTrue(postId)).thenReturn(Optional.of(post));
        when(postCommentRepository.findByIdAndActiveTrue(commentId)).thenReturn(Optional.of(comment));

        // When & Then
        assertThatThrownBy(() -> postCommentService.updateComment(postId, commentId, request, requesterId))
                .isInstanceOf(CommunityException.class)
                .extracting(throwable -> ((CommunityException) throwable).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMENT_UPDATE_FORBIDDEN);
    }

    @Test
    void should_delete_comment_when_user_is_author() {
        // Given
        Long postId = 1L;
        Long commentId = 100L;
        Long userId = 1L;

        User user = User.create("user@wearagain.kr", "사용자", null);
        ReflectionTestUtils.setField(user, "id", userId);

        CommunityCategory category = CommunityCategory.create("review");
        ReflectionTestUtils.setField(category, "id", 1L);

        CommunityPost post = CommunityPost.create(user, category, "제목", "내용", null);
        ReflectionTestUtils.setField(post, "id", postId);

        PostComment comment = PostComment.create(post, user, "댓글 내용");
        ReflectionTestUtils.setField(comment, "id", commentId);

        when(communityPostRepository.findByIdAndActiveTrue(postId)).thenReturn(Optional.of(post));
        when(postCommentRepository.findByIdAndActiveTrue(commentId)).thenReturn(Optional.of(comment));

        // When
        postCommentService.deleteComment(postId, commentId, userId);

        // Then
        assertThat(comment.getStatus()).isEqualTo(CommentStatus.INACTIVE);
    }

    @Test
    void should_throw_exception_when_comment_not_found_on_delete() {
        // Given
        Long postId = 1L;
        Long commentId = 999L;
        Long userId = 1L;

        User user = User.create("user@wearagain.kr", "사용자", null);
        ReflectionTestUtils.setField(user, "id", userId);

        CommunityCategory category = CommunityCategory.create("review");
        ReflectionTestUtils.setField(category, "id", 1L);

        CommunityPost post = CommunityPost.create(user, category, "제목", "내용", null);
        ReflectionTestUtils.setField(post, "id", postId);

        when(communityPostRepository.findByIdAndActiveTrue(postId)).thenReturn(Optional.of(post));
        when(postCommentRepository.findByIdAndActiveTrue(commentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> postCommentService.deleteComment(postId, commentId, userId))
                .isInstanceOf(CommunityException.class)
                .extracting(throwable -> ((CommunityException) throwable).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMENT_NOT_FOUND);
    }

    @Test
    void should_throw_exception_when_user_is_not_author_on_delete() {
        // Given
        Long postId = 1L;
        Long commentId = 100L;
        Long authorId = 1L;
        Long requesterId = 2L;

        User author = User.create("author@wearagain.kr", "작성자", null);
        ReflectionTestUtils.setField(author, "id", authorId);

        CommunityCategory category = CommunityCategory.create("review");
        ReflectionTestUtils.setField(category, "id", 1L);

        CommunityPost post = CommunityPost.create(author, category, "제목", "내용", null);
        ReflectionTestUtils.setField(post, "id", postId);

        PostComment comment = PostComment.create(post, author, "댓글 내용");
        ReflectionTestUtils.setField(comment, "id", commentId);

        when(communityPostRepository.findByIdAndActiveTrue(postId)).thenReturn(Optional.of(post));
        when(postCommentRepository.findByIdAndActiveTrue(commentId)).thenReturn(Optional.of(comment));

        // When & Then
        assertThatThrownBy(() -> postCommentService.deleteComment(postId, commentId, requesterId))
                .isInstanceOf(CommunityException.class)
                .extracting(throwable -> ((CommunityException) throwable).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMENT_DELETE_FORBIDDEN);
    }

    @Test
    void should_return_comments_list_when_comments_exist() {
        // Given
        Long postId = 1L;
        Long userId1 = 1L;
        Long userId2 = 2L;

        User author1 = User.create("author1@wearagain.kr", "작성자1", null);
        ReflectionTestUtils.setField(author1, "id", userId1);
        User author2 = User.create("author2@wearagain.kr", "작성자2", null);
        ReflectionTestUtils.setField(author2, "id", userId2);

        CommunityCategory category = CommunityCategory.create("review");
        ReflectionTestUtils.setField(category, "id", 1L);

        CommunityPost post = CommunityPost.create(author1, category, "제목", "내용", null);
        ReflectionTestUtils.setField(post, "id", postId);

        PostComment comment1 = PostComment.create(post, author1, "댓글1");
        ReflectionTestUtils.setField(comment1, "id", 10L);
        LocalDateTime createdAt1 = LocalDateTime.of(2025, 1, 15, 10, 30);
        ReflectionTestUtils.setField(comment1, "createdAt", createdAt1);

        PostComment comment2 = PostComment.create(post, author2, "댓글2");
        ReflectionTestUtils.setField(comment2, "id", 9L);
        LocalDateTime createdAt2 = LocalDateTime.of(2025, 1, 15, 9, 20);
        ReflectionTestUtils.setField(comment2, "createdAt", createdAt2);

        List<Long> commentIds = List.of(10L, 9L);
        List<PostComment> comments = List.of(comment1, comment2);
        CommentsRequest request = new CommentsRequest(null, 10);

        when(communityPostRepository.findByIdAndActiveTrue(postId)).thenReturn(Optional.of(post));
        when(postCommentRepository.findActiveCommentIdsByPostId(postId, null, PageRequest.of(0, 11)))
                .thenReturn(commentIds);
        when(postCommentRepository.findActiveCommentsByIds(commentIds)).thenReturn(comments);

        // When
        CommentsResponse response = postCommentService.getComments(postId, request, userId1);

        // Then
        assertThat(response.limit()).isEqualTo(10);
        assertThat(response.hasNext()).isFalse();
        assertThat(response.nextCursor()).isNull();
        assertThat(response.comments()).hasSize(2);
        assertThat(response.comments().get(0).id()).isEqualTo(10L);
        assertThat(response.comments().get(0).content()).isEqualTo("댓글1");
        assertThat(response.comments().get(0).author().id()).isEqualTo(userId1);
        assertThat(response.comments().get(0).isMine()).isTrue();
        assertThat(response.comments().get(1).id()).isEqualTo(9L);
        assertThat(response.comments().get(1).content()).isEqualTo("댓글2");
        assertThat(response.comments().get(1).isMine()).isFalse();
    }

    @Test
    void should_return_hasNext_true_when_more_comments_exist() {
        // Given
        Long postId = 1L;
        User author = User.create("author@wearagain.kr", "작성자", null);
        ReflectionTestUtils.setField(author, "id", 1L);

        CommunityCategory category = CommunityCategory.create("review");
        ReflectionTestUtils.setField(category, "id", 1L);

        CommunityPost post = CommunityPost.create(author, category, "제목", "내용", null);
        ReflectionTestUtils.setField(post, "id", postId);

        List<Long> commentIds = new ArrayList<>();
        List<PostComment> comments = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            long commentId = 20L - i;
            commentIds.add(commentId);
            PostComment comment = PostComment.create(post, author, "댓글" + i);
            ReflectionTestUtils.setField(comment, "id", commentId);
            comments.add(comment);
        }

        CommentsRequest request = new CommentsRequest(null, 10);
        List<Long> limitedIds = commentIds.subList(0, 10);
        List<PostComment> limitedComments = comments.subList(0, 10);

        when(communityPostRepository.findByIdAndActiveTrue(postId)).thenReturn(Optional.of(post));
        when(postCommentRepository.findActiveCommentIdsByPostId(postId, null, PageRequest.of(0, 11)))
                .thenReturn(commentIds);
        when(postCommentRepository.findActiveCommentsByIds(limitedIds)).thenReturn(limitedComments);

        // When
        CommentsResponse response = postCommentService.getComments(postId, request, 1L);

        // Then
        assertThat(response.limit()).isEqualTo(10);
        assertThat(response.hasNext()).isTrue();
        assertThat(response.nextCursor()).isEqualTo("11");
        assertThat(response.comments()).hasSize(10);
    }

    @Test
    void should_return_empty_list_when_no_comments_exist() {
        // Given
        Long postId = 1L;
        User author = User.create("author@wearagain.kr", "작성자", null);
        ReflectionTestUtils.setField(author, "id", 1L);

        CommunityCategory category = CommunityCategory.create("review");
        ReflectionTestUtils.setField(category, "id", 1L);

        CommunityPost post = CommunityPost.create(author, category, "제목", "내용", null);
        ReflectionTestUtils.setField(post, "id", postId);

        CommentsRequest request = new CommentsRequest(null, 10);

        when(communityPostRepository.findByIdAndActiveTrue(postId)).thenReturn(Optional.of(post));
        when(postCommentRepository.findActiveCommentIdsByPostId(postId, null, PageRequest.of(0, 11)))
                .thenReturn(List.of());

        // When
        CommentsResponse response = postCommentService.getComments(postId, request, null);

        // Then
        assertThat(response.comments()).isEmpty();
        assertThat(response.hasNext()).isFalse();
        assertThat(response.nextCursor()).isNull();
    }

    @Test
    void should_throw_exception_when_post_not_found_on_get_comments() {
        // Given
        Long postId = 999L;
        CommentsRequest request = new CommentsRequest(null, 10);

        when(communityPostRepository.findByIdAndActiveTrue(postId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> postCommentService.getComments(postId, request, null))
                .isInstanceOf(CommunityException.class)
                .extracting(throwable -> ((CommunityException) throwable).getErrorCode())
                .isEqualTo(CommunityErrorCode.POST_NOT_FOUND);
    }

    @Test
    void should_use_cursor_when_cursor_provided() {
        // Given
        Long postId = 1L;
        Long cursor = 10L;
        User author = User.create("author@wearagain.kr", "작성자", null);
        ReflectionTestUtils.setField(author, "id", 1L);

        CommunityCategory category = CommunityCategory.create("review");
        ReflectionTestUtils.setField(category, "id", 1L);

        CommunityPost post = CommunityPost.create(author, category, "제목", "내용", null);
        ReflectionTestUtils.setField(post, "id", postId);

        PostComment comment = PostComment.create(post, author, "댓글");
        ReflectionTestUtils.setField(comment, "id", 9L);

        List<Long> commentIds = List.of(9L);
        List<PostComment> comments = List.of(comment);
        CommentsRequest request = new CommentsRequest(cursor, 10);

        when(communityPostRepository.findByIdAndActiveTrue(postId)).thenReturn(Optional.of(post));
        when(postCommentRepository.findActiveCommentIdsByPostId(postId, cursor, PageRequest.of(0, 11)))
                .thenReturn(commentIds);
        when(postCommentRepository.findActiveCommentsByIds(commentIds)).thenReturn(comments);

        // When
        CommentsResponse response = postCommentService.getComments(postId, request, 1L);

        // Then
        assertThat(response.comments()).hasSize(1);
        assertThat(response.comments().get(0).id()).isEqualTo(9L);
    }

    @Test
    void should_return_isMine_true_when_user_is_author() {
        // Given
        Long postId = 1L;
        Long userId = 1L;

        User author = User.create("author@wearagain.kr", "작성자", null);
        ReflectionTestUtils.setField(author, "id", userId);

        CommunityCategory category = CommunityCategory.create("review");
        ReflectionTestUtils.setField(category, "id", 1L);

        CommunityPost post = CommunityPost.create(author, category, "제목", "내용", null);
        ReflectionTestUtils.setField(post, "id", postId);

        PostComment comment = PostComment.create(post, author, "댓글");
        ReflectionTestUtils.setField(comment, "id", 10L);

        List<Long> commentIds = List.of(10L);
        List<PostComment> comments = List.of(comment);
        CommentsRequest request = new CommentsRequest(null, 10);

        when(communityPostRepository.findByIdAndActiveTrue(postId)).thenReturn(Optional.of(post));
        when(postCommentRepository.findActiveCommentIdsByPostId(postId, null, PageRequest.of(0, 11)))
                .thenReturn(commentIds);
        when(postCommentRepository.findActiveCommentsByIds(commentIds)).thenReturn(comments);

        // When
        CommentsResponse response = postCommentService.getComments(postId, request, userId);

        // Then
        assertThat(response.comments()).hasSize(1);
        assertThat(response.comments().get(0).isMine()).isTrue();
    }

    @Test
    void should_return_isMine_false_when_user_is_not_author() {
        // Given
        Long postId = 1L;
        Long authorId = 1L;
        Long viewerId = 2L;

        User author = User.create("author@wearagain.kr", "작성자", null);
        ReflectionTestUtils.setField(author, "id", authorId);

        CommunityCategory category = CommunityCategory.create("review");
        ReflectionTestUtils.setField(category, "id", 1L);

        CommunityPost post = CommunityPost.create(author, category, "제목", "내용", null);
        ReflectionTestUtils.setField(post, "id", postId);

        PostComment comment = PostComment.create(post, author, "댓글");
        ReflectionTestUtils.setField(comment, "id", 10L);

        List<Long> commentIds = List.of(10L);
        List<PostComment> comments = List.of(comment);
        CommentsRequest request = new CommentsRequest(null, 10);

        when(communityPostRepository.findByIdAndActiveTrue(postId)).thenReturn(Optional.of(post));
        when(postCommentRepository.findActiveCommentIdsByPostId(postId, null, PageRequest.of(0, 11)))
                .thenReturn(commentIds);
        when(postCommentRepository.findActiveCommentsByIds(commentIds)).thenReturn(comments);

        // When
        CommentsResponse response = postCommentService.getComments(postId, request, viewerId);

        // Then
        assertThat(response.comments()).hasSize(1);
        assertThat(response.comments().get(0).isMine()).isFalse();
    }

    @Test
    void should_return_isMine_false_when_user_is_null() {
        // Given
        Long postId = 1L;
        Long authorId = 1L;

        User author = User.create("author@wearagain.kr", "작성자", null);
        ReflectionTestUtils.setField(author, "id", authorId);

        CommunityCategory category = CommunityCategory.create("review");
        ReflectionTestUtils.setField(category, "id", 1L);

        CommunityPost post = CommunityPost.create(author, category, "제목", "내용", null);
        ReflectionTestUtils.setField(post, "id", postId);

        PostComment comment = PostComment.create(post, author, "댓글");
        ReflectionTestUtils.setField(comment, "id", 10L);

        List<Long> commentIds = List.of(10L);
        List<PostComment> comments = List.of(comment);
        CommentsRequest request = new CommentsRequest(null, 10);

        when(communityPostRepository.findByIdAndActiveTrue(postId)).thenReturn(Optional.of(post));
        when(postCommentRepository.findActiveCommentIdsByPostId(postId, null, PageRequest.of(0, 11)))
                .thenReturn(commentIds);
        when(postCommentRepository.findActiveCommentsByIds(commentIds)).thenReturn(comments);

        // When
        CommentsResponse response = postCommentService.getComments(postId, request, null);

        // Then
        assertThat(response.comments()).hasSize(1);
        assertThat(response.comments().get(0).isMine()).isFalse();
    }
}

