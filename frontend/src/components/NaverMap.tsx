import { useEffect, useRef, useState } from 'react'

declare global {
  interface Window {
    naver?: {
      maps: {
        Map: new (element: HTMLElement, options: Record<string, unknown>) => unknown
        LatLng: new (latitude: number, longitude: number) => unknown
      }
    }
  }
}

const configuredClientId = import.meta.env.VITE_NAVER_MAP_CLIENT_ID as string | undefined
let sdkPromise: Promise<void> | undefined

export function loadNaverMapSdk(clientId: string): Promise<void> {
  if (window.naver) return Promise.resolve()
  if (sdkPromise) return sdkPromise

  sdkPromise = new Promise((resolve, reject) => {
    const existing = document.querySelector<HTMLScriptElement>('script[data-next-home-map]')
    const script = existing ?? document.createElement('script')
    script.addEventListener('load', () => resolve(), { once: true })
    script.addEventListener('error', () => {
      sdkPromise = undefined
      reject(new Error('NAVER Map SDK load failed'))
    }, { once: true })
    if (!existing) {
      script.src = `https://oapi.map.naver.com/openapi/v3/maps.js?ncpKeyId=${encodeURIComponent(clientId)}`
      script.async = true
      script.dataset.nextHomeMap = 'true'
      document.head.appendChild(script)
    }
  })
  return sdkPromise
}

export function resetNaverMapSdkForTest() {
  sdkPromise = undefined
}

type NaverMapProps = { clientId?: string }

export default function NaverMap({ clientId = configuredClientId }: NaverMapProps) {
  const wrapper = useRef<HTMLDivElement>(null)
  const container = useRef<HTMLDivElement>(null)
  const initialized = useRef(false)
  const [visible, setVisible] = useState(false)
  const [message, setMessage] = useState('지도를 불러오는 중입니다.')

  useEffect(() => {
    if (!wrapper.current) return
    if (!('IntersectionObserver' in window)) {
      setVisible(true)
      return
    }
    const observer = new IntersectionObserver(([entry]) => {
      if (entry.isIntersecting) {
        setVisible(true)
        observer.disconnect()
      }
    }, { rootMargin: '200px' })
    observer.observe(wrapper.current)
    return () => observer.disconnect()
  }, [])

  useEffect(() => {
    if (!visible || initialized.current) return
    if (!clientId) {
      setMessage('지도 Client ID를 확인해 주세요.')
      return
    }

    let active = true
    loadNaverMapSdk(clientId).then(() => {
      if (!active || initialized.current || !container.current || !window.naver) return
      new window.naver.maps.Map(container.current, {
        center: new window.naver.maps.LatLng(37.5665, 126.978),
        zoom: 11,
        minZoom: 8,
      })
      initialized.current = true
      setMessage('')
    }).catch(() => active && setMessage('지도를 불러오지 못했습니다.'))
    return () => { active = false }
  }, [clientId, visible])

  return (
    <div ref={wrapper} className="map-canvas">
      <div ref={container} className="naver-map" />
      {message && <p className="map-message">{message}</p>}
    </div>
  )
}
