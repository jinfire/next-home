# Database

## 실제 수도권 거래·급지 검증 (2026-07-21)

- 2026년 6월 수도권 83개 지역 API 순회 완료
- 신규 실거래 23,032건, 중복 제외 1,178건
- 2026년 `region_grade` 82개 지역 생성
- 옹진군은 해당 수집 월 거래가 없어 경계만 제공하고 급지는 null

## 수도권 지역 계층 검증 (2026-07-21)

- `region.level=1`: 서울특별시, 경기도, 인천광역시
- `region.level=2`: 수도권 시군구 83개
- `region.parent_id`: 모든 시군구가 소속 시도를 참조
- 실제 경계 수: 서울 25, 경기 47, 인천 11
- 모든 적재 경계는 SRID 4326 MultiPolygon으로 정규화

## 실제 수집 검증 데이터

2026-07-20 로컬 PostGIS에 종로구 코드 `11110`, 2026년 1월 아파트 26개와 중복 제거된 거래 45건을 실제 API에서 적재해 저장 흐름을 검증했다. 이 데이터는 개발용 로컬 볼륨에만 존재하며 Git에는 포함되지 않는다.

수집 후 백엔드 재계산 API를 실제 호출해 2026년 `region_grade` 1건을 생성했으며, 현재 단일 지역이므로 종로구는 1급지로 계산됐다.

## Region boundary 조회 규칙

- `region.boundary`는 SRID 4326의 MultiPolygon이다.
- 경계 API는 `region_grade.year`가 일치하고 `boundary IS NOT NULL`인 지역만 노출한다.
- PostGIS `ST_AsGeoJSON`으로 변환하므로 프런트엔드에서 별도 좌표계 변환을 수행하지 않는다.
- VWorld 수집 시 유효하지 않은 도형을 보정하고 Polygon 계열만 추출해 MultiPolygon 제약을 지킨다.
- 2026-07-20 실제 WFS 적재 결과: 종로구 `11110`, `ST_MultiPolygon`, SRID 4326, `ST_IsValid=true`, 면적 약 23.99㎢.

## GeocodingCache와 ExternalApiUsage

- `geocoding_cache`: 정규화 주소를 기본 키로 사용하고 도로명 주소, 경도, 위도, `geography(Point, 4326)`를 보관한다.
- `external_api_usage`: 공급자·API·기간 종류·기간 시작일별 호출 수를 보관한다. 조건부 upsert로 한도 이내에서만 카운터를 증가시킨다.
- `V2__add_geocoding_cache_and_api_budget.sql`이 두 테이블과 공간 인덱스를 생성한다.

## alert_condition 평가 규칙

- `target_gap_percent`: 목표 평균 평단가와 현재 지역 평균 평단가의 격차율 상한
- `historical_gap_percentile`: 현재 격차가 과거 분포에서 차지하는 백분위 상한
- `last_triggered_at`: 동일 조건의 24시간 중복 발송을 막는 기준 시각
- 동일 브라우저·현재 지역·목표 지역/급지 조건은 새 행을 만들지 않고 갱신한다.

## push_subscription 사용 규칙

- `endpoint`는 브라우저 Push 구독의 고유키이며 중복 등록 시 기존 레코드를 갱신한다.
- `browser_id`는 같은 장치의 알림 조건과 구독을 연결한다.
- `p256dh_key`와 `auth_key`는 Web Push payload 암호화에 사용한다.
- VAPID 개인키는 데이터베이스에 저장하지 않고 서버 환경변수로만 관리한다.

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

계산 규칙:

- 거래별 평단가는 `가격(원) × 3.305785 ÷ 전용면적(㎡)`이다.
- 계약 해제 거래는 제외한다.
- 같은 연도의 지역 평균 평단가를 내림차순 정렬해 1~10급지를 부여한다.

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
