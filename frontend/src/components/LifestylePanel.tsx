import { useState } from 'react'
import type { FormEvent } from 'react'

type Apartment = {
  id: number
  name: string
  address: string
  regionId: number
  regionName: string
  buildYear: number | null
}

type Recommendation = {
  apartmentId: number
  apartmentName: string
  address: string
  averagePricePerPyeong: number
  gapPerPyeong: number
  tradeCount: number
}

function price(value: number) {
  return `${Math.round(value / 10_000).toLocaleString('ko-KR')}만원/평`
}

export default function LifestylePanel({ year }: { year: number }) {
  const [query, setQuery] = useState('')
  const [apartments, setApartments] = useState<Apartment[]>([])
  const [current, setCurrent] = useState<Apartment | null>(null)
  const [recommendations, setRecommendations] = useState<Recommendation[]>([])
  const [message, setMessage] = useState('현재 아파트를 검색해 보세요.')

  async function search(event: FormEvent) {
    event.preventDefault()
    if (!query.trim()) return
    const response = await fetch(`/api/apartments?query=${encodeURIComponent(query.trim())}`)
    if (!response.ok) {
      setMessage('아파트 검색에 실패했습니다.')
      return
    }
    const data = await response.json() as Apartment[]
    setApartments(data)
    setMessage(data.length ? '현재 거주 중인 아파트를 선택하세요.' : '검색 결과가 없습니다.')
  }

  async function selectApartment(apartment: Apartment) {
    setCurrent(apartment)
    setApartments([])
    setMessage('같은 생활권의 후보를 찾고 있습니다.')
    const response = await fetch(`/api/recommendations/apartments?apartmentId=${apartment.id}&year=${year}`)
    if (!response.ok) {
      setRecommendations([])
      setMessage(response.status === 404 ? `${year}년 유효 실거래가 없습니다.` : '추천 결과를 불러오지 못했습니다.')
      return
    }
    const data = await response.json() as Recommendation[]
    setRecommendations(data)
    setMessage(data.length ? '' : '같은 생활권에 더 높은 평단가의 단지가 없습니다.')
  }

  return (
    <section className="lifestyle-panel" id="lifestyle">
      <div className="lifestyle-head">
        <div>
          <p className="section-kicker">SAME NEIGHBORHOOD, NEXT HOME</p>
          <h2>생활은 그대로, 집은 한 단계 위로</h2>
        </div>
        <p>현재 단지와 같은 행정구역에서 실거래 평균 평단가가 더 높은 아파트를 찾습니다.</p>
      </div>

      <form className="apartment-search" onSubmit={search}>
        <label>
          현재 아파트
          <input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="아파트 이름 입력" />
        </label>
        <button type="submit">검색</button>
      </form>

      {apartments.length > 0 && (
        <div className="search-results" aria-label="아파트 검색 결과">
          {apartments.map((apartment) => (
            <button key={apartment.id} onClick={() => selectApartment(apartment)}>
              <strong>{apartment.name}</strong>
              <span>{apartment.regionName} · {apartment.address}</span>
            </button>
          ))}
        </div>
      )}

      {current && <p className="current-apartment">현재 단지 <strong>{current.name}</strong> · {current.regionName}</p>}
      {message && <p className="lifestyle-message" aria-live="polite">{message}</p>}

      <div className="apartment-recommendations">
        {recommendations.map((item, index) => (
          <article key={item.apartmentId}>
            <span className="rank">{String(index + 1).padStart(2, '0')}</span>
            <div><h3>{item.apartmentName}</h3><p>{item.address}</p></div>
            <dl>
              <div><dt>평균 평단가</dt><dd>{price(item.averagePricePerPyeong)}</dd></div>
              <div><dt>현재보다</dt><dd>+{price(item.gapPerPyeong)}</dd></div>
              <div><dt>실거래</dt><dd>{item.tradeCount}건</dd></div>
            </dl>
          </article>
        ))}
      </div>
    </section>
  )
}
