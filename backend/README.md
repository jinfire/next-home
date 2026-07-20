# Backend

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
