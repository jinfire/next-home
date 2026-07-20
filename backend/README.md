# Backend

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

루트 `.env`를 선택적으로 읽으며 기본 포트는 `8080`이다.

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
