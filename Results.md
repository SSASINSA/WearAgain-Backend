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

# [TASK] 도메인 엔티티 초기 구성 (#26)
## 개요
- 관리자, 커뮤니티, 이벤트, 재무, 마스코트, 알림, 상점 도메인의 핵심 엔티티를 일괄 정의해 이후 기능 구현의 공통 기반을 마련했습니다.

## 상세 내용
- 엔티티 공통 규칙(엔티티 팩토리 메서드, `@NoArgsConstructor(access = PROTECTED)`, `BaseTimeEntity` 상속)을 준수하며 각 도메인의 핵심 테이블과 필드를 매핑했습니다.
- `Admin`, `AdminRole`을 통해 관리자 계정과 권한 롤 구조를 정의하고 비밀번호/권한 변경 도메인 메서드를 포함했습니다.
- `CommunityPost`, `PostComment`, `PostLike`, `Report`, `ReportStatus` 등 커뮤니티 상호작용 엔티티를 설정하고 좋아요/댓글 컬렉션을 LAZY로 구성했습니다.
- `Event`, `EventApplication`, `EventOption`과 상태 Enum(`EventStatus`, `EventApplicationStatus`)을 도입해 이벤트 신청과 옵션 선택 흐름을 표현했습니다.
- `CreditHistory`, `TicketHistory`, `ImpactAnalytics`로 포인트·티켓·임팩트 통계를 기록하고, 비즈니스 요구에 맞는 Enum/컬럼을 정의했습니다.
- `UserMascot`, `MascotRewardRule`로 마스코트 성장/보상 로직을 표현하고, `Notification` 엔티티로 사용자별 알림을 관리하도록 설계했습니다.
- `StoreItem`, `StoreOrder` 및 각 상태 Enum을 추가해 상점 재화/주문 상태 추적을 위한 스키마를 구성했습니다.

## 근거 및 설계 선택
- AGENTS.md의 엔티티 규칙(지연 로딩, PK Long + IDENTITY, 정적 팩토리 메서드)을 일관 적용해 추후 서비스 계층에서의 사용성을 확보했습니다.
- 도메인별 패키지 구조를 맞추기 위해 `domain/{도메인}/entity` 경로에 엔티티를 배치해 모듈화와 CQRS 확장성을 고려했습니다.
- 상태값은 모두 Enum + `@Enumerated(EnumType.STRING)`으로 관리해 가독성과 변경 안정성을 확보했습니다.
- 컬렉션 필드는 `new ArrayList<>()`로 초기화하고 편의 메서드(`addLike`, `removeLike`, `addComment`)를 제공해 양방향 관계 정합성을 유지하도록 했습니다.

## 회고/이슈
- 현재는 엔티티 정의만 포함되어 있어 마이그레이션 스크립트와 테스트 작성이 필요하며, 이 단계에서 비즈니스 규칙 검증은 추후 서비스 계층 구현과 함께 진행해야 합니다.
- 여러 도메인이 동시에 추가되었기 때문에 향후 기능 개발 시 단계별로 마이그레이션 및 리포지토리 분리를 진행해야 합니다.

### 리뷰 피드백 (seungsang2000)
- 커뮤니티 게시글 도메인: `thumbnailUrl` 컬럼 제거 및 태그 → 카테고리로 재구조화, 이미지/메타 정보는 별도 테이블 또는 컬렉션으로 분리 저장을 제안했습니다.
- 이벤트 도메인: 썸네일 컬럼 삭제, 짧은/긴 설명을 하나의 필드로 통합하고 이미지 정보도 별도 테이블(또는 리스트)로 분리하는 방안을 요청했습니다.
- 재무 이력 엔티티: `BaseTimeEntity`와 중복되는 `@AttributeOverrides` 제거 등 감사 필드 중복 여부를 재검토할 것을 권고했습니다.
- 마스코트 도메인: `UserMascot` ↔ `User` 연관관계를 실사용 구조에 맞게 일대일로 재검토하라고 피드백했습니다.

# [TASK] AGENTS.md 수정사항 반영한 엔티티 리팩토링 (#26)
## 개요
- 최신 AGENTS.md 지침(이미지 테이블 분리, 카테고리 구조, 빌더 규칙 등)에 맞춰 커뮤니티·이벤트·스토어 등 주요 엔티티를 전면 정비했습니다.

## 상세 내용
- `community`: 카테고리 전용 엔티티(`CommunityCategory`), 게시글 이미지 엔티티(`CommunityPostImage`)를 추가하고 `CommunityPost`에 일대다 연관관계를 도입했습니다 (`src/main/java/com/ssasinsa/wearagain/domain/community/entity/CommunityPost.java:1`).
- `event`: 이벤트 이미지 엔티티(`EventImage`)를 신설하고 `Event`의 설명 필드를 단일 `description`으로 통합했습니다 (`src/main/java/com/ssasinsa/wearagain/domain/event/entity/Event.java:1`).
- `store`: 상점 아이템 이미지를 `StoreItemImage`로 분리하고 `StoreItem`에 이미지 컬렉션 및 기본값을 설정했습니다 (`src/main/java/com/ssasinsa/wearagain/domain/store/entity/StoreItem.java:1`).
- `mascot`: `UserMascot`과 `User`의 연관관계를 일대일로 조정해 유저당 마스코트 단일 소유를 보장합니다 (`src/main/java/com/ssasinsa/wearagain/domain/mascot/entity/UserMascot.java:1`).
- 공통 규칙: `@AttributeOverrides` 중복 선언을 제거하고, 모든 클래스에서 `@Builder` + `@AllArgsConstructor(access = PRIVATE)` 구조로 통일했습니다 (`src/main/java/com/ssasinsa/wearagain/domain/finance/entity/CreditHistory.java:1`).

## 테스트
- `./gradlew test`
