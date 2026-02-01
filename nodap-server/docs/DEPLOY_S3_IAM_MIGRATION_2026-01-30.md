# S3 IAM Migration & Deployment notes (2026-01-30)

요약
- 발생한 문제: 프론트에서 이미지 업로드 시 S3에서 403 에러 발생 ("The AWS Access Key Id you provided does not exist").
- 원인: 배포된 서버에 잘못된/삭제된 Access Key가 설정되어 있었고, 이후에는 더 안전한 방법(IAM Role)을 사용하도록 전환하였음.

핵심 조치 (순서)
1. 조사
  - 확인한 로그: S3Exception(403) / application 로그에서 AWS 자격증명 관련 RuntimeException
  - 프로세스 환경 확인:
    - PID 찾기: `pgrep -f 'nodap-server.jar'`
    - 프로세스 env 보기: `sudo tr '\0' '\n' < /proc/<PID>/environ | egrep -i 'aws|s3|JWT'`
  - 하드코딩된 키 위치 찾기:
    - `sudo grep -RIn 'AWS_ACCESS_KEY\|AWS_SECRET_KEY' /home/ubuntu /etc /opt /srv /var`
    - 발견: `/home/ubuntu/config/.env` 에 AWS_ACCESS_KEY / AWS_SECRET_KEY 존재

2. 단기 안전조치
  - 기존 `.env`를 백업 및 비활성화:
    ```bash
    sudo cp /home/ubuntu/config/.env /home/ubuntu/config/.env.backup
    sudo mv /home/ubuntu/config/.env /home/ubuntu/config/.env.disabled
    sudo chmod 600 /home/ubuntu/config/.env.disabled
    ```
  - 애플리케이션 재시작(환경 없이)으로 동작 확인 시도 (후에 IAM Role 적용)

3. 장기 대책: IAM Role 사용으로 전환
  - GitHub → PR에서 코드 변경:
    - `S3Properties`에서 accessKey/secretKey 강제 검증 제거(버킷만 필수).
    - `S3Config`에서 accessKey/secretKey가 있으면 StaticCredentials 사용, 없으면 `DefaultCredentialsProvider`로 폴백.
  - IAM Role 생성 및 정책 설정:
    - IAM Role 이름: `nodap-s3-prod-role`
    - 앱에서 필요한 버킷 권한(`nodap-images`): 기존 `NoDapS3BucketAccess` 정책 사용
      - `s3:PutObject`, `s3:GetObject`, `s3:DeleteObject`, `s3:ListBucket` 권한
    - CodeDeploy용 배포 버킷(`nodap-project-storage`) 읽기 권한을 `nodap-s3-prod-role`에 추가:
      ```json
      {
        "Version":"2012-10-17",
        "Statement":[
          {"Effect":"Allow","Action":["s3:GetObject","s3:GetObjectVersion"],"Resource":["arn:aws:s3:::nodap-project-storage/*"]},
          {"Effect":"Allow","Action":["s3:ListBucket","s3:GetBucketLocation"],"Resource":["arn:aws:s3:::nodap-project-storage"]}
        ]
      }
      ```
  - 역할을 인스턴스에 연결(EC2 콘솔 → 인스턴스 선택 → Actions → Security → Modify IAM role).
  - IAM Role 연결 확인 (EC2 인스턴스에서):
    ```bash
    # IMDSv2 토큰 발급 및 역할 이름 확인
    TOKEN=$(curl -s -X PUT "http://169.254.169.254/latest/api/token" -H "X-aws-ec2-metadata-token-ttl-seconds:21600")
    curl -s -H "X-aws-ec2-metadata-token: $TOKEN" http://169.254.169.254/latest/meta-data/iam/security-credentials/
    ```
  - `.env` 파일에서 AWS 키 제거:
    ```bash
    # AWS_ACCESS_KEY와 AWS_SECRET_KEY 라인 제거
    sudo awk '!/^AWS_ACCESS_KEY/ && !/^AWS_SECRET_KEY/ {print}' /home/ubuntu/config/.env.backup > /home/ubuntu/config/.env
    sudo chmod 600 /home/ubuntu/config/.env
    ```

