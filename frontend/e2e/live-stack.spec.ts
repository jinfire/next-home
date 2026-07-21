import { expect, test } from '@playwright/test'

test.skip(!process.env.LIVE_E2E, 'LIVE_E2E=1일 때 로컬 백엔드와 실제 NAVER Map을 검증합니다.')

test('renders capital-area grades with the live local stack', async ({ page }) => {
  const failedResponses: string[] = []
  page.on('response', (response) => {
    if (response.status() >= 400) failedResponses.push(`${response.status()} ${new URL(response.url()).pathname}`)
  })

  await page.goto('/')
  await expect(page.getByRole('heading', { name: '수도권 급지지도', exact: true })).toBeVisible()
  await expect(page.locator('.selection-summary')).toBeVisible()

  const boundaryResponse = await page.request.get('/api/region-boundaries?year=2026')
  expect(boundaryResponse.ok()).toBeTruthy()
  const boundary = await boundaryResponse.json()
  expect(boundary.type).toBe('FeatureCollection')
  expect(boundary.features).toHaveLength(83)
  expect(boundary.features.some((feature: { properties: { regionName: string } }) => feature.properties.regionName === '종로구')).toBeTruthy()

  await expect(page.locator('.naver-map')).toBeVisible()
  await expect(page.locator('.naver-map')).toHaveAttribute('data-map-ready', 'true', { timeout: 15_000 })
  await expect(page.getByText('지도를 불러오지 못했습니다.')).toHaveCount(0)
  expect(failedResponses.filter((entry) => entry.includes('/api/'))).toEqual([])
})
