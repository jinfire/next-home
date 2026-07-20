# Architecture

## Frontend E2E test flow

Playwright가 Vite 개발 서버를 별도 포트에서 실행하고 Chromium으로 실제 DOM 상호작용을 수행한다. `/api/**`는 테스트 라우트가 고정 응답을 제공해 백엔드나 유료 가능성이 있는 외부 서비스에 의존하지 않는다. Vitest는 `e2e/**`를 제외해 단위 테스트 러너와 Playwright 러너를 분리한다.

## Region search frontend flow

`RegionSearch`가 제출된 검색어를 URL 인코딩해 `/api/regions`에 요청한다. 선택 결과는 `App`의 연도별 급지 목록과 `regionId`로 결합되며, 일치할 때 지도 하단 선택 요약과 알림 대상 지역을 함께 갱신한다.

## Region boundary overlay flow

Spring Boot가 PostGIS `region.boundary`와 `region_grade`를 연도 기준으로 결합해 `application/geo+json`을 생성한다. React 지도는 SDK 초기화 후 이 API를 요청하고 NAVER Maps Data 레이어에 추가한다. 연도 변경 시 기존 Feature를 제거한 뒤 급지별 색상과 반투명 채우기를 다시 적용한다.

## Dynamic Map lazy-load flow

`NaverMap`은 `IntersectionObserver`로 지도 영역의 접근을 감지한 뒤 공유 SDK 로더를 호출한다. 로더는 모듈 범위 Promise와 `data-next-home-map` script 하나를 재사용하므로 React 재렌더링이나 여러 소비자가 Dynamic Map 로드를 중복 발생시키지 않는다.

## Geocoding cache and budget flow

`ApartmentGeocodingJob`이 좌표가 없는 아파트를 작은 배치로 조회한다. `GeocodingService`는 정규화 주소 캐시를 먼저 확인하고, 캐시 미스일 때만 `PersistentGeocodingBudget`에서 일·월 예산을 원자적으로 예약한 뒤 NAVER API를 호출한다. 성공한 좌표는 캐시와 `apartment.location`에 저장한다. 예산 소진 시 호출을 중단하므로 브라우저 트래픽이 외부 API 사용량을 직접 늘릴 수 없다.

## Background alert evaluation and delivery

`AlertDispatchJob`은 활성 `alert_condition`을 조회하고 `JdbcAlertMarketDataProvider`가 `region_grade` 이력에서 현재 격차율과 과거 백분위를 만든다. `AlertConditionMatcher`가 임계값을 판정하면 `PushNotificationService`가 같은 `browser_id`의 모든 구독으로 JSON payload를 보낸다. `JavaWebPushGateway`는 webpush-java와 VAPID 키로 암호화·서명하며 성공한 조건은 `last_triggered_at`을 기록해 24시간 쿨다운을 적용한다.

## Web Push subscription flow

백엔드는 `VAPID_PUBLIC_KEY`만 `/api/push-subscriptions/vapid-public-key`로 제공한다. 브라우저는 `/sw.js`를 등록하고 PushManager 구독을 만든 뒤 endpoint, p256dh, auth와 장치별 UUID를 백엔드에 전송한다. 서버는 공개 구독정보를 PostgreSQL에 upsert한다. `VAPID_PRIVATE_KEY`는 서버 환경에만 존재하며 후속 발송기가 Push 서비스 요청을 서명할 때 사용한다.

## Foreground web notification flow

`AlertPanel`은 현재 급지와 최대 평당 격차를 장치 로컬 저장소에 보관한다. 페이지가 열려 있을 때 `/api/recommendations/upgrades`를 한 시간마다 조회해 조건을 평가하고 브라우저 Notification API를 호출한다. 이는 외부 메시지 서비스 비용 없이 동작하는 1단계이며, 백그라운드 알림은 기존 `push_subscription` 테이블과 Service Worker를 후속 연결한다.

## Frontend lifestyle recommendation flow

`LifestylePanel`이 `/api/apartments`로 현재 아파트 후보를 검색한다. 사용자가 단지를 선택하면 아파트 ID와 지도 기준 연도를 `/api/recommendations/apartments`로 전달하고, 동일 `region_id` 안에서 평균 평단가가 더 높은 결과를 렌더링한다.

## Frontend upgrade comparison flow

React의 `UpgradePanel`은 현재 급지와 지도에서 선택한 연도만 상태로 관리한다. 급지를 선택하면 `/api/recommendations/upgrades`를 호출하고 반환된 1·2급지 위 결과를 카드로 렌더링한다. 금융 상황은 서버에 전송하지 않으며 대출·추가 자금 계산 계층도 두지 않는다.

## Frontend grade map flow

