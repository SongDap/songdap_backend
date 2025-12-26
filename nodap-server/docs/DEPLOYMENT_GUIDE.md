# NoDap 서버 배포 가이드

> **최종 업데이트**: 2025-12-26  
> **상태**: Part 1~3 (인프라 설정) 완료 ✅

## 📋 현재 인프라 환경

- **EC2**: AWS EC2 t3.small (Ubuntu 24.04 LTS)
- **퍼블릭 IP**: `13.209.40.98`
- **SSH 키 파일**: `NoDap-Server-pem.pem`
- **설치된 소프트웨어**:
  - Java 21 (OpenJDK)
  - MySQL 8.0 Server (EC2 내부 설치) ✅ **설정 완료**
  - Redis Server (EC2 내부 설치) ✅ **설정 완료**
- **보안 그룹**: SSH(22), HTTP/HTTPS(80,443), MySQL(3306), Redis(6379)
- **사용자**: `ubuntu`

### ✅ 초기 설정 완료 현황
- [x] MySQL 데이터베이스 `nodap_db` 생성 완료 ✅ (2025-12-25)
- [x] MySQL 사용자 `nodap_admin` 생성 및 권한 부여 완료 ✅ (2025-12-25)
- [x] Redis 서비스 실행 중 확인 완료 ✅ (2025-12-25)
- [x] 애플리케이션 디렉토리 구조 생성 완료 ✅ (2025-12-26)
- [x] 환경 변수 파일(`config/.env`) 생성 및 보안 설정 완료 ✅ (2025-12-26)
- [x] systemd 서비스 파일 생성 및 활성화 완료 ✅ (2025-12-26)

---

## 🚀 배포 프로세스 개요

1. **초기 설정** (최초 1회): MySQL/Redis 설정, 디렉토리 구조 생성
2. **환경 변수 설정**: 애플리케이션 설정 파일 생성
3. **systemd 서비스 등록**: 자동 시작 및 관리 설정
4. **애플리케이션 배포**: JAR 파일 업로드 및 실행
5. **모니터링 및 유지보수**: 로그 확인, 재배포

---

## 📝 Part 1: 초기 설정 (최초 1회)

### Step 1: MySQL 데이터베이스 설정

#### 1-1. MySQL 접속
```bash
sudo mysql -u root -p
```

#### 1-2. 데이터베이스 및 사용자 생성

✅ **이미 완료됨** (2025-12-25)

실제 생성된 설정:
```sql
-- 데이터베이스 생성 완료
CREATE DATABASE nodap_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 애플리케이션 전용 사용자 생성 완료
CREATE USER 'nodap_admin'@'%' IDENTIFIED BY 'nodapDev0107ssafy';

-- 권한 부여 완료
GRANT ALL PRIVILEGES ON nodap_db.* TO 'nodap_admin'@'%';
FLUSH PRIVILEGES;
```

**현재 설정 정보:**
- **데이터베이스명**: `nodap_db`
- **사용자명**: `nodap_admin`
- **비밀번호**: `nodapDev0107ssafy`
- **권한**: `nodap_db` 데이터베이스에 대한 모든 권한

> ⚠️ **보안 주의**: 비밀번호는 프로덕션 환경에서 변경을 권장합니다.

---

### Step 2: Redis 설정 확인

#### 2-1. Redis 서비스 상태 확인

✅ **이미 완료됨** (2025-12-25)

```bash
sudo systemctl status redis-server
```

**현재 상태:**
- **서비스 상태**: `active (running)` ✅
- **포트**: `6379`
- **바인딩**: `127.0.0.1:6379` (localhost)
- **비밀번호**: 미설정 (선택사항)

#### 2-2. Redis 비밀번호 설정 (보안 강화, 선택사항)
```bash
# Redis 설정 파일 열기
sudo vi /etc/redis/redis.conf

# requirepass 설정 찾아서 주석 해제 및 비밀번호 설정
# requirepass your_redis_password_here

# Redis 재시작
sudo systemctl restart redis-server

# 비밀번호로 접속 테스트
redis-cli -a your_redis_password_here
```

> 💡 **로컬 환경이므로 Redis 비밀번호는 선택사항입니다.** 프로덕션에서는 보안을 위해 설정하는 것을 권장합니다.

---

### Step 3: 애플리케이션 디렉토리 구조 생성

✅ **이미 완료됨** (2025-12-26)

