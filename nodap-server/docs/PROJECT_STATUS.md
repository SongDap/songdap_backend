# NoDap 프로젝트 진행 현황

## 📋 코딩 컨벤션
> 상세 내용은 `docs/CODING_CONVENTIONS.md` 참고

1. **Setter 금지** - 무결성 보장 (Builder 패턴, 의미 있는 메서드명 사용)
2. **Controller 경량화** - 비즈니스 로직은 Service에서 처리
3. **Swagger 문서화** - `@Operation` 필수 사용
4. **Service 로그 형식** - `[Error-XXX] 에러내용`

---

## ✅ 완료된 작업

### 1. 환경 설정
- ✅ `application.yml` - 공통 설정 (Flyway, JWT, OAuth, CORS 포함)
- ✅ `application-local.yml` - 로컬 개발 환경 (민감정보 분리)
- ✅ `application-prod.yml` - 프로덕션 환경 (환경변수 사용)
- ✅ `.gitignore` - 민감정보 파일 제외 설정

### 2. 공통 모듈
- ✅ `BaseTimeEntity` - 공통 시간 필드 (createdAt, updatedAt, deletedAt)
- ✅ `JpaAuditConfig` - JPA Auditing 활성화
- ✅ `ApiResponse` - 공통 API 응답 형식
- ✅ `SwaggerConfig` - Swagger (OpenAPI) 설정

### 3. 에러 처리
- ✅ `ErrorCode` - 에러 코드 Enum 정의
- ✅ `ErrorResponse` - 에러 응답 DTO
- ✅ `BusinessException` - 비즈니스 예외 클래스
- ✅ `GlobalExceptionHandler` - 전역 예외 처리 (@ControllerAdvice)

### 4. 도메인 엔티티
- ✅ `User` - 사용자 엔티티
- ✅ `UserOauthAccount` - OAuth 계정 엔티티
- ✅ `Role` - 사용자 권한 Enum (USER, ADMIN)
- ✅ `Provider` - OAuth 제공자 Enum (KAKAO, GOOGLE)
- ✅ `Album` - 앨범 엔티티
- ✅ `Category` - 앨범 카테고리 Enum (SITUATION, MOOD)
- ✅ `Music` - 수록곡 엔티티

### 5. Repository 계층
- ✅ `UserRepository` - 사용자 Repository
- ✅ `UserOauthAccountRepository` - OAuth 계정 Repository
- ✅ `AlbumRepository` - 앨범 Repository
- ✅ `MusicRepository` - 수록곡 Repository

### 6. 인증/인가 (Auth)
- ✅ `JwtProperties` - JWT 설정 프로퍼티
- ✅ `JwtTokenProvider` - JWT 토큰 생성/검증
- ✅ `CookieProvider` - 쿠키 생성/관리
- ✅ `RefreshTokenRepository` - Redis Refresh Token 저장소
- ✅ `JwtAuthenticationFilter` - JWT 인증 필터
- ✅ `SecurityConfig` - Spring Security 설정
- ✅ `WebConfig` - 웹 설정 (ConfigurationProperties 스캔)

### 7. 카카오 OAuth
- ✅ `KakaoOAuthProperties` - 카카오 OAuth 설정
- ✅ `KakaoOAuthClient` - 카카오 API 클라이언트 (WebClient)

### 8. Auth API (3개)
- ✅ `AuthService` - 인증 서비스 (로그인, 토큰 재발급, 로그아웃)
- ✅ `AuthController` - 인증 컨트롤러
  - `POST /api/v1/auth/login/kakao` - 카카오 로그인
  - `POST /api/v1/auth/reissue` - 토큰 재발급
  - `POST /api/v1/auth/logout` - 로그아웃
- ✅ `KakaoLoginRequest` - 카카오 로그인 요청 DTO
- ✅ `LoginResponse` - 로그인 응답 DTO
- ✅ `HealthController` - 헬스체크 API

