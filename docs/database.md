# Database

> 이 문서는 데이터베이스 구조와 테이블 관계를 정리한다.

## 기본 원칙

- 원본 거래 데이터는 가능한 한 그대로 보존한다.
- 분석 결과는 원본 데이터에서 다시 계산할 수 있어야 한다.
- MVP에서는 꼭 필요한 테이블만 만든다.
- 가격 요약 테이블은 성능 문제가 생긴 뒤 검토한다.

## ERD

ERD는 데이터베이스 테이블과 테이블 사이의 관계를 보여주는 그림이다.

```text
Region
  │ 1
  │
  │ N
Apartment
  │ 1
  │
  │ N
Trade

Region
  │ 1
  │
  │ N
RegionGrade

AlertCondition
  │ N
  │
  │ 1
Region

PushSubscription
  │ N
  │
  │ 1
Browser

Apartment
  │ 1
  │
  │ N
AlertCondition
```

## Region

행정구역 정보를 저장한다.

주요 필드:

- id
- code
- name
- parent_id
- level
- boundary (`geometry(MultiPolygon, 4326)`)

## Apartment

아파트의 기본 정보를 저장한다.

주요 필드:

- id
- region_id
- name
- address
- latitude
- longitude
- build_year
- household_count
- location (`geography(Point, 4326)`)

## Trade

개별 실거래 정보를 저장한다.

주요 필드:

- id
- apartment_id
- contract_date
- price
- exclusive_area
- floor
- contract_type
- buyer_type
- seller_type
- source_key
- cancellation_date

## RegionGrade

연도별 지역 급지 분석 결과를 저장한다.

주요 필드:

- id
- region_id
- year
- average_price_per_pyeong
- grade
- trade_count
- calculated_at

## 아직 저장하지 않는 데이터

다음 값은 초기에는 API 요청 시 계산하거나 배치 결과로 처리한다.

- 갈아타기 추천
- 지역 간 격차
- 갈아타기 지수

성능 문제가 생기면 별도 분석 테이블 추가를 검토한다.

## AlertCondition

사용자가 등록한 갈아타기 알림 조건을 저장한다.

주요 필드:

- id
- current_region_id
- target_region_id
- target_grade
- target_gap_percent
- historical_gap_percentile
- enabled
- last_triggered_at
- created_at

브라우저 UUID로 사용자를 식별하며 웹 푸시 구독은 `PushSubscription`에 별도로 저장한다.

## PushSubscription

브라우저 웹 푸시 발송에 필요한 구독 정보를 저장한다.

주요 필드:

- id
- browser_id
- endpoint
- p256dh_key
- auth_key
- created_at
- updated_at

## 마이그레이션

- 스키마는 Backend의 Flyway 마이그레이션으로 관리한다.
- `V1__create_core_schema.sql`이 PostGIS 확장과 핵심 테이블·인덱스를 생성한다.
- 공간 컬럼에는 GiST 인덱스를 적용한다.
- 실제 PostGIS 컨테이너를 사용하는 Testcontainers 통합 테스트로 마이그레이션을 검증한다.
