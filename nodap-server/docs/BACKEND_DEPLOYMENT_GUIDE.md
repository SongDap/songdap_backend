# SongDap 백엔드 배포 가이드

> **최종 업데이트**: 2025-12-28  
> **상태**: 배포 설정 완료 ✅

## 📋 배포 환경 개요

- **프레임워크**: Spring Boot 4.0.1 (Java 21)
- **배포 방식**: systemd 서비스 + GitHub Actions CI/CD
- **데이터베이스**: MySQL 8.0 (EC2 내부 설치)
- **캐시**: Redis (EC2 내부 설치)
- **EC2**: AWS EC2 t3.small (Ubuntu 24.04 LTS)
- **퍼블릭 IP**: `13.209.40.98`

### 🏗️ 아키텍처

```
                        ┌─────────────────────────────────────────────┐
                        │              AWS EC2 서버                    │
                        │           (13.209.40.98)                    │
   인터넷               │                                              │
   사용자  ───────────▶ │  ┌─────────────────────────────────────┐    │
     │                  │  │            Nginx (:80)               │    │
     │                  │  │  ┌─────────────┐  ┌──────────────┐  │    │
     └──────────────────┼──┼─▶│  정적 파일   │  │   /api/*     │──┼────┤
                        │  │  │ (Frontend)  │  │   프록시      │  │    │
                        │  │  └─────────────┘  └──────┬───────┘  │    │
                        │  └──────────────────────────┼──────────┘    │
                        │                             │               │
                        │                             ▼               │
                        │           ┌─────────────────────────┐       │
                        │           │   Spring Boot (:8080)   │       │
                        │           │   (nodap.service)       │       │
                        │           └───────────┬─────────────┘       │
                        │                       │                     │
                        │          ┌────────────┴────────────┐        │
                        │          ▼                         ▼        │
                        │  ┌──────────────┐         ┌──────────────┐  │
                        │  │ MySQL (:3306)│         │ Redis (:6379)│  │
                        │  └──────────────┘         └──────────────┘  │
                        └─────────────────────────────────────────────┘
```

### 🔄 CI/CD 파이프라인

```
 GitHub Push       GitHub Actions         AWS S3           CodeDeploy          EC2
     │                   │                   │                  │               │
     │   main 브랜치     │                   │                  │               │
     └──────────────────▶│                   │                  │               │
                         │  Build (Gradle)   │                  │               │
                         │──────────────────▶│                  │               │
                         │  Upload ZIP       │                  │               │
                         │──────────────────▶│                  │               │
                         │                   │  Create Deploy   │               │
                         │───────────────────┼─────────────────▶│               │
                         │                   │                  │  Pull & Run   │
                         │                   │                  │──────────────▶│
                         │                   │                  │               │
                         │                   │                  │  deploy.sh    │
                         │                   │                  │──────────────▶│
                         │                   │                  │               │
                         │                   │                  │  systemctl    │
                         │                   │                  │  restart      │
                         │                   │                  │──────────────▶│
```

---

## 🚀 배포 프로세스 개요

1. **EC2 초기 설정** (최초 1회): Java, MySQL, Redis 설치
2. **환경 변수 설정**: `.env` 파일 생성
3. **systemd 서비스 등록**: 자동 시작 및 관리 설정
4. **GitHub Push**: 자동 CI/CD 배포

---

## 📝 Part 1: EC2 초기 설정 (최초 1회)

### Step 1: 시스템 업데이트 및 Java 설치

```bash
# 시스템 업데이트
sudo apt update && sudo apt upgrade -y

# Java 21 설치
sudo apt install -y openjdk-21-jdk

# 버전 확인
java -version  # openjdk 21.x.x
```

### Step 2: MySQL 설치 및 설정

#### 2-1. MySQL 설치

```bash
# MySQL 서버 설치
sudo apt install -y mysql-server

# 서비스 시작 및 활성화
sudo systemctl start mysql
sudo systemctl enable mysql
```

#### 2-2. 데이터베이스 및 사용자 생성

```bash
# MySQL 접속
sudo mysql -u root -p
```

```sql
-- 데이터베이스 생성
CREATE DATABASE nodap_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 애플리케이션 전용 사용자 생성
CREATE USER 'nodap_admin'@'%' IDENTIFIED BY 'your_password_here';

-- 권한 부여
GRANT ALL PRIVILEGES ON nodap_db.* TO 'nodap_admin'@'%';
FLUSH PRIVILEGES;

-- 확인
SHOW DATABASES;
EXIT;
```

### Step 3: Redis 설치

```bash
# Redis 서버 설치
sudo apt install -y redis-server

# 서비스 시작 및 활성화
sudo systemctl start redis-server
sudo systemctl enable redis-server

# Redis 비밀번호 설정 (권장)
sudo vi /etc/redis/redis.conf
# requirepass your_redis_password 주석 해제 후 비밀번호 설정

# Redis 외부 접속 허용 (선택)
# bind 127.0.0.1 → bind 0.0.0.0

# Redis 재시작
sudo systemctl restart redis-server
```

