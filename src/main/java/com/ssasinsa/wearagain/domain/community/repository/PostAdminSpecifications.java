package com.ssasinsa.wearagain.domain.community.repository;

import com.ssasinsa.wearagain.domain.community.entity.CommunityPost;
import com.ssasinsa.wearagain.domain.community.entity.PostAdminKeywordScope;
import com.ssasinsa.wearagain.domain.community.entity.PostStatus;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class PostAdminSpecifications {

    private PostAdminSpecifications() {
    }

    public static Specification<CommunityPost> excludeStatus(PostStatus status) {
        if (status == null) {
            return null;
        }
        return (root, query, builder) -> builder.notEqual(root.get("status"), status);
    }

    public static Specification<CommunityPost> statusEquals(PostStatus status) {
        if (status == null) {
            return null;
        }
        return (root, query, builder) -> builder.equal(root.get("status"), status);
    }

    public static Specification<CommunityPost> keywordMatches(String keyword, PostAdminKeywordScope scope) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        String lowered = "%" + keyword.trim().toLowerCase() + "%";
        return (root, query, builder) -> {
            switch (scope) {
                case TITLE:
                    return builder.like(builder.lower(root.get("title")), lowered);
                case AUTHOR:
                    return builder.like(builder.lower(root.get("user").get("displayName")), lowered);
                case ALL:
                default:
                    return builder.or(
                            builder.like(builder.lower(root.get("title")), lowered),
                            builder.like(builder.lower(root.get("content")), lowered)
                    );
            }
        };
    }
}
