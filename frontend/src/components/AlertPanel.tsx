import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'

type StoredAlert = { currentGrade: number; maxGapManwon: number; year: number }
type Upgrade = { targetGrade: number; currentGapPerPyeong: number }
const storageKey = 'next-home-alert'
const checkIntervalMs = 60 * 60 * 1000

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

export default function AlertPanel({ year }: { year: number }) {
  const [currentGrade, setCurrentGrade] = useState(0)
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
    if (!currentGrade || !Number.isFinite(gap) || gap <= 0) {
      setMessage('현재 급지와 최대 격차를 입력해 주세요.')
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
    setMessage('알림이 설정되었습니다.')
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
        <label>알림 기준 현재 급지
          <select value={currentGrade} onChange={(event) => setCurrentGrade(Number(event.target.value))}>
            <option value="0">선택</option>
            {Array.from({ length: 10 }, (_, index) => index + 1).map((grade) => (
              <option value={grade} key={grade}>{grade}급지</option>
            ))}
          </select>
        </label>
        <label>최대 평당 격차
          <span className="unit-input"><input inputMode="numeric" value={maxGap} onChange={(event) => setMaxGap(event.target.value)} /><span>만원/평</span></span>
        </label>
        <div className="alert-actions"><button type="submit">웹 알림 켜기</button><button type="button" onClick={disable}>해제</button></div>
        {message && <p aria-live="polite" className="alert-message">{message}</p>}
      </form>
    </section>
  )
}