### 9. User API (4개)
- ✅ `UserService` - 사용자 서비스 (정보 조회, 수정, 닉네임 중복 확인, 회원 탈퇴)
- ✅ `UserController` - 사용자 컨트롤러
  - `GET /api/v1/users/me` - 내 정보 조회
  - `PATCH /api/v1/users/me` - 내 정보 수정 (닉네임, 이메일, 프로필 이미지)
  - `GET /api/v1/users/check-nickname` - 닉네임 중복 확인
  - `DELETE /api/v1/users` - 회원 탈퇴 (Soft Delete)
- ✅ `UserInfoResponse` - 사용자 정보 응답 DTO
- ✅ `UpdateUserRequest` - 사용자 정보 수정 요청 DTO
- ✅ `CheckNicknameResponse` - 닉네임 중복 확인 응답 DTO

### 10. Flyway (DB 마이그레이션)
- ✅ `V1__init.sql` - 초기 스키마 (users, user_oauth_accounts, albums, musics)
- ✅ `V2__album_add_column.sql` - 앨범 컬럼 추가 및 users email NULL 허용
- ✅ `V3__delete_x_y_cardLength_from_musics.sql` - musics 테이블 컬럼 삭제

### 10. 배포 준비
- ✅ `docs/BACKEND_DEPLOYMENT_GUIDE.md` - 배포 가이드
- ✅ `scripts/ec2-init-setup.sh` - EC2 초기 설정 스크립트
- ✅ `scripts/deploy.sh` - 배포 스크립트

---

## 🚧 다음에 해야 할 작업

### Phase 1: User API ✅ 완료
- [x] `UserController` - 내 정보 조회, 수정, 닉네임 중복 확인, 회원 탈퇴
- [x] `UserService` - 사용자 비즈니스 로직
- [x] User DTO 작성

### Phase 2: Album API (앨범 CRUD)
- [ ] `AlbumController` - 앨범 목록, 상세, 생성, 수정, 삭제
- [ ] `AlbumService` - 앨범 비즈니스 로직
- [ ] Album DTO 작성

### Phase 3: Music API (수록곡 CRUD)
- [ ] `MusicController` - 수록곡 목록, 추가, 수정, 삭제
- [ ] `MusicService` - 수록곡 비즈니스 로직
- [ ] Music DTO 작성

### Phase 4: 공유 기능
- [ ] 앨범 공유 URL 생성
- [ ] 공유 링크를 통한 수록곡 추가 (비로그인)

### Phase 5: 테스트 코드
- [ ] 단위 테스트
- [ ] 통합 테스트

---

## 📁 프로젝트 구조

```
nodap-server/
├── src/main/java/com/nodap/
│   ├── application/           # 애플리케이션 서비스 계층
│   │   ├── auth/
│   │   │   └── AuthService.java
│   │   ├── user/
│   │   │   └── UserService.java
│   │   ├── album/
│   │   │   └── AlbumService.java
│   │   └── music/
│   │       └── MusicService.java
│   ├── domain/               # 도메인 계층
│   │   ├── album/
│   │   │   ├── entity/       # Album, Category
│   │   │   └── repository/   # AlbumRepository
│   │   ├── music/
│   │   │   ├── entity/       # Music
│   │   │   └── repository/   # MusicRepository
│   │   └── user/
│   │       ├── entity/       # User, UserOauthAccount, Role, Provider
│   │       └── repository/   # UserRepository, UserOauthAccountRepository
│   ├── global/               # 전역 설정 및 공통 모듈
│   │   ├── common/           # ApiResponse, BaseTimeEntity
│   │   ├── config/           # SecurityConfig, WebConfig, JpaAuditConfig
│   │   └── error/            # ErrorCode, BusinessException, GlobalExceptionHandler
│   ├── infrastructure/       # 인프라 계층
│   │   ├── auth/             # JWT, Cookie, RefreshToken 관련
│   │   └── external/         # 외부 API (카카오)
│   ├── interfaces/           # 인터페이스 계층
│   │   ├── controller/       # REST API 컨트롤러
│   │   │   ├── AuthController.java
│   │   │   ├── UserController.java
│   │   │   ├── AlbumController.java
│   │   │   └── MusicController.java
│   │   └── dto/              # 요청/응답 DTO
│   │       ├── auth/
│   │       ├── user/
│   │       ├── album/
│   │       └── music/
│   └── NodapServerApplication.java
├── src/main/resources/
│   ├── db/migration/         # Flyway 마이그레이션 스크립트
│   │   └── V1__init.sql
│   ├── application.yml       # 공통 설정
│   ├── application-local.yml # 로컬 설정
│   └── application-prod.yml  # 프로덕션 설정
└── build.gradle
```

