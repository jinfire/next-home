import { render, waitFor } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import NaverMap from './NaverMap'
import { loadNaverMapSdk, resetNaverMapSdkForTest } from './naverMapSdk'

describe('NAVER Dynamic Map usage protection', () => {
  beforeEach(() => {
    document.head.querySelectorAll('script[data-next-home-map]').forEach((element) => element.remove())
    delete window.naver
    resetNaverMapSdkForTest()
  })

  it('loads one SDK script when multiple consumers request it', () => {
    const first = loadNaverMapSdk('client-id')
    const second = loadNaverMapSdk('client-id')

    expect(first).toBe(second)
    expect(document.head.querySelectorAll('script[data-next-home-map]')).toHaveLength(1)
  })

  it('does not load the SDK until the map enters the viewport', async () => {
    let intersectionCallback: IntersectionObserverCallback | undefined
    vi.stubGlobal('IntersectionObserver', class {
      constructor(callback: IntersectionObserverCallback) { intersectionCallback = callback }
      observe() {}
      disconnect() {}
      unobserve() {}
      takeRecords() { return [] }
      root = null
      rootMargin = '0px'
      thresholds = [0]
    })

    const { container } = render(<NaverMap clientId="client-id" />)
    expect(document.head.querySelector('script[data-next-home-map]')).toBeNull()

    intersectionCallback?.([{ isIntersecting: true, target: container.querySelector('.map-canvas')! } as IntersectionObserverEntry], {} as IntersectionObserver)

    await waitFor(() => expect(document.head.querySelectorAll('script[data-next-home-map]')).toHaveLength(1))
  })

  it('loads the selected year boundaries and applies grade colors', async () => {
    const addGeoJson = vi.fn()
    const setStyle = vi.fn()
    const removeFeature = vi.fn()
    const addListener = vi.fn()
    let mapOptions: Record<string, unknown> | undefined
    addGeoJson.mockReturnValue([])
    const map = { data: { addGeoJson, setStyle, removeFeature, addListener } }
    class MockMap {
      data = map.data
      constructor(_element: HTMLElement, options: Record<string, unknown>) { mapOptions = options }
    }
    class MockLatLng {}
    window.naver = { maps: {
      Map: MockMap,
      LatLng: MockLatLng,
    } }
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: true,
      json: async () => ({ type: 'FeatureCollection', features: [] }),
    }))
    vi.stubGlobal('IntersectionObserver', class {
      constructor(callback: IntersectionObserverCallback) {
        queueMicrotask(() => callback([{ isIntersecting: true } as IntersectionObserverEntry], this as unknown as IntersectionObserver))
      }
      observe() {}
      disconnect() {}
      unobserve() {}
      takeRecords() { return [] }
      root = null
      rootMargin = '0px'
      thresholds = [0]
    })

    const { container } = render(<NaverMap clientId="client-id" year={2026} />)

    await waitFor(() => expect(fetch).toHaveBeenCalledWith('/api/region-boundaries?year=2026', expect.anything()))
    expect(container.querySelector('.naver-map')).toHaveAttribute('data-map-ready', 'true')
    expect(addGeoJson).toHaveBeenCalledWith({ type: 'FeatureCollection', features: [] })
    expect(setStyle).toHaveBeenCalled()
    expect(addListener).toHaveBeenCalledWith('click', expect.any(Function))
    expect(mapOptions).toMatchObject({ zoom: 9, minZoom: 7 })
  })

  it('keeps rendering when NAVER returns null after adding GeoJSON', async () => {
    const addGeoJson = vi.fn().mockReturnValue(null)
    const map = { data: { addGeoJson, setStyle: vi.fn(), removeFeature: vi.fn(), addListener: vi.fn() } }
    class MockMap { data = map.data }
    class MockLatLng {}
    window.naver = { maps: { Map: MockMap, LatLng: MockLatLng } }
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: true,
      json: async () => ({ type: 'FeatureCollection', features: [] }),
    }))
    vi.stubGlobal('IntersectionObserver', class {
      constructor(callback: IntersectionObserverCallback) {
        queueMicrotask(() => callback([{ isIntersecting: true } as IntersectionObserverEntry], this as unknown as IntersectionObserver))
      }
      observe() {}
      disconnect() {}
      unobserve() {}
      takeRecords() { return [] }
      root = null
      rootMargin = '0px'
      thresholds = [0]
    })

    const { rerender } = render(<NaverMap clientId="client-id" year={2026} />)
    await waitFor(() => expect(fetch).toHaveBeenCalledWith('/api/region-boundaries?year=2026', expect.anything()))

    rerender(<NaverMap clientId="client-id" year={2025} />)

    await waitFor(() => expect(fetch).toHaveBeenCalledWith('/api/region-boundaries?year=2025', expect.anything()))
    await waitFor(() => expect(addGeoJson).toHaveBeenCalledTimes(2))
    expect(map.data.removeFeature).not.toHaveBeenCalled()
  })
})
