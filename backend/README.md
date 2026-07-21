# Backend

## 행정구역 경계 API

```http
GET /api/region-boundaries?year=2026
```

PostGIS 경계가 존재하고 요청 연도의 급지가 계산된 지역만 `application/geo+json` FeatureCollection으로 반환한다.

### VWorld 경계 1회 적재

루트 `.env`에 VWorld 키와 등록 도메인을 설정하고, PostgreSQL/PostGIS 컨테이너가 실행 중일 때 전용 명령을 사용한다.

```powershell
.\gradlew.bat importVworldBoundaries
```

명령은 VWorld WFS를 한 번 호출하고, 기존 `region.code`와 일치하는 시군구 경계를 PostGIS에 신규 저장하거나 갱신한 뒤 자동 종료한다. 평상시 `bootRun`은 VWorld를 호출하지 않으며 별도의 활성화 환경변수도 필요 없다.

실제 검증에서는 현재 DB의 종로구 경계 1건이 적재됐고 유효한 SRID 4326 MultiPolygon임을 확인했다. `.env`의 키는 Git에서 제외한다.

## Geocoding 좌표 보강

좌표가 없는 아파트는 기본 1시간 주기로 최대 50건씩 처리한다. 같은 정규화 주소는 DB 캐시를 재사용하며, 코드 내부 한도인 일 900건 또는 월 18,000건에 도달하면 NAVER API를 호출하지 않는다. 한도와 배치 크기는 `NAVER_GEOCODING_*` 환경 변수로 더 낮출 수 있다. 운영 중 상향할 때도 NAVER Cloud 콘솔의 최종 한도를 먼저 확인해야 한다.

## 알림 평가 및 Web Push 발송

활성 조건은 기본 한 시간마다 평가한다. `targetGapPercent`와 `historicalGapPercentile` 중 설정된 임계값을 모두 만족하면 같은 `browserId`의 구독으로 알림을 전송한다. 동일 조건은 24시간에 한 번만 발송한다. 평가 간격은 `app.alerts.check-interval-ms`로 조정할 수 있다.

## Web Push 구독 API

```http
GET /api/push-subscriptions/vapid-public-key
POST /api/push-subscriptions
```

POST 요청에는 `browserId`, `endpoint`, `p256dh`, `auth`를 전달한다. VAPID 공개키·개인키·subject는 각각 `VAPID_PUBLIC_KEY`, `VAPID_PRIVATE_KEY`, `VAPID_SUBJECT` 환경변수로 관리한다.

## 갈아타기 알림 조건 등록 API

```http
POST /api/alerts
Content-Type: application/json

{
  "browserId": "550e8400-e29b-41d4-a716-446655440000",
  "currentRegionId": 10,
  "targetGrade": 3,
  "targetGapPercent": 15,
  "historicalGapPercentile": 30
}
```

`targetRegionId` 또는 `targetGrade` 중 하나는 필수다. 등록 성공 시 `201 Created`와 저장된 조건을 반환한다. 조건 평가와 브라우저 알림 발송은 별도 후속 단계다.

## 같은 생활권 내 상급 아파트 추천 API

```http
GET /api/recommendations/apartments?apartmentId=10&year=2026
```

선택 연도의 취소되지 않은 실거래를 기준으로, 현재 아파트와 같은 행정구역에서 평균 평단가가 더 높은 단지를 최대 10개 반환한다. 각 결과에는 평균 평단가, 유효 거래 수, 현재 아파트 대비 평당 격차가 포함된다. 해당 연도에 현재 아파트의 유효 거래가 없으면 `404 Not Found`를 반환한다.

Next Home의 조회·분석·추천·알림 API를 제공하는 Spring Boot 애플리케이션이다.

## 실행

루트의 PostgreSQL 컨테이너를 먼저 실행한 뒤 다음 명령을 사용한다.

```powershell
.\gradlew.bat bootRun
```

루트 `.env`의 `BACKEND_PORT`를 읽으며 예시 로컬 포트는 `28080`이다.

## 테스트

```powershell
.\gradlew.bat test
```

테스트는 로컬 PostgreSQL과 분리된 인메모리 DB 프로필로 실행된다.

## API

### 지역 검색

```http
GET /api/regions?query=강남
```

지역명 부분 일치 결과를 이름순 최대 20건 반환한다. 빈 검색어는 `400 Bad Request`다.

### 아파트 검색

```http
GET /api/apartments?query=래미안&regionId=1
```

아파트명 부분 검색 결과를 최대 20건 반환하며 `regionId`는 선택 항목이다.

### 아파트 실거래 조회

```http
GET /api/apartments/10/trades
```

해당 아파트의 최근 거래를 계약일 역순 최대 100건 반환한다. 해제 거래는 `cancelled`로 구분한다.

### 연도별 급지

```http
POST /api/grades/recalculate?year=2026
GET /api/grades?year=2026
```

해제되지 않은 실거래의 평균 평단가로 지역을 정렬해 1~10급지를 계산하고 조회한다.

### 1·2급지 위 추천

```http
GET /api/recommendations/upgrades?currentGrade=5&year=2026
```

현재 급지만 입력받아 1급지·2급지 위의 평균 평단가와 현재 격차, 역사적 격차 백분위를 반환한다. `0%`는 가장 작은 격차, `100%`는 가장 큰 격차다.
