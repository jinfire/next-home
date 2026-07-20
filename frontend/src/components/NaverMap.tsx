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

const clientId = import.meta.env.VITE_NAVER_MAP_CLIENT_ID as string | undefined

export default function NaverMap() {
  const container = useRef<HTMLDivElement>(null)
  const [message, setMessage] = useState('지도를 불러오는 중입니다.')

  useEffect(() => {
    if (!clientId) {
      setMessage('지도 Client ID를 확인해 주세요.')
      return
    }

    const initialize = () => {
      if (!container.current || !window.naver) return
      new window.naver.maps.Map(container.current, {
        center: new window.naver.maps.LatLng(37.5665, 126.978),
        zoom: 11,
        minZoom: 8,
      })
      setMessage('')
    }

    if (window.naver) {
      initialize()
      return
    }

    const existing = document.querySelector<HTMLScriptElement>('script[data-next-home-map]')
    const script = existing ?? document.createElement('script')
    if (!existing) {
      script.src = `https://oapi.map.naver.com/openapi/v3/maps.js?ncpKeyId=${encodeURIComponent(clientId)}`
      script.async = true
      script.dataset.nextHomeMap = 'true'
      document.head.appendChild(script)
    }
    script.addEventListener('load', initialize)
    script.addEventListener('error', () => setMessage('지도를 불러오지 못했습니다.'))
    return () => script.removeEventListener('load', initialize)
  }, [])

  return (
    <div className="map-canvas">
      <div ref={container} className="naver-map" />
      {message && <p className="map-message">{message}</p>}
    </div>
  )
}
