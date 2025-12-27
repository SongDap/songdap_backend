#!/bin/bash

# ============================================
# SongDap Backend 배포 스크립트 (systemd 방식)
# ============================================

# 1. 경로 및 변수 설정
REPOSITORY=/home/ubuntu/backend
SERVICE_NAME=nodap
JAR_TARGET=$REPOSITORY/nodap-server.jar

echo "============================================"
echo "> [배포 시작] $(date '+%Y-%m-%d %H:%M:%S')"
echo "> REPOSITORY: $REPOSITORY"
echo "> SERVICE: $SERVICE_NAME"
echo "============================================"

# 2. JAR 파일 확인
echo "> JAR 파일 확인 중..."
JAR_SOURCE=$(ls -tr $REPOSITORY/*.jar 2>/dev/null | tail -n 1)

if [ -z "$JAR_SOURCE" ]; then
    echo "> [에러] JAR 파일을 찾을 수 없습니다: $REPOSITORY/*.jar"
    exit 1
fi

echo "> 원본 JAR: $JAR_SOURCE"

# 3. JAR 파일 이름 통일 (systemd 서비스에서 고정된 이름 사용)
echo "> JAR 파일 준비: $JAR_SOURCE -> $JAR_TARGET"
cp "$JAR_SOURCE" "$JAR_TARGET"
chmod +x "$JAR_TARGET"

# 4. systemd 서비스 재시작
echo "============================================"
echo "> 서비스 재시작: $SERVICE_NAME"
sudo systemctl restart $SERVICE_NAME

# 5. 서비스 시작 확인 (최대 30초 대기)
echo "> 서비스 시작 확인 중..."
WAIT_COUNT=0
while [ $WAIT_COUNT -lt 30 ]; do
    sleep 1
    WAIT_COUNT=$((WAIT_COUNT + 1))
    
    if sudo systemctl is-active --quiet $SERVICE_NAME; then
        echo "============================================"
        echo "> [성공] 배포 완료! $(date '+%Y-%m-%d %H:%M:%S')"
        echo "> 서비스 상태: $(sudo systemctl is-active $SERVICE_NAME)"
        echo "> 상태 확인: sudo systemctl status $SERVICE_NAME"
        echo "> 로그 확인: sudo journalctl -u $SERVICE_NAME -f"
        echo "============================================"
        exit 0
    fi
    
    echo "> 서비스 시작 대기 중... ($WAIT_COUNT/30)"
done

# 6. 시작 실패 시 로그 출력
echo "============================================"
echo "> [에러] 서비스 시작 실패!"
echo "> 최근 로그:"
sudo journalctl -u $SERVICE_NAME -n 50 --no-pager
echo "============================================"
exit 1
