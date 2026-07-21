import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { expect, it, vi } from 'vitest'
import UpgradePanel from './UpgradePanel'

it('resolves the grade from a selected district and loads upper-grade comparisons', async () => {
  vi.stubGlobal('fetch', vi.fn((input: string | URL | Request) => {
    const url = String(input)
    const body = url.includes('/api/regions/options') ? [{
      id: 1, code: '41', name: '경기도', regions: [{ id: 10, code: '41135', name: '성남시 분당구' }],
    }] : {
      regionId: 10,
      regionName: '성남시 분당구',
      currentGrade: 5,
      year: 2026,
      currentAveragePricePerPyeong: 50_000_000,
      targets: [{
        currentGrade: 5, targetGrade: 4, year: 2026,
        currentAveragePricePerPyeong: 50_000_000,
        targetAveragePricePerPyeong: 70_000_000,
        currentGapPerPyeong: 20_000_000,
        historicalGapPercentile: 25,
        historicalYears: 5,
      }],
      nearbyRegions: [{ regionId: 11, regionName: '과천시', grade: 4, averagePricePerPyeong: 75_000_000 }],
    }
    return Promise.resolve({ ok: true, json: async () => body })
  }))

  render(<UpgradePanel year={2026} />)
  await userEvent.selectOptions(await screen.findByLabelText('현재 거주 지역 시·도'), '41')
  await userEvent.selectOptions(screen.getByLabelText('현재 거주 지역 시·군·구'), '10')

  expect(await screen.findByText('성남시 분당구 · 5급지')).toBeInTheDocument()
  expect(screen.getByText('7,000만원/평')).toBeInTheDocument()
  expect(screen.getByText('+2,000만원/평')).toBeInTheDocument()
  expect(screen.getByText('과거 격차의 하위 25% 수준')).toBeInTheDocument()
  expect(screen.getByText('과천시')).toBeInTheDocument()
})
