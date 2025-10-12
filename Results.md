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
