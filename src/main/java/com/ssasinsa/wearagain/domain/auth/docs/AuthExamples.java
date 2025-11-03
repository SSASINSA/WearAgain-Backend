package com.ssasinsa.wearagain.domain.auth.docs;

public final class AuthExamples {

    private AuthExamples() {
    }

    public static final String GOOGLE_AUTHORIZE_RESPONSE = """
            {
              "authorizationUrl": "https://accounts.google.com/o/oauth2/v2/auth?...state=xyz"
            }
            """;

    public static final String OAUTH_LOGIN_REQUEST = """
            {
              "authorizationCode": "auth-code-from-provider",
              "redirectUri": "https://wearagain.kr/callback/google"
            }
            """;

    public static final String OAUTH_LOGIN_RESPONSE = """
            {
              "accessToken": "access-token",
              "refreshToken": "refresh-token",
              "tokenType": "Bearer",
              "expiresIn": 900
            }
            """;

    public static final String TOKEN_REFRESH_REQUEST = """
            {
              "refreshToken": "refresh-token"
            }
            """;

    public static final String TOKEN_REFRESH_RESPONSE = """
            {
              "accessToken": "new-access-token",
              "refreshToken": "new-refresh-token",
              "tokenType": "Bearer",
              "expiresIn": 900
            }
            """;
}
