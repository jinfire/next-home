import { useRef, useState } from 'react'
import type { FormEvent } from 'react'

export type RegionSearchResult = {
  id: number
  code: string
  name: string
  level: number
}

type RegionSearchProps = {
  onSelect(region: RegionSearchResult): void
}

export default function RegionSearch({ onSelect }: RegionSearchProps) {
  const [query, setQuery] = useState('')
  const [results, setResults] = useState<RegionSearchResult[]>([])
  const [searched, setSearched] = useState(false)
  const [error, setError] = useState('')
  const controller = useRef<AbortController | null>(null)

  const submit = (event: FormEvent) => {
    event.preventDefault()
    const normalized = query.trim()
    if (!normalized) return
    controller.current?.abort()
    controller.current = new AbortController()
    setError('')
    setSearched(false)
    fetch(`/api/regions?query=${encodeURIComponent(normalized)}`, { signal: controller.current.signal })
      .then((response) => {
        if (!response.ok) throw new Error('지역 검색에 실패했습니다.')
        return response.json() as Promise<RegionSearchResult[]>
      })
      .then((data) => {
        setResults(data)
        setSearched(true)
      })
      .catch((reason: Error) => {
        if (reason.name !== 'AbortError') setError(reason.message)
      })
  }

  return (
    <div className="region-search">
      <form onSubmit={submit} role="search">
        <label htmlFor="region-query">지역 검색</label>
        <div className="region-search-row">
          <input
            id="region-query"
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            placeholder="예: 강남구"
          />
          <button type="submit">검색</button>
        </div>
      </form>
      {error && <p role="alert" className="search-status">{error}</p>}
      {searched && results.length === 0 && <p className="search-status">검색 결과가 없습니다.</p>}
      {results.length > 0 && (
        <ul className="region-search-results">
          {results.map((region) => (
            <li key={region.id}>
              <button type="button" onClick={() => { onSelect(region); setResults([]) }}>
                <strong>{region.name}</strong><span>{region.code}</span>
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