### Step 4: 디렉토리 구조 생성

```bash
# 애플리케이션 디렉토리
mkdir -p /home/ubuntu/backend
mkdir -p /home/ubuntu/config

# 권한 설정
chmod 755 /home/ubuntu/backend
chmod 700 /home/ubuntu/config
```

### Step 5: CodeDeploy Agent 설치

```bash
# CodeDeploy Agent 설치
sudo apt install -y ruby-full wget

cd /home/ubuntu
wget https://aws-codedeploy-ap-northeast-2.s3.ap-northeast-2.amazonaws.com/latest/install
chmod +x ./install
sudo ./install auto

# 서비스 시작
sudo systemctl start codedeploy-agent
sudo systemctl enable codedeploy-agent
```

---

## 📝 Part 2: 환경 변수 설정

### Step 6: 환경 변수 파일 생성

```bash
# 환경 변수 파일 생성
nano /home/ubuntu/config/.env
```

**환경 변수 내용:**

```bash
# MySQL 설정
MYSQL_USERNAME=nodap_admin
MYSQL_PASSWORD=your_mysql_password

# Redis 설정
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password

# JPA 설정
JPA_DDL_AUTO=validate
JPA_SHOW_SQL=false

# 로깅 설정
LOG_LEVEL_ROOT=WARN
LOG_LEVEL_APP=INFO
LOG_LEVEL_SQL=WARN
```

**보안 설정:**

```bash
chmod 600 /home/ubuntu/config/.env
```

> 🔒 **보안**: `.env` 파일은 소유자만 읽을 수 있도록 설정합니다.

---

## 📝 Part 3: systemd 서비스 등록

### Step 7: systemd 서비스 파일 생성

```bash
sudo nano /etc/systemd/system/nodap.service
```

**서비스 파일 내용:**

```ini
[Unit]
Description=NoDap Backend Server
After=network.target mysql.service redis.service

[Service]
User=ubuntu
Group=ubuntu
Type=simple

# 환경 변수 파일 로드
EnvironmentFile=/home/ubuntu/config/.env

# Java 실행
ExecStart=/usr/bin/java -jar \
    -Dspring.profiles.active=prod \
    -Dserver.port=8080 \
    /home/ubuntu/backend/nodap-server.jar

# 안전한 종료 (SIGTERM)
ExecStop=/bin/kill -15 $MAINPID
TimeoutStopSec=30

# 실패 시 재시작
Restart=on-failure
RestartSec=10

# 로그
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
```

### Step 8: 서비스 활성화

```bash
# systemd 재로드
sudo systemctl daemon-reload

# 서비스 활성화 (부팅 시 자동 시작)
sudo systemctl enable nodap

# 서비스 상태 확인
sudo systemctl status nodap
```

---

## 📝 Part 4: 자동 배포 (CI/CD)

### GitHub Actions 워크플로우

`.github/workflows/deploy.yml` 파일이 자동 배포를 담당합니다:

1. **main 브랜치에 Push** → GitHub Actions 트리거
2. **Gradle 빌드** → JAR 파일 생성
3. **S3 업로드** → ZIP 파일 업로드
4. **CodeDeploy 실행** → EC2에 배포

### 배포 스크립트

`scripts/deploy.sh`가 EC2에서 실행됩니다:

1. `build/libs/` 폴더에서 JAR 파일 찾기
2. `nodap-server.jar`로 이름 통일
3. `sudo systemctl restart nodap` 실행

### 배포 방법

```bash
# 로컬에서 코드 수정 후
git add .
git commit -m "feat: 새로운 기능 추가"
git push origin main
```

> ✅ GitHub Actions가 자동으로 빌드 및 배포를 진행합니다.

---

## 📊 유용한 명령어

### 서비스 관리

```bash
# 서비스 시작
sudo systemctl start nodap

# 서비스 중지
sudo systemctl stop nodap

# 서비스 재시작
sudo systemctl restart nodap

# 서비스 상태 확인
sudo systemctl status nodap

# 서비스 활성화 (부팅 시 자동 시작)
sudo systemctl enable nodap

# 서비스 비활성화
sudo systemctl disable nodap
```

### 로그 관리

```bash
# 실시간 로그 확인
sudo journalctl -u nodap -f

# 최근 100줄
sudo journalctl -u nodap -n 100

# 오늘 로그
sudo journalctl -u nodap --since today

# 에러만 확인
sudo journalctl -u nodap -p err

# 특정 시간 이후
sudo journalctl -u nodap --since "10 minutes ago"
```

### 시스템 모니터링

```bash
# CPU, 메모리 사용량
top
htop

# 디스크 사용량
df -h

# 네트워크 연결 확인
sudo netstat -tlnp | grep 8080

# Java 프로세스 확인
ps aux | grep java
```