```bash
# 홈 디렉토리로 이동
cd ~

# 애플리케이션 디렉토리 생성 (중괄호 확장이 작동하지 않을 수 있으므로 개별 생성 권장)
mkdir -p nodap-server/logs
mkdir -p nodap-server/backup
mkdir -p nodap-server/config

# 권한 설정
chmod 755 nodap-server
chmod 755 nodap-server/logs
chmod 755 nodap-server/backup
chmod 755 nodap-server/config

# 디렉토리 구조 확인
ls -la nodap-server/
```

**생성되는 디렉토리 구조:**
```
~/nodap-server/
├── config/          # 환경 변수 파일
├── logs/            # 애플리케이션 로그
├── backup/          # JAR 파일 백업
└── nodap-server.jar # 실행 파일 (나중에 업로드)
```

---

## 📝 Part 2: 환경 변수 설정

✅ **완료됨** (2025-12-26)

### Step 4: 환경 변수 파일 생성

#### 4-1. 환경 변수 파일 생성
```bash
cd ~/nodap-server
vi config/.env
```

#### 4-2. 환경 변수 입력

**실제 설정값 (복사해서 사용):**
```bash
# 프로덕션 프로파일
SPRING_PROFILES_ACTIVE=prod

# MySQL 설정 (실제 설정값 반영)
DB_HOST=localhost
DB_PORT=3306
DB_NAME=nodap_db
DB_USERNAME=nodap_admin
DB_PASSWORD=nodapDev0107ssafy

# Redis 설정
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=  # 현재 비밀번호 미설정이므로 빈칸 유지
```

> 📝 **주의**: 위 값들은 실제 EC2 서버에 설정된 값입니다. `.env` 파일에 그대로 복사해서 사용하세요.

#### 4-3. 환경 변수 파일 보안 설정

✅ **이미 완료됨** (2025-12-26)

```bash
chmod 600 config/.env
```

> 🔒 **보안**: `.env` 파일은 소유자만 읽을 수 있도록 설정되었습니다.

---

## 📝 Part 3: systemd 서비스 등록

✅ **완료됨** (2025-12-26)

### Step 5: systemd 서비스 파일 생성

#### 5-1. 서비스 파일 생성
```bash
sudo vi /etc/systemd/system/nodap.service
```

#### 5-2. 서비스 파일 내용
```ini
[Unit]
Description=NoDap Server Application
After=network.target mysql.service redis-server.service

[Service]
Type=simple
User=ubuntu
Group=ubuntu
WorkingDirectory=/home/ubuntu/nodap-server

# 환경 변수 파일 로드
EnvironmentFile=/home/ubuntu/nodap-server/config/.env

# JAR 파일 실행
ExecStart=/usr/bin/java -jar /home/ubuntu/nodap-server/nodap-server.jar

# 재시작 설정
Restart=always
RestartSec=10

# 로그 설정
StandardOutput=journal
StandardError=journal
SyslogIdentifier=nodap

# 리소스 제한
LimitNOFILE=65536

[Install]
WantedBy=multi-user.target
```

#### 5-3. 서비스 활성화

✅ **이미 완료됨** (2025-12-26)

```bash
# systemd 재로드
sudo systemctl daemon-reload

# 서비스 활성화 (부팅 시 자동 시작)
sudo systemctl enable nodap

# 서비스 상태 확인
sudo systemctl status nodap
```

**예상되는 상태:**
- `Loaded: loaded (/etc/systemd/system/nodap.service; enabled; preset: enabled)` ✅
- `Active: inactive (dead)` ✅ **정상 상태** (JAR 파일이 없어서 아직 시작하지 않음)

> 💡 **참고**: `Active: inactive (dead)` 상태는 **정상**입니다. JAR 파일이 없어서 서비스가 시작되지 않은 것이며, 나중에 JAR 파일을 업로드한 후 `sudo systemctl start nodap`으로 시작하면 됩니다.

---

## 📝 Part 4: 방화벽 설정

### Step 6: UFW 방화벽 확인

```bash
# UFW 상태 확인
sudo ufw status

# 필요한 포트 열기 (이미 열려있을 수 있음)
sudo ufw allow 22/tcp   # SSH
sudo ufw allow 80/tcp    # HTTP
sudo ufw allow 443/tcp   # HTTPS

# UFW 활성화 (비활성화 상태라면)
sudo ufw enable
```

