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
