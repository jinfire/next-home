# Architecture

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