Vite는 루트 `.env`의 `NAVER_MAP_CLIENT_ID`만 프런트 빌드에 주입하고 Client Secret은 노출하지 않는다. React가 네이버 Dynamic Map SDK를 한 번 로드하고 `/api/grades?year={year}`를 호출해 지역 급지 목록을 구성한다. 개발 환경의 `/api` 요청은 Vite 프록시를 통해 Spring Boot `8080` 포트로 전달한다.

## Alert condition registration flow

`POST /api/alerts`가 브라우저 식별자, 현재 지역, 목표 지역 또는 목표 급지와 선택적인 격차 조건을 검증한다. Spring Data JPA가 검증된 조건을 기존 `alert_condition` 테이블에 저장한다. 조건 평가는 별도 주기 작업으로, 실제 알림 전달은 `push_subscription`과 Web Push 발송 계층으로 분리한다.

## Lifestyle apartment recommendation flow

`GET /api/recommendations/apartments?apartmentId={id}&year={year}`는 선택 연도의 유효 실거래를 집계한다. Spring Boot가 아파트별 평균 평단가를 계산한 뒤 현재 아파트와 동일한 `region_id`에 속하면서 평균 평단가가 더 높은 후보만 골라 최대 10개를 반환한다. 취소 거래는 집계에서 제외하며 별도 요약 테이블 없이 PostgreSQL 조회 결과를 사용한다.

> 이 문서는 전체 시스템 구성과 데이터 흐름을 정리한다.

## 전체 구조

```text
국토교통부 실거래가 API
          │
          ▼
       Collector
          │
          ▼
      PostgreSQL
          │
          ▼
     Spring Boot API
          │
          ▼
        React
```

## 구성요소

### Collector

- 국토교통부 API에서 실거래 데이터를 가져온다.
- 지역, 아파트, 거래 데이터를 정리한다.
- PostgreSQL에 저장한다.
- 주기적으로 실행한다.
- WebClient로 법정동 코드·계약월 단위 API를 호출하고 XML 응답을 도메인 값으로 정규화한다.
- 외부 엔티티와 DTD를 차단한 보안 XML 파서를 사용한다.
- Region·Apartment·Trade를 한 트랜잭션으로 저장하고 source key로 재수집을 멱등 처리한다.
- totalCount와 페이지 크기로 전체 페이지를 계산해 순차 수집한다.
- Collector는 기본 비활성화하며 지역코드와 계약월을 명시한 일회성 작업으로 실행한다.

### PostgreSQL

- 원본 데이터와 분석 데이터를 저장한다.
- 지역, 아파트, 거래, 급지 정보를 관리한다.
- 관심 지역과 갈아타기 알림 조건을 관리한다.
- 네이버 Geocoding 결과를 저장해 같은 주소의 중복 API 호출을 방지한다.
- Flyway로 스키마 버전을 관리하고 PostGIS Testcontainers로 마이그레이션을 검증한다.

### Spring Boot

- 프론트엔드가 사용할 API를 제공한다.
- 급지, 평균 평단가, 격차, 상급지 추천, 생활권 추천을 계산하거나 조회한다.
- 사용자의 알림 조건을 등록하고 목표 격차 도달 여부를 확인한다.
- 웹 푸시 구독 정보를 관리하고 조건 도달 시 알림을 발송한다.
- Geocoding을 서버에서만 호출하고 일·월 호출 예산을 검사한다.
- 호출 예산이 소진되면 외부 API 요청을 차단하고 저장된 좌표 또는 대체 데이터를 사용한다.
- Backend와 Collector를 독립된 Spring Boot 4.0.5 애플리케이션으로 실행한다.
- `/api/regions`에서 지역명 부분 검색 결과를 최대 20건 제공한다.
- `/api/apartments`에서 지역 필터를 선택적으로 적용한 아파트명 검색을 제공한다.
- `/api/apartments/{id}/trades`에서 최근 실거래 최대 100건을 제공한다.
- `/api/recommendations/upgrades`에서 현재 급지만으로 1·2급지 위 평균 평단가와 현재·과거 격차를 제공한다.

### React

- 급지 지도를 메인 화면으로 제공한다.
- 급지와 격차 데이터를 시각화한다.
- 1급지·2급지 위 추천, 생활권 추천, 알림 조건 설정 화면을 제공한다.
- 서비스 워커를 통해 브라우저 웹 알림을 수신한다.
- React 19, TypeScript, Vite를 사용하고 Vitest로 컴포넌트를 검증한다.

## 원칙

- Collector와 API 서버는 분리한다.
- 원본 데이터와 분석 데이터를 구분한다.
- MVP에서는 불필요한 캐시나 요약 테이블을 만들지 않는다.
- 네이버 Maps 호출은 콘솔에 설정한 무료 이용 한도 안에서만 허용한다.
- 콘솔 한도와 백엔드 호출 예산을 함께 사용해 이중으로 제한한다.
