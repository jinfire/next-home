import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, expect, it, vi } from 'vitest'
import AlertPanel from './AlertPanel'

const notification = vi.fn()

beforeEach(() => {
  localStorage.clear()
  notification.mockClear()
  Object.assign(notification, { permission: 'default', requestPermission: vi.fn().mockResolvedValue('granted') })
  vi.stubGlobal('Notification', notification)
  vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
    ok: true,
    json: async () => [{ targetGrade: 4, currentGapPerPyeong: 15_000_000 }],
  }))
})

it('saves an alert and shows a browser notification when the condition is met', async () => {
  render(<AlertPanel year={2026} />)
  await userEvent.selectOptions(screen.getByLabelText('알림 기준 현재 급지'), '5')
  await userEvent.type(screen.getByLabelText(/최대 평당 격차/), '2000')
  await userEvent.click(screen.getByRole('button', { name: '웹 알림 켜기' }))

  expect(await screen.findByText('알림이 설정되었습니다.')).toBeInTheDocument()
  expect(notification).toHaveBeenCalledWith('갈아타기 조건이 좋아졌어요', expect.any(Object))
  expect(localStorage.getItem('next-home-alert')).toContain('2000')
})
