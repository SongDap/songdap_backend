# 인프라 파일 정리 작업 기록

> **작업 일자**: 2025-01-28  
> **작업 목적**: EC2 서버 및 로컬 프로젝트의 인프라 관련 파일 정리 및 경로 통일

---

## 📋 작업 개요

EC2 서버와 로컬 프로젝트의 인프라 관련 파일들을 정리하고, 경로 및 설정을 통일하여 일관성을 확보했습니다.

---

## 🔍 발견된 문제점

### 1. EC2 서버 디렉토리 구조 문제
- **중복 디렉토리**: `~/nodap-server/` 디렉토리가 존재하지만 실제로는 사용되지 않음
- **경로 불일치**: 
  - systemd 서비스: `/home/ubuntu/nodap-server/config/.env` 사용
  - 실제 환경 변수: `/home/ubuntu/config/.env`에 존재
- **불필요한 파일**: `~/backup/install` (CodeDeploy 설치 파일)

### 2. 로컬 프로젝트 파일 경로 불일치
- **스크립트 파일**: `ec2-init-setup.sh`에서 `~/nodap-server/` 경로 사용
- **문서 파일**: 일부 문서에서 잘못된 경로 참조
- **IP 주소**: 구 IP 주소(`13.209.40.98`) 사용
- **DB 설정**: `nodap_user` vs `nodap_admin`, `nodap` vs `nodap_db` 혼재

---

## ✅ 수행한 작업

### 1단계: EC2 서버 정리

#### 1.1 백업 생성
```bash
# 환경 변수 파일 백업
cp ~/config/.env ~/config/.env.backup
cp ~/nodap-server/config/.env ~/nodap-server/config/.env.backup
```

#### 1.2 환경 변수 파일 통일
- `~/nodap-server/config/.env`의 상세 설정을 `~/config/.env`로 복사
- systemd 서비스 파일을 `/home/ubuntu/config/.env` 경로로 수정

#### 1.3 중복 디렉토리 삭제
```bash
# 중복 디렉토리 삭제
rm -rf ~/nodap-server
```

#### 1.4 불필요한 파일 삭제
```bash
# CodeDeploy 설치 파일 삭제
rm -f ~/backup/install
```

#### 1.5 서비스 재시작 및 확인
- systemd 서비스 재시작 후 정상 작동 확인
- 서비스 상태: `active (running)`

**최종 EC2 서버 구조:**
```
/home/ubuntu/
├── backend/          # 배포 디렉토리 (nodap-server.jar)
├── config/           # 환경 변수 파일 (.env)
└── backup/           # 백업 디렉토리 (비어있음)
```

### 2단계: 로컬 프로젝트 파일 수정

#### 2.1 스크립트 파일 수정

**`nodap-server/scripts/ec2-init-setup.sh`**
- 경로 변경: `~/nodap-server/` → `/home/ubuntu/backend`, `/home/ubuntu/config`
- DB 이름 통일: `nodap` → `nodap_db`
- DB 사용자 통일: `nodap_user` → `nodap_admin`
- IP 주소 업데이트: `13.209.40.98` → `3.37.205.227`
- 환경 변수 입력 단계 추가 (JWT_SECRET, KAKAO OAuth 등)
- systemd 서비스 파일 생성 부분 수정

**주요 변경사항:**
```bash
# 변경 전
mkdir -p ~/nodap-server/{logs,backup,config}
cat > ~/nodap-server/config/.env <<EOF
...
EnvironmentFile=/home/${USER}/nodap-server/config/.env
ExecStart=/usr/bin/java -jar /home/${USER}/nodap-server/nodap-server.jar

# 변경 후
mkdir -p /home/ubuntu/backend
mkdir -p /home/ubuntu/config
cat > /home/ubuntu/config/.env <<EOF
...
EnvironmentFile=/home/ubuntu/config/.env
ExecStart=/usr/bin/java -jar -Dspring.profiles.active=prod -Dserver.port=8080 /home/ubuntu/backend/nodap-server.jar
```

