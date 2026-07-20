# TODO

## Upgrade comparison frontend update (2026-07-20)

- [x] 현재 급지만 입력하는 비교 화면
- [x] 1·2급지 위 평균 평단가 표시
- [x] 현재 평당 격차 표시
- [x] 과거 대비 현재 격차 백분위 표시
- [x] 대출·추가 자금 계산 제외

## Grade map frontend update (2026-07-20)

- [x] 네이버 Dynamic Map SDK 로딩
- [x] 연도별 급지 API 연동
- [x] 지역별 급지·평균 평단가·실거래 수 목록
- [x] 지역 선택 요약 및 반응형 화면
- [ ] 지도 위 행정구역 경계 색상 표시(경계 데이터 수집 후)

## Alert condition registration update (2026-07-20)

- [x] 갈아타기 알림 조건 등록 API
- [x] 목표 지역 또는 목표 급지 필수 검증
- [x] 급지 1~10 및 과거 격차 백분위 0~100 검증
- [ ] 저장된 알림 조건 주기적 평가
- [ ] 브라우저 Push 구독 및 알림 발송

## Lifestyle recommendation update (2026-07-20)

- [x] 같은 행정구역 내 상급 아파트 추천 API
- [x] 취소 거래 제외 및 연도별 평균 평단가 계산
- [x] 현재 아파트 대비 평단가 격차 제공
- [x] 최대 10개, 평균 평단가 내림차순 정렬

> 이 문서는 지금 실제로 해야 할 작업만 관리한다.

## 문서

- [x] 핵심 문서 구조 정리
- [x] 기능 정리
- [x] 전체 아키텍처 정리
- [x] DB와 ERD 초안 작성
- [x] 개발 순서 정리
- [x] 주요 결정 기록
- [x] 실제 개발에 맞춰 문서 수정

## 프로젝트 구성

- [x] Git 저장소 생성
- [x] collector 폴더 생성
- [x] backend 폴더 생성
- [x] frontend 폴더 생성
- [x] PostgreSQL 로컬 환경 구성
- [x] Backend 단위 테스트 환경 구성
- [x] Backend PostGIS 통합 테스트 환경 구성
- [x] Frontend 컴포넌트 테스트 환경 구성
- [ ] Frontend E2E 테스트 환경 구성
- [x] 네이버 Maps Application 등록 및 로컬 인증정보 설정
- [x] 네이버 Maps 대표 계정 여부 확인
- [x] Dynamic Map 한도 설정: 일 10,000건, 월 100,000건
- [x] Geocoding 한도 설정: 일 1,000건, 월 20,000건
- [ ] 네이버 Maps 사용량 70% 이메일 알림 설정
- [ ] Geocoding 일·월 호출 예산 저장 및 차단 로직 TDD 구현
- [ ] 주소 중복 제거와 좌표 재사용 로직 TDD 구현
- [ ] Dynamic Map 단일 인스턴스 및 지연 로딩 테스트 작성

## Collector

- [x] 국토교통부 API 키 준비
- [ ] API 응답 확인
- [x] API 요청 클라이언트 구현
- [x] XML 성공·오류 응답 파싱
- [x] 거래금액·면적·계약일 정규화
- [x] totalCount 기반 전체 페이지 수집
- [x] 안전한 수동 실행 설정
- [x] Region 저장
- [x] Apartment 저장
- [x] Trade 저장
- [x] 중복 거래 처리

## Backend

- [x] Spring Boot 프로젝트 생성
- [x] DB 연결 설정
- [x] Flyway 핵심 스키마 마이그레이션
- [x] 지역 검색 API
- [x] 아파트 검색 API
- [x] 실거래 조회 API
- [x] 급지 조회 API
- [x] 급지별 평균 평단가 조회 API
- [x] 1급지·2급지 위 지역 추천 API
- [x] 현재 및 과거 대비 격차 조회 API
- [ ] 같은 생활권 내 상급 아파트 추천 API
- [ ] 갈아타기 알림 조건 등록 API
- [ ] 알림 조건 확인 작업

## Frontend

- [x] React 프로젝트 생성
- [x] 지도 라이브러리 선택: 네이버 Web Dynamic Map
- [ ] 지역 검색 화면
- [ ] 급지 지도 화면
- [ ] 지도에서 급지와 평균 평단가 표시
- [ ] 현재 급지 입력 화면
- [ ] 1급지·2급지 위 추천 결과 화면
- [ ] 현재 및 과거 대비 격차 표시
- [ ] 같은 생활권 내 아파트 추천 화면
- [ ] 갈아타기 알림 조건 설정 화면