4. JWT 시크릿 문제 해결
  - 원인: jjwt 라이브러리에서 HMAC키가 256bit 미만일 경우 WeakKeyException 발생.
  - 해결:
    - base64로 인코딩된 32바이트(=256bit) 시크릿 생성:
      ```bash
      openssl rand -base64 32
      ```
    - `/home/ubuntu/config/.jwt_secret`에 안전히 저장하고 `.env`의 `JWT_SECRET` 을 이 값으로 덮어씀.
    - `.env`를 export 하여 실행하면 Spring이 `jwt.secret`을 정상 바인딩.

5. 배포 (CI/CD)
  - GitHub Actions에서 `build` -> artifact upload -> CodeDeploy로 연결 (배포 로그에서 성공 확인).
  - CodeDeploy 에이전트 로그 확인: `/var/log/aws/codedeploy-agent/codedeploy-agent.log`
  - 배포 실패 원인(초기): `nodap-s3-prod-role`에 `nodap-project-storage` 읽기 권한 부족.
  - 정책 추가 후 배포 재실행 → agent가 아카이브를 다운로드하고 `deploy.sh`를 실행하여 `/home/ubuntu/backend/nodap-server.jar`를 교체.
  - `deploy.sh` 내용(요약): 
    - `build/libs/nodap-server.jar`를 `/home/ubuntu/backend/nodap-server.jar`로 복사
    - systemd `nodap` 서비스 재시작 (`sudo systemctl restart nodap`)
  - GitHub Actions 워크플로우: `.github/workflows/deploy.yml`
    - S3 버킷: `nodap-project-storage` (CodeDeploy 아티팩트 저장용)
    - GitHub Secrets 사용: `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY` (CI/CD 전용, EC2와 분리)

6. 서버에서 재시작/검증
  - 서비스 확인:
    ```bash
    sudo systemctl status nodap
    sudo journalctl -u nodap -n 200 --no-pager
    ```
  - 프로세스 환경 확인:
    ```bash
    PID=$(pgrep -f 'nodap-server.jar' | head -n1)
    sudo tr '\0' '\n' < /proc/$PID/environ | egrep -i 'AWS|JWT' || true
    ```
  - 업로드 테스트: 프론트에서 이미지 업로드 → `tail -f /home/ubuntu/nodap-server.log`로 로그 관찰
  - S3 버킷: `nodap-images` (이미지 저장용)

7. Nginx 설정 조정 (413 Content Too Large 해결)
  - 문제: Nginx 기본 `client_max_body_size`가 1MB로 설정되어 있어, 10MB 파일 업로드 시 413 에러 발생.
  - 해결:
    ```bash
    # Nginx 설정 파일 생성
    echo 'client_max_body_size 10M;' | sudo tee /etc/nginx/conf.d/upload.conf
    
    # 설정 검증 및 재로드
    sudo nginx -t && sudo systemctl reload nginx
    ```
  - Spring Boot 설정과 일치: `spring.servlet.multipart.max-file-size: 10MB`

8. 정리 및 보안 권장
  - `.env.disabled`와 같은 민감 백업은 안전한 스토리지에 보관하거나 삭제.
  - 노출 가능 키는 AWS IAM 콘솔에서 폐기(비활성화/삭제) 및 필요시 회전.
  - 장기적으로:
    - Secrets Manager/Parameter Store로 JWT 등 민감값 보관.
    - systemd unit 환경은 `EnvironmentFile=`로 관리하거나 인스턴스 프로파일의 사용을 표준화.
    - CI에서 배포 권한과 버킷 권한을 문서화.

핵심 명령 요약 (빠른 복구용)
- 프로세스 env 확인:
  - `sudo tr '\0' '\n' < /proc/<PID>/environ | egrep -i 'AWS|JWT'`
- .env 백업/비활성화:
  - `sudo cp /home/ubuntu/config/.env /home/ubuntu/config/.env.backup`
  - `sudo mv /home/ubuntu/config/.env /home/ubuntu/config/.env.disabled`
- JWT 시크릿 생성(서버):
  - `openssl rand -base64 32 > /home/ubuntu/config/.jwt_secret && chmod 600 /home/ubuntu/config/.jwt_secret`
- 배포 재시작(환경 반영):
  - systemd 서비스 사용 시: `sudo systemctl restart nodap`
  - 수동 실행 시: `nohup bash -c 'set -o allexport; source /home/ubuntu/config/.env; set +o allexport; exec /usr/bin/java -jar /home/ubuntu/backend/nodap-server.jar' > /home/ubuntu/nodap-server.log 2>&1 &`

