package com.ssasinsa.wearagain.domain.event.docs;

public final class EventExamples {

    private EventExamples() {
    }

    public static final String ADMIN_EVENT_CREATE_REQUEST = """
            {
              "title": "지속가능 패션 워크숍",
              "description": "웨어어게인과 함께하는 리폼 클래스",
              "location": "서울시 마포구 연남동 223-14 2F",
              "startDate": "2025-11-10",
              "endDate": "2025-11-30",
              "status": "DRAFT",
              "images": [
                {
                  "url": "https://cdn.wearagain.kr/events/123/main.jpg",
                  "altText": "행사 대표 이미지",
                  "displayOrder": 1
                }
              ],
              "options": [
                {
                  "name": "11월 15일",
                  "type": "DATE",
                  "displayOrder": 1,
                  "children": [
                    {
                      "name": "오전 세션 (10:00~12:00)",
                      "type": "TIME",
                      "displayOrder": 1,
                      "children": [
                        {
                          "name": "A조",
                          "type": "GROUP",
                          "displayOrder": 1,
                          "capacity": 10
                        }
                      ]
                    }
                  ]
                }
              ]
            }
            """;

    public static final String ADMIN_EVENT_CREATE_RESPONSE = """
            {
              "eventId": 100,
              "title": "지속가능 패션 워크숍",
              "description": "웨어어게인과 함께하는 리폼 클래스",
              "location": "서울시 마포구 연남동 223-14 2F",
              "startDate": "2025-11-10",
              "endDate": "2025-11-30",
              "status": "DRAFT",
              "images": [
                {
                  "eventImageId": 1001,
                  "url": "https://cdn.wearagain.kr/events/123/main.jpg",
                  "altText": "행사 대표 이미지",
                  "displayOrder": 1
                }
              ],
              "options": [
                {
                  "eventOptionId": 2001,
                  "name": "11월 15일",
                  "type": "DATE",
                  "displayOrder": 1,
                  "capacity": null,
                  "children": [
                    {
                      "eventOptionId": 2002,
                      "name": "오전 세션 (10:00~12:00)",
                      "type": "TIME",
                      "displayOrder": 1,
                      "capacity": null,
                      "children": [
                        {
                          "eventOptionId": 2003,
                          "name": "A조",
                          "type": "GROUP",
                          "displayOrder": 1,
                          "capacity": 10,
                          "children": []
                        }
                      ]
                    }
                  ]
                }
              ],
              "createdAt": "2025-10-21T11:20:05Z"
            }
            """;

    public static final String ADMIN_EVENT_IMAGE_UPLOAD_RESPONSE = """
            {
              "imageName": "events/20251103/1a2b3c4d5e6f7g8h9i0j.jpg",
              "imageUrl": "https://admin.wearagain.kr/upload/events/20251103/1a2b3c4d5e6f7g8h9i0j.jpg"
            }
            """;

    public static final String USER_EVENT_LIST_RESPONSE = """
            {
              "events": [
                {
                  "eventId": 101,
                  "title": "지속가능 패션 워크숍",
                  "description": "웨어어게인과 함께하는 리폼 클래스",
                  "location": "서울시 마포구 연남동 223-14 2F",
                  "startDate": "2025-11-10",
                  "endDate": "2025-11-30",
                  "status": "OPEN",
                  "thumbnailUrl": "https://cdn.wearagain.kr/events/101/main.jpg"
                }
              ],
              "nextCursor": "105",
              "hasNext": true
            }
            """;

    public static final String USER_EVENT_DETAIL_RESPONSE = """
            {
              "eventId": 101,
              "title": "지속가능 패션 워크숍",
              "description": "웨어어게인과 함께하는 리폼 클래스",
              "location": "서울시 마포구 연남동 223-14 2F",
              "startDate": "2025-11-10",
              "endDate": "2025-11-30",
              "status": "OPEN",
              "images": [
                {
                  "imageId": 1001,
                  "url": "https://cdn.wearagain.kr/events/101/main.jpg",
                  "altText": "행사 대표 이미지",
                  "displayOrder": 1
                }
              ],
              "options": [
                {
                  "optionId": 2001,
                  "name": "11월 15일",
                  "type": "DATE",
                  "displayOrder": 1,
                  "capacity": null,
                  "appliedCount": null,
                  "remainingCount": null,
                  "children": [
                    {
                      "optionId": 2002,
                      "name": "오전 세션",
                      "type": "TIME",
                      "displayOrder": 1,
                      "capacity": null,
                      "appliedCount": null,
                      "remainingCount": null,
                      "children": [
                        {
                          "optionId": 2003,
                          "name": "A조",
                          "type": "GROUP",
                          "displayOrder": 1,
                          "capacity": 10,
                          "appliedCount": 7,
                          "remainingCount": 3,
                          "children": []
                        }
                      ]
                    }
                  ]
                }
              ]
            }
            """;

    public static final String USER_EVENT_APPLY_REQUEST = """
            {
              "optionId": 2003,
              "memo": "동행 1인 포함"
            }
            """;

    public static final String USER_EVENT_APPLY_RESPONSE = """
            {
              "applicationId": 5001,
              "status": "APPLIED"
            }
            """;

    public static final String USER_EVENT_CANCEL_REQUEST = """
            {
              "reason": "일정이 변경되었습니다."
            }
            """;

    public static final String USER_EVENT_CANCEL_RESPONSE = """
            {
              "applicationId": 5001,
              "status": "CANCELED"
            }
            """;
}
