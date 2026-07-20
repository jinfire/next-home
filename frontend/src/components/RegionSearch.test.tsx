import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import RegionSearch from './RegionSearch'

describe('RegionSearch', () => {
  it('searches regions and selects a result', async () => {
    const onSelect = vi.fn()
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: true,
      json: async () => [{ id: 1, code: '11680', name: '강남구', level: 2 }],
    }))
    const user = userEvent.setup()
    render(<RegionSearch onSelect={onSelect} />)

    await user.type(screen.getByLabelText('지역 검색'), '강남')
    await user.click(screen.getByRole('button', { name: '검색' }))

    expect(fetch).toHaveBeenCalledWith('/api/regions?query=%EA%B0%95%EB%82%A8', expect.anything())
    await user.click(await screen.findByRole('button', { name: /강남구/ }))
    expect(onSelect).toHaveBeenCalledWith({ id: 1, code: '11680', name: '강남구', level: 2 })
  })

  it('shows an empty result message', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({ ok: true, json: async () => [] }))
    const user = userEvent.setup()
    render(<RegionSearch onSelect={vi.fn()} />)
    await user.type(screen.getByLabelText('지역 검색'), '없는지역')
    await user.click(screen.getByRole('button', { name: '검색' }))
    expect(await screen.findByText('검색 결과가 없습니다.')).toBeInTheDocument()
  })
})
