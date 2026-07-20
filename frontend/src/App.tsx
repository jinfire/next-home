import './App.css'

function App() {
  return (
    <main className="app-shell">
      <header className="topbar">
        <a className="brand" href="/">Next Home</a>
        <span className="status">데이터 기반 갈아타기</span>
      </header>

      <section className="hero-copy">
        <p className="eyebrow">YOUR NEXT NEIGHBORHOOD</p>
        <h1>급지 지도로 시작하세요</h1>
        <p>지역별 평균 평단가와 급지를 한눈에 보고, 과거의 격차까지 비교합니다.</p>
      </section>

      <section className="map-placeholder" aria-label="급지 지도 준비 영역">
        <div>
          <span className="map-badge">MAP</span>
          <h2>급지 지도를 준비하고 있어요</h2>
          <p>네이버 지도와 지역별 급지 데이터가 이 영역에 연결됩니다.</p>
        </div>
      </section>

      <section className="recommendation-card">
        <span>갈아타기 추천</span>
        <h2>현재 급지에서 1·2급지 위를 비교해보세요</h2>
        <p>급지별 평균 평단가와 현재·과거 격차만 객관적으로 보여드립니다.</p>
      </section>
    </main>
  )
}

export default App
