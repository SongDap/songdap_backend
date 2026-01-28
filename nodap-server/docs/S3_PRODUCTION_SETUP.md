# S3 프로덕션 배포 설정 가이드

## 📋 개요

프로덕션 환경에서 S3를 안전하게 사용하기 위한 설정 가이드입니다.

## 🔑 용어 정리

### IAM 사용자 vs 환경

| IAM 사용자 | 사용 환경 | 설정 파일 | 설명 |
|-----------|----------|----------|------|
| `nodap-s3-dev` | **로컬(local)** 개발 환경 | `application-local.yml` | 개발자의 로컬 PC에서 사용 |
| `nodap-s3-prod` | **프로덕션(prod)** 환경 | EC2 환경 변수 (`.env`) | EC2 서버에서 사용 |

> 💡 **핵심**: 
> - **`dev` = `local`**: `nodap-s3-dev`는 로컬 개발 환경에서 사용하는 IAM 사용자입니다.
> - **`prod` = 프로덕션**: `nodap-s3-prod`는 프로덕션(EC2) 환경에서 사용하는 IAM 사용자입니다.
> - 같은 버킷을 사용하지만, IAM 사용자를 분리하여 보안을 강화합니다.

---

## ⚡ 빠른 시작 (최소 설정)

프로덕션에서 빠르게 작동시키려면 다음 단계를 따라하세요:

### 0️⃣ IAM 사용자 분리 (보안 강화 - 권장)

로컬 개발용과 프로덕션용 IAM 사용자를 분리합니다:

> 💡 **용어 정리**:
> - **`nodap-s3-dev`**: 개발용 IAM 사용자 → **로컬(local) 개발 환경**에서 사용
> - **`nodap-s3-prod`**: 프로덕션용 IAM 사용자 → **프로덕션(prod) 환경(EC2)**에서 사용
> - 즉, `dev` = `local` 환경에서 사용하는 IAM 사용자입니다.

> ⚠️ **중요**: 정책을 먼저 생성한 후 사용자를 생성하는 것을 권장합니다!

#### 0-1. 커스텀 정책 생성 (먼저 생성 필요!)

1. **IAM 콘솔 접속:**
   - AWS 콘솔 → "IAM" 검색 → IAM 서비스 선택
   - 왼쪽 메뉴에서 **"정책"** 클릭
   - 상단 **"정책 만들기"** 버튼 클릭

2. **"정책 생성" 화면 - 1단계:**
   - **정책 이름**: `NoDapS3BucketAccess` 입력
   - **설명 (선택 사항)**: "NoDap 프로젝트 S3 버킷 접근 권한" 입력 (선택사항)

