# EC2 세팅 확인 체크리스트

> **최종 업데이트**: 2025-01-13  
> **용도**: EC2 서버 세팅 상태 점검 및 문제 진단

## 📋 목차

- [빠른 확인 스크립트](#빠른-확인-스크립트)
- [단계별 확인 가이드](#단계별-확인-가이드)
- [문제 해결 가이드](#문제-해결-가이드)

---

## 🚀 빠른 확인 스크립트

한 번에 모든 항목을 확인하려면 다음 스크립트를 실행하세요:

```bash
#!/bin/bash
echo "=== EC2 세팅 확인 ==="
echo ""
echo "1. Java 버전:"
java -version
echo ""
echo "2. MySQL 상태:"
sudo systemctl status mysql --no-pager | head -3
echo ""
echo "3. Redis 상태:"
sudo systemctl status redis-server --no-pager | head -3
echo ""
echo "4. nodap 서비스 상태:"
sudo systemctl status nodap --no-pager | head -3
echo ""
echo "5. 포트 확인:"
sudo ss -tlnp | grep -E "8080|3306|6379"
echo ""
echo "6. JAR 파일:"
ls -lh /home/ubuntu/backend/nodap-server.jar 2>/dev/null || echo "JAR 파일 없음"
echo ""
echo "7. 환경 변수 파일:"
ls -la /home/ubuntu/config/.env 2>/dev/null || echo ".env 파일 없음"
echo ""
echo "8. CodeDeploy Agent:"
sudo systemctl is-active codedeploy-agent 2>/dev/null || echo "CodeDeploy Agent 없음"
```

---

## 📝 단계별 확인 가이드

### 1단계: 기본 시스템 확인

```bash
# 시스템 정보 확인
uname -a
cat /etc/os-release

# 디스크 사용량 확인
df -h

# 메모리 확인
free -h
```

**예상 결과:**
- OS: Ubuntu 24.04.3 LTS
- 디스크: 여유 공간 확인
- 메모리: 사용량 확인

---

### 2단계: Java 21 설치 확인

```bash
# Java 버전 확인 (21.x.x 여야 함)
java -version

# Java 경로 확인
which java
readlink -f $(which java)
```

**예상 결과:**
- `openjdk version "21.x.x"`
- 경로: `/usr/lib/jvm/java-21-openjdk-amd64/bin/java`

---

### 3단계: MySQL 설치 및 서비스 확인

```bash
# MySQL 서비스 상태 확인
sudo systemctl status mysql

# MySQL 버전 확인
mysql --version

# MySQL 서비스가 실행 중인지 확인
sudo systemctl is-active mysql
```

**예상 결과:**
- `active (running)`
- 버전: MySQL 8.0.x

---

### 4단계: MySQL 데이터베이스 및 사용자 확인

```bash
# MySQL 접속 (root로)
sudo mysql -u root -p
```

**MySQL 내부에서 실행할 명령어:**

```sql
-- 데이터베이스 목록 확인
SHOW DATABASES;
-- nodap_db 또는 nodap 데이터베이스가 있어야 함

-- 사용자 목록 확인
SELECT user, host FROM mysql.user;
-- nodap_admin 또는 nodap_user가 있어야 함

-- 권한 확인
SHOW GRANTS FOR 'nodap_admin'@'%';
-- 또는
SHOW GRANTS FOR 'nodap_user'@'%';

-- 데이터베이스 접속 테스트
USE nodap_db;
-- 또는
USE nodap;

-- 테이블 목록 확인
SHOW TABLES;
-- users, albums, musics 등이 있어야 함

EXIT;
```

**예상 결과:**
- 데이터베이스: `nodap_db` 존재
- 사용자: `nodap_admin@%` 또는 `nodap_user@%` 존재
- 테이블: `users`, `albums`, `musics`, `user_oauth_accounts`, `flyway_schema_history`

---

### 5단계: Redis 설치 및 서비스 확인

```bash
# Redis 서비스 상태 확인
sudo systemctl status redis-server

# Redis 버전 확인
redis-server --version

# Redis 서비스가 실행 중인지 확인
sudo systemctl is-active redis-server
```

**예상 결과:**
- `active (running)`

---

### 6단계: Redis 비밀번호 설정 확인

```bash
# Redis 비밀번호 설정 확인
sudo grep "^requirepass" /etc/redis/redis.conf

# Redis 연결 테스트 (비밀번호 없을 경우)
redis-cli ping

# 만약 비밀번호가 설정되어 있다면
# redis-cli -a "비밀번호" ping
```

**예상 결과:**
- `PONG` - Redis 연결 성공
- `requirepass` 설정 여부 확인

---

### 7단계: 디렉토리 구조 확인

```bash
# 애플리케이션 디렉토리 확인
ls -la /home/ubuntu/backend/

# 환경 변수 디렉토리 확인
ls -la /home/ubuntu/config/

# 디렉토리 권한 확인
ls -ld /home/ubuntu/backend
ls -ld /home/ubuntu/config
```

**예상 결과:**
- `/home/ubuntu/backend/` - JAR 파일이 있어야 함
- `/home/ubuntu/config/` - `.env` 파일이 있어야 함
- 권한: `drwxrwxr-x` (755)

---

### 8단계: 환경 변수 파일(.env) 확인

```bash
# 환경 변수 파일 존재 확인
ls -la /home/ubuntu/config/.env

# 파일 권한 확인 (600이어야 함)
stat -c "%a %n" /home/ubuntu/config/.env

# 환경 변수 내용 확인 (민감 정보 주의!)
cat /home/ubuntu/config/.env
```

**확인할 환경 변수:**
- `MYSQL_USERNAME`
- `MYSQL_PASSWORD`
- `REDIS_HOST`
- `REDIS_PORT`
- `REDIS_PASSWORD` (설정되어 있다면)
- `JWT_SECRET`
- `KAKAO_CLIENT_ID`
- `KAKAO_REDIRECT_URI`
- `CORS_ALLOWED_ORIGINS`

**예상 결과:**
- 파일 권한: `600` (소유자만 읽기/쓰기)
- 모든 환경 변수 값이 설정되어 있어야 함

---

### 9단계: systemd 서비스 파일 확인

```bash
# 서비스 파일 존재 확인
ls -la /etc/systemd/system/nodap.service

# 서비스 파일 내용 확인
sudo cat /etc/systemd/system/nodap.service
```

**확인할 내용:**
- `EnvironmentFile=/home/ubuntu/config/.env` 경로가 올바른지
- `ExecStart` 경로가 올바른지 (`/home/ubuntu/backend/nodap-server.jar`)
- `User=ubuntu`, `Group=ubuntu` 설정 확인

**예상 결과:**
- 파일 존재
- 모든 경로가 올바르게 설정됨

---

### 10단계: systemd 서비스 상태 확인

```bash
# 서비스 상태 확인
sudo systemctl status nodap

# 서비스 활성화 여부 확인 (부팅 시 자동 시작)
sudo systemctl is-enabled nodap

# 서비스 실행 중인지 확인
sudo systemctl is-active nodap
```

**예상 결과:**
- `enabled` - 부팅 시 자동 시작 설정됨
- `active (running)` - 서비스 실행 중

---

### 11단계: JAR 파일 확인

```bash
# JAR 파일 존재 확인
ls -lh /home/ubuntu/backend/nodap-server.jar

# JAR 파일 권한 확인
stat -c "%a %n" /home/ubuntu/backend/nodap-server.jar

# JAR 파일 정보 확인
file /home/ubuntu/backend/nodap-server.jar

# 빌드 디렉토리 확인
ls -la /home/ubuntu/backend/build/libs/
```

**예상 결과:**
- 파일 존재 (약 70-80MB)
- 파일 권한: `755` (실행 가능)
- 파일 타입: Zip archive (JAR 파일 정상)

---

### 12단계: 포트 확인

```bash
# 8080 포트 (Spring Boot) 확인
sudo ss -tlnp | grep 8080
# 또는
sudo netstat -tlnp | grep 8080

# 3306 포트 (MySQL) 확인
sudo ss -tlnp | grep 3306

# 6379 포트 (Redis) 확인
sudo ss -tlnp | grep 6379

# 모든 리스닝 포트 확인
sudo ss -tlnp | head -20
```

**예상 결과:**
- 8080 포트: Java 프로세스가 리스닝 중
- 3306 포트: MySQL이 리스닝 중
- 6379 포트: Redis가 리스닝 중

---

### 13단계: CodeDeploy Agent 확인

```bash
# CodeDeploy Agent 서비스 상태 확인
sudo systemctl status codedeploy-agent

# CodeDeploy Agent 실행 중인지 확인
sudo systemctl is-active codedeploy-agent

# CodeDeploy Agent 로그 확인 (최근 20줄)
sudo tail -20 /var/log/aws/codedeploy-agent/codedeploy-agent.log
```

**예상 결과:**
- `active (running)` - 서비스 실행 중
- 로그에 정상 통신 메시지 확인

---

### 14단계: 방화벽(UFW) 설정 확인

```bash
# UFW 상태 확인
sudo ufw status

# 허용된 포트 확인 (상세)
sudo ufw status numbered

# 또는 간단히
sudo ufw status verbose
```

**예상 결과:**
- `Status: inactive` - UFW 비활성화 (AWS 보안 그룹 사용)
- 또는 `Status: active` - UFW 활성화 시 허용된 포트 확인

**참고:** AWS EC2는 보안 그룹(Security Group)으로 방화벽을 관리하므로 UFW가 비활성화되어 있어도 문제없습니다.

---

### 15단계: API 헬스체크 확인

```bash
# 로컬에서 헬스체크 (EC2 서버 내부에서)
curl http://localhost:8080/api/v1/health

# 또는 더 자세한 정보
curl -v http://localhost:8080/api/v1/health
```

**예상 결과:**
- `200 OK` - 서버 정상 작동
- JSON 응답 또는 성공 메시지

---

### 16단계: 애플리케이션 로그 확인

```bash
# 최근 50줄 로그 확인
sudo journalctl -u nodap -n 50 --no-pager

# 에러만 확인
sudo journalctl -u nodap -p err -n 20 --no-pager

# 오늘 로그 확인
sudo journalctl -u nodap --since today --no-pager | tail -30

# 실시간 로그 확인
sudo journalctl -u nodap -f
```

**예상 결과:**
- 에러 로그 없음
- 정상 작동 로그 확인

---

## 🚨 문제 해결 가이드

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

# 서비스 재시작
sudo systemctl restart nodap
```

### MySQL 연결 실패

```bash
# MySQL 서비스 상태 확인
sudo systemctl status mysql

# MySQL 로그 확인
sudo tail -f /var/log/mysql/error.log

# MySQL 접속 테스트
mysql -u nodap_admin -p -h localhost nodap_db

# MySQL 재시작
sudo systemctl restart mysql
```

### Redis 연결 실패

```bash
# Redis 서비스 상태 확인
sudo systemctl status redis-server

# Redis 연결 테스트
redis-cli ping
# 또는 비밀번호가 있다면
redis-cli -a "your_password" ping

# Redis 재시작
sudo systemctl restart redis-server
```

### 배포 실패

```bash
# CodeDeploy 로그 확인
cat /var/log/aws/codedeploy-agent/codedeploy-agent.log | tail -100

# backend 폴더 확인
ls -la /home/ubuntu/backend/

# JAR 파일 경로 확인
ls -la /home/ubuntu/backend/build/libs/

# 배포 스크립트 확인
cat /home/ubuntu/backend/scripts/deploy.sh
```

### 포트가 리스닝되지 않을 때

```bash
# 포트 사용 중인 프로세스 확인
sudo lsof -i :8080
sudo lsof -i :3306
sudo lsof -i :6379

# 프로세스 확인
ps aux | grep java
ps aux | grep mysql
ps aux | grep redis
```

---

## ✅ 체크리스트 요약

| 항목 | 확인 명령어 | 예상 결과 |
|------|------------|----------|
| Java 21 | `java -version` | openjdk 21.x.x |
| MySQL | `sudo systemctl status mysql` | active (running) |
| MySQL DB | `sudo mysql -u root -p` → `SHOW DATABASES;` | nodap_db 존재 |
| Redis | `sudo systemctl status redis-server` | active (running) |
| Redis 연결 | `redis-cli ping` | PONG |
| 디렉토리 | `ls -la /home/ubuntu/backend/` | JAR 파일 존재 |
| 환경 변수 | `ls -la /home/ubuntu/config/.env` | 파일 존재, 권한 600 |
| 서비스 파일 | `sudo cat /etc/systemd/system/nodap.service` | 경로 올바름 |
| 서비스 상태 | `sudo systemctl status nodap` | active (running) |
| JAR 파일 | `ls -lh /home/ubuntu/backend/nodap-server.jar` | 파일 존재 (70-80MB) |
| 포트 8080 | `sudo ss -tlnp \| grep 8080` | Java 프로세스 리스닝 |
| 포트 3306 | `sudo ss -tlnp \| grep 3306` | MySQL 리스닝 |
| 포트 6379 | `sudo ss -tlnp \| grep 6379` | Redis 리스닝 |
| CodeDeploy | `sudo systemctl status codedeploy-agent` | active (running) |
| API 헬스체크 | `curl http://localhost:8080/api/v1/health` | 200 OK |
| 에러 로그 | `sudo journalctl -u nodap -p err` | 에러 없음 |

---

## 📞 참고 자료

- [백엔드 배포 가이드](./BACKEND_DEPLOYMENT_GUIDE.md)
- [프로젝트 진행 현황](./PROJECT_STATUS.md)
- [코딩 컨벤션](./CODING_CONVENTIONS.md)

---

## 🔄 업데이트 이력

- **2025-01-13**: 초기 문서 작성
