import { useEffect, useMemo, useState } from 'react'
import './App.css'
import NaverMap from './components/NaverMap'
import UpgradePanel from './components/UpgradePanel'
import LifestylePanel from './components/LifestylePanel'
import RegionSelector, { type RegionSelection } from './components/RegionSelector'

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
  const [availableYears, setAvailableYears] = useState<number[]>(Array.from({ length: currentYear - 2014 }, (_, index) => 2015 + index))
  const [year, setYear] = useState(currentYear)
  const [grades, setGrades] = useState<GradeSummary[]>([])
  const [selected, setSelected] = useState<GradeSummary | null>(null)
  const [error, setError] = useState('')

  useEffect(() => {
    fetch('/api/grades/years')
      .then((response) => response.ok ? response.json() as Promise<number[]> : Promise.reject())
      .then((years) => {
        if (years.length === 0) return
        const first = Math.min(2015, ...years)
        const last = Math.max(currentYear, ...years)
        setAvailableYears(Array.from({ length: last - first + 1 }, (_, index) => first + index))
        setYear(Math.max(...years))
      })
      .catch(() => undefined)
  }, [])

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
        setSelected((previous) => data.find((item) => item.regionId === previous?.regionId) ?? data[0] ?? null)
      })
      .catch((reason: Error) => {
        if (reason.name !== 'AbortError') setError(reason.message)
      })
    return () => controller.abort()
  }, [year])

  const yearIndex = Math.max(0, availableYears.indexOf(year))
  const groupedCounts = useMemo(() => Array.from({ length: 10 }, (_, index) => ({
    grade: index + 1,
    count: grades.filter((item) => item.grade === index + 1).length,
  })), [grades])

  const selectRegion = (region: RegionSelection) => {
    const matching = grades.find((item) => item.regionId === region.id)
    setSelected(matching ?? null)
    setError(matching ? '' : `${region.provinceName} ${region.name}의 ${year}년 실거래 급지 데이터가 없습니다.`)
  }

  const selectRegionFromMap = (regionId: number) => {
    const matching = grades.find((item) => item.regionId === regionId)
    setSelected(matching ?? null)
    setError(matching ? '' : `${year}년 실거래 급지 데이터가 없는 지역입니다.`)
  }

  return (
    <main className="app-shell">
      <header className="topbar">
        <a className="brand" href="/">수도권 급지지도</a>
        <nav aria-label="주요 메뉴">
          <a href="#grade-map">지도</a>
          <a href="#upgrade">갈아타기 비교</a>
          <a href="#lifestyle">아파트 추천</a>
        </nav>
      </header>

      <section className="page-title">
        <h1>수도권 급지지도</h1>
        <p>실거래 평균 평단가를 기준으로 서울·경기·인천 지역의 상대적인 주거 가격 수준을 보여줍니다.</p>
      </section>

      <section id="grade-map" className="map-panel" aria-label="지역 급지 지도">
        <div className="map-heading">
          <div>
            <h2>수도권 지역별 주거 가치</h2>
            <p>같은 연도의 아파트 실거래 평균 평단가를 비교해 수도권 시·군·구를 1~10급지로 나눈 결과입니다.</p>
          </div>
          <RegionSelector
            id="map-region"
            label="지도 지역"
            selectedRegionId={selected?.regionId}
            onSelect={selectRegion}
          />
        </div>

        <div className="year-control">
          <div className="year-control-title">
            <label htmlFor="grade-year">기준 연도</label>
            <output htmlFor="grade-year">{year}년</output>
          </div>
          <input
            id="grade-year"
            aria-label="기준 연도"
            type="range"
            min="0"
            max={Math.max(0, availableYears.length - 1)}
            step="1"
            value={yearIndex}
            onChange={(event) => setYear(availableYears[Number(event.target.value)] ?? year)}
          />
          <div className="year-ticks" aria-hidden="true">
            {availableYears.map((item) => <span key={item}>{item}</span>)}
          </div>
        </div>

        <div className="grade-legend" aria-label="급지 색상 범례">
          {groupedCounts.map(({ grade, count }) => (
            <span key={grade}><i className={`legend-color grade-color-${grade}`} />{grade}급지 <small>{count}</small></span>
          ))}
          <span><i className="legend-color no-data" />데이터 없음</span>
        </div>

        <div className="map-with-info">
          <NaverMap year={year} onSelectRegion={selectRegionFromMap} />
          <aside className="map-region-info" aria-live="polite">
            <span className="info-eyebrow">지도에서 지역을 클릭하세요</span>
            {selected ? <>
              <h3>{selected.regionName}</h3>
              <div className="selection-summary">
            <div><span>선택 지역</span><strong>{selected.regionName}</strong></div>
            <div><span>급지</span><strong>{selected.grade}급지</strong></div>
            <div><span>평균 평단가</span><strong>{formatPrice(selected.averagePricePerPyeong)}</strong></div>
            <div><span>실거래</span><strong>{selected.tradeCount.toLocaleString('ko-KR')}건</strong></div>
              </div>
            </> : <p>{year}년 급지 정보가 있는 지역을 선택해 주세요.</p>}
          </aside>
        </div>
        {error && <p role="alert" className="map-error">{error}</p>}
      </section>

      <UpgradePanel year={year} />
      <LifestylePanel year={year} />
    </main>
  )
}

export default App