기록 위치
- 이 파일: `nodap-server/docs/DEPLOY_S3_IAM_MIGRATION_2026-01-30.md`

문의/추가
- 문서에 누락된 로그나 특정 명령(예: appspec hooks 전체, 생성된 정책 이름 등) 추가하길 원하면 알려주세요.

---

추가: 상세 사건 보고서 (운영용)

1) 전체 타임라인 (세부)
- 2026-01-29 ~ 01-30: 프론트팀으로부터 이미지 업로드 실패 보고 접수(사용자 업로드 403 응답)
- 2026-01-29 22:04 ~ 22:13: 서버 로그에서 S3 업로드 시도 중 "The AWS Access Key Id you provided does not exist" 에러 발생 확인
- 2026-01-30 00:xx: 서버에서 프로세스(PID) 환경 점검 → `/home/ubuntu/config/.env`에서 `AWS_ACCESS_KEY` 발견
- 2026-01-30 00:yy: 백업: `/home/ubuntu/config/.env.backup` 생성, 원본을 `/home/ubuntu/config/.env.disabled`로 이동
- 2026-01-30 00:zz: IAM 역할(`nodap-s3-prod-role`)이 인스턴스에 연결되어 있었으나, 해당 역할에 CodeDeploy 아카이브 버킷(`nodap-project-storage`) 읽기 권한 부재로 배포 실패 확인 (AccessDenied 에러)
- 2026-01-30 00:aa: IAM 역할에 `s3:GetObject`/`s3:ListBucket` 정책 추가(정책명: CodeDeployReadNodapProjectStorage)
- 2026-01-30 00:bb: PR로 코드 변경(Repository) — S3Config에서 IAM Role(DefaultCredentialsProvider) 폴백 추가, S3Properties 검증 완화
- 2026-01-30 00:cc: PR 머지 → GitHub Actions 빌드 → artifact 업로드 → CodeDeploy 실행
- 2026-01-30 00:47: CodeDeploy 재시도 → 아카이브 다운로드 성공 → `deploy.sh` 실행(새 JAR 배포)
- 2026-01-30 00:58~01:00: 애플리케이션 재시작 시 JWT secret 길이 부족(WeakKeyException)으로 기동 실패 확인
- 2026-01-30 01:00: JWT secret을 base64(32바이트)로 새로 생성하여 `/home/ubuntu/config/.jwt_secret`에 저장하고 `.env`에 반영
- 2026-01-30 01:01: 애플리케이션 재시작 → S3Config가 DefaultCredentialsProvider 사용을 로그에 기록 → 앱 정상 기동 확인
- 2026-01-30 01:30: 프론트에서 업로드 시 413 발생 → nginx 기본 1M 제약 확인 → `/etc/nginx/conf.d/upload.conf`에 `client_max_body_size 10M;` 추가 → nginx 재로드 → 업로드 성공 확인

2) 변경된 파일(요약)
- 소스 코드
  - `nodap-server/src/main/java/com/nodap/infrastructure/external/S3Properties.java`
    - `accessKey`/`secretKey` 검증 제거 (IAM Role 사용 시 선택적)
  - `nodap-server/src/main/java/com/nodap/infrastructure/external/S3Config.java`
    - `DefaultCredentialsProvider` 폴백 로직 추가 (IAM Role 자동 감지)
  - (배포 관련 문서) `nodap-server/docs/DEPLOY_S3_IAM_MIGRATION_2026-01-30.md` 추가/수정
- 서버 설정 파일
  - `/home/ubuntu/config/.env`: `AWS_ACCESS_KEY`, `AWS_SECRET_KEY` 제거
  - `/etc/nginx/conf.d/upload.conf`: `client_max_body_size 10M;` 추가
- 배포 스크립트
  - `nodap-server/scripts/deploy.sh` (CodeDeploy로 전달된 스크립트; JAR 복사 및 systemd 재시작)
  - `nodap-server/appspec.yml` (CodeDeploy 설정 파일)

