# [Spring 3기] FirstBeat Project
![image.jpg](https://github.com/RealFirstBeat/FirstBeat/blob/readme/image/image.jpg)

## 📁 프로젝트 소개

- 바쁜 현대인들을 위한 사용자 맞춤형 음악 추천 서비스 기능 앱
- 개인이 선호하는 장르를 선택하고, 그에 맞는 음악을 외부 API인 Spotify API를 이용해 곡을 추천해줍니다.
- 추천 곡 리스트 중 마음에 드는 곡은 내가 만든 플레이리스트에 추가할 수 있고, 마음에 들지 않으면 스킵하여 사용자의 취향에 맞게 점차 리스트를 만들도록 했습니다.
- 타인이 만든 플레이리스트를 키워드 기반으로 검색할 수 있고, 검색 횟수를 집계하여 결과값이 높은 키워드를 검색창으로 알 수 있습니다.
  
<br>

## 팀원 구성
<br>

<div align="center">

| **장문석** | **안예원** | **장용환** |
| :------: |  :------: | :------: |
| [<img src="" height=150 width=150> <br/> @jangms3](https://github.com/jangms3) | [<img src="https://github.com/RealFirstBeat/FirstBeat/blob/readme/image/%EA%B0%95%EC%95%84%EC%A7%80.jpg" height=150 width=150> <br/> @yaewon0441](https://github.com/yaewon0411) | [<img src="https://github.com/RealFirstBeat/FirstBeat/blob/readme/image/%EA%B3%B0.jpg" height=150 width=150> <br/> @Hae-Dal](https://github.com/Hae-Dal) |


| **이시우** | **류지수** |
| :------: |  :------: |
| [<img src="https://github.com/RealFirstBeat/FirstBeat/blob/readme/image/%EA%B3%A0%EC%96%91%EC%9D%B4.jpg" height=150 width=150> <br/> @matino0216](https://github.com/matino0216) | [<img src="https://github.com/RealFirstBeat/FirstBeat/blob/readme/image/%ED%86%A0%EB%81%BC.jpg" height=150 width=150> <br/> @dameun0527](https://github.com/dameun0527) |


</div>
<br>

## 📋 목차
- [1. 개발 환경](#1-개발-환경)
- [2. 채택 개발 기술 및 브랜치 전략](#2-채택-개발-기술-및-브랜치-전략)
- [3. 프로젝트 구조](#3-프로젝트-구조)
- [4. 역할 분담](#4-역할-분담)
- [5. 개발 기간 및 작업 관리](#5-개발-기간-및-작업-관리)
- [6. 구현 기능 소개](#6-구현-기능-소개)
- [7. 트러블 슈팅](#7-트러블-슈팅)
- [8. 개선할만한 사항](#8-개선할만한-사항)
- [9. 프로젝트 후기](#9-프로젝트-후기)

<br>

### 1. 개발 환경
<ul>
 <li><Strong>Server 및 Back-end</Strong>: <img src="https://img.shields.io/badge/java-007396?style=for-the-badge&logo=java&logoColor=white">
<img src="https://img.shields.io/badge/springboot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white">
<img src="https://img.shields.io/badge/gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white"> 
<img src="https://img.shields.io/badge/redis-FF4438?style=for-the-badge&logo=redis&logoColor=white">
 </li>
</ul>
<ul>
  <li><Strong>버전 및 이슈 관리</Strong>: <a href="https://github.com/RealFirstBeat/FirstBeat"><img src="https://img.shields.io/badge/github-181717?style=for-the-badge&logo=github&logoColor=white">
   </li>
</ul>
<ul>
  <li><Strong>협업 툴</Strong>: <img src="https://img.shields.io/badge/Notion-%23000000.svg?style=for-the-badge&logo=notion&logoColor=white">
   </li>
</ul>
<ul>
  <li>디자인(와이어프레임): <img src="https://img.shields.io/badge/figma-%23F24E1E.svg?style=for-the-badge&logo=figma&logoColor=white"
   </li>
</ul>
<ul>
  <li>외부 Open API: <img src="https://img.shields.io/badge/Spotify-1ED760?style=for-the-badge&logo=spotify&logoColor=white">
   </li>
</ul>

<br>

### 2. 채택 개발 기술 및 브랜치 전략

#### Spring Security, JWT Token

- Spring Security
  - 인증 및 인가를 쉽게 구현할 수 있는 강력한 보안 프레임워크.
  - 역할 기반 접근 제어(Role-Based Access Control)를 지원하여 유저 권한 관리가 용이.
  - 커스터마이징 가능: 프로젝트 요구 사항에 맞춘 보안 로직을 간단히 추가할 수 있음
- JWT Token
  - Stateless 인증 방식으로, 토큰 자체에 인증 정보를 포함하여 클라이언트-서버 간 데이터 전송 최소화.
  - 확장성이 뛰어남.

#### Data JPA, Redis, Caffeine cache

- Data JPA
  - CRUD 작업과 쿼리 작성의 반복을 줄여줌.
- Redis
  - 빠른 데이터 처리가 필요한 캐싱 및 실시간 데이터 관리에 적합.
  - 유연성과 성능을 보장하며, 데이터 만료(Expiration)을 쉽게 설정 가능.
  - 실시간 이벤트 처리나 메시지 큐로 활용 가능.
- Caffeine cache
  - 높은 성능과 낮은 지연시간을 제공하는 Java 기반의 캐싱 라이브러리.
  - Redis와 달리 메모리 내 캐싱을 제공하여 빠른 응답 시간 요구에 적합.
  - 간단한 설정으로도 캐시 만료, 용량 초과 처리 등 고급 기능 사용 가능.

#### H2

- 개발 및 테스트 환경에서 사용하기 좋은 In-Memory 데이터베이스.
- 간편한 설정으로 빠르게 테스트 가능한 개발 환경 제공.
- SQL과 관계형 데이터베이스를 사용하는 코드 테스트에 적합하며, 실제 DB로 쉽게 전환 가능.

#### 브랜치 전략

- Git-flow 전략을 기반으로 main, dev 브랜치와 feature 보조 브랜치 운용
- main, dev, feat 브랜치로 나누어 개발
  - **main**
  - **dev**: 개발 단계에서 git-flow의 main 역할
  - **feat**: 기능 단위로 독립적인 개발 환경을 위해 사용.

<br>


### 3. 프로젝트 구조

<br>

<details>
<summary> 프로젝트 구조 </summary>

```
FirstBeat
├─ image.jpg
├─ README.md
└─ src
   ├─ main
   │  ├─ java.com.my.firstbeat
   │  │     ├─ client
   │  │     │  └─ spotify
   │  │     │     ├─ config
   │  │     │     │  ├─ env
   │  │     │     │  │  └─ EnvLoader.java
   │  │     │     │  └─ SpotifyConfig.java
   │  │     │     ├─ dto
   │  │     │     │  └─ response
   │  │     │     │     ├─ RecommendationResponse.java
   │  │     │     │     └─ TrackSearchResponse.java
   │  │     │     ├─ ex
   │  │     │     │  ├─ ErrorCode.java
   │  │     │     │  └─ SpotifyApiException.java
   │  │     │     ├─ handler
   │  │     │     │  └─ SpotifyExceptionHandler.java
   │  │     │     ├─ SpotifyApiCall.java
   │  │     │     ├─ SpotifyClient.java
   │  │     │     └─ SpotifyTokenManager.java
   │  │     ├─ FirstBeatApplication.java
   │  │     └─ web
   │  │        ├─ config
   │  │        │  ├─ async
   │  │        │  │  ├─ AsyncConfig.java
   │  │        │  │  └─ AsyncProperties.java
   │  │        │  ├─ cache
   │  │        │  │  └─ CacheConfig.java
   │  │        │  ├─ jwt
   │  │        │  │  ├─ JwtAuthenticationFilter.java
   │  │        │  │  ├─ JwtAuthorizationFilter.java
   │  │        │  │  ├─ JwtExceptionFilter.java
   │  │        │  │  ├─ JwtUtil.java
   │  │        │  │  └─ JwtVo.java
   │  │        │  ├─ RecommendationConfig.java
   │  │        │  ├─ redis
   │  │        │  │  ├─ RedisConfig.java
   │  │        │  │  ├─ RedissonConfig.java
   │  │        │  │  └─ SchedulerConfiguration.java
   │  │        │  └─ security
   │  │        │     ├─ handler
   │  │        │     │  ├─ CustomAccessDeniedHandler.java
   │  │        │     │  ├─ CustomAuthenticationEntryPoint.java
   │  │        │     │  └─ SecurityResponseHandler.java
   │  │        │     ├─ loginuser
   │  │        │     │  ├─ dto
   │  │        │     │  │  ├─ LoginRequest.java
   │  │        │     │  │  └─ LoginResponse.java
   │  │        │     │  ├─ LoginUser.java
   │  │        │     │  └─ LoginUserService.java
   │  │        │     └─ SecurityConfig.java
   │  │        ├─ controller
   │  │        │  ├─ genre
   │  │        │  │  ├─ dto
   │  │        │  │  │  ├─ request
   │  │        │  │  │  │  └─ GenreCreateRequest.java
   │  │        │  │  │  └─ response
   │  │        │  │  │     └─ GenreCreateResponse.java
   │  │        │  │  └─ GenreController.java
   │  │        │  ├─ playlist
   │  │        │  │  ├─ dto
   │  │        │  │  │  ├─ request
   │  │        │  │  │  │  ├─ PlaylistCreateRequest.java
   │  │        │  │  │  │  └─ PlaylistSearchRequest.java
   │  │        │  │  │  └─ response
   │  │        │  │  │     ├─ PaginationInfo.java
   │  │        │  │  │     ├─ PlaylistCreateResponse.java
   │  │        │  │  │     ├─ PlaylistResponse.java
   │  │        │  │  │     ├─ PlaylistRetrieveResponse.java
   │  │        │  │  │     ├─ PlaylistsData.java
   │  │        │  │  │     ├─ PlaylistSearchResponse.java
   │  │        │  │  │     └─ TrackListResponse.java
   │  │        │  │  ├─ PlaylistController.java
   │  │        │  │  └─ SearchController.java
   │  │        │  ├─ track
   │  │        │  │  ├─ dto
   │  │        │  │  │  ├─ request
   │  │        │  │  │  │  ├─ TrackCreateRequest.java
   │  │        │  │  │  │  └─ TrackRequestDto.java
   │  │        │  │  │  └─ response
   │  │        │  │  │     ├─ TrackCreateResponse.java
   │  │        │  │  │     └─ TrackRecommendationResponse.java
   │  │        │  │  └─ TrackController.java
   │  │        │  └─ user
   │  │        │     ├─ dto
   │  │        │     │  ├─ request
   │  │        │     │  │  ├─ JoinRequest.java
   │  │        │     │  │  ├─ SignupRequestDto.java
   │  │        │     │  │  └─ UpdateMyPageRequest.java
   │  │        │     │  ├─ response
   │  │        │     │  │  ├─ GetMyPageResponse.java
   │  │        │     │  │  ├─ JoinResponse.java
   │  │        │     │  │  └─ UpdateMyPageResponse.java
   │  │        │     │  └─ valid
   │  │        │     │     ├─ PasswordValidator.java
   │  │        │     │     └─ ValidPassword.java
   │  │        │     └─ UserController.java
   │  │        ├─ domain
   │  │        │  ├─ base
   │  │        │  │  └─ BaseEntity.java
   │  │        │  ├─ genre
   │  │        │  │  ├─ Genre.java
   │  │        │  │  └─ GenreRepository.java
   │  │        │  ├─ playlist
   │  │        │  │  ├─ InMemoryPopularSearchCache.java
   │  │        │  │  ├─ Playlist.java
   │  │        │  │  ├─ PlaylistRepository.java
   │  │        │  │  ├─ PopularSearchCache.java
   │  │        │  │  └─ RedisPopularSearchCache.java
   │  │        │  ├─ playlistTrack
   │  │        │  │  ├─ PlaylistTrack.java
   │  │        │  │  └─ PlaylistTrackRepository.java
   │  │        │  ├─ track
   │  │        │  │  ├─ Track.java
   │  │        │  │  └─ TrackRepository.java
   │  │        │  ├─ user
   │  │        │  │  ├─ Role.java
   │  │        │  │  ├─ User.java
   │  │        │  │  └─ UserRepository.java
   │  │        │  └─ userGenre
   │  │        │     ├─ UserGenre.java
   │  │        │     └─ UserGenreRepository.java
   │  │        ├─ dummy
   │  │        │  └─ DummyObject.java
   │  │        ├─ ex
   │  │        │  ├─ BusinessException.java
   │  │        │  ├─ ErrorCode.java
   │  │        │  └─ ServerException.java
   │  │        ├─ handler
   │  │        │  └─ GlobalExceptionHandler.java
   │  │        ├─ service
   │  │        │  ├─ GenreService.java
   │  │        │  ├─ InMemorySearchService.java
   │  │        │  ├─ PlaylistService.java
   │  │        │  ├─ PlaylistSwipeService.java
   │  │        │  ├─ recommemdation
   │  │        │  │  ├─ lock
   │  │        │  │  │  ├─ metric
   │  │        │  │  │  │  ├─ LockExecutionMetrics.java
   │  │        │  │  │  │  └─ LockMetricsConstants.java
   │  │        │  │  │  └─ RedisLockManager.java
   │  │        │  │  ├─ property
   │  │        │  │  │  ├─ LockProperties.java
   │  │        │  │  │  ├─ RecommendationProperties.java
   │  │        │  │  │  └─ RecommendationRefreshTask.java
   │  │        │  │  ├─ RecommendationService.java
   │  │        │  │  ├─ RecommendationServiceWithoutLock.java
   │  │        │  │  └─ RecommendationServiceWithRedis.java
   │  │        │  ├─ search
   │  │        │  │  └─ SearchServiceConfig.java
   │  │        │  ├─ SearchService.java
   │  │        │  ├─ TrackService.java
   │  │        │  └─ UserService.java
   │  │        └─ util
   │  │           └─ api
   │  │              ├─ ApiError.java
   │  │              └─ ApiResult.java
   │  └─ resources
   │     ├─ application-dev.yaml
   │     ├─ application-test.yaml
   │     └─ application.yaml
```

</details>

<br>

### 4. 역할 분담
#### [문석]
- 플레이리스트의 default 플레이리스트 설정 및 가져오기 , 플레이리스트 변경 기능   
- 스와이프 기능을 통해 사람들의 즉각적인 반응을 분석

<br>

#### [예원]
- 초기 프로젝트 설정 진행(시큐리티, Spotify API 호출 메서드 추상화 및 예외 처리 등)
- 트랙 추천 기능
   - v1: 인메모리 캐시(caffiene)
   - v2: 인메모리 캐시(caffiene) + ReentrantLock
   - v3: Redis를 사용한 추천 트랙 데이터 캐싱과 분산락(Redisson) 
- 백그라운드 추천 트랙 갱신 기능
- 분산락 획득 성공/실패에 대한 메트릭 수집 기능

<br>

#### [용환]
- 마이페이지
  - 조회
  - 수정
- 플레이리스트 검색
  - v1: RDBMS
  - v2: 인메모리 캐시 + RDBMS
- nGrinder 테스트 실행

<br>

#### [시우]
- 회원가입
  - 이메일 중복 검사
  - 장르 유효성 검사
- 플레이리스트 단건 삭제 기능

<br>

[지수]
- 플레이리스트
  - 플레이리스트 생성
  - 내가 만든 플레이리스트 조회
- 본인이 작성한 기능의 모든 단건 테스트

<br>

### 5. 개발 기간 및 작업 관리

- 기간: 2024/11/22 ~ 202411/29 (총 7일)

  <br>
  
- 작업 관리
  - Github의 Issue 사용 -> 구현 기능 및 커밋을 알아보기 쉽게 정리
  - 매일 오후 8시 30분 ~ 9시 사이에 진행상황 공유 및 목표 확인

<br>

### 6. 구현 기능 소개
#### [스와이프 기능]
- Spotify API에 의해서 트랙을 추천받기.
- 추천받은 트랙을 사용자에게 제공.
- 제공 받은 트랙을 like 시 플레이 리스트에 추가.
- 플레이 리스트가 없는 경우, 기본 플레이리스트를 자동으로 생성.
- 제공 받은 트랙을 skip 할 시 추천 트랙에서 제거.
- 스킵된 트랙은 이후 추천에서 제외.

<br>

#### [트랙 추천 기능]
- Spotify API에 사용자의 선호도 데이터를 시드값으로 담아 그에 맞는 트랙 추천 리스트 요청
- 해당 리스트를 캐싱해서 임계치(5개) 이하가 되기 전까지 캐싱한 데이터로 빠른 반환 시도 (따라서 총 트랙 추천 기능 15회 이용 동안: 1회의 Spotify API 호출 + 14회의 캐시를 통한 빠른 반환)
- 분산락을 통한 Spotify API 중복 호출 방어

<br>

#### [백그라운드 추천 트랙 갱신]
- 매일 새벽 3시에 실행
- 활성 사용자 대상으로 캐시에 임계치 이하의 데이터가 있을 경우 추천 트랙 갱신 진행
- 백그라운드 작업을 위한 스레드풀 설정 내에서 병렬로 작업 진행

<br>

#### [플레이리스트 검색 기능 캐싱]
- 검색어 키워드, 검색량, 검색 결과 데이터 캐싱
- 일정 검색량 마다 검색 결과 데이터를 최신화

<br>

#### [로그인 / 회원가입 기능]
- 회원 가입 시 중복된 이메일을 입력할 경우 예외처리
- Spotify api 내에서 갖고 있는 장르 목록 중 최소 3가지 선택해야함
- 이메일, 비밀번호, 이름, 장르 유효성 검증 로직 구현

<br>

#### [마이페이지 조회 / 수정 기능]
- 이메일 유효성 검증 로직 구현
- 장르 불일치 시 예외처리
- 이름, 또는 선택한 장르 추가 및 삭제 가능

<br>

### 7. 트러블 슈팅
**[문석]**
- Swipe 기능 구현 도중  Controller를 어디다 만들어서 책임분리에 관해 어떻게 처리할지에 관한 고민이 있었습니다.

<br>
  
**[예원]**
- nGrinder 스크립트 오류로 테스트를 위한 설정에 예상했던 것보다 많은 시간이 들었던 것 같습니다

<br>

**[용환]**
- h2 데이터베이스가 안 익숙해서 테스트하는데 좀 힘들었습니다...

<br>

**[시우]**
- 테스트 코드를 처음 작성해보았는데 시간이 많이 소요되었지만 요구사항을 검증하면서 더욱 탄탄하게 개발할 수 있었습니다.

<br>

**[지수]**
- 캐시 및 Redis에 대한 이해도 부족으로 처음 계획했던 기능을 제시간 안에 구현하지 못했습니다.

<br>

### 8. 전체적 회고
#### 잘된 점
- nGrinder를 적극적으로 활용하면서 기술 선택에 대한 이유와 적용이후 성능 지표를 볼 수 있어서 좋았다.
- 실제 데이터를 적용해서 프로젝트를 작성한 부분이 인상깊었다.
<br>

#### 개선할만한 점
- 백그라운드 트랙 갱신 스케줄러 서버 분리
- 플레이리스트 검색 캐싱 기능 remote cache로 변경

<br>

#### 고민해볼 만한 점
- 외부연동에 대한 Fail 처리 혹은 성능이슈에 대한 고민이 있었는지 
- 스케줄러의 서버 분리에 대한 고민이 필요해 보인다. 

<br>

### 9. 프로젝트 후기

#### [문석]
개인상의 문제로 프로젝트 직전에 팀에 합류하여 소원할 줄 알았지만, 소통이 잘되어서 재밌고 유익한 시간이였습니다. :>

<br>

#### [예원]
다양한 시나리오를 예상하며 여러 기능을 붙여가는게 재밌었습니다~

<br>

#### [용환]
시간 분배가 잘 되지 않아서 힘들었지만 좋은 경험이 된 것 같습니다~ :)

<br>

#### [시우]
캐싱도 팀원들과 함께 공부하여 즐겁게 개발 할 수 있었습니다!

<br>

#### [지수]
제게는 새로운 개념인 캐시에 대한 공부와, 실력이 좋은 팀원의 코드 리뷰를 통해 얻게 된 지식이 가장 비중이 큰 것 같습니다. 끝까지 책임감 있게 마무리하지 못한 것 같아 조금 아쉽지만 좋은 팀원들을 만나서 어떻게든 결과물을 만들어내서 뿌듯하기도 합니다.





