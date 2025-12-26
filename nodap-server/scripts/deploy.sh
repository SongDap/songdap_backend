#!/bin/bash

REPOSITORY=/home/ubuntu/backend
PROJECT_NAME=your-project-name # 여기에 실제 프로젝트 이름을 적으세요 (JAR 파일명에 포함되는 이름)

echo "> 현재 실행 중인 애플리케이션 PID 확인"
CURRENT_PID=$(pgrep -fl $PROJECT_NAME | grep jar | awk '{print $1}')

if [ -z "$CURRENT_PID" ]; then
    echo "> 현재 실행 중인 애플리케이션이 없으므로 종료하지 않습니다."
else
    echo "> kill -15 $CURRENT_PID"
    kill -15 $CURRENT_PID
    sleep 5
fi

echo "> 새 애플리케이션 배포"
JAR_NAME=$(ls -tr $REPOSITORY/*.jar | tail -n 1)

echo "> JAR Name: $JAR_NAME"
echo "> $JAR_NAME 에 실행권한 추가"
chmod +x $JAR_NAME

echo "> $JAR_NAME 실행"
# 로그는 nohup.out에 기록되며, 백그라운드에서 실행됩니다.
nohup java -jar $JAR_NAME > $REPOSITORY/nohup.out 2>&1 &