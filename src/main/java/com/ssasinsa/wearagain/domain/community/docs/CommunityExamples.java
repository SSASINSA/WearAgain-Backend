package com.ssasinsa.wearagain.domain.community.docs;

public final class CommunityExamples {

    private CommunityExamples() {
    }

    public static final String POST_DETAIL_RESPONSE = """
            {
              "id": 1,
              "imageUrl": "https://cdn.wearagain.kr/community/posts/1/image1.jpg",
              "author": {
                "id": 1,
                "name": "박길동"
              },
              "createdAt": "2025-01-15T10:30:00",
              "title": "리폼 후기 공유합니다",
              "content": "집에서 직접 리폼해 본 경험을 공유하고 싶어서 글을 남깁니다...",
              "likeCount": 15,
              "commentCount": 8,
              "keyword": "review",
              "isMine": true,
              "isLiked": true
            }
            """;

    public static final String POST_CREATE_REQUEST = """
            {
              "title": "리폼 후기 공유합니다",
              "content": "집에서 직접 리폼해 본 경험을 공유하고 싶어서 글을 남깁니다...",
              "keyword": "review",
              "imageUrls": [
                "https://cdn.wearagain.kr/community/posts/1/image1.jpg"
              ]
            }
            """;

    public static final String POST_UPDATE_REQUEST = """
            {
              "title": "리폼 후기 공유합니다(수정)",
              "content": "집에서 직접 리폼해 본 경험을 공유하고 싶어서 글을 남깁니다... (수정)",
              "keyword": "review",
              "imageUrls": [
                "https://cdn.wearagain.kr/community/posts/1/image1.jpg"
              ]
            }
            """;

    public static final String POSTS_RESPONSE = """
            {
              "limit": 10,
              "nextCursor": "100",
              "hasNext": true,
              "posts": [
                {
                  "id": 1,
                  "imageUrl": "https://cdn.wearagain.kr/community/posts/1/image1.jpg",
                  "author": {
                    "id": 1,
                    "name": "박길동"
                  },
                  "createdAt": "2025-01-15T10:30:00",
                  "title": "리폼 후기 공유합니다",
                  "content": "집에서 직접 리폼해 본 경험을 공유하고 싶어서 글을 남깁니다...",
                  "likeCount": 15,
                  "commentCount": 8,
                  "keyword": "review",
                  "isLiked": true
                },
                {
                  "id": 2,
                  "imageUrl": null,
                  "author": {
                    "id": 2,
                    "name": "김철수"
                  },
                  "createdAt": "2025-01-14T15:20:00",
                  "title": "리폼 질문 올립니다",
                  "content": "리폼은 처음으로 해보려는데 어떤 자재를 준비해야 할까요?",
                  "likeCount": 5,
                  "commentCount": 3,
                  "keyword": "question",
                  "isLiked": false
                }
              ]
            }
            """;

    public static final String REPORT_POST_REQUEST = """
            {
              "postId": 1,
              "reason": "부적절한 광고성 내용으로 의심됩니다"
            }
            """;

    public static final String COMMENT_CREATE_REQUEST = """
            {
              "content": "좋은 후기네요! 많이 도움이 됐어요"
            }
            """;

    public static final String COMMENT_UPDATE_REQUEST = """
            {
              "content": "좋은 후기네요! 많이 도움이 됐어요 (수정)"
            }
            """;

    public static final String COMMENTS_RESPONSE = """
            {
              "limit": 10,
              "nextCursor": "100",
              "hasNext": true,
              "comments": [
                {
                  "id": 101,
                  "author": {
                    "id": 1,
                    "name": "박길동"
                  },
                  "createdAt": "2025-01-15T10:30:00",
                  "content": "좋은 후기네요! 많이 도움이 됐어요",
                  "isMine": true
                },
                {
                  "id": 100,
                  "author": {
                    "id": 2,
                    "name": "김철수"
                  },
                  "createdAt": "2025-01-15T09:20:00",
                  "content": "정보 공유해 주셔서 감사합니다",
                  "isMine": false
                }
              ]
            }
            """;

    public static final String IMAGE_UPLOAD_RESPONSE = """
            {
              "imageName": "6f7e4a1b2c3d4e5f6a7b8c9d0e1f2a3b.jpg",
              "imageUrl": "https://admin.wearagain.kr/uploads/6f7e4a1b2c3d4e5f6a7b8c9d0e1f2a3b.jpg"
            }
            """;

    public static final String KEYWORDS_RESPONSE = """
            {
              "keywords": [
                "질문",
                "리뷰",
                "추천"
              ]
            }
            """;

    public static final String POST_LIKE_RESPONSE = """
            {
              "isLiked": true,
              "likeCount": 16
            }
            """;
}
