package com.ssasinsa.wearagain.domain.community.service;

import com.ssasinsa.wearagain.domain.community.dto.admin.PostAdminDetailResponse;
import com.ssasinsa.wearagain.domain.community.dto.admin.PostAdminListRequest;
import com.ssasinsa.wearagain.domain.community.dto.admin.PostAdminListResponse;
import com.ssasinsa.wearagain.domain.community.entity.CommentStatus;
import com.ssasinsa.wearagain.domain.community.entity.CommunityPost;
import com.ssasinsa.wearagain.domain.community.entity.CommunityPostImage;
import com.ssasinsa.wearagain.domain.community.entity.PostComment;
import com.ssasinsa.wearagain.domain.community.entity.PostStatus;
import com.ssasinsa.wearagain.domain.community.exception.CommunityErrorCode;
import com.ssasinsa.wearagain.domain.community.exception.CommunityException;
import com.ssasinsa.wearagain.domain.community.repository.CommunityPostRepository;
import com.ssasinsa.wearagain.domain.community.repository.PostAdminSpecifications;
import com.ssasinsa.wearagain.domain.community.repository.PostCommentRepository;
import com.ssasinsa.wearagain.domain.community.repository.ReportRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostAdminServiceImpl implements PostAdminService {

    private static final String TARGET_TYPE_POST = "POST";

    private final CommunityPostRepository communityPostRepository;
    private final PostCommentRepository postCommentRepository;
    private final ReportRepository reportRepository;

    @Override
    @Transactional(readOnly = true)
    public PostAdminListResponse getPosts(PostAdminListRequest request) {
        Page<CommunityPost> page = communityPostRepository.findAll(
                PostAdminSpecifications.excludeStatus(PostStatus.INACTIVE)
                        .and(PostAdminSpecifications.statusEquals(request.status()))
                        .and(PostAdminSpecifications.keywordMatches(request.keyword(), request.keywordScope())),
                request.toPageable()
        );

        List<Long> postIds = page.getContent().stream()
                .map(CommunityPost::getId)
                .toList();

        Map<Long, Integer> commentCounts = buildCommentCountMap(postIds);
        Map<Long, Integer> reportCounts = buildReportCountMap(postIds);

        List<PostAdminListResponse.PostAdminSummary> summaries = page.getContent().stream()
                .map(post -> mapToSummary(post, commentCounts, reportCounts))
                .toList();

        return new PostAdminListResponse(
                summaries,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PostAdminDetailResponse getPostDetail(Long postId) {
        CommunityPost post = communityPostRepository.findByIdAndStatusNot(postId, PostStatus.INACTIVE)
                .orElseThrow(() -> new CommunityException(CommunityErrorCode.POST_NOT_FOUND));

        List<PostComment> comments = postCommentRepository.findAllByPostIdForAdmin(postId);
        long activeCommentCount = comments.stream()
                .filter(comment -> comment.getStatus() != CommentStatus.INACTIVE)
                .count();
        long reportCount = reportRepository.countByTargetTypeAndTargetId(TARGET_TYPE_POST, postId);

        List<String> imageUrls = post.getImages().stream()
                .sorted(Comparator.comparingInt(CommunityPostImage::getSortOrder))
                .map(CommunityPostImage::getImageUrl)
                .toList();

        List<PostAdminDetailResponse.CommentInfo> commentInfos = comments.stream()
                .map(this::mapToCommentInfo)
                .toList();

        return new PostAdminDetailResponse(
                post.getId(),
                post.getStatus(),
                post.getTitle(),
                post.getContent(),
                post.getCategory().getName(),
                new PostAdminDetailResponse.AuthorInfo(
                        post.getUser().getId(),
                        post.getUser().getDisplayName(),
                        post.getUser().getEmail()
                ),
                imageUrls,
                post.getLikeCount(),
                (int) activeCommentCount,
                (int) reportCount,
                post.getCreatedAt(),
                post.getUpdatedAt(),
                commentInfos
        );
    }

    private Map<Long, Integer> buildCommentCountMap(List<Long> postIds) {
        if (postIds.isEmpty()) {
            return Map.of();
        }
        return communityPostRepository.countActiveCommentsByPostIds(postIds).stream()
                .collect(Collectors.toMap(
                        result -> (Long) result[0],
                        result -> ((Long) result[1]).intValue()
                ));
    }

    private Map<Long, Integer> buildReportCountMap(List<Long> postIds) {
        if (postIds.isEmpty()) {
            return Map.of();
        }
        return reportRepository.countReportsByTargetTypeAndTargetIds(TARGET_TYPE_POST, postIds).stream()
                .collect(Collectors.toMap(
                        result -> (Long) result[0],
                        result -> ((Long) result[1]).intValue()
                ));
    }

    private PostAdminListResponse.PostAdminSummary mapToSummary(
            CommunityPost post,
            Map<Long, Integer> commentCounts,
            Map<Long, Integer> reportCounts
    ) {
        return new PostAdminListResponse.PostAdminSummary(
                post.getId(),
                post.getTitle(),
                post.getStatus(),
                post.getCategory().getName(),
                new PostAdminListResponse.PostAdminSummary.AuthorInfo(
                        post.getUser().getId(),
                        post.getUser().getDisplayName(),
                        post.getUser().getEmail()
                ),
                post.getLikeCount(),
                commentCounts.getOrDefault(post.getId(), 0),
                reportCounts.getOrDefault(post.getId(), 0),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    private PostAdminDetailResponse.CommentInfo mapToCommentInfo(PostComment comment) {
        return new PostAdminDetailResponse.CommentInfo(
                comment.getId(),
                comment.getContent(),
                comment.getStatus(),
                new PostAdminDetailResponse.CommentInfo.CommentAuthorInfo(
                        comment.getUser().getId(),
                        comment.getUser().getDisplayName(),
                        comment.getUser().getEmail()
                ),
                comment.getCreatedAt()
        );
    }
}