3. **"정책 정의" 섹션:**
   - **JSON** 탭 클릭 (기본적으로 선택되어 있을 수 있음)
   - 기존 예제 코드가 있다면 **모두 삭제**
   - 다음 정책 JSON을 **복사해서 붙여넣기**:

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "AllowS3AccessToSpecificBucket",
            "Effect": "Allow",
            "Action": [
                "s3:PutObject",
                "s3:GetObject",
                "s3:DeleteObject",
                "s3:ListBucket"
            ],
            "Resource": [
                "arn:aws:s3:::nodap-images",
                "arn:aws:s3:::nodap-images/*"
            ]
        }
    ]
}
```

   - ⚠️ **중요**: `nodap-images` 부분을 실제 버킷 이름으로 변경하세요!
   - JSON이 올바르게 입력되었는지 확인
   - 하단 **"다음"** 버튼 클릭

4. **"정책 검토 및 생성" 화면:**
   - 정책 이름과 내용 확인
   - **"정책 만들기"** 버튼 클릭

5. ✅ **정책 생성 완료!**
   - 이제 사용자 생성 시 이 정책을 검색해서 선택할 수 있습니다.

---

#### 0-2. 개발용 IAM 사용자 생성

1. AWS IAM 콘솔 → "사용자" → "사용자 만들기" 클릭

2. **"사용자 세부 정보 지정" 화면:**
   - **사용자 이름**: `nodap-s3-dev` 입력
   - **AWS Management Console에 대한 사용자 액세스 권한 제공**: ✅ **체크하지 않음** (프로그래밍 방식만 사용)
   - 하단 "다음" 버튼 클릭

3. **"권한 설정" 화면:**
   - **권한 옵션**에서 **"직접 정책 연결"** 라디오 버튼 선택 ✅
   - 검색창에 `NoDapS3BucketAccess` 입력
   - ⚠️ **정책이 안 보이면**: 
     - 정책 이름을 정확히 입력했는지 확인
     - "필터링 기준 유형"에서 "모든 유형" 선택되어 있는지 확인
     - 정책 생성(0-1 단계)을 먼저 완료했는지 확인
   - `NoDapS3BucketAccess` 정책이 보이면 체크박스 선택 ✅
   - "다음" 버튼 클릭

4. **"태그 추가 (선택 사항)" 화면:**
   - 태그는 선택사항이므로 그대로 "다음" 버튼 클릭

5. **"검토 및 생성" 화면:**
   - 설정 확인 후 "사용자 만들기" 버튼 클릭

6. **"액세스 키 모범 사례 및 대안" 화면:**
   - ⚠️ **중요**: 이 화면에서 사용 사례를 선택해야 합니다!
   - **"로컬 코드"** 라디오 버튼 선택 ✅
     - 설명: "로컬 개발 환경의 애플리케이션 코드를 사용하여 AWS 계정에 액세스할 수 있도록 이 액세스 키를 사용할 것입니다."
   - "다음" 버튼 클릭

7. **"액세스 키 및 비밀 액세스 키" 화면:**
   - ⚠️ **중요**: 이 화면에서만 Access Key와 Secret Key를 확인할 수 있습니다!
   - **액세스 키 ID** 복사 → 안전한 곳에 저장 (예: `application-local.yml`에 바로 입력)
   - **비밀 액세스 키 표시** 클릭 → **비밀 액세스 키** 복사 → 안전한 곳에 저장
   - "CSV 다운로드" 클릭하여 백업 (선택사항)
   - "완료" 버튼 클릭

#### 0-3. 프로덕션용 IAM 사용자 생성

1. AWS IAM 콘솔 → "사용자" → "사용자 만들기" 클릭

2. **"사용자 세부 정보 지정" 화면:**
   - **사용자 이름**: `nodap-s3-prod` 입력
   - **AWS Management Console에 대한 사용자 액세스 권한 제공**: ✅ **체크하지 않음**
   - 하단 "다음" 버튼 클릭

3. **"권한 설정" 화면:**
   - **권한 옵션**에서 **"직접 정책 연결"** 라디오 버튼 선택 ✅
   - 검색창에 `NoDapS3BucketAccess` 입력
   - ⚠️ **정책이 안 보이면**: 
     - 정책 이름을 정확히 입력했는지 확인
     - "필터링 기준 유형"에서 "모든 유형" 선택되어 있는지 확인
     - 정책 생성(0-1 단계)을 먼저 완료했는지 확인
   - `NoDapS3BucketAccess` 정책이 보이면 체크박스 선택 ✅
   - "다음" 버튼 클릭

4. **"태그 추가 (선택 사항)" 화면:**
   - 그대로 "다음" 버튼 클릭

5. **"검토 및 생성" 화면:**
   - 설정 확인 후 "사용자 만들기" 버튼 클릭

6. **"액세스 키 모범 사례 및 대안" 화면:**
   - ⚠️ **중요**: 이 화면에서 사용 사례를 선택해야 합니다!
   - **"AWS 컴퓨팅 서비스에서 실행되는 애플리케이션"** 라디오 버튼 선택 ✅
     - 설명: "Amazon EC2, Amazon ECS 또는 AWS Lambda와 같은 AWS 컴퓨팅 서비스에서 실행되는 애플리케이션 코드를 사용하여 AWS 계정에 액세스할 수 있도록 이 액세스 키를 사용할 것입니다."
     - 💡 **참고**: EC2에서 실행되므로 이 옵션을 선택합니다.
   - "다음" 버튼 클릭

7. **"액세스 키 및 비밀 액세스 키" 화면:**
   - ⚠️ **중요**: 프로덕션용 키이므로 더욱 주의깊게 저장하세요!
   - **액세스 키 ID** 복사 → EC2 환경 변수 파일에 사용할 예정
   - **비밀 액세스 키 표시** 클릭 → **비밀 액세스 키** 복사
   - "CSV 다운로드" 클릭하여 백업 (권장)
   - "완료" 버튼 클릭

---

#### 0-4. 정책 연결 (사용자 생성 시 정책을 연결하지 않은 경우)

> 💡 **참고**: 사용자 생성 시 이미 정책을 연결했다면 이 단계는 건너뛰세요.

**개발용 사용자 (`nodap-s3-dev`)에 정책 연결:**

1. IAM 콘솔 → "사용자" → `nodap-s3-dev` 클릭

2. **"권한" 탭** 클릭

3. **"권한 추가"** 버튼 클릭

4. **"권한 추가" 화면:**
   - **권한 옵션**에서 **"직접 정책 연결"** 라디오 버튼 선택 ✅
   - 검색창에 `NoDapS3BucketAccess` 입력
   - `NoDapS3BucketAccess` 정책의 체크박스 선택 ✅
   - "다음" 버튼 클릭

5. **"권한 검토" 화면:**
   - 연결할 정책 확인
   - "권한 추가" 버튼 클릭

**프로덕션용 사용자 (`nodap-s3-prod`)에 정책 연결:**

1. 동일한 방법으로 `nodap-s3-prod` 사용자에 `NoDapS3BucketAccess` 정책 연결

#### 0-5. Access Key 저장

⚠️ **중요**: 각 사용자 생성 시 Access Key와 Secret Key를 안전하게 저장하세요!

- **개발용 키**: `application-local.yml`에 사용
- **프로덕션용 키**: EC2 환경 변수에 사용

---

### 1️⃣ AWS S3 버킷 정책 설정 (필수)

AWS S3 콘솔 → `nodap-images` 버킷 → "권한" 탭 → "버킷 정책" → 다음 정책 추가:

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "PublicReadGetObject",
            "Effect": "Allow",
            "Principal": "*",
            "Action": "s3:GetObject",
            "Resource": "arn:aws:s3:::nodap-images/*"
        }
    ]
}
```

