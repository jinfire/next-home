import { render, screen } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import App from './App'

describe('Next Home grade map', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: true,
      json: async () => [
        { regionId: 1, regionCode: '11680', regionName: '강남구', year: 2026,
          averagePricePerPyeong: 90000000, grade: 1, tradeCount: 42 },
      ],
    }))
  })

  it('presents the grade map as the main experience and loads grades', async () => {
    render(<App />)

    expect(screen.getByRole('heading', { name: '사는 곳의 가치를 한눈에' })).toBeInTheDocument()
    expect(screen.getByLabelText('지역 급지 지도')).toBeInTheDocument()
    expect(await screen.findAllByText('강남구')).toHaveLength(2)
    expect(screen.getAllByText('1급지')).toHaveLength(2)
    expect(screen.getAllByText('9,000만원/평')).toHaveLength(2)
  })
})
