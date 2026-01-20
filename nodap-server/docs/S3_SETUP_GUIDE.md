# AWS S3 설정 가이드

## 1단계: S3 버킷 생성

### 1-1. AWS 콘솔 접속
1. https://console.aws.amazon.com 접속
2. AWS 계정으로 로그인

### 1-2. S3 서비스 이동
1. 상단 검색창에 "S3" 입력
2. "S3" 서비스 선택
3. 왼쪽 메뉴에서 "버킷" 클릭
4. "버킷 만들기" 버튼 클릭

### 1-3. 버킷 설정
**일반 설정:**
- **버킷 이름**: 예) `songdap-images` 
  - ⚠️ 전역적으로 고유해야 함 (다른 사람이 사용한 이름은 불가)
  - 소문자, 숫자, 하이픈(-)만 사용 가능
  - 예시: `songdap-images`, `songdap-images-2025`, `my-songdap-bucket`
  
- **AWS 리전**: `아시아 태평양(서울) ap-northeast-2` 선택

**객체 소유권:**
- "ACL 비활성화됨(권장)" 선택

**퍼블릭 액세스 차단 설정:**
- ⚠️ **중요**: 이미지를 외부에서 접근할 수 있도록 설정 필요
- "모든 퍼블릭 액세스 차단" 체크박스를 **해제**
- 경고 메시지가 나타나면 확인 후 체크박스 4개 모두 체크

**버킷 버전 관리:**
- "버전 관리 비활성화" (기본값)

**기본 암호화:**
- "Amazon S3 관리형 키(SSE-S3)" 선택 (기본값)

**고급 설정:**
- 기본값 유지

### 1-4. 버킷 만들기
1. 하단 "버킷 만들기" 버튼 클릭
2. 생성 완료 후 버킷 목록에서 확인
3. **버킷 이름을 기록해두세요!** (예: `songdap-images`)

---

## 2단계: 버킷 CORS 설정 (프론트엔드 접근용)

### 2-1. 버킷 선택
1. 생성한 버킷 클릭

### 2-2. 권한 탭 이동
1. 상단 "권한" 탭 클릭

### 2-3. CORS 설정
1. "교차 출처 리소스 공유(CORS)" 섹션 찾기
2. "편집" 버튼 클릭
3. 다음 JSON 코드 입력:

```json
[
    {
        "AllowedHeaders": ["*"],
        "AllowedMethods": ["GET", "PUT", "POST", "DELETE", "HEAD"],
        "AllowedOrigins": ["*"],
        "ExposeHeaders": ["ETag"],
        "MaxAgeSeconds": 3000
    }
]
```

4. "변경 사항 저장" 클릭

---

## 3단계: IAM 사용자 생성 및 권한 설정

### 3-1. IAM 서비스 이동
1. AWS 콘솔에서 "IAM" 검색
2. IAM 서비스 선택

### 3-2. 사용자 생성
1. 왼쪽 메뉴에서 "사용자" 클릭
2. "사용자 만들기" 버튼 클릭

### 3-3. 사용자 이름 설정
- **사용자 이름**: `songdap-s3-user` (또는 원하는 이름)
- "AWS 자격 증명 유형 제공" 선택
- "액세스 키 - 프로그래밍 방식 액세스" 선택
- "다음" 클릭

### 3-4. 권한 설정
1. "기존 정책 직접 연결" 선택
2. 검색창에 "S3" 입력
3. **`AmazonS3FullAccess`** 정책 선택
   - ⚠️ 프로덕션 환경에서는 더 제한적인 정책 사용 권장
   - 예: 특정 버킷만 접근 가능한 커스텀 정책
4. "다음" 클릭

### 3-5. 사용자 생성 완료
1. "다음" 클릭 (태그는 선택사항)
2. "사용자 만들기" 클릭

### 3-6. Access Key 및 Secret Key 저장
⚠️ **중요**: 이 화면에서만 키를 확인할 수 있습니다!