### 2️⃣ EC2 환경 변수 추가 (필수)

EC2 서버에 SSH 접속 후:

```bash
# 환경 변수 파일 편집
nano /home/ubuntu/config/.env
```

**기존 환경 변수에 S3 설정 추가:**

```bash
# 기존 환경 변수들...
MYSQL_USERNAME=...
MYSQL_PASSWORD=...
# ... 기타 설정들 ...

# ⬇️ 아래 S3 설정 추가 (프로덕션용 IAM 사용자 키 사용)
AWS_S3_REGION=ap-northeast-2
AWS_S3_BUCKET=nodap-images
AWS_ACCESS_KEY=AKIA...  # nodap-s3-prod 사용자의 Access Key
AWS_SECRET_KEY=...      # nodap-s3-prod 사용자의 Secret Key
AWS_S3_BASE_URL=https://nodap-images.s3.ap-northeast-2.amazonaws.com
```

### 3️⃣ 서비스 재시작 (필수)

```bash
sudo systemctl daemon-reload
sudo systemctl restart nodap
sudo systemctl status nodap
```

**이제 프로덕션에서도 S3 업로드가 작동합니다!** ✅

> 💡 **보안 강화는 나중에**: 위 3가지만으로도 작동하지만, 보안을 강화하려면 아래 상세 가이드를 참고하세요.

---

## 🔒 1단계: AWS S3 버킷 보안 설정

> ⚠️ **중요**: 아래 보안 설정은 **프로덕션 배포 후**에 적용하세요!  
> 로컬 개발 중에는 이 설정을 하면 이미지 URL 접근이 안 됩니다.

### 1-1. 퍼블릭 액세스 차단 (프로덕션 배포 후 적용)

> 💡 **언제 적용하나요?**
> - ✅ 프로덕션 배포 후 보안 강화 시
> - ❌ 로컬 개발 중에는 적용하지 마세요 (이미지 접근 불가)

1. AWS S3 콘솔 → `nodap-images` 버킷 선택
2. "권한" 탭 → "퍼블릭 액세스 차단 설정"
3. "편집" 클릭
4. **"모든 퍼블릭 액세스 차단" 체크박스 모두 체크** ✅
5. "변경 사항 저장"

⚠️ **주의**: 
- 이렇게 하면 버킷 정책으로 퍼블릭 읽기를 허용해도 차단됩니다.
- **로컬 개발 환경에서는 이미지 URL 접근이 안 됩니다.**
- 프로덕션에서는 CloudFront를 사용하거나 서명된 URL을 사용해야 합니다.