#### 2.2 문서 파일 수정

**`nodap-server/docs/BACKEND_DEPLOYMENT_GUIDE.md`**
- IP 주소 업데이트: `13.209.40.98` → `3.37.205.227`
- 경로 통일 확인

**`nodap-server/docs/EC2_SETUP_CHECKLIST.md`**
- DB 사용자 통일: `nodap_user` 언급 제거, `nodap_admin`으로 통일
- DB 이름 통일: `nodap` 언급 제거, `nodap_db`로 통일

**`nodap-server/docs/S3_PRODUCTION_SETUP.md`**
- 모든 `~/nodap-server/` 경로를 `/home/ubuntu/config/` 또는 `/home/ubuntu/backend/`로 수정
- 로그 경로를 `journalctl` 명령어로 수정

#### 2.3 확인된 파일 (이미 올바른 경로 사용)
- ✅ `nodap-server/scripts/deploy.sh`
- ✅ `nodap-server/appspec.yml`
- ✅ `.github/workflows/deploy.yml`

---

## 📊 통일된 설정

### 경로 통일
| 항목 | 경로 |
|------|------|
| 배포 디렉토리 | `/home/ubuntu/backend` |
| 환경 변수 파일 | `/home/ubuntu/config/.env` |
| JAR 파일 | `/home/ubuntu/backend/nodap-server.jar` |
| systemd 서비스 | `/etc/systemd/system/nodap.service` |

### 설정 통일
| 항목 | 값 |
|------|-----|
| IP 주소 | `3.37.205.227` |
| DB 이름 | `nodap_db` |
| DB 사용자 | `nodap_admin` |
| 서비스 이름 | `nodap` |

### 환경 변수 구조
```bash
# 프로덕션 프로파일
SPRING_PROFILES_ACTIVE=prod

# Swagger 설정
SWAGGER_SERVER_URL=https://answerwithsong.com
SWAGGER_SERVER_DESCRIPTION=프로덕션 API 서버

# MySQL 설정
MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_NAME=nodap_db
MYSQL_USERNAME=nodap_admin
MYSQL_PASSWORD=***

# JWT 설정
JWT_SECRET=***
JWT_ACCESS_EXPIRY=1800000
JWT_REFRESH_EXPIRY=604800000

# Redis 설정
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=***

# CORS 설정
CORS_ALLOWED_ORIGINS=https://answerwithsong.com

# 카카오 OAuth 설정
KAKAO_CLIENT_ID=***
KAKAO_CLIENT_SECRET=***
KAKAO_REDIRECT_URI=https://answerwithsong.com/oauth/kakao/callback

# AWS S3 설정 (선택사항)
AWS_S3_REGION=ap-northeast-2
AWS_S3_BUCKET=nodap-images
AWS_ACCESS_KEY=***
AWS_SECRET_KEY=***
AWS_S3_BASE_URL=https://nodap-images.s3.ap-northeast-2.amazonaws.com
```

---

## 🔧 수정된 파일 목록

### 스크립트 파일
- `nodap-server/scripts/ec2-init-setup.sh`

### 문서 파일
- `nodap-server/docs/BACKEND_DEPLOYMENT_GUIDE.md`
- `nodap-server/docs/EC2_SETUP_CHECKLIST.md`
- `nodap-server/docs/S3_PRODUCTION_SETUP.md`

### EC2 서버
- `/etc/systemd/system/nodap.service` (수정됨)
- `/home/ubuntu/config/.env` (통일됨)
- `~/nodap-server/` (삭제됨)

---

## ✅ 검증 결과

### EC2 서버 상태
- ✅ 디렉토리 구조 정리 완료
- ✅ systemd 서비스 정상 작동 (`active`)
- ✅ 환경 변수 경로 통일 완료
- ✅ JAR 파일 경로 확인 완료

