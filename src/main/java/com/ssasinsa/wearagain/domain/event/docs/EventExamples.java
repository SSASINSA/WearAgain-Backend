package com.ssasinsa.wearagain.domain.event.docs;

public final class EventExamples {

    private EventExamples() {
    }

    public static final String ADMIN_EVENT_CREATE_REQUEST = """
            {
              "title": "지속가능 패션 워크숍",
              "description": "웨어어게인과 함께하는 리폼 클래스",
              "usageGuide": "준비물은 개인 텀블러를 지참해주세요.",
              "precautions": "화재 예방을 위해 지정된 구역에서만 작업해주세요.",
              "location": "서울시 마포구 연남동 223-14 2F",
              "startDate": "2025-11-10",
              "endDate": "2025-11-30",
              "images": [
                {
                  "url": "https://cdn.wearagain.kr/events/123/main.jpg",
                  "altText": "행사 대표 이미지",
                  "displayOrder": 1
                }
              ],
              "options": [
                {
                  "name": "11월 15일 세션",
                  "displayOrder": 1,
                  "capacity": 10,
                  "children": [
                    {
                      "name": "오전 세션",
                      "displayOrder": 1,
                      "capacity": null,
                      "children": [
                        {
                          "name": "A조",
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
              "usageGuide": "준비물은 개인 텀블러를 지참해주세요.",
              "precautions": "화재 예방을 위해 지정된 구역에서만 작업해주세요.",
              "location": "서울시 마포구 연남동 223-14 2F",
              "organizerName": "운영자",
              "organizerContact": "admin@wearagain.kr",
              "organizerAdminId": 11,
              "organizerAdminEmail": "admin@wearagain.kr",
              "organizerAdminName": "홍길동",
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
                  "displayOrder": 1,
                  "capacity": null,
                  "children": [
                    {
                      "eventOptionId": 2002,
                      "name": "오전 세션 (10:00~12:00)",
                      "displayOrder": 1,
                      "capacity": null,
                      "children": [
                        {
                          "eventOptionId": 2003,
                          "name": "A조",
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
              "imageName": "6f7e4a1b2c3d4e5f6a7b8c9d0e1f2a3b.jpg",
              "imageUrl": "https://admin.wearagain.kr/uploads/6f7e4a1b2c3d4e5f6a7b8c9d0e1f2a3b.jpg"
            }
            """;

    public static final String ADMIN_EVENT_STAFF_CODE_RESPONSE = """
            {
              "eventId": 101,
              "staffCode": "023941",
              "issuedAt": "2025-02-01T10:15:20Z"
            }
            """;

    public static final String USER_EVENT_APPLICATION_LIST_RESPONSE = """
            {
              "items": [
                {
                  "applicationId": 123,
                  "eventId": 45,
                  "eventTitle": "업사이클링 원데이 클래스",
                  "thumbnailUrl": "https://cdn.wearagain.kr/events/45/main.jpg",
                  "description": "'교환'과 '수선’으로 끝까지 입는 경험과 실천을 제공하는 지속 가능한 의생활 실험 공간",
                  "location": "서울시 마포구 연남동 223-14 2F",
                  "eventPeriod": {
                    "startDate": "2025-02-10",
                    "endDate": "2025-02-11"
                  },
                  "eventStatus": "OPEN"
                }
              ],
              "nextCursor": "MjAyNS0wMS0yOFQxMjozMDowMC4wMDBaOjEyMw==",
              "hasNext": true
            }
            """;

    public static final String USER_EVENT_APPLICATION_QR_RESPONSE = """
            {
              "qrToken": "3b3f6e3456d34a84b41ce8a3f7fb16b1",
              "expiresIn": 600
            }
            """;

    public static final String STAFF_EVENT_CHECK_IN_REQUEST = """
            {
              "qrToken": "3b3f6e3456d34a84b41ce8a3f7fb16b1",
              "code": "023941"
            }
            """;

    public static final String STAFF_EVENT_CHECK_IN_RESPONSE = """
            {
              "applicationId": 123,
              "status": "CHECKED_IN",
              "checkedInAt": "2025-02-10T09:05:12Z",
              "userDisplayName": "홍길동",
              "eventTitle": "업사이클링 원데이 클래스"
            }
            """;

    public static final String STAFF_CODE_VERIFY_REQUEST = """
            {
              "code": "023941"
            }
            """;

