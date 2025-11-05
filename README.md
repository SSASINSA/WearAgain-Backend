# WearAgain-Backend
피우다프로젝트

## 환경 변수 설정
- `.env.example` 파일을 참고해 루트 경로에 `.env`를 생성합니다.
- 애플리케이션은 `application.yml`을 통해 MySQL, Redis, JWT, OAuth2 Provider 정보를 환경 변수에서 주입받습니다.
- Kakao idToken 흐름을 사용하는 경우 `OAUTH_KAKAO_APP_CLIENT_ID`(모바일 앱 키)를 별도로 설정해야 합니다.

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
  - Scope: `openid profile_nickname account_email`
- **POST** `/api/v1/auth/kakao/callback`
- 요청
  ```json
  {
    "authorizationCode": "AUTHORIZATION_CODE"
  }
  ```
- 성공 응답: Google과 동일 구조 (`userId`는 UUID 문자열)
- 실패 시 `A1002`(토큰/사용자 정보 조회 또는 이메일 미제공) 또는 `A1004`(인가 코드 누락) 에러 코드 사용
- **POST** `/api/v1/auth/kakao/id-token`
- 요청
  ```json
  {
    "idToken": "KAKAO_ID_TOKEN"
  }
  ```
- 처리
  - 전달된 `idToken`으로 카카오 사용자 정보를 검증 및 조회한다.
  - 사용자 정보가 존재하지 않으면 자동 회원가입을 수행하고 JWT를 발급한다.
  - RTR 정책에 따라 Refresh Token을 회전시킨다.
- 성공 응답: Google과 동일 구조 (`userId`는 UUID 문자열)
- 실패 시 `A1002`(카카오 idToken 검증 실패/사용자 정보 조회 실패) 또는 `A1004`(입력 파라미터 누락) 에러 코드 사용

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

#### 공통 (CommonErrorCode)
| 코드 | HTTP Status | 메시지 |
|------|-------------|--------|
| C1000 | 500 Internal Server Error | 서버 내부 오류가 발생했습니다. |
| C1001 | 400 Bad Request | 잘못된 요청입니다. |
| C1002 | 401 Unauthorized | 인증이 필요합니다. |
| C1003 | 403 Forbidden | 접근이 거부되었습니다. |

#### 사용자 인증 (AuthErrorCode)
| 코드 | HTTP Status | 메시지 |
|------|-------------|--------|
| A1001 | 401 Unauthorized | Google 토큰 발급 실패 또는 사용자 정보 조회 실패 |
| A1002 | 401 Unauthorized | Kakao 토큰 발급/사용자 정보 조회 실패 또는 이메일 미제공 |
| A1003 | 401 Unauthorized | Apple 토큰 발급 또는 사용자 정보 조회 실패 |
| A1004 | 400 Bad Request | 인가 코드가 필요합니다. |
| A1005 | 401 Unauthorized | Refresh Token 만료 또는 불일치 |
| A1006 | 401 Unauthorized | Access Token이 만료되었습니다. |
| A1007 | 401 Unauthorized | 재사용된 Refresh Token 입니다. |

#### 관리자 인증 (AdminAuthErrorCode)
| 코드 | HTTP Status | 메시지 |
|------|-------------|--------|
| AD1001 | 400 Bad Request | 입력 값이 유효하지 않습니다. |
| AD1002 | 401 Unauthorized | 이메일 또는 비밀번호가 올바르지 않습니다. |
| AD1003 | 403 Forbidden | 승인되지 않은 계정입니다. |
| AD1004 | 403 Forbidden | SUPER_ADMIN 권한이 필요합니다. |
| AD1005 | 409 Conflict | 이미 처리된 신청입니다. |
| AD1006 | 409 Conflict | 이미 등록된 이메일입니다. |
| AD1007 | 410 Gone | 가입 신청이 만료되었습니다. |
| AD1008 | 500 Internal Server Error | 관리자 인증 처리 중 오류가 발생했습니다. |
| AD1009 | 401 Unauthorized | 관리자 인증 토큰이 유효하지 않습니다. |

#### 행사 도메인 (EventErrorCode)
| 코드 | HTTP Status | 메시지 |
|------|-------------|--------|
| E1001 | 400 Bad Request | 필수 입력 값이 누락되었습니다. |
| E1002 | 400 Bad Request | 행사 기간이 유효하지 않습니다. |
| E1003 | 400 Bad Request | 옵션 트리 깊이가 허용 범위를 초과했습니다. |
| E1004 | 400 Bad Request | 옵션 정보가 올바르지 않습니다. |
| E1005 | 400 Bad Request | 이미지 정보가 올바르지 않습니다. |
| E1006 | 409 Conflict | 중복된 옵션이 존재합니다. |
| E1007 | 403 Forbidden | 행사 등록 권한이 없습니다. |
| E1008 | 500 Internal Server Error | 행사 등록 처리 중 오류가 발생했습니다. |
| E1009 | 500 Internal Server Error | 이미지 업로드 처리 중 오류가 발생했습니다. |
| E1010 | 404 Not Found | 행사를 찾을 수 없습니다. |
| E1011 | 400 Bad Request | 행사를 신청할 수 없는 상태입니다. |
| E1012 | 404 Not Found | 행사 옵션을 찾을 수 없습니다. |
| E1013 | 409 Conflict | 이미 신청한 행사입니다. |
| E1014 | 409 Conflict | 신청 가능 인원이 초과되었습니다. |
| E1015 | 404 Not Found | 신청 정보를 찾을 수 없습니다. |
| E1016 | 409 Conflict | 취소할 수 없는 신청 상태입니다. |
| E1017 | 400 Bad Request | 행사 조회 요청이 올바르지 않습니다. |
| E1018 | 409 Conflict | 이미 보관 처리된 행사입니다. |
| E1019 | 409 Conflict | 이미 처리된 신청입니다. |
| E1020 | 404 Not Found | 행사 담당 관리자를 찾을 수 없습니다. |
| E1021 | 403 Forbidden | 행사 수정 권한이 없습니다. |
| E1022 | 403 Forbidden | 행사 상태 변경 권한이 없습니다. |
| E1023 | 409 Conflict | 허용되지 않은 상태 전환입니다. |

## 행사 API 개요

- **관리자(Admin)**
  - `POST /api/v1/admin/events`: 행사 등록
  - `GET /api/v1/admin/events`: offset 기반 목록 조회(상태 필터, 신청 통계, 담당 관리자 정보 포함)
  - `GET /api/v1/admin/events/{eventId}`: 상세 조회(이미지, 옵션, 신청 목록, 담당 관리자 정보)
  - `PUT /api/v1/admin/events/{eventId}`: 부분 수정(기본 정보/이미지/옵션)
  - `PATCH /api/v1/admin/events/{eventId}/status`: 상태 변경(권한/전환 검증 포함)
  - `DELETE /api/v1/admin/events/{eventId}`: 보관 처리(ARCHIVED)
  - `PATCH /api/v1/admin/applications/{applicationId}/reject`: 신청 반려
- **사용자(User)**
  - `GET /api/v1/events`: cursor 기반 행사 목록 조회
  - `GET /api/v1/events/{eventId}`: 행사 상세 조회
  - `POST /api/v1/events/{eventId}/apply`: 행사 신청
  - `PATCH /api/v1/events/applications/{applicationId}/cancel`: 신청 취소
