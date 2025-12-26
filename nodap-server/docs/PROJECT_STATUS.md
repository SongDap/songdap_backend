# NoDap 프로젝트 진행 현황

## ✅ 완료된 작업 (기초 공사)

### 1. 환경 설정
- ✅ `application.yml` - 공통 설정
- ✅ `application-local.yml` - 로컬 개발 환경 (민감정보 분리)
- ✅ `application-prod.yml` - 프로덕션 환경 (환경변수 사용)
- ✅ `.gitignore` - 민감정보 파일 제외 설정

### 2. 공통 모듈
- ✅ `BaseTimeEntity` - 공통 시간 필드 (createdAt, updatedAt, deletedAt)
- ✅ `JpaAuditConfig` - JPA Auditing 활성화

### 3. 도메인 엔티티 (User 관련)
- ✅ `User` - 사용자 엔티티
- ✅ `UserOauthAccount` - OAuth 계정 엔티티
- ✅ `Role` - 사용자 권한 Enum (USER, ADMIN)
- ✅ `Provider` - OAuth 제공자 Enum (KAKAO, GOOGLE)

### 4. 배포 가이드
- ✅ `DEPLOYMENT_GUIDE.md` - 통합 배포 가이드
- ✅ `scripts/ec2-init-setup.sh` - EC2 초기 설정 자동화 스크립트

---

## 🚧 다음에 해야 할 작업 (애플리케이션 개발)

### Phase 1: 도메인 엔티티 완성
- [ ] `Album` 엔티티 생성
- [ ] `Music` 엔티티 생성

### Phase 2: Repository 구현
- [ ] `UserRepository` 인터페이스
- [ ] `UserOauthAccountRepository` 인터페이스
- [ ] `AlbumRepository` 인터페이스
- [ ] `MusicRepository` 인터페이스

### Phase 3: Service 계층 구현
- [ ] `UserService` - 사용자 관련 비즈니스 로직
- [ ] `AuthService` - 인증/인가 로직 (OAuth)
- [ ] `AlbumService` - 앨범 관리 로직
- [ ] `MusicService` - 수록곡 관리 로직

### Phase 4: Controller & DTO 구현
- [ ] `AuthController` - 로그인/회원가입 API
- [ ] `UserController` - 사용자 정보 API
- [ ] `AlbumController` - 앨범 CRUD API
- [ ] `MusicController` - 수록곡 CRUD API
- [ ] 각 Controller에 맞는 Request/Response DTO

### Phase 5: 인증/인가 구현
- [ ] OAuth 2.0 (카카오, 구글) 연동
- [ ] JWT 토큰 발급/검증
- [ ] Spring Security 설정

### Phase 6: 예외 처리
- [ ] 전역 예외 처리 (`@ControllerAdvice`)
- [ ] 커스텀 예외 클래스
- [ ] 에러 응답 DTO

### Phase 7: 테스트
- [ ] 단위 테스트 작성
- [ ] 통합 테스트 작성

---

## 📋 배포 가이드 사용 시점

**`docs/DEPLOYMENT_GUIDE.md`는 다음 상황에서 사용하세요:**

### ✅ 지금 당장 사용할 것
- **아니요!** 아직 애플리케이션이 완성되지 않았습니다.

### ✅ 나중에 사용할 것 (애플리케이션 완성 후)
1. **로컬에서 개발 완료** 후
2. **JAR 파일 빌드** 후
3. **EC2에 배포**할 때

---

## 🎯 지금 해야 할 일

### 1단계: 나머지 엔티티 생성
```
Album 엔티티
Music 엔티티
```

### 2단계: Repository 인터페이스 생성
```
각 엔티티별 JpaRepository 인터페이스
```

### 3단계: Service 계층 구현
```
비즈니스 로직 구현
```

### 4단계: Controller & DTO 구현
```
REST API 엔드포인트 구현
```

### 5단계: 인증/인가 구현
```
OAuth, JWT 등
```

### 6단계: 테스트 및 배포
```
로컬 테스트 → EC2 배포
```

---

## 💡 요약

**현재 상태:**
- ✅ 기초 설정 완료 (환경 설정, 공통 모듈, User 엔티티)
- ❌ 애플리케이션 로직 미구현 (Service, Controller, DTO 등)

**다음 단계:**
1. **애플리케이션 개발** (Album, Music 엔티티부터 시작)
2. **로컬에서 테스트**
3. **배포 가이드 참고하여 EC2에 배포**

**배포 가이드는 나중에 사용하세요!** 지금은 애플리케이션 개발에 집중하시면 됩니다. 🚀

