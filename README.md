# AIBE2-Project2-Team05-BE

# 🧳 TravelMate - Backend

> **여행 동반자 매칭 플랫폼 백엔드 레포지토리**

본 프로젝트는 여행을 계획 중인 사용자들이
비슷한 일정·지역·관심사를 가진 동행자나 가이드를 매칭하고
함께 여행을 떠날 수 있도록 돕는 **여행 동반자 매칭 플랫폼**의 백엔드 서버입니다.

---

## 🌐 주요 기능 (백엔드 기준)

* **회원가입/로그인/로그아웃 (JWT 기반 인증)**
* **역할 기반 권한 관리 (여행자 / 가이드 / 관리자)**
* **여행 일정 등록 및 여행 피드 자동 생성**
* **동행자/가이드 검색 및 매칭 요청/수락 로직**
* **개인 및 그룹 채팅 기능 (WebSocket)**
* **여행 후기 작성 및 사용자 평가 시스템**
* **자유 게시판 (모집형 글 게시/조회)**
* **신고/블라인드 처리 및 관리자 기능**

---

## 🏗 기술 스택

| 항목        | 기술                             |
| --------- | ------------------------------ |
| Language  | Java 17                        |
| Framework | Spring Boot 3.x                |
| ORM       | Spring Data JPA                |
| DB        | MySQL                          |
| Auth      | Spring Security + JWT          |
| API       | RESTful API 설계                 |
| 채팅        | WebSocket                      |
| 문서화       | Swagger(OpenAPI) 예정            |
| ERD 설계    | dbdiagram.io                   |
| 협업 관리     | Git / GitHub Projects / Notion |

---

## 📁 주요 폴더 구조

```
📦src
 ┣ 📂main
 ┃ ┣ 📂java/com/travelmate
 ┃ ┃ ┣ 📂user         // 회원가입, 로그인, 프로필
 ┃ ┃ ┣ 📂plan         // 여행 일정 등록
 ┃ ┃ ┣ 📂feed         // 여행 피드 게시
 ┃ ┃ ┣ 📂match        // 동행자/가이드 매칭
 ┃ ┃ ┣ 📂chat         // 채팅 (WebSocket)
 ┃ ┃ ┣ 📂review       // 후기 작성
 ┃ ┃ ┣ 📂admin        // 관리자 기능
 ┃ ┃ ┗ 📂common       // 예외 처리, 응답 포맷 등
 ┃ ┗ 📂resources
 ┃   ┗ application.yml
```

---

## ⚙️ 로컬 실행 방법

```bash
git clone https://github.com/your-org/travelmate-backend.git
cd travelmate-backend
./gradlew bootRun
```

> `.env` 또는 `application.yml`에 MySQL 및 JWT 설정이 필요합니다.

---

## 🧑‍💻 팀원 (백엔드)

| 이름 | 역할    | 주요 담당                     |
| -- | ----- | ------------------------- |
| A  | 백엔드 1 | 회원가입, 여행 일정, 피드           |
| B  | 백엔드 2 | 매칭 시스템, 후기, 채팅            |
| C  | 백엔드 3 | 관리자 기능, 신고 관리, 정지/블라인드 처리 |

---

## ✅ 현재 진행 상황

* [x] ERD 설계 완료
* [x] 회원가입/로그인 기능 구현
* [x] 여행 일정 등록 기능 완료
* [ ] 매칭 기능 개발 중
* [ ] 채팅 시스템 연동 예정
* [ ] 관리자 페이지 개발 예정

---

## 🚀 향후 계획

* AI 기반 관심사/일정 자동 매칭 로직
* 후기 기반 신뢰도 지수 가중치 설계
* 유닛 테스트 및 통합 테스트 강화
* CI/CD 환경 구축 (선택)
