import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'

type StoredAlert = { currentGrade: number; maxGapManwon: number; year: number }
type Upgrade = { targetGrade: number; currentGapPerPyeong: number }
const storageKey = 'next-home-alert'
const checkIntervalMs = 60 * 60 * 1000
const browserIdKey = 'next-home-browser-id'

function applicationServerKey(value: string) {
  const padding = '='.repeat((4 - value.length % 4) % 4)
  const base64 = (value + padding).replace(/-/g, '+').replace(/_/g, '/')
  return Uint8Array.from(atob(base64), (character) => character.charCodeAt(0))
}

function browserId() {
  const existing = localStorage.getItem(browserIdKey)
  if (existing) return existing
  const created = crypto.randomUUID()
  localStorage.setItem(browserIdKey, created)
  return created
}

async function registerBackgroundPush() {
  if (!('serviceWorker' in navigator)) return false
  const registration = await navigator.serviceWorker.register('/sw.js')
  const keyResponse = await fetch('/api/push-subscriptions/vapid-public-key')
  if (!keyResponse.ok) return false
  const { publicKey } = await keyResponse.json() as { publicKey: string }
  if (!publicKey) return false
  const existing = await registration.pushManager.getSubscription()
  const subscription = existing ?? await registration.pushManager.subscribe({
    userVisibleOnly: true,
    applicationServerKey: applicationServerKey(publicKey),
  })
  const serialized = subscription.toJSON()
  const response = await fetch('/api/push-subscriptions', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      browserId: browserId(),
      endpoint: subscription.endpoint,
      p256dh: serialized.keys?.p256dh,
      auth: serialized.keys?.auth,
    }),
  })
  return response.ok
}

async function checkAlert(condition: StoredAlert, permission: NotificationPermission = Notification.permission) {
  const response = await fetch(`/api/recommendations/upgrades?currentGrade=${condition.currentGrade}&year=${condition.year}`)
  if (!response.ok) return false
  const upgrades = await response.json() as Upgrade[]
  const match = upgrades.find((item) => item.currentGapPerPyeong <= condition.maxGapManwon * 10_000)
  if (!match || permission !== 'granted') return false
  new Notification('갈아타기 조건이 좋아졌어요', {
    body: `${match.targetGrade}급지와의 격차가 설정한 범위 안에 들어왔습니다.`,
    tag: `next-home-${condition.currentGrade}-${match.targetGrade}-${condition.year}`,
  })
  return true
}

type AlertPanelProps = {
  year: number
  currentRegionId?: number
  currentGrade?: number
  currentAveragePricePerPyeong?: number
  regionName?: string
}

export default function AlertPanel({ year, currentRegionId, currentGrade, currentAveragePricePerPyeong, regionName }: AlertPanelProps) {
  const [maxGap, setMaxGap] = useState('')
  const [message, setMessage] = useState('')

  useEffect(() => {
    const saved = localStorage.getItem(storageKey)
    if (!saved) return
    const condition = JSON.parse(saved) as StoredAlert
    const interval = window.setInterval(() => void checkAlert(condition), checkIntervalMs)
    return () => window.clearInterval(interval)
  }, [])

  async function enable(event: FormEvent) {
    event.preventDefault()
    if (!('Notification' in window)) {
      setMessage('이 브라우저는 웹 알림을 지원하지 않습니다.')
      return
    }
    const gap = Number(maxGap)
    if (!currentRegionId || !currentGrade || !currentAveragePricePerPyeong || !Number.isFinite(gap) || gap <= 0) {
      setMessage('지도에서 현재 지역을 선택하고 최대 격차를 입력해 주세요.')
      return
    }
    const permission = Notification.permission === 'granted'
      ? 'granted'
      : await Notification.requestPermission()
    if (permission !== 'granted') {
      setMessage('브라우저에서 알림 권한을 허용해 주세요.')
      return
    }
    const condition = { currentGrade, maxGapManwon: gap, year }
    localStorage.setItem(storageKey, JSON.stringify(condition))
    const backgroundReady = await registerBackgroundPush().catch(() => false)
    if (backgroundReady) {
      const targetGrades = [currentGrade - 1, currentGrade - 2].filter((grade) => grade >= 1)
      const targetGapPercent = gap * 10_000 / currentAveragePricePerPyeong * 100
      await Promise.all(targetGrades.map((targetGrade) => fetch('/api/alerts', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          browserId: browserId(),
          currentRegionId,
          targetGrade,
          targetGapPercent,
        }),
      })))
    }
    setMessage(backgroundReady
      ? '알림이 설정되었습니다.'
      : '알림이 설정되었습니다. 현재 브라우저에서는 페이지가 열려 있을 때 작동합니다.')
    await checkAlert(condition, permission)
  }

  function disable() {
    localStorage.removeItem(storageKey)
    setMessage('알림 설정을 해제했습니다.')
  }

  return (
    <section className="alert-panel" id="alerts">
      <div>
        <p className="section-kicker">PRICE GAP ALERT</p>
        <h2>원하는 격차가 오면 알려드릴게요</h2>
        <p>페이지가 열려 있는 동안 한 시간마다 가격 격차를 확인해 브라우저 알림으로 알려드립니다.</p>
      </div>
      <form onSubmit={enable}>
        <div className="alert-current"><span>알림 기준 현재 지역</span><strong>{regionName && currentGrade ? `${regionName} · ${currentGrade}급지` : '지도에서 지역을 선택하세요'}</strong></div>
        <label>최대 평당 격차
          <span className="unit-input"><input inputMode="numeric" value={maxGap} onChange={(event) => setMaxGap(event.target.value)} /><span>만원/평</span></span>
        </label>
        <div className="alert-actions"><button type="submit">웹 알림 켜기</button><button type="button" onClick={disable}>해제</button></div>
        {message && <p aria-live="polite" className="alert-message">{message}</p>}
      </form>
    </section>
  )
}
