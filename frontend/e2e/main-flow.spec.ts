import { expect, test } from '@playwright/test'

const grade = {
  regionId: 1, regionCode: '11680', regionName: '강남구', year: 2026,
  averagePricePerPyeong: 90_000_000, grade: 5, tradeCount: 42,
}

test.beforeEach(async ({ page }) => {
  await page.route('**/api/**', async (route) => {
    const url = new URL(route.request().url())
    if (url.pathname === '/api/grades') return route.fulfill({ json: [grade] })
    if (url.pathname === '/api/region-boundaries') return route.fulfill({ json: { type: 'FeatureCollection', features: [] } })
    if (url.pathname === '/api/regions') return route.fulfill({ json: [{ id: 1, code: '11680', name: '강남구', level: 2 }] })
    if (url.pathname === '/api/recommendations/upgrades') return route.fulfill({ json: [{
      currentGrade: 5, targetGrade: 4, year: 2026,
      currentAveragePricePerPyeong: 90_000_000,
      targetAveragePricePerPyeong: 110_000_000,
      currentGapPerPyeong: 20_000_000, historicalGapPercentile: 35, historicalYears: 8,
    }] })
    return route.fulfill({ status: 404, json: {} })
  })
})

test('searches a region and compares one-grade-up prices', async ({ page }) => {
  await page.goto('/')
  await expect(page.getByRole('heading', { name: '사는 곳의 가치를 한눈에' })).toBeVisible()
  await expect(page.getByLabel('지역 급지 목록').getByText('강남구')).toBeVisible()

  await page.getByLabel('지역 검색').fill('강남')
  await page.getByRole('search').getByRole('button', { name: '검색', exact: true }).click()
  await page.getByRole('button', { name: /강남구.*11680/ }).click()
  await expect(page.locator('.selection-summary')).toContainText('강남구')

  await page.getByLabel('현재 급지').selectOption('5')
  const recommendation = page.locator('.upgrade-card').filter({ hasText: '4급지' })
  await expect(recommendation.getByText('1단계 위')).toBeVisible()
  await expect(recommendation.getByText('+2,000만원/평')).toBeVisible()
})