3) 실행한 주요 명령 (참고용, 민감 정보 제외)
- 프로세스/환경 확인
```bash
pgrep -f 'nodap-server.jar'
sudo tr '\0' '\n' < /proc/<PID>/environ | egrep -i 'aws|s3|JWT'
```
- .env 백업/비활성화
```bash
sudo cp /home/ubuntu/config/.env /home/ubuntu/config/.env.backup
sudo mv /home/ubuntu/config/.env /home/ubuntu/config/.env.disabled
```
- JWT 시크릿 생성(서버)
```bash
SECRET=$(openssl rand -base64 32)
echo "$SECRET" | sudo tee /home/ubuntu/config/.jwt_secret
```
- IAM Role 확인 (EC2 인스턴스에서)
```bash
TOKEN=$(curl -s -X PUT "http://169.254.169.254/latest/api/token" -H "X-aws-ec2-metadata-token-ttl-seconds:21600")
curl -s -H "X-aws-ec2-metadata-token: $TOKEN" http://169.254.169.254/latest/meta-data/iam/security-credentials/
```
- .env에서 AWS 키 제거
```bash
sudo awk '!/^AWS_ACCESS_KEY/ && !/^AWS_SECRET_KEY/ {print}' /home/ubuntu/config/.env.backup > /home/ubuntu/config/.env
sudo chmod 600 /home/ubuntu/config/.env
```
- nginx 허용 크기 설정
```bash
echo 'client_max_body_size 10M;' | sudo tee /etc/nginx/conf.d/upload.conf
sudo nginx -t && sudo systemctl reload nginx
```
- 배포 재실행(자동/CodeDeploy)
```bash
# GitHub Actions ran and CodeDeploy was triggered; CodeDeploy logs inspected at:
sudo tail -n 200 /var/log/aws/codedeploy-agent/codedeploy-agent.log
```

4) 문제 원인(요약)
- 루트 케이스 1: 서버에 하드코딩(또는 환경으로 주입된) AWS 액세스 키가 더 이상 유효하지 않아 S3 업로드가 403 발생.
- 루트 케이스 2: CodeDeploy가 아카이브를 내려받을 권한이 없어 초기에 배포 실패(AWS IAM 정책 미부여).
- 부수 문제: JWT 시크릿 길이 부족으로 애플리케이션이 시작 실패(이를 먼저 해결해야 정상 재시작 가능).
- 부수 문제 2: nginx 기본 body size가 1M으로, 프론트에서 10MB 파일 업로드 시 413 발생.

5) 조치 결과
- IAM 역할(`nodap-s3-prod-role`) 정책 추가 후 CodeDeploy 재시도하여 아카이브 다운로드 및 배포 성공.
- 코드 변경으로 IAM Role(Instance Profile)을 우선 사용하도록 하여 환경변수 기반 키(`AWS_ACCESS_KEY`, `AWS_SECRET_KEY`)를 제거해도 동작하도록 함.
- JWT 시크릿을 256-bit(32바이트 base64)로 교체하여 WeakKeyException 해결.
- nginx 설정(`/etc/nginx/conf.d/upload.conf`)을 10M으로 조정하여 프론트 업로드 413 문제 해결.
- S3 버킷(`nodap-images`) 접근이 IAM Role을 통해 정상 작동 확인.

6) 후속 권장 작업
- 민감 백업(예: `/home/ubuntu/config/.env.backup`, `.env.disabled`) 안전 보관 또는 폐기.
- 노출 가능 키 즉시 폐기(필요 시 IAM 콘솔에서 Access Key 삭제).
  - **중요**: EC2 서버는 이제 IAM Role을 사용하므로 더 이상 Access Key가 필요 없음.
  - GitHub Actions는 여전히 `secrets.AWS_ACCESS_KEY_ID`/`secrets.AWS_SECRET_ACCESS_KEY`를 사용 중 (CodeDeploy 배포용).
- 배포 파이프라인에 배포 성공/실패 알림(예: Slack) 설정.
- 프론트팀: 업로드 전 파일 크기 검사 및 이미지 압축 적용(문서화 완료).
- 운영팀: deployment 권한과 버킷 정책을 문서화 및 감사.
- IAM Role 정책 정기 점검: 최소 권한 원칙 준수 확인.

7) 담당자/연락처
- 인프라: (예: infra-team@company)
- 백엔드: (예: backend-team@company)
- 프론트: (예: frontend-team@company)

끝.
