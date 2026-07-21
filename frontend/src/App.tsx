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
  const [availableYears, setAvailableYears] = useState<number[]>(Array.from({ length: currentYear - 2014 }, (_, index) => 2015 + index))
  const [year, setYear] = useState(currentYear)
  const [latestDataYear, setLatestDataYear] = useState(currentYear)
  const [grades, setGrades] = useState<GradeSummary[]>([])
  const [selected, setSelected] = useState<GradeSummary | null>(null)
  const [error, setError] = useState('')
  const [tradeMonths, setTradeMonths] = useState<string[]>([])

  useEffect(() => {
    fetch('/api/grades/years')
      .then((response) => response.ok ? response.json() as Promise<number[]> : Promise.reject())
      .then((years) => {
        if (years.length === 0) return
        const first = Math.min(2015, ...years)
        const last = Math.max(currentYear, ...years)
        setAvailableYears(Array.from({ length: last - first + 1 }, (_, index) => first + index))
        const latest = Math.max(...years)
        setYear(latest)
        setLatestDataYear(latest)
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

  useEffect(() => {
    fetch(`/api/grades/coverage?year=${year}`)
      .then((response) => response.ok ? response.json() as Promise<string[]> : [])
      .then(setTradeMonths)
      .catch(() => setTradeMonths([]))
  }, [year])

  const coverageLabel = tradeMonths.length === 0
    ? '수집된 실거래 없음'
    : `${tradeMonths.map((month) => Number(month.slice(5))).join('·')}월 실거래 기준`

  const yearIndex = Math.max(0, availableYears.indexOf(year))
  const groupedCounts = useMemo(() => Array.from({ length: 10 }, (_, index) => ({
    grade: index + 1,
    count: grades.filter((item) => item.grade === index + 1).length,
  })), [grades])
  const marketStats = useMemo(() => {
    if (grades.length === 0) return null
    const ascending = grades.map((item) => item.averagePricePerPyeong).sort((a, b) => a - b)
    const middle = Math.floor(ascending.length / 2)
    const median = ascending.length % 2 ? ascending[middle] : (ascending[middle - 1] + ascending[middle]) / 2
    const ranked = [...grades].sort((a, b) => b.averagePricePerPyeong - a.averagePricePerPyeong)
    const rank = selected ? ranked.findIndex((item) => item.regionId === selected.regionId) + 1 : 0
    return {
      median,
      rank,
      percentile: rank ? Math.ceil(rank / grades.length * 100) : 0,
      priceIndex: selected ? selected.averagePricePerPyeong / median * 100 : 0,
    }
  }, [grades, selected])

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
            <p>지역 평균 평단가를 수도권 중간 평단가와 비교한 가격지수로 시·군·구를 1~10급지로 나눈 결과입니다.</p>
          </div>
        </div>

        <div className="year-control">
          <div className="year-control-title">
            <label htmlFor="grade-year">기준 연도</label>
            <output htmlFor="grade-year">{year}년</output>
          </div>
          <p className="coverage-label">{year}년 {coverageLabel}</p>
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
            {marketStats && <>
              <div><span>수도권 가격지수</span><strong>{Math.round(marketStats.priceIndex)}</strong></div>
              <div><span>중간값 대비</span><strong>{(marketStats.priceIndex / 100).toFixed(1)}배</strong></div>
              <div><span>수도권 순위</span><strong>{marketStats.rank}위</strong></div>
              <div><span>상위 백분위</span><strong>상위 {marketStats.percentile}%</strong></div>
            </>}
              </div>
            </> : <p>{year}년 급지 정보가 있는 지역을 선택해 주세요.</p>}
          </aside>
        </div>
        {error && <p role="alert" className="map-error">{error}</p>}
        <section className="grade-methodology" aria-labelledby="grade-method-title">
          <div>
            <span>급지 계산 방식</span>
            <h3 id="grade-method-title">수도권 중간 평단가 대비 가격지수</h3>
            <p><code>지역 가격지수 = 지역 평균 평단가 ÷ 수도권 중간 평단가 × 100</code></p>
            <p>가격지수 100은 수도권 중간 수준, 200은 중간값의 2배입니다. 급지는 학군·교통·환경을 평가하지 않고 실거래 가격의 상대적 위치만 나타냅니다.</p>
          </div>
          <div className="grade-thresholds" aria-label="가격지수 급지 기준">
            {[
              ['1급지', '250 이상'], ['2급지', '200~250'], ['3급지', '165~200'], ['4급지', '140~165'], ['5급지', '120~140'],
              ['6급지', '100~120'], ['7급지', '85~100'], ['8급지', '70~85'], ['9급지', '55~70'], ['10급지', '55 미만'],
            ].map(([grade, range]) => <div key={grade}><strong>{grade}</strong><span>{range}</span></div>)}
          </div>
          <small>평단가는 취소 거래를 제외한 수집 실거래의 평균이며, 표본 수와 수집 기간에 따라 달라질 수 있습니다. 이 급지는 투자 권유나 주거 품질의 절대 평가가 아닙니다.</small>
        </section>
      </section>

      <UpgradePanel />
      <LifestylePanel year={latestDataYear} />
    </main>
  )
}

export default App
