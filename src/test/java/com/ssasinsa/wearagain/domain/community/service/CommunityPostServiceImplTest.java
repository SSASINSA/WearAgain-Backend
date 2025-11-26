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
import com.ssasinsa.wearagain.domain.community.dto.request.PostsRequest;
import com.ssasinsa.wearagain.domain.community.dto.request.PostUpdateRequest;
import com.ssasinsa.wearagain.domain.community.dto.response.PostDetailResponse;
import com.ssasinsa.wearagain.domain.community.dto.response.PostsResponse;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    @Test
    void should_return_posts_list_when_posts_exist() {
        // Given
        Long userId = 1L;
        User author1 = User.create("author1@wearagain.kr", "작성자1", null);
        ReflectionTestUtils.setField(author1, "id", 1L);
        User author2 = User.create("author2@wearagain.kr", "작성자2", null);
        ReflectionTestUtils.setField(author2, "id", 2L);

        CommunityCategory category = CommunityCategory.create("review");
        ReflectionTestUtils.setField(category, "id", 1L);

        CommunityPost post1 = CommunityPost.create(author1, category, "제목1", "내용1", null);
        ReflectionTestUtils.setField(post1, "id", 10L);
        ReflectionTestUtils.setField(post1, "likeCount", 5);
        LocalDateTime createdAt1 = LocalDateTime.of(2025, 1, 15, 10, 30);
        ReflectionTestUtils.setField(post1, "createdAt", createdAt1);

        CommunityPost post2 = CommunityPost.create(author2, category, "제목2", "내용2", null);
        ReflectionTestUtils.setField(post2, "id", 9L);
        ReflectionTestUtils.setField(post2, "likeCount", 3);
        LocalDateTime createdAt2 = LocalDateTime.of(2025, 1, 14, 15, 20);
        ReflectionTestUtils.setField(post2, "createdAt", createdAt2);

        List<CommunityPost> posts = List.of(post1, post2);
        PostsRequest request = new PostsRequest(null, 10, null);

        when(communityPostRepository.findActivePostsWithCursor(null, null, PageRequest.of(0, 11)))
                .thenReturn(posts);
        when(communityPostRepository.countActiveCommentsByPostId(10L)).thenReturn(2L);
        when(communityPostRepository.countActiveCommentsByPostId(9L)).thenReturn(1L);
        when(postLikeRepository.existsByPostIdAndUserId(10L, userId)).thenReturn(true);
        when(postLikeRepository.existsByPostIdAndUserId(9L, userId)).thenReturn(false);

        // When
        PostsResponse response = communityPostService.getPosts(request, userId);

        // Then
        assertThat(response.limit()).isEqualTo(10);
        assertThat(response.hasNext()).isFalse();
        assertThat(response.nextCursor()).isNull();
        assertThat(response.posts()).hasSize(2);
        assertThat(response.posts().get(0).id()).isEqualTo(10L);
        assertThat(response.posts().get(0).title()).isEqualTo("제목1");
        assertThat(response.posts().get(0).isLiked()).isTrue();
        assertThat(response.posts().get(0).commentCount()).isEqualTo(2);
        assertThat(response.posts().get(1).id()).isEqualTo(9L);
        assertThat(response.posts().get(1).isLiked()).isFalse();
        assertThat(response.posts().get(1).commentCount()).isEqualTo(1);
    }

    @Test
    void should_return_hasNext_true_when_more_posts_exist() {
        // Given
        Long userId = 1L;
        User author = User.create("author@wearagain.kr", "작성자", null);
        ReflectionTestUtils.setField(author, "id", 1L);

        CommunityCategory category = CommunityCategory.create("review");
        ReflectionTestUtils.setField(category, "id", 1L);

        List<CommunityPost> posts = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            CommunityPost post = CommunityPost.create(author, category, "제목" + i, "내용" + i, null);
            ReflectionTestUtils.setField(post, "id", (long) (20 - i));
            posts.add(post);
        }

        PostsRequest request = new PostsRequest(null, 10, null);

        when(communityPostRepository.findActivePostsWithCursor(null, null, PageRequest.of(0, 11)))
                .thenReturn(posts);
        when(communityPostRepository.countActiveCommentsByPostId(anyLong())).thenReturn(0L);
        when(postLikeRepository.existsByPostIdAndUserId(anyLong(), anyLong())).thenReturn(false);

        // When
        PostsResponse response = communityPostService.getPosts(request, userId);

        // Then
        assertThat(response.limit()).isEqualTo(10);
        assertThat(response.hasNext()).isTrue();
        assertThat(response.nextCursor()).isEqualTo("11");
        assertThat(response.posts()).hasSize(10);
    }

    @Test
    void should_return_nextCursor_when_hasNext_is_true() {
        // Given
        Long userId = 1L;
        User author = User.create("author@wearagain.kr", "작성자", null);
        ReflectionTestUtils.setField(author, "id", 1L);

        CommunityCategory category = CommunityCategory.create("review");
        ReflectionTestUtils.setField(category, "id", 1L);

        CommunityPost post1 = CommunityPost.create(author, category, "제목1", "내용1", null);
        ReflectionTestUtils.setField(post1, "id", 10L);
        CommunityPost post2 = CommunityPost.create(author, category, "제목2", "내용2", null);
        ReflectionTestUtils.setField(post2, "id", 9L);
        CommunityPost post3 = CommunityPost.create(author, category, "제목3", "내용3", null);
        ReflectionTestUtils.setField(post3, "id", 8L);

        PostsRequest request = new PostsRequest(null, 2, null);

        when(communityPostRepository.findActivePostsWithCursor(null, null, PageRequest.of(0, 3)))
                .thenReturn(List.of(post1, post2, post3));
        when(communityPostRepository.countActiveCommentsByPostId(anyLong())).thenReturn(0L);
        when(postLikeRepository.existsByPostIdAndUserId(anyLong(), anyLong())).thenReturn(false);

        // When
        PostsResponse response = communityPostService.getPosts(request, userId);

        // Then
        assertThat(response.hasNext()).isTrue();
        assertThat(response.nextCursor()).isEqualTo("9");
        assertThat(response.posts()).hasSize(2);
    }

    @Test
    void should_filter_by_keyword_when_keyword_provided() {
        // Given
        Long userId = 1L;
        User author = User.create("author@wearagain.kr", "작성자", null);
        ReflectionTestUtils.setField(author, "id", 1L);

        CommunityCategory reviewCategory = CommunityCategory.create("review");
        ReflectionTestUtils.setField(reviewCategory, "id", 1L);
        CommunityCategory questionCategory = CommunityCategory.create("question");
        ReflectionTestUtils.setField(questionCategory, "id", 2L);

        CommunityPost reviewPost = CommunityPost.create(author, reviewCategory, "후기", "후기 내용", null);
        ReflectionTestUtils.setField(reviewPost, "id", 10L);
        CommunityPost questionPost = CommunityPost.create(author, questionCategory, "질문", "질문 내용", null);
        ReflectionTestUtils.setField(questionPost, "id", 9L);

        PostsRequest request = new PostsRequest(null, 10, "review");

        when(communityPostRepository.findActivePostsWithCursor(null, "review", PageRequest.of(0, 11)))
                .thenReturn(List.of(reviewPost));
        when(communityPostRepository.countActiveCommentsByPostId(10L)).thenReturn(0L);
        when(postLikeRepository.existsByPostIdAndUserId(10L, userId)).thenReturn(false);

        // When
        PostsResponse response = communityPostService.getPosts(request, userId);

        // Then
        assertThat(response.posts()).hasSize(1);
        assertThat(response.posts().get(0).keyword()).isEqualTo("review");
        assertThat(response.posts().get(0).title()).isEqualTo("후기");
    }

    @Test
    void should_use_cursor_when_cursor_provided() {
        // Given
        Long userId = 1L;
        Long cursor = 10L;
        User author = User.create("author@wearagain.kr", "작성자", null);
        ReflectionTestUtils.setField(author, "id", 1L);

        CommunityCategory category = CommunityCategory.create("review");
        ReflectionTestUtils.setField(category, "id", 1L);

        CommunityPost post = CommunityPost.create(author, category, "제목", "내용", null);
        ReflectionTestUtils.setField(post, "id", 9L);

        PostsRequest request = new PostsRequest(cursor, 10, null);

        when(communityPostRepository.findActivePostsWithCursor(cursor, null, PageRequest.of(0, 11)))
                .thenReturn(List.of(post));
        when(communityPostRepository.countActiveCommentsByPostId(9L)).thenReturn(0L);
        when(postLikeRepository.existsByPostIdAndUserId(9L, userId)).thenReturn(false);

        // When
        PostsResponse response = communityPostService.getPosts(request, userId);

        // Then
        assertThat(response.posts()).hasSize(1);
        assertThat(response.posts().get(0).id()).isEqualTo(9L);
        verify(communityPostRepository).findActivePostsWithCursor(cursor, null, PageRequest.of(0, 11));
    }

    @Test
    void should_return_isLiked_false_when_user_is_null() {
        // Given
        User author = User.create("author@wearagain.kr", "작성자", null);
        ReflectionTestUtils.setField(author, "id", 1L);

        CommunityCategory category = CommunityCategory.create("review");
        ReflectionTestUtils.setField(category, "id", 1L);

        CommunityPost post = CommunityPost.create(author, category, "제목", "내용", null);
        ReflectionTestUtils.setField(post, "id", 10L);

        PostsRequest request = new PostsRequest(null, 10, null);

        when(communityPostRepository.findActivePostsWithCursor(null, null, PageRequest.of(0, 11)))
                .thenReturn(List.of(post));
        when(communityPostRepository.countActiveCommentsByPostId(10L)).thenReturn(0L);

        // When
        PostsResponse response = communityPostService.getPosts(request, null);

        // Then
        assertThat(response.posts()).hasSize(1);
        assertThat(response.posts().get(0).isLiked()).isFalse();
        verify(postLikeRepository, never()).existsByPostIdAndUserId(anyLong(), anyLong());
    }

    @Test
    void should_apply_default_limit_when_limit_is_null() {
        // Given
        Long userId = 1L;
        User author = User.create("author@wearagain.kr", "작성자", null);
        ReflectionTestUtils.setField(author, "id", 1L);

        CommunityCategory category = CommunityCategory.create("review");
        ReflectionTestUtils.setField(category, "id", 1L);

        CommunityPost post = CommunityPost.create(author, category, "제목", "내용", null);
        ReflectionTestUtils.setField(post, "id", 10L);

        PostsRequest request = new PostsRequest(null, null, null);

        when(communityPostRepository.findActivePostsWithCursor(null, null, PageRequest.of(0, 11)))
                .thenReturn(List.of(post));
        when(communityPostRepository.countActiveCommentsByPostId(10L)).thenReturn(0L);
        when(postLikeRepository.existsByPostIdAndUserId(10L, userId)).thenReturn(false);

        // When
        PostsResponse response = communityPostService.getPosts(request, userId);

        // Then
        assertThat(response.limit()).isEqualTo(10);
    }

    @Test
    void should_apply_default_limit_when_limit_is_invalid() {
        // Given
        Long userId = 1L;
        User author = User.create("author@wearagain.kr", "작성자", null);
        ReflectionTestUtils.setField(author, "id", 1L);

        CommunityCategory category = CommunityCategory.create("review");
        ReflectionTestUtils.setField(category, "id", 1L);

        CommunityPost post = CommunityPost.create(author, category, "제목", "내용", null);
        ReflectionTestUtils.setField(post, "id", 10L);

        PostsRequest request = new PostsRequest(null, 100, null); // limit > 50

        when(communityPostRepository.findActivePostsWithCursor(null, null, PageRequest.of(0, 11)))
                .thenReturn(List.of(post));
        when(communityPostRepository.countActiveCommentsByPostId(10L)).thenReturn(0L);
        when(postLikeRepository.existsByPostIdAndUserId(10L, userId)).thenReturn(false);

        // When
        PostsResponse response = communityPostService.getPosts(request, userId);

        // Then
        assertThat(response.limit()).isEqualTo(10);
    }

    @Test
    void should_return_empty_list_when_no_posts_exist() {
        // Given
        Long userId = 1L;
        PostsRequest request = new PostsRequest(null, 10, null);

        when(communityPostRepository.findActivePostsWithCursor(null, null, PageRequest.of(0, 11)))
                .thenReturn(List.of());

        // When
        PostsResponse response = communityPostService.getPosts(request, userId);

        // Then
        assertThat(response.posts()).isEmpty();
        assertThat(response.hasNext()).isFalse();
        assertThat(response.nextCursor()).isNull();
        verify(postLikeRepository, never()).existsByPostIdAndUserId(anyLong(), anyLong());
    }

    @Test
    void should_return_correct_comment_counts() {
        // Given
        Long userId = 1L;
        User author = User.create("author@wearagain.kr", "작성자", null);
        ReflectionTestUtils.setField(author, "id", 1L);

        CommunityCategory category = CommunityCategory.create("review");
        ReflectionTestUtils.setField(category, "id", 1L);

        CommunityPost post1 = CommunityPost.create(author, category, "제목1", "내용1", null);
        ReflectionTestUtils.setField(post1, "id", 10L);
        CommunityPost post2 = CommunityPost.create(author, category, "제목2", "내용2", null);
        ReflectionTestUtils.setField(post2, "id", 9L);

        PostsRequest request = new PostsRequest(null, 10, null);

        when(communityPostRepository.findActivePostsWithCursor(null, null, PageRequest.of(0, 11)))
                .thenReturn(List.of(post1, post2));
        when(communityPostRepository.countActiveCommentsByPostId(10L)).thenReturn(5L);
        when(communityPostRepository.countActiveCommentsByPostId(9L)).thenReturn(3L);
        when(postLikeRepository.existsByPostIdAndUserId(anyLong(), anyLong())).thenReturn(false);

        // When
        PostsResponse response = communityPostService.getPosts(request, userId);

        // Then
        assertThat(response.posts()).hasSize(2);
        assertThat(response.posts().get(0).commentCount()).isEqualTo(5);
        assertThat(response.posts().get(1).commentCount()).isEqualTo(3);
    }
}