> 💡 **보안 그룹**: AWS 보안 그룹에서도 이미 설정되어 있으므로, UFW는 추가 보안 레이어입니다.

---

## 📝 Part 5: 애플리케이션 배포

### Step 7: JAR 파일 업로드

#### 방법 A: SCP로 업로드 (권장)

**로컬 터미널에서 실행:**
```bash
# 로컬 프로젝트 디렉토리에서
./gradlew build -x test

# JAR 파일 업로드 (실제 SSH 키 파일명 사용)
scp -i "NoDap-Server-pem.pem" \
    build/libs/nodap-server-0.0.1-SNAPSHOT.jar \
    ubuntu@13.209.40.98:~/nodap-server/nodap-server.jar
```

> 📝 **SSH 키 파일**: `NoDap-Server-pem.pem` 파일이 로컬 프로젝트 디렉토리 또는 `~/.ssh/` 디렉토리에 있어야 합니다.

#### 방법 B: Git으로 클론 후 빌드 (EC2에서)

```bash
# EC2에서
cd ~/nodap-server
git clone your-repository-url .
./gradlew build -x test
cp build/libs/nodap-server-*.jar nodap-server.jar
```

#### JAR 파일 권한 설정
```bash
chmod +x ~/nodap-server/nodap-server.jar
```

---

### Step 8: 애플리케이션 실행 및 테스트

#### 8-1. 서비스 시작
```bash
sudo systemctl start nodap
```

#### 8-2. 서비스 상태 확인
```bash
sudo systemctl status nodap
```

#### 8-3. 로그 확인
```bash
# 실시간 로그 확인
sudo journalctl -u nodap -f

# 최근 100줄 로그 확인
sudo journalctl -u nodap -n 100

# 특정 시간 이후 로그
sudo journalctl -u nodap --since "10 minutes ago"
```

#### 8-4. 애플리케이션 헬스 체크
```bash
# EC2 내부에서 테스트
curl http://localhost:8080

# 외부에서 테스트 (브라우저)
http://13.209.40.98:8080
```

---

## 📝 Part 6: 재배포 프로세스

### 배포 스크립트 사용 (권장)

EC2에 배포 스크립트를 생성:
```bash
vi ~/nodap-server/deploy.sh
```

**배포 스크립트 내용:**
```bash
#!/bin/bash

set -e

APP_DIR="/home/ubuntu/nodap-server"
JAR_FILE="${APP_DIR}/nodap-server.jar"
SERVICE_NAME="nodap"

echo "🚀 NoDap 서버 배포를 시작합니다..."

cd ${APP_DIR}

# 기존 서비스 중지
if systemctl is-active --quiet ${SERVICE_NAME} 2>/dev/null; then
    echo "📦 기존 서비스를 중지합니다..."
    sudo systemctl stop ${SERVICE_NAME}
fi

# 기존 JAR 파일 백업
if [ -f "${JAR_FILE}" ]; then
    echo "💾 기존 JAR 파일을 백업합니다..."
    cp ${JAR_FILE} ${APP_DIR}/backup/nodap-server.jar.backup.$(date +%Y%m%d_%H%M%S)
fi

# 새 JAR 파일 확인
NEW_JAR=$(ls -t ${APP_DIR}/nodap-server-*.jar 2>/dev/null | head -1)
if [ -z "${NEW_JAR}" ]; then
    echo "❌ 새로운 JAR 파일을 찾을 수 없습니다!"
    exit 1
fi

echo "📦 새 JAR 파일: ${NEW_JAR}"

# JAR 파일 교체
cp ${NEW_JAR} ${JAR_FILE}
chmod +x ${JAR_FILE}

# 서비스 시작
echo "🔄 서비스를 재시작합니다..."
sudo systemctl start ${SERVICE_NAME}
sleep 3

# 상태 확인
sudo systemctl status ${SERVICE_NAME} --no-pager

echo "✅ 배포가 완료되었습니다!"
echo "📊 로그 확인: sudo journalctl -u ${SERVICE_NAME} -f"
```

**스크립트 실행 권한 부여:**
```bash
chmod +x ~/nodap-server/deploy.sh
```

**재배포 시 사용:**
```bash
# 1. 로컬에서 새 JAR 파일 업로드 (실제 SSH 키 파일명 사용)
scp -i "NoDap-Server-pem.pem" \
    build/libs/nodap-server-*.jar \
    ubuntu@13.209.40.98:~/nodap-server/

# 2. EC2에서 배포 스크립트 실행
ssh -i "NoDap-Server-pem.pem" ubuntu@13.209.40.98
cd ~/nodap-server
./deploy.sh
```

