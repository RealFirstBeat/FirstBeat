# [Spring 3기] FirstBeat Project
![image.jpg](image.jpg)

## 📁 프로젝트 소개


## 팀원 구성


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

### 2. 채택 개발 기술 및 브랜치 전략


#### Spring Security, JWT Token

- Spring Security
  - 보안
- JWT Token
  - 토큰

#### Data JPA, Redis, Caffeine cache

- Data JPA
  - 쿼리
- Redis
  - 캐시
- Caffeine cache
  - 캐시

#### H2

- DB

#### 브랜치 전략

- Git-flow 전략을 기반으로 main, dev 브랜치와 feature 보조 브랜치 운용
- main, dev, feat 브랜치로 나누어 개발
  - **main**
  - **dev**
  - **feat**

<br>


### 3. 프로젝트 구조

<details>
<summary> 프로젝트 구조 </summary>

```

```

</details>

### 4. 역할 분담

[예원]
- 초기 프로젝트 설정 진행(시큐리티, Spotify API 호출 메서드 추상화 및 예외 처리 등)
- 트랙 추천 기능
   - v1: 인메모리 캐시(caffien)
   - v2: 인메모리 캐시(caffien) + ReentrantLock
   - v3: Redis를 사용한 추천 트랙 데이터 캐싱과 분산락(Redisson) 
- 백그라운드 추천 트랙 갱신 기능
- 분산락 획득 성공/실패에 대한 메트릭 수집 기능


### 5. 개발 기간 및 작업 관리
기간: 2024/11/23 ~ 202411/29 (총 7일)
작업 관리: 깃허브 및 슬랙


### 6. 구현 기능 소개

[트랙 추천 기능]
- Spotify API에 사용자의 선호도 데이터를 시드값으로 담아 그에 맞는 트랙 추천 리스트 요청
- 해당 리스트를 캐싱해서 임계치(5개) 이하가 되기 전까지 캐싱한 데이터로 빠른 반환 시도 (따라서 총 트랙 추천 기능 15회 이용 동안: 1회의 Spotify API 호출 + 14회의 캐시를 통한 빠른 반환)
- 분산락을 통한 Spotify API 중복 호출 방어

[백그라운드 추천 트랙 갱신]
- 매일 새벽 3시에 실행
- 활성 사용자 대상으로 캐시에 임계치 이하의 데이터가 있을 경우 추천 트랙 갱신 진행
- 백그라운드 작업을 위한 스레드풀 설정 내에서 병렬로 작업 진행


### 7. 트러블 슈팅

[예원]
- nGrinder 스크립트 오류로 테스트를 위한 설정에 예상했던 것보다 많은 시간이 들었던 것 같습니다


### 8. 개선할만한 사항

- 백그라운드 트랙 갱신 스케줄러 서버 분리

### 9. 프로젝트 후기

[예원]
다양한 시나리오를 예상하며 여러 기능을 붙여가는게 재밌었습니다~

