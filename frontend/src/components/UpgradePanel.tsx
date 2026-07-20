import { useEffect, useState } from 'react'

type Upgrade = {
  currentGrade: number
  targetGrade: number
  year: number
  currentAveragePricePerPyeong: number
  targetAveragePricePerPyeong: number
  currentGapPerPyeong: number
  historicalGapPercentile: number
  historicalYears: number
}

function price(value: number) {
  return `${Math.round(value / 10_000).toLocaleString('ko-KR')}만원/평`
}

export default function UpgradePanel({ year }: { year: number }) {
  const [currentGrade, setCurrentGrade] = useState(0)
  const [results, setResults] = useState<Upgrade[]>([])
  const [error, setError] = useState('')

  useEffect(() => {
    if (!currentGrade) {
      setResults([])
      return
    }
    const controller = new AbortController()
    setError('')
    fetch(`/api/recommendations/upgrades?currentGrade=${currentGrade}&year=${year}`, {
      signal: controller.signal,
    })
      .then((response) => {
        if (!response.ok) throw new Error('추천 결과를 불러오지 못했습니다.')
        return response.json() as Promise<Upgrade[]>
      })
      .then(setResults)
      .catch((reason: Error) => {
        if (reason.name !== 'AbortError') setError(reason.message)
      })
    return () => controller.abort()
  }, [currentGrade, year])

  return (
    <section id="upgrade" className="upgrade-panel">
      <div className="upgrade-intro">
        <p className="section-kicker">MOVE-UP SIGNAL</p>
        <h2>한 단계 위는 지금 얼마나 멀까요?</h2>
        <p>대출이나 추가 자금은 계산하지 않습니다. 시장의 가격 격차만 선명하게 비교합니다.</p>
        <label>
          현재 급지
          <select value={currentGrade} onChange={(event) => setCurrentGrade(Number(event.target.value))}>
            <option value="0">급지를 선택하세요</option>
            {Array.from({ length: 10 }, (_, index) => index + 1).map((grade) => (
              <option key={grade} value={grade}>{grade}급지</option>
            ))}
          </select>
        </label>
      </div>

      <div className="upgrade-results" aria-live="polite">
        {!currentGrade && <p className="upgrade-prompt">현재 급지를 고르면 1·2급지 위를 바로 비교해 드려요.</p>}
        {error && <p role="alert" className="upgrade-prompt">{error}</p>}
        {currentGrade > 0 && !error && results.length === 0 && (
          <p className="upgrade-prompt">{year}년 비교 가능한 상위 급지 데이터가 없습니다.</p>
        )}
        {results.map((item) => (
          <article className="upgrade-card" key={item.targetGrade}>
            <div className="upgrade-card-head">
              <span>{item.currentGrade - item.targetGrade}단계 위</span>
              <strong>{item.targetGrade}급지</strong>
            </div>
            <dl>
              <div><dt>평균 평단가</dt><dd>{price(item.targetAveragePricePerPyeong)}</dd></div>
              <div><dt>현재 격차</dt><dd>+{price(item.currentGapPerPyeong)}</dd></div>
              <div><dt>과거 대비</dt><dd>과거 격차의 하위 {Math.round(item.historicalGapPercentile)}% 수준</dd></div>
            </dl>
            <small>{item.historicalYears}개 연도 기준</small>
          </article>
        ))}
      </div>
    </section>
  )
}
