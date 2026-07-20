# Folder Structure

## Frontend E2E additions

- `frontend/playwright.config.ts`: Chromium 프로젝트와 Vite 테스트 서버 설정
- `frontend/e2e/main-flow.spec.ts`: 급지 지도·검색·갈아타기 핵심 흐름
- `frontend/test-results`, `frontend/playwright-report`: Git에서 제외되는 실행 결과

## Region search frontend additions

- `frontend/src/components/RegionSearch.tsx`: 지역 검색 폼·결과·오류 상태
- `frontend/src/components/RegionSearch.test.tsx`: 검색·선택·빈 결과 테스트

## Region boundary overlay additions

- `backend/.../region/RegionBoundaryService.java`: PostGIS 경계와 급지를 GeoJSON으로 결합
- `backend/.../region/RegionBoundaryController.java`: 연도별 경계 API
- `frontend/src/components/NaverMap.tsx`: NAVER Maps Data 레이어 급지 색상 표시

## Dynamic Map usage protection additions

- `frontend/src/components/NaverMap.tsx`: 뷰포트 기반 지연 로딩과 단일 SDK·지도 인스턴스 관리
- `frontend/src/components/naverMapSdk.ts`: 컴포넌트와 분리된 공유 SDK 로더
- `frontend/src/components/NaverMap.test.tsx`: SDK 중복 요청 방지와 지연 로딩 회귀 테스트

## Geocoding additions

- `backend/.../geocoding`: 주소 정규화, 좌표 캐시, 영속 호출 예산, NAVER 게이트웨이, 아파트 좌표 보강 작업
- `backend/.../db/migration/V2__add_geocoding_cache_and_api_budget.sql`: 캐시와 외부 API 사용량 스키마
- `backend/.../geocoding` 테스트: 캐시·예산·PostGIS 저장·배치 흐름 검증

## Web Push additions

- `backend/.../push`: Push 구독 API, 서비스, JPA 엔티티와 저장소
- `frontend/public/sw.js`: 백그라운드 Push 수신과 시스템 알림 처리
- `.env.example`: 실제 비밀값이 없는 로컬 환경변수 템플릿

> 이 문서는 프로젝트 전체 폴더 구조와 각 폴더의 역할을 정리한다.

## 전체 구조

```text
next-home/
├── README.md
├── compose.yaml
├── database/
├── docs/
├── collector/
├── backend/
└── frontend/
```

## docs

기획과 설계 내용을 관리한다.

```text
docs/
├── groundrule.md
├── vision.md
├── features.md
├── architecture.md
├── folder-structure.md
├── database.md
├── roadmap.md
├── decision-log.md
├── todo.md
└── ideas.md
```

## collector

국토교통부 데이터를 수집하고 DB에 저장한다.

```text
collector/
├── gradle/
├── src/main/
├── src/test/
├── build.gradle
├── gradlew.bat
└── README.md
```

세부 구조는 개발을 시작한 뒤 실제 코드에 맞춰 결정한다.

## backend

Spring Boot API 서버다.

```text
backend/
├── gradle/
├── src/main/
├── src/test/
├── build.gradle
├── gradlew.bat
└── README.md
```

초기에는 기능별로 단순하게 나눈다.

예시:

```text
region/
apartment/
trade/
grade/
transfer/
```

## frontend

React 화면을 관리한다.

```text
frontend/
├── src/
│   ├── test/
│   ├── App.tsx
│   └── App.test.tsx
├── package.json
├── vite.config.ts
└── README.md
```

## 원칙

- 모든 코드 작업이 끝나면 관련 문서를 함께 업데이트한다.
- 세부 작업 규칙은 `groundrule.md`를 따른다.
- 처음부터 복잡하게 나누지 않는다.
- 실제 기능이 생길 때 폴더를 추가한다.
- 현재 구조와 코드가 달라지면 이 문서를 수정한다.
