package com.ssasinsa.wearagain.domain.community.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ssasinsa.wearagain.domain.auth.entity.User;
import com.ssasinsa.wearagain.domain.auth.repository.UserRepository;
import com.ssasinsa.wearagain.domain.community.dto.request.PostCreateRequest;
import com.ssasinsa.wearagain.domain.community.dto.request.PostUpdateRequest;
import com.ssasinsa.wearagain.domain.community.dto.response.PostDetailResponse;
import com.ssasinsa.wearagain.domain.community.entity.CommunityCategory;
import com.ssasinsa.wearagain.domain.community.entity.CommunityPost;
import com.ssasinsa.wearagain.domain.community.entity.CommunityPostImage;
import com.ssasinsa.wearagain.domain.community.exception.CommunityErrorCode;
import com.ssasinsa.wearagain.domain.community.exception.CommunityException;
import com.ssasinsa.wearagain.domain.community.repository.CommunityCategoryRepository;
import com.ssasinsa.wearagain.domain.community.repository.CommunityPostImageRepository;
import com.ssasinsa.wearagain.domain.community.repository.CommunityPostRepository;
import com.ssasinsa.wearagain.domain.community.repository.PostLikeRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CommunityPostServiceImplTest {

    @Mock
    private CommunityPostRepository communityPostRepository;

    @Mock
    private CommunityCategoryRepository communityCategoryRepository;

    @Mock
    private CommunityPostImageRepository communityPostImageRepository;

    @Mock
    private PostLikeRepository postLikeRepository;

    @Mock
    private UserRepository userRepository;

    private CommunityPostServiceImpl communityPostService;

    @BeforeEach
    void setUp() {
        communityPostService = new CommunityPostServiceImpl(
                communityPostRepository,
                communityCategoryRepository,
                communityPostImageRepository,
                postLikeRepository,
                userRepository
        );
    }

    @Test
    void should_return_post_detail_when_post_exists() {
        // Given
        Long postId = 1L;
        Long userId = 1L;

        User author = User.create("author@wearagain.kr", "작성자", null);
        ReflectionTestUtils.setField(author, "id", userId);

        CommunityCategory category = CommunityCategory.create("review");
        ReflectionTestUtils.setField(category, "id", 1L);

        CommunityPost post = CommunityPost.create(author, category, "제목", "내용", null);
        ReflectionTestUtils.setField(post, "id", postId);
        LocalDateTime createdAt = LocalDateTime.of(2025, 1, 15, 10, 30);
        ReflectionTestUtils.setField(post, "createdAt", createdAt);

        when(communityPostRepository.findByIdAndActiveTrue(postId)).thenReturn(Optional.of(post));
        when(communityPostRepository.countActiveCommentsByPostId(postId)).thenReturn(5L);
        when(postLikeRepository.existsByPostIdAndUserId(postId, userId)).thenReturn(false);

        // When
        PostDetailResponse response = communityPostService.getPostDetail(postId, userId);

        // Then
        assertThat(response.id()).isEqualTo(postId);
        assertThat(response.title()).isEqualTo("제목");
        assertThat(response.content()).isEqualTo("내용");
        assertThat(response.author().id()).isEqualTo(userId);
        assertThat(response.author().name()).isEqualTo("작성자");
        assertThat(response.keyword()).isEqualTo("review");
        assertThat(response.likeCount()).isEqualTo(0);
        assertThat(response.commentCount()).isEqualTo(5);
        assertThat(response.createdAt()).isEqualTo(createdAt);
    }

    @Test
    void should_return_isMine_true_when_user_is_author() {
        // Given
        Long postId = 1L;
        Long authorId = 1L;

        User author = User.create("author@wearagain.kr", "작성자", null);
        ReflectionTestUtils.setField(author, "id", authorId);

        CommunityCategory category = CommunityCategory.create("review");
        CommunityPost post = CommunityPost.create(author, category, "제목", "내용", null);
        ReflectionTestUtils.setField(post, "id", postId);

        when(communityPostRepository.findByIdAndActiveTrue(postId)).thenReturn(Optional.of(post));
        when(communityPostRepository.countActiveCommentsByPostId(postId)).thenReturn(0L);
        when(postLikeRepository.existsByPostIdAndUserId(postId, authorId)).thenReturn(false);

        // When
        PostDetailResponse response = communityPostService.getPostDetail(postId, authorId);

        // Then
        assertThat(response.isMine()).isTrue();
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
        CommunityPost post = CommunityPost.create(author, category, "제목", "내용", null);
        ReflectionTestUtils.setField(post, "id", postId);

        when(communityPostRepository.findByIdAndActiveTrue(postId)).thenReturn(Optional.of(post));
        when(communityPostRepository.countActiveCommentsByPostId(postId)).thenReturn(0L);
        when(postLikeRepository.existsByPostIdAndUserId(postId, viewerId)).thenReturn(false);

        // When
        PostDetailResponse response = communityPostService.getPostDetail(postId, viewerId);

        // Then
        assertThat(response.isMine()).isFalse();
    }

    @Test
    void should_return_isLiked_true_when_user_liked_post() {
        // Given
        Long postId = 1L;
        Long userId = 1L;

        User author = User.create("author@wearagain.kr", "작성자", null);
        ReflectionTestUtils.setField(author, "id", 2L);

        CommunityCategory category = CommunityCategory.create("review");
        CommunityPost post = CommunityPost.create(author, category, "제목", "내용", null);
        ReflectionTestUtils.setField(post, "id", postId);

        when(communityPostRepository.findByIdAndActiveTrue(postId)).thenReturn(Optional.of(post));
        when(communityPostRepository.countActiveCommentsByPostId(postId)).thenReturn(0L);
        when(postLikeRepository.existsByPostIdAndUserId(postId, userId)).thenReturn(true);

        // When
        PostDetailResponse response = communityPostService.getPostDetail(postId, userId);

        // Then
        assertThat(response.isLiked()).isTrue();
    }

    @Test
    void should_return_isLiked_false_when_user_not_liked_post() {
        // Given
        Long postId = 1L;
        Long userId = 1L;

        User author = User.create("author@wearagain.kr", "작성자", null);
        ReflectionTestUtils.setField(author, "id", 2L);

        CommunityCategory category = CommunityCategory.create("review");
        CommunityPost post = CommunityPost.create(author, category, "제목", "내용", null);
        ReflectionTestUtils.setField(post, "id", postId);

        when(communityPostRepository.findByIdAndActiveTrue(postId)).thenReturn(Optional.of(post));
        when(communityPostRepository.countActiveCommentsByPostId(postId)).thenReturn(0L);
        when(postLikeRepository.existsByPostIdAndUserId(postId, userId)).thenReturn(false);

        // When
        PostDetailResponse response = communityPostService.getPostDetail(postId, userId);

        // Then
        assertThat(response.isLiked()).isFalse();
    }

    @Test
    void should_return_false_for_isMine_and_isLiked_when_user_is_null() {
        // Given
        Long postId = 1L;

        User author = User.create("author@wearagain.kr", "작성자", null);
        ReflectionTestUtils.setField(author, "id", 1L);

        CommunityCategory category = CommunityCategory.create("review");
        CommunityPost post = CommunityPost.create(author, category, "제목", "내용", null);
        ReflectionTestUtils.setField(post, "id", postId);

        when(communityPostRepository.findByIdAndActiveTrue(postId)).thenReturn(Optional.of(post));
        when(communityPostRepository.countActiveCommentsByPostId(postId)).thenReturn(0L);

        // When
        PostDetailResponse response = communityPostService.getPostDetail(postId, null);

        // Then
        assertThat(response.isMine()).isFalse();
        assertThat(response.isLiked()).isFalse();
        verify(postLikeRepository, never()).existsByPostIdAndUserId(anyLong(), anyLong());
    }

    @Test
    void should_return_imageUrl_when_post_has_images() {
        // Given
        Long postId = 1L;
        Long userId = 1L;

        User author = User.create("author@wearagain.kr", "작성자", null);
        ReflectionTestUtils.setField(author, "id", userId);

        CommunityCategory category = CommunityCategory.create("review");
        CommunityPost post = CommunityPost.create(author, category, "제목", "내용", List.of("image1.jpg", "image2.jpg"));
        ReflectionTestUtils.setField(post, "id", postId);

        when(communityPostRepository.findByIdAndActiveTrue(postId)).thenReturn(Optional.of(post));
        when(communityPostRepository.countActiveCommentsByPostId(postId)).thenReturn(0L);
        when(postLikeRepository.existsByPostIdAndUserId(postId, userId)).thenReturn(false);

        // When
        PostDetailResponse response = communityPostService.getPostDetail(postId, userId);

        // Then
        assertThat(response.imageUrl()).isEqualTo("image1.jpg");
    }

    @Test
    void should_throw_exception_when_post_not_found() {
        // Given
        Long postId = 999L;
        when(communityPostRepository.findByIdAndActiveTrue(postId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> communityPostService.getPostDetail(postId, 1L))
                .isInstanceOf(CommunityException.class)
                .extracting(throwable -> ((CommunityException) throwable).getErrorCode())
                .isEqualTo(CommunityErrorCode.POST_NOT_FOUND);
    }

    @Test
    void should_create_post_when_valid_request() {
        // Given
        Long userId = 1L;
        User user = User.create("user@wearagain.kr", "사용자", null);
        ReflectionTestUtils.setField(user, "id", userId);

        CommunityCategory category = CommunityCategory.create("review");
        ReflectionTestUtils.setField(category, "id", 1L);

        PostCreateRequest request = new PostCreateRequest(
                "제목",
                "내용",
                "review",
                null
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(communityCategoryRepository.findByName("review")).thenReturn(Optional.of(category));

        ArgumentCaptor<CommunityPost> postCaptor = ArgumentCaptor.forClass(CommunityPost.class);
        when(communityPostRepository.save(any(CommunityPost.class))).thenAnswer(invocation -> {
            CommunityPost saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 1L);
            return saved;
        });

        // When
        communityPostService.createPost(request, userId);

        // Then
        verify(communityPostRepository).save(postCaptor.capture());
        CommunityPost savedPost = postCaptor.getValue();
        assertThat(savedPost.getTitle()).isEqualTo("제목");
        assertThat(savedPost.getContent()).isEqualTo("내용");
        assertThat(savedPost.getUser().getId()).isEqualTo(userId);
        assertThat(savedPost.getCategory().getName()).isEqualTo("review");
    }

    @Test
    void should_create_post_with_images() {
        // Given
        Long userId = 1L;
        User user = User.create("user@wearagain.kr", "사용자", null);
        ReflectionTestUtils.setField(user, "id", userId);

        CommunityCategory category = CommunityCategory.create("review");
        ReflectionTestUtils.setField(category, "id", 1L);

        PostCreateRequest request = new PostCreateRequest(
                "제목",
                "내용",
                "review",
                List.of("image1.jpg", "image2.jpg")
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(communityCategoryRepository.findByName("review")).thenReturn(Optional.of(category));
        when(communityPostRepository.save(any(CommunityPost.class))).thenAnswer(invocation -> {
            CommunityPost saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 1L);
            return saved;
        });

        // When
        communityPostService.createPost(request, userId);

        // Then
        ArgumentCaptor<CommunityPost> postCaptor = ArgumentCaptor.forClass(CommunityPost.class);
        verify(communityPostRepository).save(postCaptor.capture());
        CommunityPost savedPost = postCaptor.getValue();
        assertThat(savedPost.getImages()).hasSize(2);
        assertThat(savedPost.getImages().get(0).getImageUrl()).isEqualTo("image1.jpg");
        assertThat(savedPost.getImages().get(1).getImageUrl()).isEqualTo("image2.jpg");
    }

    @Test
    void should_throw_exception_when_user_not_found_on_create() {
        // Given
        Long userId = 999L;
        PostCreateRequest request = new PostCreateRequest("제목", "내용", "review", null);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> communityPostService.createPost(request, userId))
                .isInstanceOf(CommunityException.class)
                .extracting(throwable -> ((CommunityException) throwable).getErrorCode())
                .isEqualTo(CommunityErrorCode.INVALID_POST_DATA);
    }

    @Test
    void should_throw_exception_when_category_not_found_on_create() {
        // Given
        Long userId = 1L;
        User user = User.create("user@wearagain.kr", "사용자", null);
        ReflectionTestUtils.setField(user, "id", userId);

        PostCreateRequest request = new PostCreateRequest("제목", "내용", "invalid", null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(communityCategoryRepository.findByName("invalid")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> communityPostService.createPost(request, userId))
                .isInstanceOf(CommunityException.class)
                .extracting(throwable -> ((CommunityException) throwable).getErrorCode())
                .isEqualTo(CommunityErrorCode.CATEGORY_NOT_FOUND);
    }

    @Test
    void should_update_post_when_user_is_author() {
        // Given
        Long postId = 1L;
        Long userId = 1L;

        User author = User.create("author@wearagain.kr", "작성자", null);
        ReflectionTestUtils.setField(author, "id", userId);

        CommunityCategory category = CommunityCategory.create("review");
        ReflectionTestUtils.setField(category, "id", 1L);

        CommunityPost post = CommunityPost.create(author, category, "제목", "내용", null);
        ReflectionTestUtils.setField(post, "id", postId);

        PostUpdateRequest request = new PostUpdateRequest(
                "수정된 제목",
                "수정된 내용",
                null,
                null
        );

        when(communityPostRepository.findByIdAndActiveTrue(postId)).thenReturn(Optional.of(post));

        // When
        communityPostService.updatePost(postId, request, userId);

        // Then
        assertThat(post.getTitle()).isEqualTo("수정된 제목");
        assertThat(post.getContent()).isEqualTo("수정된 내용");
    }

    @Test
    void should_update_title_only() {
        // Given
        Long postId = 1L;
        Long userId = 1L;

        User author = User.create("author@wearagain.kr", "작성자", null);
        ReflectionTestUtils.setField(author, "id", userId);

        CommunityCategory category = CommunityCategory.create("review");
        CommunityPost post = CommunityPost.create(author, category, "제목", "내용", null);
        ReflectionTestUtils.setField(post, "id", postId);

        PostUpdateRequest request = new PostUpdateRequest(
                "수정된 제목",
                null,
                null,
                null
        );

        when(communityPostRepository.findByIdAndActiveTrue(postId)).thenReturn(Optional.of(post));

        // When
        communityPostService.updatePost(postId, request, userId);

        // Then
        assertThat(post.getTitle()).isEqualTo("수정된 제목");
        assertThat(post.getContent()).isEqualTo("내용");
    }

    @Test
    void should_update_category() {
        // Given
        Long postId = 1L;
        Long userId = 1L;

        User author = User.create("author@wearagain.kr", "작성자", null);
        ReflectionTestUtils.setField(author, "id", userId);

        CommunityCategory oldCategory = CommunityCategory.create("review");
        CommunityCategory newCategory = CommunityCategory.create("question");
        ReflectionTestUtils.setField(newCategory, "id", 2L);

        CommunityPost post = CommunityPost.create(author, oldCategory, "제목", "내용", null);
        ReflectionTestUtils.setField(post, "id", postId);

        PostUpdateRequest request = new PostUpdateRequest(
                null,
                null,
                "question",
                null
        );

        when(communityPostRepository.findByIdAndActiveTrue(postId)).thenReturn(Optional.of(post));
        when(communityCategoryRepository.findByName("question")).thenReturn(Optional.of(newCategory));

        // When
        communityPostService.updatePost(postId, request, userId);

        // Then
        assertThat(post.getCategory().getName()).isEqualTo("question");
    }

    @Test
    void should_update_images() {
        // Given
        Long postId = 1L;
        Long userId = 1L;

        User author = User.create("author@wearagain.kr", "작성자", null);
        ReflectionTestUtils.setField(author, "id", userId);

        CommunityCategory category = CommunityCategory.create("review");
        CommunityPost post = CommunityPost.create(author, category, "제목", "내용", List.of("old1.jpg"));
        ReflectionTestUtils.setField(post, "id", postId);

        PostUpdateRequest request = new PostUpdateRequest(
                null,
                null,
                null,
                List.of("new1.jpg", "new2.jpg")
        );

        when(communityPostRepository.findByIdAndActiveTrue(postId)).thenReturn(Optional.of(post));

        // When
        communityPostService.updatePost(postId, request, userId);

        // Then
        verify(communityPostImageRepository).deleteByPost(post);
        assertThat(post.getImages()).hasSize(2);
        assertThat(post.getImages().get(0).getImageUrl()).isEqualTo("new1.jpg");
        assertThat(post.getImages().get(1).getImageUrl()).isEqualTo("new2.jpg");
    }

    @Test
    void should_throw_exception_when_post_not_found_on_update() {
        // Given
        Long postId = 999L;
        PostUpdateRequest request = new PostUpdateRequest("제목", null, null, null);

        when(communityPostRepository.findByIdAndActiveTrue(postId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> communityPostService.updatePost(postId, request, 1L))
                .isInstanceOf(CommunityException.class)
                .extracting(throwable -> ((CommunityException) throwable).getErrorCode())
                .isEqualTo(CommunityErrorCode.POST_NOT_FOUND);
    }

    @Test
    void should_throw_exception_when_user_is_not_author_on_update() {
        // Given
        Long postId = 1L;
        Long authorId = 1L;
        Long requesterId = 2L;

        User author = User.create("author@wearagain.kr", "작성자", null);
        ReflectionTestUtils.setField(author, "id", authorId);

        CommunityCategory category = CommunityCategory.create("review");
        CommunityPost post = CommunityPost.create(author, category, "제목", "내용", null);
        ReflectionTestUtils.setField(post, "id", postId);

        PostUpdateRequest request = new PostUpdateRequest("제목", null, null, null);

        when(communityPostRepository.findByIdAndActiveTrue(postId)).thenReturn(Optional.of(post));

        // When & Then
        assertThatThrownBy(() -> communityPostService.updatePost(postId, request, requesterId))
                .isInstanceOf(CommunityException.class)
                .extracting(throwable -> ((CommunityException) throwable).getErrorCode())
                .isEqualTo(CommunityErrorCode.POST_UPDATE_FORBIDDEN);
    }

    @Test
    void should_throw_exception_when_category_not_found_on_update() {
        // Given
        Long postId = 1L;
        Long userId = 1L;

        User author = User.create("author@wearagain.kr", "작성자", null);
        ReflectionTestUtils.setField(author, "id", userId);

        CommunityCategory category = CommunityCategory.create("review");
        CommunityPost post = CommunityPost.create(author, category, "제목", "내용", null);
        ReflectionTestUtils.setField(post, "id", postId);

        PostUpdateRequest request = new PostUpdateRequest(null, null, "invalid", null);

        when(communityPostRepository.findByIdAndActiveTrue(postId)).thenReturn(Optional.of(post));
        when(communityCategoryRepository.findByName("invalid")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> communityPostService.updatePost(postId, request, userId))
                .isInstanceOf(CommunityException.class)
                .extracting(throwable -> ((CommunityException) throwable).getErrorCode())
                .isEqualTo(CommunityErrorCode.CATEGORY_NOT_FOUND);
    }

    @Test
    void should_delete_post_when_user_is_author() {
        // Given
        Long postId = 1L;
        Long userId = 1L;

        User author = User.create("author@wearagain.kr", "작성자", null);
        ReflectionTestUtils.setField(author, "id", userId);

        CommunityCategory category = CommunityCategory.create("review");
        CommunityPost post = CommunityPost.create(author, category, "제목", "내용", null);
        ReflectionTestUtils.setField(post, "id", postId);

        when(communityPostRepository.findByIdAndActiveTrue(postId)).thenReturn(Optional.of(post));

        // When
        communityPostService.deletePost(postId, userId);

        // Then
        assertThat(post.isActive()).isFalse();
    }

    @Test
    void should_throw_exception_when_post_not_found_on_delete() {
        // Given
        Long postId = 999L;
        when(communityPostRepository.findByIdAndActiveTrue(postId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> communityPostService.deletePost(postId, 1L))
                .isInstanceOf(CommunityException.class)
                .extracting(throwable -> ((CommunityException) throwable).getErrorCode())
                .isEqualTo(CommunityErrorCode.POST_NOT_FOUND);
    }

    @Test
    void should_throw_exception_when_user_is_not_author_on_delete() {
        // Given
        Long postId = 1L;
        Long authorId = 1L;
        Long requesterId = 2L;

        User author = User.create("author@wearagain.kr", "작성자", null);
        ReflectionTestUtils.setField(author, "id", authorId);

        CommunityCategory category = CommunityCategory.create("review");
        CommunityPost post = CommunityPost.create(author, category, "제목", "내용", null);
        ReflectionTestUtils.setField(post, "id", postId);

        when(communityPostRepository.findByIdAndActiveTrue(postId)).thenReturn(Optional.of(post));

        // When & Then
        assertThatThrownBy(() -> communityPostService.deletePost(postId, requesterId))
                .isInstanceOf(CommunityException.class)
                .extracting(throwable -> ((CommunityException) throwable).getErrorCode())
                .isEqualTo(CommunityErrorCode.POST_DELETE_FORBIDDEN);
    }
}

