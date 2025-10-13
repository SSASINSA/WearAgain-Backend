# [TASK] Google OAuth2 로그인/회원가입 처리 (#2)
## 개요
- Google OAuth2 인증 코드를 받아 회원가입/로그인을 처리하고 JWT 토큰을 발급하는 API를 구현했습니다.

## 상세 내용
- `POST /api/v1/auth/google/callback` 엔드포인트를 추가하고 요청 DTO/응답 DTO를 정의했습니다.
- Google 토큰 발급 및 사용자 정보 조회 전용 `GoogleOAuthClient`를 구성해 OAuth2 플로우를 캡슐화했습니다.
- 신규/기존 사용자 식별 로직과 JWT(Access/Refresh) 발급, Redis 기반 Refresh Token 저장 로직을 AuthService로 구현했습니다.
- 공통 예외 구조(`ErrorCode`, `CustomException`, GlobalExceptionHandler`)를 추가하고 Auth 도메인 오류 코드를 정의했습니다.
- `/api/v1/auth/google/authorize-url` 엔드포인트와 `google-login-test.html` 테스트 페이지를 추가해 브라우저에서 손쉽게 인증 코드를 발급·검증할 수 있도록 했습니다.

## 회고/이슈
- 실제 Google API 호출을 위해서는 `OAUTH_GOOGLE_CLIENT_ID`, `OAUTH_GOOGLE_CLIENT_SECRET`, `OAUTH_GOOGLE_REDIRECT_URI` 값이 유효해야 하며, 로컬 테스트 전에 환경 변수를 반드시 점검해야 합니다.
- JWT 비밀키는 충분한 길이로 설정하지 않으면 런타임 예외가 발생하므로 운영 환경에서 안전한 키 관리가 필요합니다.

# [TASK] Kakao OAuth2 로그인/회원가입 처리 (#3)
## 개요
- Kakao OAuth2 Authorization Code 플로우를 통해 로그인/회원가입을 처리하고 JWT 토큰을 발급하는 기능을 추가했습니다.

## 상세 내용
- `GET /api/v1/auth/kakao/authorize-url`, `POST /api/v1/auth/kakao/callback` 엔드포인트를 구현해 Kakao 인가 URL 발급과 로그인 처리를 분리했습니다.
- Kakao 토큰 발급/사용자 정보 조회를 담당하는 `KakaoOAuthClient` 및 DTO를 추가하고, 이메일 제공이 누락된 경우 예외를 변환하도록 했습니다.
- 공통 OAuth 회원 조회/생성 로직을 재사용하도록 `AuthServiceImpl`을 리팩터링하고 JWT 발급/저장 과정을 메서드로 분리했습니다.
- README에 Kakao API 명세와 에러 코드 설명을 업데이트하고, `AuthErrorCode`에 Kakao 전용 코드를 확장했습니다.

## 회고/이슈
- Kakao 계정에서 이메일 제공 권한을 미동의한 사용자는 `A1002` 에러로 응답되므로, 프론트엔드에서 이메일 제공 동의를 유도하는 UX가 필요합니다.
- 로컬에서 `./gradlew test` 실행 시 JDK 17 이상이 필요하므로 개발 환경 JDK 버전을 점검해야 합니다.
