# Frontend

## 갈아타기 비교

현재 급지를 선택하면 `/api/recommendations/upgrades`에서 1·2급지 위 평균 평단가, 현재 격차와 과거 격차 백분위를 읽어 온다. 개인 대출 정보나 추가 자금은 입력받지 않는다.

## Next Home 급지 지도

메인 화면은 네이버 Dynamic Map과 연도별 지역 급지 목록을 보여준다. 루트 `.env`의 `NAVER_MAP_CLIENT_ID`를 사용하며 지도 Client Secret은 프런트엔드에 포함하지 않는다. 개발 서버의 `/api` 요청은 `http://localhost:8080`으로 전달된다.

```powershell
npm run dev
npm test
npm run build
```

급지 지도를 메인으로 제공하는 React + TypeScript 애플리케이션이다.

## 실행

```powershell
npm install
npm run dev
```

개발 주소는 `http://localhost:5173`이다.

## 검증

```powershell
npm test
npm run build
npm run lint
```