### 1-2. 버킷 정책 제거 또는 제한 (프로덕션 배포 후 적용)

> ⚠️ **중요**: 로컬 개발 중에는 버킷 정책을 유지하세요!  
> 버킷 정책을 제거하면 로컬에서 이미지 접근이 안 됩니다.

**옵션 A: 버킷 정책 완전 제거 (프로덕션 배포 후 - 가장 안전)**
- "권한" 탭 → "버킷 정책" → "편집" → 정책 삭제
- ⚠️ **로컬 개발 중에는 하지 마세요!**

**옵션 B: 특정 IP만 허용 (선택사항)**
```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "AllowSpecificIP",
            "Effect": "Allow",
            "Principal": "*",
            "Action": "s3:GetObject",
            "Resource": "arn:aws:s3:::nodap-images/*",
            "Condition": {
                "IpAddress": {
                    "aws:SourceIp": "YOUR_SERVER_IP/32"
                }
            }
        }
    ]
}
```

### 1-3. CloudFront 사용 (권장)

프로덕션에서는 CloudFront를 통해 이미지를 제공하는 것을 권장합니다:

1. **CloudFront 배포 생성**
   - Origin: S3 버킷 (`nodap-images`)
   - Viewer Protocol Policy: Redirect HTTP to HTTPS
   - Allowed HTTP Methods: GET, HEAD, OPTIONS

2. **base-url 설정**
   ```yaml
   aws:
     s3:
       base-url: https://d1234567890.cloudfront.net  # CloudFront 도메인
   ```

---

## 🔐 2단계: 로컬 개발 환경 설정 업데이트

IAM 사용자를 분리했으므로, 로컬 개발 환경 설정도 업데이트해야 합니다.

### 2-1. application-local.yml 업데이트

`application-local.yml` 파일을 열어서 개발용 IAM 사용자 키로 변경:

```yaml
aws:
  s3:
    region: ap-northeast-2
    bucket: nodap-images
    access-key: AKIA...  # nodap-s3-dev 사용자의 Access Key
    secret-key: "..."    # nodap-s3-dev 사용자의 Secret Key (따옴표로 감싸기)
    base-url: https://nodap-images.s3.ap-northeast-2.amazonaws.com
```

### 2-2. 기존 IAM 사용자 정리 (선택사항)

기존 `nodap-s3-user`를 사용 중이라면:

**옵션 A: 기존 사용자 유지**
- 개발/프로덕션 모두에서 계속 사용 가능
- 보안상 권장하지 않음

**옵션 B: 기존 사용자 삭제 (권장)**
1. IAM 콘솔 → `nodap-s3-user` → "보안 자격 증명" 탭
2. Access Key 비활성화 또는 삭제
3. 사용자 삭제

---

## ⚙️ 3단계: EC2 서버 환경 변수 설정

### 3-1. 환경 변수 파일 생성

EC2 서버에 SSH 접속 후:

```bash
# 환경 변수 파일 생성
sudo nano /home/ubuntu/config/.env
```

### 3-2. S3 관련 환경 변수 추가

⚠️ **중요**: 프로덕션용 IAM 사용자(`nodap-s3-prod`)의 키를 사용하세요!

⚠️ **따옴표 주의**: 환경 변수 파일(.env)에서는 **따옴표를 사용하지 마세요!**  
따옴표를 사용하면 따옴표 자체가 값에 포함되어 인증이 실패할 수 있습니다.

```bash
# AWS S3 설정 (프로덕션용)
# ✅ 올바른 방법: 따옴표 없이 설정
AWS_S3_REGION=ap-northeast-2
AWS_S3_BUCKET=nodap-images
AWS_ACCESS_KEY=AKIA...  # nodap-s3-prod 사용자의 Access Key (따옴표 없음)
AWS_SECRET_KEY=...      # nodap-s3-prod 사용자의 Secret Key (따옴표 없음)
AWS_S3_BASE_URL=https://nodap-images.s3.ap-northeast-2.amazonaws.com

# Swagger 설정 (HTTPS 필수!)
SWAGGER_SERVER_URL=https://answerwithsong.com  # 실제 프로덕션 도메인
SWAGGER_SERVER_DESCRIPTION=프로덕션 API 서버

# ❌ 잘못된 방법: 따옴표 사용 (값에 따옴표가 포함됨)
# AWS_ACCESS_KEY="AKIA..."  # 이렇게 하면 안 됨!
# AWS_SECRET_KEY="..."      # 이렇게 하면 안 됨!
```