---

## 🚨 문제 해결

### MySQL 연결 실패
```bash
# MySQL 서비스 상태 확인
sudo systemctl status mysql

# MySQL 로그 확인
sudo tail -f /var/log/mysql/error.log

# MySQL 접속 테스트 (실제 사용자명 사용)
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

### 애플리케이션 실행 실패
```bash
# 상세 로그 확인
sudo journalctl -u nodap -n 200 --no-pager

# 환경 변수 확인
sudo systemctl show nodap | grep Environment

# Java 버전 확인
java -version

# 포트 사용 확인
sudo netstat -tlnp | grep 8080
```

### 서비스가 시작되지 않을 때
```bash
# 서비스 상태 확인
sudo systemctl status nodap

# 서비스 재시작
sudo systemctl restart nodap

# 서비스 비활성화 후 다시 활성화
sudo systemctl disable nodap
sudo systemctl enable nodap
sudo systemctl start nodap
```

---

## ✅ 배포 체크리스트

### 초기 설정
- [x] MySQL 데이터베이스 `nodap_db` 생성 완료 ✅ (2025-12-25)
- [x] MySQL 사용자 `nodap_admin` 생성 및 권한 부여 완료 ✅ (2025-12-25)
- [x] Redis 서비스 실행 중 확인 완료 ✅ (2025-12-25)
- [x] 애플리케이션 디렉토리 구조 생성 완료 ✅ (2025-12-26)
- [x] 환경 변수 파일(`config/.env`) 생성 및 보안 설정 완료 ✅ (2025-12-26)
- [x] systemd 서비스 파일 생성 및 활성화 완료 ✅ (2025-12-26)
- [ ] UFW 방화벽 설정 확인 완료 (선택사항)

### 배포
- [ ] 로컬에서 JAR 파일 빌드 완료
- [ ] EC2로 JAR 파일 업로드 완료
- [ ] JAR 파일 실행 권한 설정 완료
- [ ] 서비스 시작 및 상태 확인 완료
- [ ] 애플리케이션 로그 확인 완료
- [ ] 헬스 체크 통과 확인 완료

---

## 🔐 보안 권장사항

### 1. 환경 변수 파일 보안
- `.env` 파일은 `chmod 600`으로 설정하여 소유자만 읽을 수 있도록 함
- Git에 커밋하지 않도록 `.gitignore`에 추가됨

### 2. MySQL 보안
- 애플리케이션 전용 사용자 생성 (root 사용 금지)
- 강력한 비밀번호 사용
- 필요한 호스트에서만 접근 허용

### 3. Redis 보안
- 프로덕션 환경에서는 비밀번호 설정 권장
- 외부 접속이 필요하지 않다면 localhost만 허용

### 4. 방화벽
- AWS 보안 그룹과 UFW 이중 방어
- 필요한 포트만 열기

---

## 📊 유용한 명령어 모음

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

# 특정 시간 이후
sudo journalctl -u nodap --since "2024-01-01 00:00:00"

# 에러만 확인
sudo journalctl -u nodap -p err
```

### 시스템 모니터링
```bash
# CPU, 메모리 사용량
top
# 또는
htop

# 디스크 사용량
df -h

# 네트워크 연결 확인
sudo netstat -tlnp

# Java 프로세스 확인
ps aux | grep java
```

---

## 📝 다음 단계 (선택사항)

1. **도메인 연결**: Route 53 또는 다른 DNS 서비스 사용
2. **SSL 인증서 설정**: Let's Encrypt를 사용한 HTTPS 설정
3. **로드 밸런서**: 트래픽 증가 시 ALB 설정
4. **모니터링**: CloudWatch 또는 다른 모니터링 도구 연동
5. **백업 자동화**: 데이터베이스 자동 백업 스크립트 설정

---

## 📞 참고 자료

- [Spring Boot 공식 문서](https://spring.io/projects/spring-boot)
- [systemd 서비스 관리](https://www.freedesktop.org/software/systemd/man/systemd.service.html)
- [MySQL 8.0 문서](https://dev.mysql.com/doc/refman/8.0/en/)
- [Redis 문서](https://redis.io/documentation)

