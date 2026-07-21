import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { expect, it, vi } from 'vitest'
import LifestylePanel from './LifestylePanel'

it('searches current apartment and recommends better apartments in the same region', async () => {
  vi.stubGlobal('fetch', vi.fn((input: string | URL | Request) => {
    const url = String(input)
    const body = url.includes('/api/regions/options')
      ? [{ id: 1, code: '11', name: '서울특별시', regions: [{ id: 10, code: '11440', name: '마포구' }] }]
      : url.includes('/api/apartments?')
      ? [{ id: 1, name: '현재아파트', address: '아현동 1', roadAddress: '서울특별시 마포구 마포대로 1', regionId: 10, regionName: '마포구', buildYear: 2010 }]
      : [{ apartmentId: 2, apartmentName: '추천아파트', address: '서울특별시 마포구 마포대로 2', averagePricePerPyeong: 80_000_000, gapPerPyeong: 20_000_000, tradeCount: 5 }]
    return Promise.resolve({ ok: true, json: async () => body })
  }))

  render(<LifestylePanel year={2026} tradeMonths={['2026-06']} />)
  await userEvent.selectOptions(await screen.findByLabelText('아파트 지역 시·도'), '11')
  await userEvent.selectOptions(screen.getByLabelText('아파트 지역 시·군·구'), '10')
  await userEvent.type(screen.getByLabelText('현재 아파트명'), '현재')
  await userEvent.click(screen.getByRole('button', { name: '검색' }))
  await userEvent.click(await screen.findByRole('button', { name: /현재아파트/ }))

  expect(await screen.findByText('추천아파트')).toBeInTheDocument()
  expect(screen.queryByText('아현동 1')).not.toBeInTheDocument()
  expect(screen.getByText(/서울특별시 마포구 마포대로 1/)).toBeInTheDocument()
  expect(screen.getByText('8,000만원/평')).toBeInTheDocument()
  expect(screen.getByText('+2,000만원/평')).toBeInTheDocument()
  expect(screen.getByText('6월 실거래 합계')).toBeInTheDocument()
  expect(fetch).toHaveBeenCalledWith('/api/apartments?query=%ED%98%84%EC%9E%AC&regionId=10')
})
