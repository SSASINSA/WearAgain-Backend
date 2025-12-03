package com.ssasinsa.wearagain.domain.user.docs;

import lombok.experimental.UtilityClass;

@UtilityClass
public class UserExamples {

    public static final String USER_SUMMARY_RESPONSE = """
            {
              \"displayName\": \"김웨어\",
              \"ticketBalance\": 3,
              \"creditBalance\": 120,
              \"totalTicketChangeAmount\": 15
            }
            """;

    public static final String MY_POSTS_RESPONSE = """
            {
              \"limit\": 10,
              \"nextCursor\": \"100\",
              \"hasNext\": true,
              \"posts\": [
                {
                  \"id\": 1,
                  \"imageUrl\": \"https://cdn.wearagain.kr/community/posts/1/image1.jpg\",
                  \"author\": {
                    \"id\": 1,
                    \"name\": \"홍길동\"
                  },
                  \"createdAt\": \"2025-01-15T10:30:00\",
                  \"title\": \"리폼 후기 공유합니다\",
                  \"content\": \"첫 리폼 경험을 공유하고 싶어서 글을 올립니다...\",
                  \"likeCount\": 15,
                  \"commentCount\": 8,
                  \"keyword\": \"review\",
                  \"isLiked\": true
                },
                {
                  \"id\": 2,
                  \"imageUrl\": null,
                  \"author\": {
                    \"id\": 1,
                    \"name\": \"홍길동\"
                  },
                  \"createdAt\": \"2025-01-14T15:20:00\",
                  \"title\": \"리폼 질문드립니다\",
                  \"content\": \"리폼을 처음 해보는데 어떤 점을 주의해야 할까요?\",
                  \"likeCount\": 5,
                  \"commentCount\": 3,
                  \"keyword\": \"question\",
                  \"isLiked\": false
                }
              ]
            }
            """;

    public static final String MY_COMMENTED_POSTS_RESPONSE = """
            {
              \"limit\": 10,
              \"nextCursor\": \"100\",
              \"hasNext\": true,
              \"posts\": [
                {
                  \"id\": 5,
                  \"imageUrl\": \"https://cdn.wearagain.kr/community/posts/5/image1.jpg\",
                  \"author\": {
                    \"id\": 2,
                    \"name\": \"김철수\"
                  },
                  \"createdAt\": \"2025-01-13T09:15:00\",
                  \"title\": \"수선 후기입니다\",
                  \"content\": \"수선을 맡겼는데 정말 만족스럽습니다...\",
                  \"likeCount\": 20,
                  \"commentCount\": 12,
                  \"keyword\": \"repair\",
                  \"isLiked\": true
                }
              ]
            }
            """;

    public static final String UPDATE_DISPLAY_NAME_RESPONSE = """
            {
              \"displayName\": \"웨어어게인러버\"
            }
            """;
}
