# Next Home

지역별 급지와 평균 평단가, 지역 간 가격 격차를 바탕으로 다음 주거지를 탐색하는 서비스다.

## 구성

- `frontend`: React + TypeScript + Vite
- `backend`: Spring Boot API
- `collector`: 국토교통부 실거래가 수집기
- `database`: PostgreSQL + PostGIS 초기화
- `docs`: 제품·기술 문서와 작업 규칙

## 로컬 실행

1. `.env.example`을 참고해 `.env`를 준비한다.
2. `docker compose up -d`로 PostgreSQL + PostGIS를 실행한다.
3. `backend/gradlew.bat bootRun`으로 API를 실행한다.
4. `collector/gradlew.bat bootRun`으로 필요할 때 수집기를 실행한다.
5. `frontend`에서 `npm install`, `npm run dev`를 실행한다.

## 테스트

```powershell
cd backend
.\gradlew.bat test

cd ..\collector
.\gradlew.bat test

cd ..\frontend
npm test
```

세부 개발 규칙은 `docs/groundrule.md`를 따른다.