1. **액세스 키 ID** 복사 → 안전한 곳에 저장
2. **비밀 액세스 키** 복사 → 안전한 곳에 저장
   - "비밀 액세스 키 표시" 클릭하여 확인
3. "CSV 다운로드" 클릭하여 백업 (선택사항)
4. "완료" 클릭

---

## 4단계: 환경 변수 설정

### 4-1. 로컬 개발 환경 설정

1. `application-local.yml.example` 파일을 복사하여 `application-local.yml` 생성
   ```bash
   # Windows PowerShell
   Copy-Item src/main/resources/application-local.yml.example src/main/resources/application-local.yml
   
   # 또는 직접 파일 복사
   ```

2. `application-local.yml` 파일 열기

3. S3 설정 부분 수정:
```yaml
aws:
  s3:
    region: ap-northeast-2  # 서울 리전 (변경 불필요)
    bucket: songdap-images  # ⚠️ 1단계에서 생성한 버킷 이름으로 변경
    access-key: AKIAIOSFODNN7EXAMPLE  # ⚠️ 3-6에서 복사한 Access Key로 변경
    secret-key: wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY  # ⚠️ 3-6에서 복사한 Secret Key로 변경
    base-url: https://songdap-images.s3.ap-northeast-2.amazonaws.com  # ⚠️ 버킷 이름으로 변경
```

### 4-2. 프로덕션 환경 설정 (EC2)

EC2 서버에서 환경 변수로 설정:

```bash
# EC2 서버에 SSH 접속 후
sudo nano /etc/environment

# 또는 systemd 서비스 파일에 추가
sudo nano /etc/systemd/system/nodap-server.service
```

환경 변수 추가:
```bash
AWS_S3_REGION=ap-northeast-2
AWS_S3_BUCKET=songdap-images
AWS_ACCESS_KEY=AKIAIOSFODNN7EXAMPLE
AWS_SECRET_KEY=wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
AWS_S3_BASE_URL=https://songdap-images.s3.ap-northeast-2.amazonaws.com
```

---

## 5단계: Gradle 빌드 및 테스트

### 5-1. 의존성 다운로드
```bash
cd songdap_backend/nodap-server
./gradlew build
```

### 5-2. 애플리케이션 실행
```bash
./gradlew bootRun
```

### 5-3. 테스트
- 애플리케이션이 정상적으로 시작되는지 확인
- 로그에서 S3 관련 에러가 없는지 확인

---

## 문제 해결

### 버킷 이름이 이미 사용 중
- 다른 이름으로 변경 (예: `songdap-images-2025`, `my-songdap-bucket`)

### Access Key를 잃어버림
- IAM → 사용자 → 해당 사용자 → "보안 자격 증명" 탭
- "액세스 키 만들기" 클릭하여 새 키 생성
- 기존 키는 비활성화 또는 삭제

### 이미지 업로드 실패
- 버킷 이름 확인
- Access Key / Secret Key 확인
- IAM 권한 확인
- CORS 설정 확인

---

## 보안 권장사항

### 프로덕션 환경
1. **IAM 정책 제한**: 전체 S3 접근 대신 특정 버킷만 접근 가능하도록 제한
2. **환경 변수 사용**: 코드에 직접 키를 작성하지 말고 환경 변수 사용
3. **키 로테이션**: 정기적으로 Access Key 교체
4. **CloudFront 사용**: S3 직접 접근 대신 CloudFront CDN 사용 권장

### 커스텀 IAM 정책 예시 (특정 버킷만 접근)
```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "s3:PutObject",
                "s3:GetObject",
                "s3:DeleteObject"
            ],
            "Resource": "arn:aws:s3:::songdap-images/*"
        },
        {
            "Effect": "Allow",
            "Action": "s3:ListBucket",
            "Resource": "arn:aws:s3:::songdap-images"
        }
    ]
}
```
