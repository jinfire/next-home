import { useEffect, useState } from 'react'
import RegionSelector, { type RegionSelection } from './RegionSelector'

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

type Comparison = {
  regionId: number
  regionName: string
  currentGrade: number
  year: number
  currentAveragePricePerPyeong: number
  targets: Upgrade[]
  nearbyRegions: { regionId: number; regionName: string; grade: number; averagePricePerPyeong: number }[]
}

function price(value: number) {
  return `${Math.round(value / 10_000).toLocaleString('ko-KR')}만원/평`
}

function homePrice(perPyeong: number, pyeong: number) {
  const eok = perPyeong * pyeong / 100_000_000
  return `${eok.toLocaleString('ko-KR', { maximumFractionDigits: 1 })}억원`
}

export default function UpgradePanel({ year }: { year: number }) {
  const [region, setRegion] = useState<RegionSelection | null>(null)
  const [comparison, setComparison] = useState<Comparison | null>(null)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!region) {
      setComparison(null)
      return
    }
    const controller = new AbortController()
    setError('')
    fetch(`/api/recommendations/upgrades?regionId=${region.id}&year=${year}`, { signal: controller.signal })
      .then((response) => {
        if (response.status === 404) throw new Error(`${year}년 급지 데이터가 없는 지역입니다.`)
        if (!response.ok) throw new Error('갈아타기 비교를 불러오지 못했습니다.')
        return response.json() as Promise<Comparison>
      })
      .then(setComparison)
      .catch((reason: Error) => {
        if (reason.name !== 'AbortError') {
          setComparison(null)
          setError(reason.message)
        }
      })
    return () => controller.abort()
  }, [region, year])

  return (
    <section id="upgrade" className="upgrade-panel section-panel">
      <div className="section-heading">
        <div>
          <h2>상급지로 갈아타면 평당 가격 차이가 얼마나 날까요?</h2>
          <p>현재 거주 지역을 선택하면 급지를 자동으로 확인하고, 한·두 급지 위의 평균 평단가와 시장 격차를 비교합니다.</p>
        </div>
      </div>

      <RegionSelector id="upgrade-region" label="현재 거주 지역" onSelect={setRegion} />

      <div className="upgrade-results" aria-live="polite">
        {!region && <p className="panel-placeholder">시·도와 시·군·구를 차례로 선택해 주세요.</p>}
        {error && <p role="alert" className="panel-placeholder">{error}</p>}
        {comparison && (
          <div className="current-region-card">
            <span>현재 지역</span>
            <strong>{comparison.regionName} · {comparison.currentGrade}급지</strong>
            <small>{price(comparison.currentAveragePricePerPyeong)}</small>
          </div>
        )}
        {comparison && comparison.nearbyRegions.length === 0 && (
          <p className="panel-placeholder">현재 지역보다 높은 급지 데이터가 없습니다.</p>
        )}
        {comparison && (comparison.nearbyRegions?.length ?? 0) > 0 && (
          <div className="nearby-regions">
            <h3>{comparison.regionName}에서 가까운 상급지</h3>
            <div>
              {comparison.nearbyRegions.slice(0, 4).map((item) => (
                <article key={item.regionId}>
                  <div><strong>{item.regionName}</strong><em>{item.grade}급지</em></div>
                  <dl>
                    <div><dt>평단가</dt><dd>{price(item.averagePricePerPyeong)}</dd></div>
                    <div><dt>25평 기준</dt><dd>{homePrice(item.averagePricePerPyeong, 25)}</dd></div>
                    <div><dt>34평 기준</dt><dd>{homePrice(item.averagePricePerPyeong, 34)}</dd></div>
                  </dl>
                </article>
              ))}
            </div>
          </div>
        )}
      </div>
    </section>
  )
}
