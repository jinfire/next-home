import { expect, test } from '@playwright/test'

const grade = {
  regionId: 1, regionCode: '11680', regionName: '강남구', year: 2026,
  averagePricePerPyeong: 90_000_000, grade: 5, tradeCount: 42,
}

const regionOptions = [{
  id: 100, code: '11', name: '서울특별시',
  regions: [{ id: 1, code: '11680', name: '강남구' }],
}]

test.beforeEach(async ({ page }) => {
  await page.route('**/api/**', async (route) => {
    const url = new URL(route.request().url())
    if (url.pathname === '/api/grades/years') return route.fulfill({ json: [2025, 2026] })
    if (url.pathname === '/api/grades/coverage') return route.fulfill({ json: ['2026-06'] })
    if (url.pathname === '/api/grades') return route.fulfill({ json: [grade] })
    if (url.pathname === '/api/region-boundaries') return route.fulfill({ json: { type: 'FeatureCollection', features: [] } })
    if (url.pathname === '/api/regions/options') return route.fulfill({ json: regionOptions })
    if (url.pathname === '/api/recommendations/upgrades') return route.fulfill({ json: {
      regionId: 1, regionName: '강남구', currentGrade: 5, year: 2026,
      currentAveragePricePerPyeong: 90_000_000,
      targets: [{
        currentGrade: 5, targetGrade: 4, year: 2026,
        currentAveragePricePerPyeong: 90_000_000,
        targetAveragePricePerPyeong: 110_000_000,
        currentGapPerPyeong: 20_000_000, historicalGapPercentile: 35, historicalYears: 2,
      }],
      nearbyRegions: [{ regionId: 2, regionName: '서초구', grade: 4, averagePricePerPyeong: 115_000_000 }],
    } })
    return route.fulfill({ status: 404, json: {} })
  })
})

test('selects a district and compares one-grade-up prices', async ({ page }) => {
  await page.goto('/')
  await expect(page.getByRole('heading', { name: '수도권 급지지도', exact: true })).toBeVisible()
  await expect(page.getByLabel('기준 연도')).toBeVisible()

  await expect(page.locator('.selection-summary')).toContainText('강남구')

  await page.getByLabel('현재 거주 지역 시·도').selectOption('11')
  await page.getByLabel('현재 거주 지역 시·군·구').selectOption('1')
  const recommendation = page.locator('.nearby-regions').filter({ hasText: '서초구' })
  await expect(recommendation.getByText('서초구')).toBeVisible()
  await expect(recommendation).toContainText('25평 기준')
  await expect(recommendation).toContainText('34평 기준')
})