    public static final String STAFF_CODE_VERIFY_RESPONSE = """
            {
              "valid": true,
              "event": {
                "eventId": 123,
                "title": "업사이클링 체험전",
                "status": "OPEN",
                "startDate": "2025-03-01",
                "endDate": "2025-03-02",
                "location": "서울 성수동 123-4",
                "usageGuide": "입장 시 QR 확인",
                "precautions": "음식물 반입 금지",
                "staffCodeIssuedAt": "2025-02-25T01:20:00Z",
                "organizerName": "홍길동"
              }
            }
            """;

    public static final String USER_EVENT_APPLICATION_DETAIL_RESPONSE = """
            {
              "applicationId": 123,
              "eventId": 45,
              "eventTitle": "업사이클링 원데이 클래스",
              "eventStatus": "OPEN",
              "applicationStatus": "APPLIED",
              "eventPeriod": {
                "startDate": "2025-02-10",
                "endDate": "2025-02-11"
              },
              "location": "서울시 마포구 연남동 223-14 2F",
              "description": "'교환'과 '수선’으로 끝까지 입는 경험과 실천을 제공하는 지속 가능한 의생활 실험 공간",
              "usageGuide": "현장에는 개인 텀블러를 지참해주세요.",
              "precautions": "화재 예방을 위해 지정된 구역에서만 작업해주세요.",
              "optionTrail": [
                {
                  "eventOptionId": 2001,
                  "name": "11월 15일"
                },
                {
                  "eventOptionId": 2002,
                  "name": "오전 세션"
                },
                {
                  "eventOptionId": 2003,
                  "name": "A조"
                }
              ]
            }
            """;

    public static final String ADMIN_EVENT_LIST_RESPONSE = """
            {
              "events": [
                {
                  "eventId": 101,
                  "title": "지속가능 패션 워크숍",
                  "status": "OPEN",
                  "startDate": "2025-11-10",
                  "endDate": "2025-11-30",
                  "location": "서울시 마포구 연남동 223-14 2F",
                  "totalCapacity": 120,
                  "appliedCount": 87,
                  "remainingCount": 33,
                  "organizerName": "운영자",
                  "organizerContact": "admin@wearagain.kr",
                  "organizerAdminId": 11,
                  "organizerAdminEmail": "admin@wearagain.kr",
                  "organizerAdminName": "홍길동"
                }
              ],
              "page": 0,
              "size": 10,
              "totalElements": 42,
              "totalPages": 5,
              "hasNext": true
            }
            """;

    public static final String ADMIN_EVENT_DETAIL_RESPONSE = """
            {
              "eventId": 101,
              "title": "지속가능 패션 워크숍",
              "description": "웨어어게인과 함께하는 리폼 클래스",
              "usageGuide": "준비물은 개인 텀블러를 지참해주세요.",
              "precautions": "화재 예방을 위해 지정된 구역에서만 작업해주세요.",
              "location": "서울시 마포구 연남동 223-14 2F",
              "organizerName": "운영자",
              "organizerContact": "admin@wearagain.kr",
              "organizerAdminId": 11,
              "organizerAdminEmail": "admin@wearagain.kr",
              "organizerAdminName": "홍길동",
              "startDate": "2025-11-10",
              "endDate": "2025-11-30",
              "status": "OPEN",
              "totalCapacity": 120,
              "appliedCount": 87,
              "remainingCount": 33,
              "staffCode": "023941",
              "staffCodeIssuedAt": "2025-02-01T10:15:20Z",
              "createdAt": "2025-10-21T11:20:05Z",
              "updatedAt": "2025-11-01T09:00:00Z",
              "images": [
                {
                  "imageId": 1001,
                  "url": "https://cdn.wearagain.kr/events/101/main.jpg",
                  "altText": "대표 이미지",
                  "displayOrder": 1
                }
              ],
              "options": [
                {
                  "optionId": 2001,
                  "name": "11월 15일",
                  "displayOrder": 1,
                  "capacity": null,
                  "appliedCount": null,
                  "remainingCount": null,
                  "children": [
                    {
                      "optionId": 2003,
                      "name": "A조",
                      "displayOrder": 1,
                      "capacity": 30,
                      "appliedCount": 25,
                      "remainingCount": 5,
                      "children": []
                    }
                  ]
                }
              ],
              "applications": [
                {
                  "applicationId": 5001,
                  "email": "user@wearagain.kr",
                  "displayName": "사용자",
                  "optionId": 2003,
                  "status": "APPLIED",
                  "appliedAt": "2025-11-12T04:00:00Z",
                  "reason": null
                }
              ],
              "impactAnalytics": {
                "available": true,
                "co2Saved": 12.34,
                "waterSaved": 210.5,
                "energySaved": 45.67,
                "message": null
              }
            }
            """;

