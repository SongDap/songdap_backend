#!/bin/bash

# ============================================
# EC2 초기 설정 자동화 스크립트
# Ubuntu 24.04 LTS 환경에서 실행
# ============================================

set -e

echo "🚀 NoDap EC2 초기 설정을 시작합니다..."
echo ""

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 1. MySQL 데이터베이스 설정
echo -e "${YELLOW}[1/6] MySQL 데이터베이스 설정${NC}"
read -p "MySQL root 비밀번호를 입력하세요: " MYSQL_ROOT_PASS
read -p "애플리케이션용 DB 사용자 비밀번호를 입력하세요: " NODAP_DB_PASS

echo "데이터베이스와 사용자를 생성합니다..."
sudo mysql -u root -p${MYSQL_ROOT_PASS} <<EOF
CREATE DATABASE IF NOT EXISTS nodap CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'nodap_user'@'localhost' IDENTIFIED BY '${NODAP_DB_PASS}';
CREATE USER IF NOT EXISTS 'nodap_user'@'%' IDENTIFIED BY '${NODAP_DB_PASS}';
GRANT ALL PRIVILEGES ON nodap.* TO 'nodap_user'@'localhost';
GRANT ALL PRIVILEGES ON nodap.* TO 'nodap_user'@'%';
FLUSH PRIVILEGES;
EOF

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ MySQL 설정 완료${NC}"
else
    echo -e "${RED}❌ MySQL 설정 실패${NC}"
    exit 1
fi

# 2. Redis 설정 확인
echo -e "${YELLOW}[2/6] Redis 서비스 확인${NC}"
if systemctl is-active --quiet redis-server; then
    echo -e "${GREEN}✅ Redis 서비스 실행 중${NC}"
else
    echo -e "${RED}❌ Redis 서비스가 실행되지 않았습니다${NC}"
    read -p "Redis를 시작하시겠습니까? (y/n): " start_redis
    if [ "$start_redis" = "y" ]; then
        sudo systemctl start redis-server
        sudo systemctl enable redis-server
        echo -e "${GREEN}✅ Redis 시작 완료${NC}"
    fi
fi

read -p "Redis 비밀번호를 설정하시겠습니까? (y/n): " set_redis_pass
if [ "$set_redis_pass" = "y" ]; then
    read -s -p "Redis 비밀번호를 입력하세요: " REDIS_PASS
    echo ""
    sudo sed -i "s/# requirepass foobared/requirepass ${REDIS_PASS}/" /etc/redis/redis.conf
    sudo systemctl restart redis-server
    echo -e "${GREEN}✅ Redis 비밀번호 설정 완료${NC}"
else
    REDIS_PASS=""
fi

# 3. 애플리케이션 디렉토리 생성
echo -e "${YELLOW}[3/6] 애플리케이션 디렉토리 생성${NC}"
mkdir -p ~/nodap-server/{logs,backup,config}
chmod 755 ~/nodap-server
chmod 755 ~/nodap-server/logs
chmod 755 ~/nodap-server/backup
echo -e "${GREEN}✅ 디렉토리 생성 완료${NC}"

# 4. 환경 변수 파일 생성
echo -e "${YELLOW}[4/6] 환경 변수 파일 생성${NC}"
cat > ~/nodap-server/config/.env <<EOF
SPRING_PROFILES_ACTIVE=prod
DB_HOST=localhost
DB_PORT=3306
DB_NAME=nodap
DB_USERNAME=nodap_user
DB_PASSWORD=${NODAP_DB_PASS}
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=${REDIS_PASS}
EOF

chmod 600 ~/nodap-server/config/.env
echo -e "${GREEN}✅ 환경 변수 파일 생성 완료${NC}"

# 5. systemd 서비스 파일 생성
echo -e "${YELLOW}[5/6] systemd 서비스 파일 생성${NC}"
sudo tee /etc/systemd/system/nodap.service > /dev/null <<EOF
[Unit]
Description=NoDap Server Application
After=network.target mysql.service redis-server.service

[Service]
Type=simple
User=${USER}
Group=${USER}
WorkingDirectory=/home/${USER}/nodap-server
EnvironmentFile=/home/${USER}/nodap-server/config/.env
ExecStart=/usr/bin/java -jar /home/${USER}/nodap-server/nodap-server.jar
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal
SyslogIdentifier=nodap
LimitNOFILE=65536

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl daemon-reload
sudo systemctl enable nodap
echo -e "${GREEN}✅ systemd 서비스 등록 완료${NC}"

# 6. UFW 방화벽 설정 확인
echo -e "${YELLOW}[6/6] UFW 방화벽 설정 확인${NC}"
if sudo ufw status | grep -q "Status: active"; then
    echo "UFW가 이미 활성화되어 있습니다."
else
    read -p "UFW를 활성화하시겠습니까? (y/n): " enable_ufw
    if [ "$enable_ufw" = "y" ]; then
        sudo ufw allow 22/tcp
        sudo ufw allow 80/tcp
        sudo ufw allow 443/tcp
        sudo ufw --force enable
        echo -e "${GREEN}✅ UFW 활성화 완료${NC}"
    fi
fi

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}✅ EC2 초기 설정이 완료되었습니다!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "📋 다음 단계:"
echo "1. 로컬에서 JAR 파일 빌드: ./gradlew build -x test"
echo "2. EC2로 JAR 파일 업로드: scp -i key.pem build/libs/nodap-server-*.jar ubuntu@13.209.40.98:~/nodap-server/nodap-server.jar"
echo "3. 서비스 시작: sudo systemctl start nodap"
echo "4. 로그 확인: sudo journalctl -u nodap -f"
echo ""

