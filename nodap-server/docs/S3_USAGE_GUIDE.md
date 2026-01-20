# S3Service 사용 가이드

## 개요

`S3Service`는 AWS S3에 이미지를 업로드하고 삭제하는 기능을 제공하는 서비스입니다.

## 주요 기능

- 이미지 파일 업로드 (jpg, jpeg, png, gif, webp)
- 파일 크기 제한 (최대 10MB)
- 고유한 파일명 생성 (UUID 사용)
- 파일 삭제 기능
- 폴더별 이미지 관리

## 폴더 구조

S3 버킷 내 이미지는 다음 폴더 구조로 관리됩니다:

```
nodap-images/
├── songs/              # 노래 이미지
├── albums/
│   ├── cover/         # 앨범 커버 이미지
│   └── lp/            # LP 원형 이미지
└── users/
    └── profile/       # 사용자 프로필 이미지
```

## 사용 방법

### 1. S3Service 주입

```java
@Service
@RequiredArgsConstructor
public class MusicService {
    private final S3Service s3Service;
    // ...
}
```

### 2. 이미지 업로드

#### 방법 1: S3Folder enum 사용 (권장)

```java
public String uploadSongImage(MultipartFile imageFile) throws IOException {
    // S3Folder enum 사용 - 타입 안전성 보장
    String imageUrl = s3Service.uploadImage(imageFile, S3Folder.SONGS);
    return imageUrl;
}

public String uploadAlbumCover(MultipartFile imageFile) throws IOException {
    return s3Service.uploadImage(imageFile, S3Folder.ALBUM_COVER);
}

public String uploadLpImage(MultipartFile imageFile) throws IOException {
    return s3Service.uploadImage(imageFile, S3Folder.ALBUM_LP);
}

public String uploadProfileImage(MultipartFile imageFile) throws IOException {
    return s3Service.uploadImage(imageFile, S3Folder.USER_PROFILE);
}
```

#### 방법 2: 커스텀 폴더 경로 사용

```java
public String uploadCustomImage(MultipartFile imageFile) throws IOException {
    // 커스텀 폴더 경로 사용
    String customPath = "albums/custom/2025";
    String imageUrl = s3Service.uploadImage(imageFile, customPath);
    return imageUrl;
}
```

### 3. 이미지 삭제

```java
public void deleteImage(String imageUrl) {
    // S3에서 이미지 삭제
    s3Service.deleteFile(imageUrl);
}
```

### 4. 전체 예시: Music 생성 시 이미지 업로드

```java
@Service
@RequiredArgsConstructor
@Transactional
public class MusicService {
    private final MusicRepository musicRepository;
    private final AlbumRepository albumRepository;
    private final S3Service s3Service;

    public Music createMusic(Long albumId, MusicCreateRequest request) throws IOException {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ALBUM_NOT_FOUND));

        // 이미지 업로드
        String imageUrl = null;
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            imageUrl = s3Service.uploadImage(request.getImage(), S3Folder.SONGS);
        } else {
            // 기본 이미지 URL 설정 (선택사항)
            imageUrl = "https://nodap-images.s3.ap-northeast-2.amazonaws.com/default/song-default.jpg";
        }

        // Music 엔티티 생성
        Music music = Music.builder()
                .album(album)
                .title(request.getTitle())
                .artist(request.getArtist())
                .message(request.getMessage())
                .url(request.getUrl())
                .writer(request.getWriter())
                .image(imageUrl)  // S3 URL 저장
                .posX(request.getPosX())
                .posY(request.getPosY())
                .cardLength(request.getCardLength())
                .build();

        return musicRepository.save(music);
    }

    public void deleteMusic(Long musicId) {
        Music music = musicRepository.findById(musicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MUSIC_NOT_FOUND));

        // S3에서 이미지 삭제
        if (music.getImage() != null) {
            s3Service.deleteFile(music.getImage());
        }

        // DB에서 삭제
        musicRepository.delete(music);
    }
}
```

## 예외 처리

### 지원하지 않는 파일 형식

```java
try {
    String imageUrl = s3Service.uploadImage(file, S3Folder.SONGS);
} catch (IllegalArgumentException e) {
    // "지원하지 않는 이미지 형식입니다. (jpg, jpeg, png, gif, webp만 가능)"
    return ResponseEntity.badRequest().body("지원하지 않는 파일 형식입니다.");
}
```

### 파일 크기 초과

```java
try {
    String imageUrl = s3Service.uploadImage(file, S3Folder.SONGS);
} catch (IllegalArgumentException e) {
    // "파일 크기는 10MB를 초과할 수 없습니다."
    return ResponseEntity.badRequest().body("파일 크기가 너무 큽니다. (최대 10MB)");
}
```

### S3 업로드 실패

```java
try {
    String imageUrl = s3Service.uploadImage(file, S3Folder.SONGS);
} catch (RuntimeException e) {
    // "S3 업로드 실패: ..."
    log.error("S3 업로드 실패", e);
    return ResponseEntity.status(500).body("이미지 업로드에 실패했습니다.");
}
```

## 지원하는 파일 형식

- `jpg`, `jpeg`
- `png`
- `gif`
- `webp`

## 파일 크기 제한

- 최대 크기: **10MB**

## 반환되는 URL 형식

업로드된 이미지의 URL은 다음과 같은 형식입니다:

```
https://nodap-images.s3.ap-northeast-2.amazonaws.com/songs/uuid.jpg
https://nodap-images.s3.ap-northeast-2.amazonaws.com/albums/cover/uuid.png
https://nodap-images.s3.ap-northeast-2.amazonaws.com/users/profile/uuid.webp
```

## 주의사항

1. **이미지가 필수가 아닌 경우**: `null` 체크 후 업로드
2. **기존 이미지 교체 시**: 기존 이미지 삭제 후 새 이미지 업로드
3. **엔티티 삭제 시**: S3에서도 이미지 삭제
4. **트랜잭션 롤백 시**: S3에 업로드된 파일은 자동으로 삭제되지 않으므로 주의

## 트랜잭션 처리 예시

```java
@Transactional
public Music createMusicWithImage(MusicCreateRequest request) throws IOException {
    String imageUrl = null;
    
    try {
        // 1. 이미지 업로드
        if (request.getImage() != null) {
            imageUrl = s3Service.uploadImage(request.getImage(), S3Folder.SONGS);
        }
        
        // 2. DB에 저장
        Music music = Music.builder()
                .image(imageUrl)
                // ... 다른 필드들
                .build();
        
        return musicRepository.save(music);
        
    } catch (Exception e) {
        // DB 저장 실패 시 S3에서 이미지 삭제
        if (imageUrl != null) {
            try {
                s3Service.deleteFile(imageUrl);
            } catch (Exception deleteException) {
                log.error("S3 이미지 삭제 실패: {}", imageUrl, deleteException);
            }
        }
        throw e;
    }
}
```

## 관련 파일

- `S3Service.java`: S3 업로드/삭제 서비스 구현
- `S3Config.java`: S3 클라이언트 빈 설정
- `S3Properties.java`: S3 설정 프로퍼티
- `S3Folder.java`: 폴더 경로 enum

## 참고 문서

- [S3 설정 가이드](./S3_SETUP_GUIDE.md): AWS S3 초기 설정 방법
