# 🎵 NoDap Backend

노답(NoDap) - 음악 추천 앨범 공유 서비스 백엔드 API

---

## 📋 목차

- [기술 스택](#-기술-스택)
- [프로젝트 구조](#-프로젝트-구조)
- [로컬 개발 환경 설정](#-로컬-개발-환경-설정)
- [환경 변수](#-환경-변수)
- [API 문서](#-api-문서)
- [코딩 컨벤션](#-코딩-컨벤션)
- [배포](#-배포)

---

## 🛠 기술 스택

| 분류 | 기술 |
|------|------|
| **Framework** | Spring Boot 3.4.1 |
| **Language** | Java 21 |
| **Database** | MySQL 8.0 + Flyway |
| **Cache** | Redis 7 |
| **Security** | Spring Security + JWT (쿠키 기반) |
| **OAuth** | 카카오 로그인 |
| **API 문서** | Swagger (SpringDoc OpenAPI) |
| **Build** | Gradle |
| **Container** | Docker, Docker Compose |

---

## 📁 프로젝트 구조

```
nodap-server/
├── src/main/java/com/nodap/
│   ├── application/           # 애플리케이션 서비스 (UseCase)
│   ├── domain/               # 도메인 (Entity, Repository)
│   │   ├── album/
│   │   ├── music/
│   │   └── user/
│   ├── global/               # 전역 설정, 공통 모듈
│   │   ├── common/           # ApiResponse, BaseTimeEntity
│   │   ├── config/           # Security, Swagger 등
│   │   └── error/            # 예외 처리
│   ├── infrastructure/       # 인프라 (JWT, 외부 API)
│   │   ├── auth/
│   │   └── external/
│   └── interfaces/           # 인터페이스 (Controller, DTO)
│       ├── controller/
│       └── dto/
├── src/main/resources/
│   ├── db/migration/              # Flyway 마이그레이션
│   ├── application.yml            # 공통 설정
│   ├── application-local.yml.example  # 로컬 설정 예시 (복사해서 사용)
│   └── application-prod.yml       # 프로덕션 설정
└── build.gradle
```

---

## 🚀 로컬 개발 환경 설정

### 1. 사전 준비

- **Java 21** 설치
- **Docker Desktop** 설치 ([다운로드](https://www.docker.com/products/docker-desktop/))

### 2. Docker로 MySQL, Redis 실행

```bash
# 프로젝트 루트에서 실행
docker-compose up -d

# 상태 확인
docker-compose ps

# 중지
docker-compose down
```

#### ⚠️ Windows 포트 문제

Windows에서 Hyper-V가 3306 포트를 예약할 수 있습니다.  
그래서 MySQL은 **33060** 포트를 사용합니다.

```
MySQL: localhost:33060
Redis: localhost:6379
```

<details>
<summary>3306 포트 사용하고 싶다면? (클릭)</summary>

PowerShell을 **관리자 권한**으로 실행:

```powershell
# 동적 포트 범위를 높은 쪽으로 변경
netsh int ipv4 set dynamic tcp start=49152 num=16384
```

PC 재시작 후 `docker-compose.yml`에서 포트를 `3306:3306`으로 변경.

</details>

### 3. 로컬 설정 파일 생성

⚠️ `application-local.yml`은 Git에 커밋되지 않습니다. 직접 생성해야 합니다!

```bash
# example 파일을 복사하여 로컬 설정 파일 생성
cp nodap-server/src/main/resources/application-local.yml.example \
   nodap-server/src/main/resources/application-local.yml
```

복사 후 `application-local.yml`에서 카카오 OAuth 설정을 실제 값으로 변경:

```yaml
oauth:
  kakao:
    client-id: your-kakao-rest-api-key      # 팀에서 공유받은 값
    client-secret: your-kakao-client-secret  # 선택사항
    redirect-uri: http://localhost:3000/oauth/kakao/callback
```

### 4. 애플리케이션 실행

```bash
# Gradle로 실행
./gradlew bootRun

# 또는 IntelliJ에서 NodapServerApplication 실행
```

### 5. 접속 확인

| URL | 설명 |
|-----|------|
| http://localhost:8080/api/v1/health | 헬스체크 |
| http://localhost:8080/swagger-ui.html | API 문서 |

---

## 🔐 환경 변수

### 프로덕션 필수 환경 변수

| 변수명 | 설명 | 필수 | 기본값 |
|--------|------|:----:|--------|
| `JWT_SECRET` | JWT 서명 키 (32자 이상) | ✅ | - |
| `MYSQL_USERNAME` | MySQL 사용자명 | ✅ | - |
| `MYSQL_PASSWORD` | MySQL 비밀번호 | ✅ | - |
| `REDIS_HOST` | Redis 호스트 | ❌ | localhost |
| `REDIS_PORT` | Redis 포트 | ❌ | 6379 |
| `REDIS_PASSWORD` | Redis 비밀번호 | ❌ | (없음) |
| `KAKAO_CLIENT_ID` | 카카오 REST API 키 | ✅ | - |
| `KAKAO_CLIENT_SECRET` | 카카오 Client Secret | ❌ | (없음) |
| `KAKAO_REDIRECT_URI` | 카카오 리다이렉트 URI | ✅ | - |
| `CORS_ALLOWED_ORIGINS` | 허용할 Origin (쉼표 구분) | ✅ | - |
| `SWAGGER_SERVER_URL` | Swagger 서버 URL (HTTPS) | ❌ | https://answerwithsong.com |
| `SWAGGER_SERVER_DESCRIPTION` | Swagger 서버 설명 | ❌ | 프로덕션 API 서버 |

### JWT 설정

| 변수명 | 설명 | 기본값 |
|--------|------|--------|
| `JWT_ACCESS_EXPIRY` | Access Token 만료 시간 (ms) | 1800000 (30분) |
| `JWT_REFRESH_EXPIRY` | Refresh Token 만료 시간 (ms) | 604800000 (7일) |

---

## 📚 API 문서

### Swagger UI

서버 실행 후 접속:
```
http://localhost:8080/swagger-ui.html
```

### Auth API

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | `/api/v1/auth/login/kakao` | 카카오 로그인 | ❌ |
| POST | `/api/v1/auth/reissue` | 토큰 재발급 | ❌ (쿠키) |
| POST | `/api/v1/auth/logout` | 로그아웃 | ❌ (쿠키) |

### 인증 방식

- **쿠키 기반 JWT 인증**
- 로그인 시 `accessToken`, `refreshToken` 쿠키 자동 설정
- 쿠키 속성: `HttpOnly; Secure; SameSite=None`
- 프론트엔드에서 `withCredentials: true` 설정 필요

---

## 📐 코딩 컨벤션

> 상세 내용: `docs/CODING_CONVENTIONS.md`

### 핵심 규칙

| 규칙 | 설명 |
|------|------|
| **Setter 금지** | 무결성 보장, Builder 패턴 사용 |
| **Controller 경량화** | 비즈니스 로직은 Service에서 |
| **Swagger 문서화** | 모든 API에 `@Operation` 필수 |
| **로그 형식** | `[Error-XXX] 에러내용` |

### 예시

```java
// ❌ BAD
@Setter
public class User { }

// ✅ GOOD
public class User {
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }
}
```

---

## 🌐 환경별 설정

| 환경 | 설정 파일 | MySQL | Redis |
|------|----------|-------|-------|
| **로컬** | `application-local.yml` | Docker (33060) | Docker (6379) |
| **프로덕션** | `application-prod.yml` | RDS / EC2 | ElastiCache / EC2 |

---

## 📦 배포

> 상세 내용: `docs/BACKEND_DEPLOYMENT_GUIDE.md`

### 빌드

```bash
./gradlew clean build -x test
```

### JAR 실행

```bash
java -jar -Dspring.profiles.active=prod build/libs/nodap-server-0.0.1-SNAPSHOT.jar
```

---

## 🗂 관련 문서

| 문서 | 설명 |
|------|------|
| `docs/CODING_CONVENTIONS.md` | 코딩 컨벤션 |
| `docs/PROJECT_STATUS.md` | 프로젝트 진행 현황 |
| `docs/BACKEND_DEPLOYMENT_GUIDE.md` | 배포 가이드 |

---

## 👥 팀

- Backend: NoDap Team

---

## 📝 라이선스

Private Project
