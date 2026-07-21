# Collector

## 수도권 전체 기간 수집

루트 `.env`에서 수집 범위를 설정한다.

```env
CAPITAL_COLLECTION_START_MONTH=2026-01
CAPITAL_COLLECTION_END_MONTH=2026-06
CAPITAL_COLLECTION_ROWS=500
```

PostGIS와 수도권 경계 적재가 끝난 뒤 실행한다.

```powershell
.\gradlew.bat collectCapitalAreaTrades
```

서울·경기·인천의 전체 시군구와 설정 기간을 순차 처리하고 자동 종료한다. 재실행 시 source key가 같은 거래는 중복으로 기록하지 않는다.

국토교통부 아파트 매매 실거래가 API를 수집해 PostgreSQL에 저장하는 Spring Boot 애플리케이션이다.

## 실행

```powershell
.\gradlew.bat bootRun
```

기본 상태에서는 외부 API를 호출하지 않는다. 특정 지역과 월을 수집할 때만 다음 환경변수를 설정한다.

```powershell
$env:COLLECTOR_ENABLED='true'
$env:COLLECTOR_REGION_CODE='11110'
$env:COLLECTOR_REGION_NAME='종로구'
$env:COLLECTOR_MONTH='2026-01'
$env:COLLECTOR_ROWS='100'
.\gradlew.bat bootRun
```

루트 `.env`의 국토교통부 API 설정과 DB 설정을 사용한다. API 수집 로직은 TDD로 단계적으로 구현한다.

## 구현 상태

- 법정동 코드와 계약월 기준 아파트 매매 조회
- 페이지 번호와 조회 건수 지정
- XML 성공·오류 응답 검증
- 거래금액(만원)을 원 단위로 정규화
- 계약일, 전용면적, 층, 건축연도, 해제일 파싱
- Region과 Apartment를 없을 때만 생성
- 거래 source key를 이용한 DB 중복 방지
- 동일 응답 페이지 안의 중복 거래 방지
- Region·Apartment·Trade를 한 트랜잭션으로 저장
- API totalCount에 따라 마지막 페이지까지 자동 수집
- 페이지별 저장 결과와 중복 건수 합산
- 명시적으로 활성화한 경우에만 외부 API 호출

## 실제 API 검증 상태

2026-07-20에 종로구(11110) 2026년 1월 자료를 실제 호출했다. 인증 응답 `000/OK`, 전체 46건을 확인했고 한 페이지 호출로 중복 1건을 제외한 거래 45건과 아파트 26개가 로컬 PostgreSQL에 저장됐다. 실제 응답의 해제일 `yy.MM.dd` 형식도 회귀 테스트로 고정했다.

## 테스트

```powershell
.\gradlew.bat test
```
