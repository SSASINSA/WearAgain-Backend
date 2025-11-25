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
                "name": "홍길동"
              },
              "createdAt": "2025-01-15T10:30:00",
              "title": "리폼 후기 공유합니다",
              "content": "첫 리폼 경험을 공유하고 싶어서 글을 올립니다...",
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
              "content": "첫 리폼 경험을 공유하고 싶어서 글을 올립니다...",
              "keyword": "review",
              "imageUrls": [
                "https://cdn.wearagain.kr/community/posts/1/image1.jpg"
              ]
            }
            """;

    public static final String POST_UPDATE_REQUEST = """
            {
              "title": "리폼 후기 공유합니다 (수정)",
              "content": "첫 리폼 경험을 공유하고 싶어서 글을 올립니다... (수정)",
              "keyword": "review",
              "imageUrls": [
                "https://cdn.wearagain.kr/community/posts/1/image1.jpg"
              ]
            }
            """;
}

