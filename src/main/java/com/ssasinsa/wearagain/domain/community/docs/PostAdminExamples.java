package com.ssasinsa.wearagain.domain.community.docs;

public final class PostAdminExamples {

    private PostAdminExamples() {
    }

    public static final String POST_LIST_RESPONSE = """
            {
              "posts": [
                {
                  "postId": 101,
                  "title": "업사이클링 꿀팁",
                  "status": "ACTIVE",
                  "categoryName": "eco",
                  "author": {
                    "authorId": 5,
                    "displayName": "에코러버",
                    "email": "eco@example.com"
                  },
                  "likeCount": 42,
                  "commentCount": 3,
                  "reportCount": 1,
                  "createdAt": "2025-12-03T11:20:00",
                  "updatedAt": "2025-12-03T11:30:00"
                }
              ],
              "page": 0,
              "size": 20,
              "totalElements": 1,
              "totalPages": 1,
              "hasNext": false
            }
            """;

    public static final String POST_DETAIL_RESPONSE = """
            {
              "postId": 101,
              "status": "ACTIVE",
              "title": "업사이클링 꿀팁",
              "content": "집에서 쉽게 따라 할 수 있는 리폼 아이디어 모음",
              "categoryName": "eco",
              "author": {
                "authorId": 5,
                "displayName": "에코러버",
                "email": "eco@example.com"
              },
              "imageUrls": [
                "https://cdn.example.com/posts/101-1.png"
              ],
              "likeCount": 42,
              "commentCount": 3,
              "reportCount": 1,
              "createdAt": "2025-12-03T11:20:00",
              "updatedAt": "2025-12-03T11:30:00",
              "comments": [
                {
                  "commentId": 301,
                  "content": "좋은 정보 감사합니다!",
                  "status": "ACTIVE",
                  "author": {
                    "authorId": 7,
                    "displayName": "wearagain",
                    "email": "community@example.com"
                  },
                  "createdAt": "2025-12-03T11:25:00"
                }
              ]
            }
            """;
}