    public static final String ADMIN_EVENT_UPDATE_REQUEST = """
            {
              "title": "지속가능 패션 워크숍 (업데이트)",
              "description": "워크숍 일정이 업데이트되었습니다.",
              "usageGuide": "업데이트된 이용 방법",
              "precautions": "안전 수칙을 다시 확인해주세요.",
              "location": "서울시 마포구 연남동 223-14 3F",
              "startDate": "2025-11-12",
              "endDate": "2025-12-01",
              "status": "OPEN",
              "images": [
                {
                  "url": "https://cdn.wearagain.kr/events/101/main.jpg",
                  "altText": "대표 이미지",
                  "displayOrder": 1
                }
              ],
              "options": [
                {
                  "name": "11월 20일",
                  "displayOrder": 1,
                  "capacity": null,
                  "children": [
                    {
                      "name": "오전 세션",
                      "displayOrder": 1,
                      "capacity": null,
                      "children": [
                        {
                          "name": "A조",
                          "displayOrder": 1,
                          "capacity": 30,
                          "children": []
                        }
                      ]
                    }
                  ]
                }
              ]
            }
            """;

    public static final String ADMIN_EVENT_UPDATE_RESPONSE = ADMIN_EVENT_DETAIL_RESPONSE;

    public static final String ADMIN_EVENT_STATUS_UPDATE_REQUEST = """
            {
              "status": "OPEN",
              "memo": "검수 완료"
            }
            """;

    public static final String ADMIN_EVENT_STATUS_UPDATE_RESPONSE = ADMIN_EVENT_DETAIL_RESPONSE;

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
              "organizerName": "운영자",
              "organizerContact": "admin@wearagain.kr",
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
                  "displayOrder": 1,
                  "capacity": null,
                  "appliedCount": null,
                  "remainingCount": null,
                  "children": [
                    {
                      "optionId": 2002,
                      "name": "오전 세션",
                      "displayOrder": 1,
                      "capacity": null,
                      "appliedCount": null,
                      "remainingCount": null,
                      "children": [
                        {
                          "optionId": 2003,
                          "name": "A조",
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

    public static final String ADMIN_EVENT_APPROVAL_LIST_RESPONSE = """
            {
              "approvals": [
                {
                  "approvalRequestId": 1,
                  "event": {
                    "eventId": 100,
                    "title": "지속가능 패션 워크숍",
                    "location": "서울시 마포구 연남동 223-14 2F",
                    "startDate": "2025-11-10",
                    "endDate": "2025-11-30"
                  },
                  "requestingAdmin": {
                    "name": "홍길동",
                    "email": "admin1@wearagain.kr"
                  },
                  "createdAt": "2025-10-20T10:00:00Z"
                },
                {
                  "approvalRequestId": 2,
                  "event": {
                    "eventId": 101,
                    "title": "업사이클링 패션 쇼",
                    "location": "서울시 강남구 테헤란로 123",
                    "startDate": "2025-11-15",
                    "endDate": "2025-11-25"
                  },
                  "requestingAdmin": {
                    "name": "김영희",
                    "email": "admin2@wearagain.kr"
                  },
                  "createdAt": "2025-10-21T14:30:00Z"
                }
              ],
              "page": 0,
              "size": 10,
              "totalElements": 2,
              "totalPages": 1,
              "hasNext": false
            }
            """;

    public static final String ADMIN_EVENT_APPROVAL_DETAIL_RESPONSE = """
            {
              "approvalRequestId": 1,
              "createdAt": "2025-10-20T10:00:00Z",
              "processedAt": null,
              "requestingAdmin": {
                "name": "홍길동",
                "email": "admin1@wearagain.kr"
              },
              "processedByAdmin": null,
              "event": {
                "eventId": 100,
                "title": "지속가능 패션 워크숍",
                "description": "웨어어게인과 함께하는 리폼 클래스",
                "location": "서울시 마포구 연남동 223-14 2F",
                "startDate": "2025-11-10",
                "endDate": "2025-11-30",
                "status": "DRAFT"
              }
            }
            """;

    public static final String ADMIN_EVENT_APPROVE_RESPONSE = """
            "행사 승인이 완료되었습니다."
            """;

    public static final String ADMIN_EVENT_REJECT_APPROVAL_RESPONSE = """
            "행사 승인이 거부되었습니다."
            """;
}
