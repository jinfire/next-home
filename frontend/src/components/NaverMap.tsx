import { useEffect, useRef, useState } from 'react'
import { loadNaverMapSdk } from './naverMapSdk'

declare global {
  interface Window {
    naver?: {
      maps: {
        Map: new (element: HTMLElement, options: Record<string, unknown>) => NaverMapInstance
        LatLng: new (latitude: number, longitude: number) => unknown
      }
    }
  }
}

type GeoJson = { type: 'FeatureCollection'; features: unknown[] }
type NaverFeature = { getProperty(name: string): unknown }
type NaverMapInstance = {
  data: {
    addGeoJson(value: GeoJson): NaverFeature[]
    removeFeature(feature: NaverFeature): void
    setStyle(style: (feature: NaverFeature) => Record<string, unknown>): void
    addListener(event: 'click', listener: (event: { feature: NaverFeature }) => void): void
  }
}

const configuredClientId = import.meta.env.VITE_NAVER_MAP_CLIENT_ID as string | undefined

type NaverMapProps = { clientId?: string; year?: number; onSelectRegion?: (regionId: number) => void }

const gradeColors = ['#7c3aed', '#2563eb', '#0891b2', '#059669', '#65a30d', '#ca8a04', '#ea580c', '#dc2626', '#be185d', '#64748b']

export default function NaverMap({ clientId = configuredClientId, year = new Date().getFullYear(), onSelectRegion }: NaverMapProps) {
  const wrapper = useRef<HTMLDivElement>(null)
  const container = useRef<HTMLDivElement>(null)
  const initialized = useRef(false)
  const map = useRef<NaverMapInstance | null>(null)
  const boundaryFeatures = useRef<NaverFeature[]>([])
  const onSelectRegionRef = useRef(onSelectRegion)
  onSelectRegionRef.current = onSelectRegion
  const [mapReady, setMapReady] = useState(false)
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
      map.current = new window.naver.maps.Map(container.current, {
        center: new window.naver.maps.LatLng(37.45, 127.15),
        zoom: 9,
        minZoom: 7,
      })
      map.current.data.addListener('click', (event) => {
        const regionId = Number(event.feature.getProperty('regionId'))
        if (Number.isFinite(regionId)) onSelectRegionRef.current?.(regionId)
      })
      initialized.current = true
      setMapReady(true)
      setMessage('')
    }).catch(() => active && setMessage('지도를 불러오지 못했습니다.'))
    return () => { active = false }
  }, [clientId, visible])

  useEffect(() => {
    if (!mapReady || !map.current) return
    const controller = new AbortController()
    fetch(`/api/region-boundaries?year=${year}`, { signal: controller.signal })
      .then((response) => {
        if (!response.ok) throw new Error('행정구역 경계를 불러오지 못했습니다.')
        return response.json() as Promise<GeoJson>
      })
      .then((geoJson) => {
        if (!map.current) return
        boundaryFeatures.current.forEach((feature) => map.current?.data.removeFeature(feature))
        boundaryFeatures.current = map.current.data.addGeoJson(geoJson)
        map.current.data.setStyle((feature) => {
          const grade = Number(feature.getProperty('grade'))
          return {
            fillColor: gradeColors[grade - 1] ?? '#94a3b8',
            fillOpacity: 0.42,
            strokeColor: '#ffffff',
            strokeWeight: 1.5,
          }
        })
      })
      .catch((reason: Error) => {
        if (reason.name !== 'AbortError') setMessage(reason.message)
      })
    return () => controller.abort()
  }, [mapReady, year])

  return (
    <div ref={wrapper} className="map-canvas">
      <div ref={container} className="naver-map" data-map-ready={mapReady} />
      {message && <p className="map-message">{message}</p>}
    </div>
  )
}