---

## 🔑 환경 변수 (application-local.yml에 설정 필요)

```yaml
# JWT
jwt:
  secret: your-secret-key-at-least-32-characters

# 카카오 OAuth
oauth:
  kakao:
    client-id: your-kakao-rest-api-key
    client-secret: your-kakao-client-secret  # (선택)
    redirect-uri: http://localhost:3000/oauth/kakao/callback

# CORS
cors:
  allowed-origins: http://localhost:3000
```

---

## 🎯 API 엔드포인트 정리

### Auth API (완료)
| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | `/api/v1/auth/login/kakao` | 카카오 로그인 | ❌ |
| POST | `/api/v1/auth/reissue` | 토큰 재발급 | ❌ (쿠키) |
| POST | `/api/v1/auth/logout` | 로그아웃 | ❌ (쿠키) |
| GET | `/api/v1/health` | 헬스체크 | ❌ |

### User API (완료)
| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/api/v1/users/me` | 내 정보 조회 | ✅ |
| PATCH | `/api/v1/users/me` | 내 정보 수정 (닉네임, 이메일, 프로필 이미지) | ✅ |
| GET | `/api/v1/users/check-nickname` | 닉네임 중복 확인 | ✅ |
| DELETE | `/api/v1/users` | 회원 탈퇴 (Soft Delete) | ✅ |

### Album API (예정)
| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/api/v1/albums` | 내 앨범 목록 | ✅ |
| GET | `/api/v1/albums/{uuid}` | 앨범 상세 (공개 시 비로그인 가능) | △ |
| POST | `/api/v1/albums` | 앨범 생성 | ✅ |
| PATCH | `/api/v1/albums/{uuid}` | 앨범 수정 | ✅ |
| DELETE | `/api/v1/albums/{uuid}` | 앨범 삭제 | ✅ |

### Music API (예정)
| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/api/v1/albums/{uuid}/musics` | 수록곡 목록 | △ |
| POST | `/api/v1/albums/{uuid}/musics` | 수록곡 추가 (공유 링크) | ❌ |
| PATCH | `/api/v1/musics/{uuid}` | 수록곡 수정 | ✅ |
| DELETE | `/api/v1/musics/{uuid}` | 수록곡 삭제 | ✅ |

---

## 💡 기술 스택

- **Framework:** Spring Boot 4.0.1
- **Language:** Java 21
- **Database:** MySQL + Flyway
- **Cache:** Redis (Refresh Token)
- **Security:** Spring Security + JWT (쿠키 기반)
- **OAuth:** 카카오 로그인
- **API 문서:** Swagger (SpringDoc OpenAPI)
- **Build:** Gradle

---

## 📚 Swagger UI 접속

```
http://localhost:8080/swagger-ui.html
```

---

## ✨ 빌드 & 실행 방법

```bash
# 빌드
./gradlew build

# 실행 (로컬)
./gradlew bootRun

# 실행 전 MySQL, Redis 실행 필요
# MySQL: nodap_db 데이터베이스 생성
# Redis: 기본 포트 (6379)
```
