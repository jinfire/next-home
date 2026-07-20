# Folder Structure

> 이 문서는 프로젝트 전체 폴더 구조와 각 폴더의 역할을 정리한다.

## 전체 구조

```text
next-home/
├── README.md
├── compose.yaml
├── database/
├── docs/
├── collector/
├── backend/
└── frontend/
```

## docs

기획과 설계 내용을 관리한다.

```text
docs/
├── groundrule.md
├── vision.md
├── features.md
├── architecture.md
├── folder-structure.md
├── database.md
├── roadmap.md
├── decision-log.md
├── todo.md
└── ideas.md
```

## collector

국토교통부 데이터를 수집하고 DB에 저장한다.

```text
collector/
├── gradle/
├── src/main/
├── src/test/
├── build.gradle
├── gradlew.bat
└── README.md
```

세부 구조는 개발을 시작한 뒤 실제 코드에 맞춰 결정한다.

## backend

Spring Boot API 서버다.

```text
backend/
├── gradle/
├── src/main/
├── src/test/
├── build.gradle
├── gradlew.bat
└── README.md
```

초기에는 기능별로 단순하게 나눈다.

예시:

```text
region/
apartment/
trade/
grade/
transfer/
```

## frontend

React 화면을 관리한다.

```text
frontend/
├── src/
│   ├── test/
│   ├── App.tsx
│   └── App.test.tsx
├── package.json
├── vite.config.ts
└── README.md
```

## 원칙

- 모든 코드 작업이 끝나면 관련 문서를 함께 업데이트한다.
- 세부 작업 규칙은 `groundrule.md`를 따른다.
- 처음부터 복잡하게 나누지 않는다.
- 실제 기능이 생길 때 폴더를 추가한다.
- 현재 구조와 코드가 달라지면 이 문서를 수정한다.
