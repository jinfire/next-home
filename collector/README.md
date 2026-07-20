# Collector

국토교통부 아파트 매매 실거래가 API를 수집해 PostgreSQL에 저장하는 Spring Boot 애플리케이션이다.

## 실행

```powershell
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

실제 공공데이터 게이트웨이 호출 검증은 인증 응답이 정상화된 뒤 진행한다.

## 테스트

```powershell
.\gradlew.bat test
```
