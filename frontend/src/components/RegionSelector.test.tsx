import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import RegionSelector from './RegionSelector'

describe('RegionSelector', () => {
  it('selects a district through province and district dropdowns', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: true,
      json: async () => [
        { id: 1, code: '11', name: '서울특별시', regions: [{ id: 11, code: '11680', name: '강남구' }] },
        { id: 2, code: '41', name: '경기도', regions: [{ id: 21, code: '41135', name: '성남시 분당구' }] },
      ],
    }))
    const onSelect = vi.fn()
    const user = userEvent.setup()
    render(<RegionSelector id="home" label="현재 거주 지역" onSelect={onSelect} />)

    await user.selectOptions(await screen.findByLabelText('현재 거주 지역 시·도'), '41')
    await user.selectOptions(screen.getByLabelText('현재 거주 지역 시·군·구'), '21')

    expect(onSelect).toHaveBeenCalledWith({
      id: 21, code: '41135', name: '성남시 분당구', provinceName: '경기도',
    })
  })
})
