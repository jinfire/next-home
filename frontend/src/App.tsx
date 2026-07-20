import { useEffect, useMemo, useState } from 'react'
import './App.css'
import NaverMap from './components/NaverMap'
import UpgradePanel from './components/UpgradePanel'
import LifestylePanel from './components/LifestylePanel'

type GradeSummary = {
  regionId: number
  regionCode: string
  regionName: string
  year: number
  averagePricePerPyeong: number
  grade: number
  tradeCount: number
}

const currentYear = new Date().getFullYear()

function formatPrice(value: number) {
  return `${Math.round(value / 10_000).toLocaleString('ko-KR')}만원/평`
}

function App() {
  const [year, setYear] = useState(currentYear)
  const [grades, setGrades] = useState<GradeSummary[]>([])
  const [selected, setSelected] = useState<GradeSummary | null>(null)
  const [error, setError] = useState('')

  useEffect(() => {
    const controller = new AbortController()
    setError('')
    fetch(`/api/grades?year=${year}`, { signal: controller.signal })
      .then((response) => {
        if (!response.ok) throw new Error('급지 데이터를 불러오지 못했습니다.')
        return response.json() as Promise<GradeSummary[]>
      })
      .then((data) => {
        setGrades(data)
        setSelected(data[0] ?? null)
      })
      .catch((reason: Error) => {
        if (reason.name !== 'AbortError') setError(reason.message)
      })
    return () => controller.abort()
  }, [year])

  const yearOptions = useMemo(
    () => Array.from({ length: 7 }, (_, index) => currentYear - index),
    [],
  )

  return (
    <main className="app-shell">
      <header className="topbar">
        <a className="brand" href="/">NEXT HOME</a>
        <nav aria-label="주요 메뉴">
          <a href="#grade-map">급지 지도</a>
          <a href="#upgrade">갈아타기</a>
        </nav>
      </header>

      <section className="hero-copy">
        <p className="eyebrow">DATA-GUIDED HOME UPGRADE</p>
        <h1>사는 곳의 가치를 한눈에</h1>
        <p>실거래 평균 평단가로 지역의 현재 위치를 읽고, 다음 집으로 가는 선택지를 차분하게 비교하세요.</p>
      </section>

      <section id="grade-map" className="map-panel" aria-label="지역 급지 지도">
        <div className="map-toolbar">
          <div>
            <p className="section-kicker">REGION GRADE MAP</p>
            <h2>지역별 주거 가치</h2>
          </div>
          <label>
            기준 연도
            <select value={year} onChange={(event) => setYear(Number(event.target.value))}>
              {yearOptions.map((option) => <option key={option}>{option}</option>)}
            </select>
          </label>
        </div>

        <div className="map-grid">
          <NaverMap />
          <aside className="grade-list" aria-label="지역 급지 목록">
            {error && <p role="alert" className="empty-state">{error}</p>}
            {!error && grades.length === 0 && <p className="empty-state">{year}년 급지 데이터가 아직 없습니다.</p>}
            {grades.map((item) => (
              <button
                className={selected?.regionId === item.regionId ? 'grade-item selected' : 'grade-item'}
                key={item.regionId}
                onClick={() => setSelected(item)}
              >
                <span className={`grade-dot grade-${item.grade}`}>{item.grade}</span>
                <span><strong>{item.regionName}</strong><small>{item.tradeCount}건 실거래</small></span>
                <span className="grade-price"><strong>{item.grade}급지</strong><small>{formatPrice(item.averagePricePerPyeong)}</small></span>
              </button>
            ))}
          </aside>
        </div>

        {selected && (
          <div className="selection-summary" aria-live="polite">
            <span>{selected.regionName}</span>
            <strong>{selected.grade}급지</strong>
            <span>{formatPrice(selected.averagePricePerPyeong)}</span>
          </div>
        )}
      </section>

      <UpgradePanel year={year} />
      <LifestylePanel year={year} />
    </main>
  )
}

export default App
