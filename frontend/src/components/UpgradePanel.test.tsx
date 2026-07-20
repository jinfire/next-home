import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { expect, it, vi } from 'vitest'
import UpgradePanel from './UpgradePanel'

it('loads one and two grade upgrade comparisons from only the current grade', async () => {
  vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
    ok: true,
    json: async () => [{
      currentGrade: 5, targetGrade: 4, year: 2026,
      currentAveragePricePerPyeong: 50_000_000,
      targetAveragePricePerPyeong: 70_000_000,
      currentGapPerPyeong: 20_000_000,
      historicalGapPercentile: 25,
      historicalYears: 5,
    }],
  }))

  render(<UpgradePanel year={2026} />)
  await userEvent.selectOptions(screen.getByLabelText('현재 급지'), '5')

  expect(await screen.findAllByText('4급지')).toHaveLength(2)
  expect(screen.getByText('7,000만원/평')).toBeInTheDocument()
  expect(screen.getByText('+2,000만원/평')).toBeInTheDocument()
  expect(screen.getByText('과거 격차의 하위 25% 수준')).toBeInTheDocument()
})