> 💡 **참고**: YAML 파일(`application-local.yml`)에서는 따옴표를 사용할 수 있지만,  
> 환경 변수 파일(`.env`)에서는 따옴표를 사용하면 안 됩니다.

> 🔒 **보안**: 개발용 키(`nodap-s3-dev`)는 절대 프로덕션 환경 변수에 넣지 마세요!

### 3-3. 파일 권한 설정

```bash
# 민감 정보이므로 권한 제한
chmod 600 /home/ubuntu/config/.env
```

### 3-4. systemd 서비스 파일 업데이트

⚠️ **중요**: 환경 변수 파일 경로가 올바른지 확인하세요!

```bash
# 현재 설정 확인
sudo systemctl show nodap | grep EnvironmentFile

# 경로가 /home/ubuntu/config/.env로 되어 있다면 수정 필요!
```

**경로 확인:**

```bash
# systemd 서비스 파일 확인
sudo cat /etc/systemd/system/nodap.service
```

**올바른 설정:**

```ini
[Service]
EnvironmentFile=/home/ubuntu/config/.env
ExecStart=/usr/bin/java -jar -Dspring.profiles.active=prod -Dserver.port=8080 /home/ubuntu/backend/nodap-server.jar
```

**수정 확인:**

```bash
# 수정된 경로 확인
sudo cat /etc/systemd/system/nodap.service | grep EnvironmentFile
```

### 3-5. 서비스 재시작

```bash
sudo systemctl daemon-reload
sudo systemctl restart nodap
sudo systemctl status nodap
```

---

## 🧪 4단계: 프로덕션 환경 테스트

### 4-1. 애플리케이션 로그 확인

**방법 1: 최근 로그만 확인 (권장)**
```bash
# 최근 100줄 로그에서 S3 Config 검색
sudo journalctl -u nodap -n 100 | grep "S3 Config"

# 또는 최근 5분간의 로그 확인
sudo journalctl -u nodap --since "5 minutes ago" | grep "S3 Config"
```

**방법 2: 실시간 로그 확인 (Ctrl+C로 종료)**
```bash
# 실시간 로그 확인 (Ctrl+C로 종료)
sudo journalctl -u nodap -f

# 또는 특정 키워드만 필터링
sudo journalctl -u nodap -f | grep --line-buffered "S3 Config"
```

**방법 3: 로그 파일 직접 확인**
```bash
# 로그 파일 확인
sudo journalctl -u nodap -n 100 | grep "S3 Config"
```

다음 메시지가 보여야 합니다:
```
[S3 Config] Access Key: AKIAVSH*** (길이: 20)
[S3 Config] Secret Key 길이: 40
[S3 Config] Bucket: nodap-images
[S3 Config] Region: ap-northeast-2
```

> 💡 **팁**: 만약 로그가 안 보이면 서비스를 재시작하세요:
> ```bash
> sudo systemctl restart nodap
> sudo journalctl -u nodap -n 50 | grep "S3 Config"
> ```

### 4-2. API 테스트

```bash
# Swagger UI 접속 (프로덕션에서는 비활성화 권장)
curl -X POST http://your-server:8080/api/v1/dev/s3/upload/albums/cover \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "file=@test-image.jpg"
```

### 4-3. 이미지 접근 확인

**CloudFront 사용 시:**
```bash
curl -I https://d1234567890.cloudfront.net/albums/cover/uuid.png
# HTTP/2 200 응답 확인
```

**직접 S3 접근 시 (퍼블릭 차단되어 있으면 실패):**
```bash
curl -I https://nodap-images.s3.ap-northeast-2.amazonaws.com/albums/cover/uuid.png
# Access Denied 응답이 정상 (보안상 차단됨)
```

---

## 🔍 5단계: 보안 체크리스트

배포 전 확인사항:

- [ ] **IAM 사용자 분리 완료**
  - [ ] `nodap-s3-dev` 사용자 생성 및 키 발급
  - [ ] `nodap-s3-prod` 사용자 생성 및 키 발급
  - [ ] 두 사용자 모두 `NoDapS3BucketAccess` 정책 연결
  - [ ] 개발용 키는 `application-local.yml`에만 사용
  - [ ] 프로덕션용 키는 EC2 환경 변수에만 사용
