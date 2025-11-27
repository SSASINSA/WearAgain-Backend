package com.ssasinsa.wearagain.domain.community.service;

import com.ssasinsa.wearagain.domain.auth.entity.User;
import com.ssasinsa.wearagain.domain.auth.repository.UserRepository;
import com.ssasinsa.wearagain.domain.community.dto.request.PostCreateRequest;
import com.ssasinsa.wearagain.domain.community.dto.request.PostUpdateRequest;
import com.ssasinsa.wearagain.domain.community.dto.request.PostsRequest;
import com.ssasinsa.wearagain.domain.community.dto.response.PostDetailResponse;
import com.ssasinsa.wearagain.domain.community.dto.response.PostDetailResponse.AuthorInfo;
import com.ssasinsa.wearagain.domain.community.dto.response.PostsResponse;
import com.ssasinsa.wearagain.domain.community.dto.response.PostsResponse.PostsItem;
import com.ssasinsa.wearagain.domain.community.entity.CommunityCategory;
import com.ssasinsa.wearagain.domain.community.entity.CommunityPost;
import com.ssasinsa.wearagain.domain.community.entity.CommunityPostImage;
import com.ssasinsa.wearagain.domain.community.exception.CommunityErrorCode;
import com.ssasinsa.wearagain.domain.community.exception.CommunityException;
import com.ssasinsa.wearagain.domain.community.repository.CommunityCategoryRepository;
import com.ssasinsa.wearagain.domain.community.repository.CommunityPostImageRepository;
import com.ssasinsa.wearagain.domain.community.repository.CommunityPostRepository;
import com.ssasinsa.wearagain.domain.community.repository.PostLikeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityPostServiceImpl implements CommunityPostService {

    private final CommunityPostRepository communityPostRepository;
    private final CommunityCategoryRepository communityCategoryRepository;
    private final CommunityPostImageRepository communityPostImageRepository;
    private final PostLikeRepository postLikeRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public PostsResponse getPosts(PostsRequest request, Long userId) {
        int limit = request.limit() != null ? request.limit() : 10;
        if (limit <= 0 || limit > 50) {
            limit = 10;
        }

        // 1단계: Post ID만 먼저 조회 (Cursor + Pagination 정상 동작)
        Pageable pageable = PageRequest.of(0, limit + 1);
        List<Long> postIds = communityPostRepository.findActivePostIdsForCursor(
                request.cursor(),
                request.keyword(),
                pageable
        );

        if (postIds.isEmpty()) {
            return new PostsResponse(limit, null, false, new ArrayList<>());
        }

        boolean hasNext = postIds.size() > limit;
        List<Long> limitedIds = hasNext ? postIds.subList(0, limit) : postIds;

        // 2단계: ID 목록으로 필요한 연관 엔티티 Fetch Join
        List<CommunityPost> posts = communityPostRepository.findPostsByIds(limitedIds);

        // 3단계: images 동시 로딩 최적화 (Batch Fetching 자동 적용)
        // @BatchSize로 인해 한 번의 쿼리로 모든 images 로딩됨
        posts.forEach(post -> post.getImages().size());

        // 각 게시물의 댓글 수 일괄 조회 (N+1 문제 해결)
        List<Object[]> commentCountResults = communityPostRepository.countActiveCommentsByPostIds(limitedIds);
        Map<Long, Long> commentCountMap = commentCountResults.stream()
                .collect(Collectors.toMap(
                        result -> (Long) result[0],
                        result -> (Long) result[1]
                ));

        // 각 게시물의 좋아요 여부 일괄 조회 (N+1 문제 해결)
        Set<Long> likedPostIds = userId != null
                ? new HashSet<>(postLikeRepository.findLikedPostIdsByPostIdsAndUserId(limitedIds, userId))
                : Collections.emptySet();

        List<PostsItem> postsItems = new ArrayList<>();
        for (CommunityPost post : posts) {
            String imageUrl = post.getImages().stream()
                    .min(Comparator.comparingInt(CommunityPostImage::getSortOrder))
                    .map(CommunityPostImage::getImageUrl)
                    .orElse(null);

            Long postId = post.getId();
            long commentCount = commentCountMap.getOrDefault(postId, 0L);
            boolean isLiked = likedPostIds.contains(postId);

            postsItems.add(new PostsItem(
                    postId,
                    imageUrl,
                    new PostsItem.AuthorInfo(post.getUser().getId(), post.getUser().getDisplayName()),
                    post.getCreatedAt(),
                    post.getTitle(),
                    post.getContent(),
                    post.getLikeCount(),
                    (int) commentCount,
                    post.getCategory().getName(),
                    isLiked
            ));
        }

        String nextCursor = hasNext && !limitedIds.isEmpty()
                ? String.valueOf(limitedIds.get(limitedIds.size() - 1))
                : null;

        return new PostsResponse(limit, nextCursor, hasNext, postsItems);
    }

    @Override
    @Transactional(readOnly = true)
    public PostDetailResponse getPostDetail(Long postId, Long userId) {
        CommunityPost post = communityPostRepository.findByIdAndActiveTrue(postId)
                .orElseThrow(() -> new CommunityException(CommunityErrorCode.POST_NOT_FOUND));

        String imageUrl = post.getImages().stream()
                .min(Comparator.comparingInt(CommunityPostImage::getSortOrder))
                .map(CommunityPostImage::getImageUrl)
                .orElse(null);

        long commentCount = communityPostRepository.countActiveCommentsByPostId(postId);

        boolean isMine = userId != null && userId.equals(post.getUser().getId());
        boolean isLiked = userId != null && postLikeRepository.existsByPostIdAndUserId(postId, userId);

        return new PostDetailResponse(
                post.getId(),
                imageUrl,
                new AuthorInfo(post.getUser().getId(), post.getUser().getDisplayName()),
                post.getCreatedAt(),
                post.getTitle(),
                post.getContent(),
                post.getLikeCount(),
                (int) commentCount,
                post.getCategory().getName(),
                isMine,
                isLiked
        );
    }

    @Override
    @Transactional
    public void createPost(PostCreateRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CommunityException(CommunityErrorCode.INVALID_POST_DATA));

        CommunityCategory category = communityCategoryRepository.findByName(request.keyword())
                .orElseThrow(() -> new CommunityException(CommunityErrorCode.CATEGORY_NOT_FOUND));

        List<String> imageUrls = request.imageUrls() != null ? request.imageUrls() : new ArrayList<>();
        CommunityPost post = CommunityPost.create(user, category, request.title(), request.content(), imageUrls);

        communityPostRepository.save(post);
        log.info("게시글 생성 완료: postId={}, userId={}", post.getId(), userId);
    }

    @Override
    @Transactional
    public void updatePost(Long postId, PostUpdateRequest request, Long userId) {
        CommunityPost post = communityPostRepository.findByIdAndActiveTrue(postId)
                .orElseThrow(() -> new CommunityException(CommunityErrorCode.POST_NOT_FOUND));

        if (!post.getUser().getId().equals(userId)) {
            throw new CommunityException(CommunityErrorCode.POST_UPDATE_FORBIDDEN);
        }

        if (request.title() != null && !request.title().isBlank()) {
            post.updateTitle(request.title());
        }

        if (request.content() != null && !request.content().isBlank()) {
            post.updateContent(request.content());
        }

        if (request.keyword() != null && !request.keyword().isBlank()) {
            CommunityCategory category = communityCategoryRepository.findByName(request.keyword())
                    .orElseThrow(() -> new CommunityException(CommunityErrorCode.CATEGORY_NOT_FOUND));
            post.assignCategory(category);
        }

        if (request.imageUrls() != null) {
            communityPostImageRepository.deleteByPost(post);
            post.clearImages();
            int order = 0;
            for (String imageUrl : request.imageUrls()) {
                CommunityPostImage.create(post, imageUrl, order++);
            }
        }

        log.info("게시글 수정 완료: postId={}, userId={}", postId, userId);
    }

    @Override
    @Transactional
    public void deletePost(Long postId, Long userId) {
        CommunityPost post = communityPostRepository.findByIdAndActiveTrue(postId)
                .orElseThrow(() -> new CommunityException(CommunityErrorCode.POST_NOT_FOUND));

        if (!post.getUser().getId().equals(userId)) {
            throw new CommunityException(CommunityErrorCode.POST_DELETE_FORBIDDEN);
        }

        post.deactivate();
        log.info("게시글 삭제 완료: postId={}, userId={}", postId, userId);
    }
}

