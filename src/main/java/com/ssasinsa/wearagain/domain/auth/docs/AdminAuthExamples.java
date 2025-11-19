package com.ssasinsa.wearagain.domain.auth.docs;

public final class AdminAuthExamples {

    private AdminAuthExamples() {
    }

    public static final String ADMIN_LOGIN_REQUEST = """
            {
              "email": "admin@wearagain.kr",
              "password": "AdminPassw0rd!"
            }
            """;

    public static final String ADMIN_LOGIN_RESPONSE = """
            {
              "accessToken": "admin-access-token",
              "refreshToken": "admin-refresh-token",
              "tokenType": "Bearer",
              "expiresIn": 1800
            }
            """;

    public static final String ADMIN_REFRESH_REQUEST = """
            {
              "refreshToken": "admin-refresh-token"
            }
            """;

    public static final String ADMIN_SIMPLE_RESPONSE = """
            {
              "message": "요청이 정상 처리되었습니다."
            }
            """;

    public static final String ADMIN_SIGNUP_REQUEST = """
            {
              "email": "new.admin@wearagain.kr",
              "name": "관리자 후보",
              "reason": "운영팀 합류 예정",
              "password": "AdminCandidate1!"
            }
            """;

    public static final String ADMIN_SIGNUP_RESPONSE = """
            {
              "signupRequestId": 10,
              "email": "new.admin@wearagain.kr",
              "name": "관리자 후보",
              "status": "PENDING",
              "createdAt": "2025-11-03T10:00:00Z"
            }
            """;

    public static final String ADMIN_SIGNUP_LIST_RESPONSE = """
            {
              "items": [
                {
                  "signupRequestId": 10,
                  "email": "new.admin@wearagain.kr",
                  "name": "관리자 후보",
                  "requestedRole": "ADMIN",
                  "status": "PENDING",
                  "reason": "운영팀 신규 인력",
                  "createdAt": "2025-11-03T10:00:00",
                  "reviewedAt": null,
                  "reviewer": null
                }
              ]
            }
            """;

    public static final String ADMIN_APPROVE_RESPONSE = """
            {
              "adminUserId": 5,
              "email": "new.admin@wearagain.kr",
              "role": "ADMIN",
              "status": "ACTIVE"
            }
            """;

}
