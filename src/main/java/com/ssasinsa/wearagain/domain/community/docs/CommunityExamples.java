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
                    "name": "홍길동"
                  },
                  "createdAt": "2025-01-15T10:30:00",
                  "title": "리폼 후기 공유합니다",
                  "content": "첫 리폼 경험을 공유하고 싶어서 글을 올립니다...",
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
                  "title": "리폼 질문드립니다",
                  "content": "리폼을 처음 해보는데 어떤 점을 주의해야 할까요?",
                  "likeCount": 5,
                  "commentCount": 3,
                  "keyword": "question",
                  "isLiked": false
                }
              ]
            }
            """;
}

