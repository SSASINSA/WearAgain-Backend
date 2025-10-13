# WearAgain-Backend
피우다프로젝트

## 환경 변수 설정
- `.env.example` 파일을 참고해 루트 경로에 `.env`를 생성합니다.
- 애플리케이션은 `application.yml`을 통해 MySQL, Redis, JWT, OAuth2 Provider 정보를 환경 변수에서 주입받습니다.

## 실행 전 준비
- MySQL 8.x 인스턴스를 준비하고 UTF-8 환경으로 `wearagain` 데이터베이스를 생성합니다.
- Redis 서버를 실행하고 필요 시 비밀번호를 `.env`에 설정합니다.
- Google, Kakao, Apple OAuth2 자격 증명을 발급 받아 `.env`에 입력합니다.

## OAuth2 API 명세

### 1. Google 로그인/회원가입
- **GET** `/api/v1/auth/google/authorize-url`
  - 응답
    ```json
    {
      "authorizationUrl": "https://accounts.google.com/o/oauth2/..."
    }
    ```
- **POST** `/api/v1/auth/google/callback`
- 요청
  ```json
  {
    "authorizationCode": "AUTHORIZATION_CODE"
  }
  ```
- 성공 응답
  ```json
  {
    "userId": "00000000-0000-0000-0000-000000000000",
    "email": "user@example.com",
    "displayName": "사용자",
    "profileImageUrl": "https://lh3.googleusercontent.com/...",
    "accessToken": "JWT_ACCESS_TOKEN",
    "refreshToken": "JWT_REFRESH_TOKEN",
    "accessTokenExpiresIn": 900,
    "refreshTokenExpiresIn": 1209600
  }
  ```
- 실패 시 `A1001` 또는 `A1004` 에러 코드 사용

### 2. Kakao 로그인/회원가입
- **GET** `/api/v1/auth/kakao/authorize-url`
  - 응답
    ```json
    {
      "authorizationUrl": "https://kauth.kakao.com/oauth/authorize?..."
    }
    ```
- **POST** `/api/v1/auth/kakao/callback`
- 요청
  ```json
  {
    "authorizationCode": "AUTHORIZATION_CODE"
  }
  ```
- 성공 응답: Google과 동일 구조 (`userId`는 UUID 문자열)
- 실패 시 `A1002`(토큰/사용자 정보 조회 또는 이메일 미제공) 또는 `A1004`(인가 코드 누락) 에러 코드 사용

### 3. Apple 로그인/회원가입
- **POST** `/api/v1/auth/apple/callback`
- 요청
  ```json
  {
    "code": "AUTHORIZATION_CODE",
    "id_token": "APPLE_ID_TOKEN"
  }
  ```
- 성공 응답: Google과 동일 구조 (`userId`는 UUID 문자열)
- 실패 시 `A1003` 또는 `A1004` 에러 코드 사용

### 4. Refresh Token Rotation 재발급
- **POST** `/api/v1/auth/refresh`
- 요청
  ```json
  {
    "refreshToken": "JWT_REFRESH_TOKEN"
  }
  ```
- 성공 응답
  ```json
  {
    "accessToken": "NEW_JWT_ACCESS_TOKEN",
    "refreshToken": "NEW_JWT_REFRESH_TOKEN"
  }
  ```
- 실패 시 `A1005`(만료/불일치) 또는 `A1007`(재사용) 에러 코드 사용

## 표준 에러 응답 구조

모든 인증 관련 API는 다음 표준 구조를 따른다.

```json
{
  "timestamp": "2025-10-04T12:34:56",
  "statusCode": 401,
  "errorCode": "A1001",
  "message": "에러 설명 메시지",
  "path": "/api/v1/auth/..."
}
```

### 에러 코드 목록

| ErrorCode | HTTP Status | 설명 |
|-----------|-------------|------|
| A1001 | 401 Unauthorized | Google OAuth 인증 실패 |
| A1002 | 401 Unauthorized | Kakao OAuth 인증 실패 또는 이메일 미제공 |
| A1003 | 401 Unauthorized | Apple OAuth 인증 실패 |
| A1004 | 400 Bad Request | Authorization Code 누락/유효하지 않음 |
| A1005 | 401 Unauthorized | Refresh Token 만료 또는 불일치 |
| A1006 | 401 Unauthorized | Access Token 만료 |
| A1007 | 401 Unauthorized | RTR 검증 실패 (재사용된 Refresh Token) |