- [ ] S3 버킷 퍼블릭 액세스 차단 설정 완료
- [ ] 버킷 정책 제거 또는 제한적 설정
- [ ] 환경 변수 파일 권한 설정 (600)
- [ ] Access Key와 Secret Key가 코드에 하드코딩되지 않음
- [ ] CloudFront 사용 시 base-url 설정 확인
- [ ] 프로덕션 로그에서 민감 정보 노출 확인
- [ ] Swagger UI 비활성화 (선택사항)

---

## 🚨 문제 해결

### 이미지 업로드는 되지만 접근이 안 됨

**원인**: 퍼블릭 액세스가 차단되어 있음

**해결 방법**:
1. CloudFront 사용 (권장)
2. 또는 서명된 URL 생성 (코드 수정 필요)

### S3 Config 로그가 안 보임

**증상**: `sudo journalctl -u nodap -n 50 | grep "S3 Config"` 실행 시 아무것도 안 나옴

**원인 및 해결**:

1. **애플리케이션이 시작되지 않았을 수 있음**
   ```bash
   # 서비스 상태 확인
   sudo systemctl status nodap
   
   # 에러 로그 확인
   sudo journalctl -u nodap -n 100 | grep -i error
   ```

2. **환경 변수가 로드되지 않았을 수 있음**
   ```bash
   # 환경 변수 파일 경로 확인
   sudo systemctl show nodap | grep EnvironmentFile
   
   # 환경 변수 파일 내용 확인
   cat /home/ubuntu/config/.env | grep AWS
   
   # 수동으로 환경 변수 로드 테스트
   source /home/ubuntu/config/.env
   echo $AWS_ACCESS_KEY
   echo $AWS_SECRET_KEY
   ```

3. **애플리케이션이 아직 시작 중일 수 있음**
   ```bash
   # 서비스 재시작 후 잠시 대기
   sudo systemctl restart nodap
   sleep 10
   sudo journalctl -u nodap -n 100 | grep "S3 Config"
   ```

4. **전체 로그 확인**
   ```bash
   # 최근 200줄 로그 전체 확인
   sudo journalctl -u nodap -n 200
   
   # S3 관련 모든 로그 확인
   sudo journalctl -u nodap -n 200 | grep -i s3
   ```

### 환경 변수가 로드되지 않음

**확인 사항**:
```bash
# 환경 변수 확인
sudo systemctl show nodap | grep EnvironmentFile

# 수동으로 환경 변수 로드 테스트
source /home/ubuntu/config/.env
echo $AWS_ACCESS_KEY
```

### IAM 권한 오류

**확인 사항**:
- IAM 사용자에 올바른 정책이 연결되어 있는지
- Access Key가 활성화되어 있는지
- 정책의 Resource ARN이 올바른지

---

## 📚 추가 자료

- [AWS S3 보안 모범 사례](https://docs.aws.amazon.com/AmazonS3/latest/userguide/security-best-practices.html)
- [CloudFront 설정 가이드](https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/GettingStarted.html)
- [IAM 정책 작성 가이드](https://docs.aws.amazon.com/IAM/latest/UserGuide/access_policies.html)

---

## 💡 참고사항

### 개발 환경 vs 프로덕션 환경

| 항목 | 개발 (local) | 프로덕션 (prod) |
|------|-------------|---------------|
| **IAM 사용자** | `nodap-s3-dev` | `nodap-s3-prod` |
| **설정 파일** | `application-local.yml` | 환경 변수 (`.env`) |
| **퍼블릭 액세스** | 허용 (버킷 정책) | 차단 |
| **ACL 설정** | PUBLIC_READ | 설정 안 함 |
| **이미지 접근** | 직접 S3 URL | CloudFront 또는 서명된 URL |
| **IAM 정책** | `NoDapS3BucketAccess` (버킷 제한) | `NoDapS3BucketAccess` (버킷 제한) |
| **Swagger UI** | 활성화 | 비활성화 권장 |

### CloudFront 사용 시 장점

1. **CDN 캐싱**: 전 세계 빠른 이미지 로딩
2. **HTTPS 강제**: 보안 강화
3. **비용 절감**: S3 직접 접근보다 저렴할 수 있음
4. **보안**: S3 버킷을 완전히 비공개로 유지 가능
