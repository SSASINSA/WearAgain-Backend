package com.ssasinsa.wearagain.domain.community.dto.admin;

import com.ssasinsa.wearagain.domain.community.entity.PostAdminKeywordScope;
import com.ssasinsa.wearagain.domain.community.entity.PostStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

public record PostAdminListRequest(
        int page,
        int size,
        PostStatus status,
        String keyword,
        PostAdminKeywordScope keywordScope,
        Sort sort
) {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 50;

    public static PostAdminListRequest of(
            Integer page,
            Integer size,
            String status,
            String keyword,
            String keywordScope,
            String sort
    ) {
        int sanitizedPage = page == null || page < 0 ? DEFAULT_PAGE : page;
        int rawSize = size == null ? DEFAULT_SIZE : size;
        int sanitizedSize = Math.min(Math.max(rawSize, 1), MAX_SIZE);

        PostStatus statusFilter = parseStatus(status);
        String normalizedKeyword = normalize(keyword);
        PostAdminKeywordScope scope = PostAdminKeywordScope.from(keywordScope);
        Sort sortSpec = parseSort(sort);

        return new PostAdminListRequest(
                sanitizedPage,
                sanitizedSize,
                statusFilter,
                normalizedKeyword,
                scope,
                sortSpec
        );
    }

    public Pageable toPageable() {
        return PageRequest.of(page, size, sort);
    }

    private static PostStatus parseStatus(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return PostStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static String normalize(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        return keyword.trim();
    }

    private static Sort parseSort(String sort) {
        if (!StringUtils.hasText(sort)) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        String value = sort.trim().toUpperCase();
        return switch (value) {
            case "OLDEST" -> Sort.by(Sort.Direction.ASC, "createdAt");
            case "TITLE" -> Sort.by(Sort.Direction.ASC, "title");
            case "LATEST" -> Sort.by(Sort.Direction.DESC, "createdAt");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }
}
