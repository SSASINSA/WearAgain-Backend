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

# [TASK] Apple OAuth2 로그인/회원가입 처리 (#4)
## 개요
- Apple OAuth2 Authorization Code 플로우를 지원해 로그인/회원가입과 JWT 발급을 완료했습니다.

## 상세 내용
- `POST /api/v1/auth/apple/callback` 엔드포인트와 `AppleOAuthLoginRequest` DTO를 추가하고 서비스/컨트롤러에 애플 로그인을 연동했습니다.
- 애플 토큰 요청·ID 토큰 검증을 담당하는 `AppleOAuthClient`를 구현해 클라이언트 시크릿 생성, JWKS 캐싱, ID 토큰 검증 로직을 캡슐화했습니다.
- ID 토큰 검증 후 기존 OAuth 계정 조회/생성, JWT 발급, Redis RTR 저장까지 기존 흐름과 일관되게 통합했습니다.
- Apple 전용 프로퍼티/에러 코드를 추가하고 README 명세에 맞춰 애플 설정을 application.yml에 반영했습니다.

## 회고/이슈
- 새로 작성한 단위 테스트는 JDK 17 이상 환경에서 `./gradlew test`가 실행되어야 하며, 현재 로컬 JVM(16)에서는 빌드가 실패하므로 환경 업데이트가 필요합니다.

# [TASK] RTR 기반 토큰 재발급 API 구현 (#5)
## 개요
- Refresh Token Rotation 전략을 적용한 토큰 재발급 API를 구현했습니다.

## 상세 내용
- `POST /api/v1/auth/refresh` 엔드포인트를 추가하고 요청/응답 DTO를 정의해 RTR 흐름을 노출했습니다.
- Refresh Token 파싱/검증 기능을 `JwtTokenProvider`에 확장하고, Redis에 저장된 사용자별 최신 토큰과 회전 감지 키를 함께 검증하도록 했습니다.
- 토큰 재사용 시 `A1007`, 만료·불일치 시 `A1005`를 반환하도록 예외 코드를 확장하고, 신규 토큰 발급 시 Redis 키를 갱신하도록 로직을 정리했습니다.
- 단위 테스트를 통해 정상 회전, 재사용 감지, 파싱 실패 시나리오를 검증했습니다.

## 회고/이슈
- RTR 검증 로직은 Redis 키 존재 여부에 의존하므로 운영 환경에서 적절한 TTL 설정과 Redis 가용성을 함께 모니터링해야 합니다.

# [TASK] Access Token 기반 인증 필터 구현 (#13)
## 개요
- Access Token을 이용해 API 요청을 인증하는 JWT 필터와 예시 엔드포인트를 추가했습니다.

## 상세 내용
- `JwtAuthenticationFilter`를 도입해 Authorization 헤더의 Bearer 토큰을 검증하고 `SecurityContext`에 사용자 정보를 주입합니다.
- 실패 시 일관된 에러 응답을 반환하는 `JwtAuthenticationEntryPoint`를 구현하고 시큐리티 설정에 필터/엔트리포인트를 등록했습니다.
- 인증 필요/불필요 샘플 API(`/api/v1/sample/private`, `/api/v1/sample/public`)를 작성해 동작을 검증할 수 있도록 했습니다.
- Access Token 파싱 로직을 `JwtTokenProvider`에 확장하고 필터 단위 테스트를 추가했습니다.

## 회고/이슈
- 현재 권한(roles) 정보가 없어 기본적으로 빈 권한으로 처리되므로, 추후 역할 기반 인가가 필요하면 토큰 구조를 확장해야 합니다.