### 데이터베이스

```bash
# MySQL 접속
mysql -u nodap_admin -p nodap_db

# MySQL 상태 확인
sudo systemctl status mysql

# Redis 접속
redis-cli -a your_password

# Redis 상태 확인
sudo systemctl status redis
```

---

## 🚨 문제 해결

### 서비스가 시작되지 않을 때

```bash
# 상세 로그 확인
sudo journalctl -u nodap -n 100 --no-pager

# 환경 변수 확인
cat /home/ubuntu/config/.env

# JAR 파일 존재 확인
ls -l /home/ubuntu/backend/nodap-server.jar

# Java 버전 확인
java -version
```

### MySQL 연결 실패

```bash
# MySQL 서비스 상태 확인
sudo systemctl status mysql

# MySQL 로그 확인
sudo tail -f /var/log/mysql/error.log

# MySQL 접속 테스트
mysql -u nodap_admin -p nodap_db
```

### Redis 연결 실패

```bash
# Redis 서비스 상태 확인
sudo systemctl status redis-server

# Redis 연결 테스트
redis-cli ping
# 또는 비밀번호가 있다면
redis-cli -a your_password ping
```

### 배포 실패

```bash
# CodeDeploy 로그 확인
cat /var/log/aws/codedeploy-agent/codedeploy-agent.log | tail -100

# backend 폴더 확인
ls -la /home/ubuntu/backend/

# JAR 파일 경로 확인
ls -la /home/ubuntu/backend/build/libs/
```

---

## ✅ 배포 체크리스트

### EC2 초기 설정
- [x] Java 21 설치 ✅
- [x] MySQL 설치 및 데이터베이스 생성 ✅
- [x] Redis 설치 및 비밀번호 설정 ✅
- [x] CodeDeploy Agent 설치 ✅
- [x] 디렉토리 구조 생성 ✅

### 환경 설정
- [x] `.env` 파일 생성 ✅
- [x] 파일 권한 설정 (600) ✅
- [x] systemd 서비스 파일 생성 ✅
- [x] 서비스 활성화 (enable) ✅

### CI/CD
- [x] GitHub Secrets 설정 ✅
- [x] GitHub Actions 워크플로우 설정 ✅
- [x] appspec.yml 설정 ✅
- [x] deploy.sh 스크립트 설정 ✅

### 확인
- [x] 자동 배포 테스트 ✅
- [x] 서비스 실행 확인 ✅
- [x] API 테스트 ✅

---

## 📁 프로젝트 구조

```
nodap-server/
├── appspec.yml                    # AWS CodeDeploy 설정
├── scripts/
│   ├── deploy.sh                  # 배포 스크립트
│   └── ec2-init-setup.sh          # EC2 초기 설정 스크립트
├── docs/
│   ├── BACKEND_DEPLOYMENT_GUIDE.md  # 이 문서
│   └── PROJECT_STATUS.md
├── src/
│   └── main/
│       ├── java/com/nodap/        # 소스 코드
│       └── resources/
│           ├── application.yml     # 공통 설정
│           ├── application-prod.yml # 프로덕션 설정
│           └── application-local.yml # 로컬 설정
├── build.gradle
└── settings.gradle
```

### EC2 서버 구조

```
/home/ubuntu/
├── backend/                       # 배포 디렉토리
│   ├── nodap-server.jar           # 실행 JAR 파일
│   ├── build/libs/                # 빌드된 JAR 파일들
│   └── scripts/deploy.sh          # 배포 스크립트
└── config/
    └── .env                       # 환경 변수 파일

/etc/systemd/system/
└── nodap.service                  # systemd 서비스 파일
```

---

## 🔐 보안 권장사항

### 1. 환경 변수 파일 보안
- `.env` 파일은 `chmod 600`으로 설정
- Git에 커밋하지 않음 (`.gitignore`에 추가)

### 2. MySQL 보안
- 애플리케이션 전용 사용자 사용 (root 사용 금지)
- 강력한 비밀번호 설정
- 필요한 호스트에서만 접근 허용

### 3. Redis 보안
- 비밀번호 설정 필수
- 외부 접속이 필요하지 않다면 localhost만 허용

### 4. AWS 보안
- AWS 보안 그룹에서 필요한 포트만 허용
- IAM 역할로 최소 권한 부여

---

## 📞 참고 자료

- [Spring Boot 공식 문서](https://spring.io/projects/spring-boot)
- [systemd 서비스 관리](https://www.freedesktop.org/software/systemd/man/systemd.service.html)
- [MySQL 8.0 문서](https://dev.mysql.com/doc/refman/8.0/en/)
- [Redis 문서](https://redis.io/documentation)
- [AWS CodeDeploy](https://docs.aws.amazon.com/codedeploy/)

