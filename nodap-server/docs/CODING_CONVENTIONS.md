# NoDap 코딩 컨벤션

## 📋 규칙 목록

### 1. Setter 금지 (무결성 보장)
- **모든 엔티티와 DTO에서 `@Setter` 사용 금지**
- 객체 생성 시 `@Builder` 패턴 또는 생성자 사용
- 상태 변경이 필요한 경우 의미 있는 메서드명으로 정의
  - ❌ `setNickname()`
  - ✅ `updateNickname()`
- `@ConfigurationProperties`는 생성자 바인딩 또는 `record` 사용

```java
// ❌ BAD
@Setter
public class User {
    private String nickname;
}

// ✅ GOOD
public class User {
    private String nickname;
    
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }
}
```

---

### 2. Controller 경량화
- **Controller는 요청/응답 처리만 담당**
- 비즈니스 로직은 모두 Service 계층에서 처리
- Validation, 로깅, 에러 처리는 공통 모듈 활용

```java
// ❌ BAD - Controller에 로직이 있음
@PostMapping("/login")
public ResponseEntity<?> login(@RequestBody LoginRequest request) {
    if (request.getCode() == null) {
        throw new BusinessException(ErrorCode.AUTH_INVALID_CODE);
    }
    // 여러 로직...
}

// ✅ GOOD - Service에 위임
@PostMapping("/login")
public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
    return ResponseEntity.ok(authService.login(request));
}
```

---

### 3. Swagger 문서화 (꼼꼼하게)
- **모든 API에 `@Operation` 어노테이션 필수**
- `summary`: 간단한 API 설명
- `description`: 상세 설명 (필요시)
- `@ApiResponse`: 응답 코드별 설명
- `@Parameter`: 파라미터 설명
- `@Schema`: DTO 필드 설명

```java
@Operation(
    summary = "카카오 로그인",
    description = "카카오 인가 코드를 이용한 로그인/회원가입"
)
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "로그인 성공"),
    @ApiResponse(responseCode = "401", description = "인가 코드 만료")
})
@PostMapping("/login/kakao")
public ResponseEntity<?> loginWithKakao(...) { }
```

---

### 4. Service 로그 형식
- **에러 로그 형식: `[Error-XXX] 에러내용`**
- XXX는 ErrorCode의 코드 사용
- 중요한 비즈니스 로직에는 INFO 로그 추가

```java
// ✅ GOOD
log.error("[Error-AUTH_002] 유효하지 않은 인가 코드: code={}", code);
log.info("[Auth] 사용자 로그인 성공: userId={}", userId);
log.warn("[Auth] 토큰 재발급 시도: userId={}", userId);
```

---

## 📝 추가 컨벤션

### 패키지 구조
```
com.nodap/
├── application/     # 애플리케이션 서비스 (UseCase)
├── domain/          # 도메인 (Entity, Repository, Domain Service)
├── global/          # 전역 설정, 공통 모듈
├── infrastructure/  # 인프라 (외부 API, 인증 등)
└── interfaces/      # 인터페이스 (Controller, DTO)
```

### 네이밍 규칙
- **Controller**: `XxxController`
- **Service**: `XxxService`
- **Repository**: `XxxRepository`
- **DTO**: `XxxRequest`, `XxxResponse`
- **Exception**: `XxxException`

### 응답 형식
```json
{
  "code": 200,
  "message": "성공 메시지",
  "data": { ... }
}
```

### 에러 응답 형식
```json
{
  "code": 401,
  "errorCode": "AUTH_002",
  "message": "유효하지 않은 인가 코드입니다.",
  "timestamp": "2025-12-28T12:00:00"
}
```

---

## 🔄 변경 이력
- 2025.12.28: 초안 작성



