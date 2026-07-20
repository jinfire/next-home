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
  const subscription = {
    endpoint: 'https://push.example/1',
    toJSON: () => ({ endpoint: 'https://push.example/1', keys: { p256dh: 'key', auth: 'auth' } }),
  }
  Object.defineProperty(navigator, 'serviceWorker', {
    configurable: true,
    value: { register: vi.fn().mockResolvedValue({ pushManager: {
      getSubscription: vi.fn().mockResolvedValue(null),
      subscribe: vi.fn().mockResolvedValue(subscription),
    } }) },
  })
  vi.stubGlobal('fetch', vi.fn((input: string | URL | Request) => {
    const url = String(input)
    const json = url.endsWith('vapid-public-key')
      ? { publicKey: 'BEl6ZQmFakePublicKey' }
      : url.includes('/recommendations/upgrades')
        ? [{ targetGrade: 4, currentGapPerPyeong: 15_000_000 }]
        : { id: 1 }
    return Promise.resolve({ ok: true, json: async () => json })
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
  expect(fetch).toHaveBeenCalledWith('/api/push-subscriptions', expect.objectContaining({ method: 'POST' }))
})
