import { render, waitFor } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import NaverMap, { loadNaverMapSdk, resetNaverMapSdkForTest } from './NaverMap'

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
})