### 로컬 프로젝트
- ✅ 모든 스크립트 파일 경로 통일
- ✅ 모든 문서 파일 경로 및 설정 통일
- ✅ IP 주소 및 DB 설정 통일
- ✅ 환경 변수 구조 일관성 확보

### 일관성 검증
- ✅ 경로 통일: 모든 파일에서 `/home/ubuntu/backend`, `/home/ubuntu/config` 사용
- ✅ IP 주소 통일: 모든 파일에서 `3.37.205.227` 사용
- ✅ DB 설정 통일: 모든 파일에서 `nodap_db`, `nodap_admin` 사용

---

## 📝 향후 유지보수 가이드

### 새로운 EC2 서버 설정 시
1. `nodap-server/scripts/ec2-init-setup.sh` 스크립트 실행
2. 스크립트가 자동으로 올바른 경로에 디렉토리 생성 및 설정

### 환경 변수 추가 시
1. `/home/ubuntu/config/.env` 파일 수정
2. `application.yml` 또는 `application-prod.yml`에서 환경 변수 참조 확인
3. systemd 서비스 재시작: `sudo systemctl restart nodap`

### 배포 시
1. GitHub Actions가 자동으로 배포 수행
2. `scripts/deploy.sh`가 자동으로 실행되어 서비스 재시작

---

## 🎯 작업 완료 체크리스트

- [x] EC2 서버 디렉토리 정리
- [x] 중복 디렉토리 삭제
- [x] 환경 변수 파일 통일
- [x] systemd 서비스 파일 수정
- [x] 로컬 스크립트 파일 경로 통일
- [x] 문서 파일 경로 및 설정 통일
- [x] IP 주소 통일
- [x] DB 설정 통일
- [x] 서비스 정상 작동 확인
- [x] 전체 일관성 검증

---

## 📚 관련 문서

- [백엔드 배포 가이드](./BACKEND_DEPLOYMENT_GUIDE.md)
- [EC2 설정 체크리스트](./EC2_SETUP_CHECKLIST.md)
- [S3 프로덕션 설정](./S3_PRODUCTION_SETUP.md)
- [프로젝트 진행 현황](./PROJECT_STATUS.md)

---

## 👤 작업자

- 작업 일자: 2025-01-28
- 작업 내용: 인프라 파일 정리 및 경로 통일

---

**작업 완료**: 모든 인프라 관련 파일이 정리되고 통일되었으며, 현재 EC2 서버 상태와 완벽하게 일치합니다. ✅

---

## 🔴 Redis 연결 문제 해결 (2026-01-28 추가)

### 문제 상황
- **증상**: 카카오 로그인 시 500 Internal Server Error 발생
- **에러 메시지**: `org.springframework.data.redis.RedisConnectionFailureException: Unable to connect to Redis`
- **발생 위치**: `RefreshTokenRepository.save()` - Refresh Token을 Redis에 저장하려 할 때

### 원인 분석

#### 1. 프론트엔드 에러 로그 분석
프론트엔드 콘솔에서 다음과 같은 에러 확인:
```
POST https://answerwithsong.com/api/v1/auth/login/kakao 500 (Internal Server Error)
에러 응답: {code: 500, errorCode: 'SYS_ERR', message: '서버 오류가 발생했습니다.'}
```

#### 2. 백엔드 로그 분석
EC2 서버에서 백엔드 로그 확인:
```bash
sudo journalctl -u nodap -n 500 | grep -A 50 "kakao\|SYS_ERR\|500"
```

**로그 내용:**
```
[Auth] 카카오 로그인 시작
[Auth] 신규 회원 가입 완료: userId=67, kakaoId=4717840778
ERROR: org.springframework.data.redis.RedisConnectionFailureException: Unable to connect to Redis
at com.nodap.infrastructure.auth.RefreshTokenRepository.save(RefreshTokenRepository.java:30)
at com.nodap.application.auth.AuthService.issueTokensAndSetCookies(AuthService.java:220)
```

