import { render, screen } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import App from './App'

describe('Capital area grade map', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', vi.fn((input: string | URL | Request) => {
      const url = String(input)
      const body = url.includes('/api/grades/years') ? [2025, 2026]
        : url.includes('/api/regions/options') ? [{
            id: 1, code: '11', name: '서울특별시', regions: [{ id: 10, code: '11680', name: '강남구' }],
          }]
        : url.includes('/api/grades?') ? [{
            regionId: 10, regionCode: '11680', regionName: '강남구', year: 2026,
            averagePricePerPyeong: 90_000_000, grade: 1, tradeCount: 42,
          }]
        : { type: 'FeatureCollection', features: [] }
      return Promise.resolve({ ok: true, json: async () => body })
    }))
  })

  it('presents a wide capital-area map with selectors and a year slider', async () => {
    render(<App />)

    expect(screen.getByRole('heading', { name: '수도권 급지지도' })).toBeInTheDocument()
    expect(screen.getByText(/서울·경기·인천 지역의 상대적인 주거 가격 수준/)).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: '수도권 지역별 주거 가치' })).toBeInTheDocument()
    expect(await screen.findByLabelText('지도 지역 시·도')).toBeInTheDocument()
    expect(screen.getByLabelText('기준 연도')).toHaveAttribute('type', 'range')
    expect(screen.getByLabelText('지역 급지 지도')).toBeInTheDocument()
    expect(await screen.findAllByText('강남구')).not.toHaveLength(0)
    expect(screen.queryByText('원하는 격차가 오면 알려드릴게요')).not.toBeInTheDocument()
  })
})
