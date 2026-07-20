# Frontend

## 서버 알림 조건 연결

지도에서 선택한 현재 지역과 급지, 사용자가 입력한 최대 평당 격차를 서버용 격차율로 변환한다. 1급지 위와 2급지 위 조건을 `/api/alerts`에 저장하므로 페이지나 브라우저가 닫힌 뒤에도 서버 Web Push를 받을 수 있다.

## Service Worker Push 구독

웹 알림을 켜면 `/sw.js`를 등록하고 VAPID 공개키로 PushManager 구독을 만든다. 구독은 백엔드에 저장되며 Push 수신 시 시스템 알림을 표시하고 클릭하면 Next Home 창을 연다.

## 웹 알림

현재 급지와 최대 평당 격차를 설정하면 페이지가 열린 동안 한 시간마다 조건을 확인한다. 조건 충족 시 브라우저 Notification API로 알리며 설정은 현재 장치의 `localStorage`에 저장된다. 브라우저가 닫힌 상태의 Web Push는 아직 후속 작업이다.

## 같은 생활권 아파트 추천

현재 아파트를 검색하고 선택하면 같은 행정구역의 더 높은 평균 평단가 단지를 최대 10개 표시한다. 추천의 “상급”은 가격 데이터 기준이며 생활 품질을 단정하지 않는다.

## 갈아타기 비교

현재 급지를 선택하면 `/api/recommendations/upgrades`에서 1·2급지 위 평균 평단가, 현재 격차와 과거 격차 백분위를 읽어 온다. 개인 대출 정보나 추가 자금은 입력받지 않는다.

## Next Home 급지 지도

메인 화면은 네이버 Dynamic Map과 연도별 지역 급지 목록을 보여준다. 루트 `.env`의 `NAVER_MAP_CLIENT_ID`를 사용하며 지도 Client Secret은 프런트엔드에 포함하지 않는다. 개발 서버의 `/api` 요청은 `http://localhost:8080`으로 전달된다.

Dynamic Map SDK는 지도 영역이 화면 가까이에 들어올 때 지연 로딩하며, 페이지 안에서 script와 SDK 로드 요청을 한 번만 공유한다.

기준 연도의 행정구역 GeoJSON이 있으면 NAVER Maps Data 레이어에서 1~10급지 색상으로 표시한다. 연도 변경 시 경계도 함께 갱신된다.

지도 상단의 지역 검색은 `/api/regions` 결과를 현재 연도 급지 목록과 연결해 선택 요약과 알림 대상을 갱신한다.

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