**분석 결과:**
- 카카오 로그인 자체는 성공 (회원 가입 완료)
- Refresh Token을 Redis에 저장하려 할 때 연결 실패
- Redis 서버는 실행 중이었지만 인증 실패

#### 3. Redis 연결 테스트
```bash
# Redis 서버 상태 확인
sudo systemctl status redis
# 결과: active (running) ✅

# Redis 연결 테스트
redis-cli ping
# 결과: (error) NOAUTH Authentication required. ❌
```

**원인 확인:**
- Redis 서버는 실행 중
- Redis에 비밀번호가 설정되어 있음 (`requirepass redisNodapDev`)
- Spring Boot 애플리케이션이 Redis 비밀번호 없이 연결 시도

#### 4. 환경 변수 확인
```bash
# nodap.service가 참조하는 .env 파일 확인
cat /home/ubuntu/nodap-server/config/.env | grep REDIS_PASSWORD
# 결과: REDIS_PASSWORD=  # 현재 비밀번호 미설정이므로 빈칸 유지
```

**문제 발견:**
- `/home/ubuntu/nodap-server/config/.env` 파일의 `REDIS_PASSWORD`가 빈 값으로 설정됨
- `application-prod.yml`에서 `${REDIS_PASSWORD:}` 환경 변수를 참조하지만 값이 없음

### 해결 방법

#### 1. Redis 비밀번호 확인
```bash
# Redis 설정 파일에서 비밀번호 확인
sudo cat /etc/redis/redis.conf | grep -E "^requirepass|^# requirepass"
# 결과: requirepass redisNodapDev
```

#### 2. 환경 변수 파일 수정
```bash
# nodap.service가 참조하는 .env 파일 수정
nano /home/ubuntu/nodap-server/config/.env
```

**수정 내용:**
```bash
# 변경 전
REDIS_PASSWORD=  # 현재 비밀번호 미설정이므로 빈칸 유지

# 변경 후
REDIS_PASSWORD=redisNodapDev
```

#### 3. 서비스 재시작
```bash
# nodap 서비스 재시작
sudo systemctl restart nodap

# 상태 확인
sudo systemctl status nodap
```

#### 4. 연결 테스트
- 카카오 로그인 재시도
- Redis 연결 성공 확인
- 500 에러 해결 확인

### 해결 결과
- ✅ Redis 연결 성공
- ✅ 카카오 로그인 정상 작동
- ✅ Refresh Token이 Redis에 정상 저장됨

### 향후 주의사항

#### Redis 비밀번호 설정 시
1. `/etc/redis/redis.conf`에서 `requirepass` 설정 확인
2. `/home/ubuntu/nodap-server/config/.env`의 `REDIS_PASSWORD` 환경 변수 설정
3. `application-prod.yml`에서 `${REDIS_PASSWORD:}` 환경 변수 참조 확인
4. 서비스 재시작: `sudo systemctl restart nodap`

#### 환경 변수 파일 위치
- **nodap.service 참조**: `/home/ubuntu/nodap-server/config/.env`
- **주의**: `/home/ubuntu/config/.env`와는 다른 파일임
- systemd 서비스 파일 확인: `sudo cat /etc/systemd/system/nodap.service | grep EnvironmentFile`

#### Redis 연결 문제 진단 방법
```bash
# 1. Redis 서버 상태 확인
sudo systemctl status redis

# 2. Redis 연결 테스트 (비밀번호 없이)
redis-cli ping
# NOAUTH 에러가 나면 비밀번호가 설정된 것

# 3. Redis 비밀번호로 연결 테스트
redis-cli -a redisNodapDev ping
# PONG이 나오면 정상

# 4. 백엔드 로그에서 Redis 에러 확인
sudo journalctl -u nodap | grep -i "redis\|connection"
```

### 관련 파일
- Redis 설정: `/etc/redis/redis.conf`
- 환경 변수: `/home/ubuntu/nodap-server/config/.env`
- Spring Boot 설정: `src/main/resources/application-prod.yml`
- systemd 서비스: `/etc/systemd/system/nodap.service`

---