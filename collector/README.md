# Collector

국토교통부 아파트 매매 실거래가 API를 수집해 PostgreSQL에 저장하는 Spring Boot 애플리케이션이다.

## 실행

```powershell
.\gradlew.bat bootRun
```

루트 `.env`의 국토교통부 API 설정과 DB 설정을 사용한다. API 수집 로직은 TDD로 단계적으로 구현한다.

## 테스트

```powershell
.\gradlew.bat test
```
