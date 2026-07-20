import { expect, test } from '@playwright/test'

test.skip(!process.env.LIVE_E2E, 'LIVE_E2E=1일 때 로컬 백엔드·DB와 실제 NAVER Map을 검증합니다.')

test('renders collected grades with the live local stack', async ({ page }) => {
  const failedResponses: string[] = []
  page.on('response', (response) => {
    if (response.status() >= 400) failedResponses.push(`${response.status()} ${new URL(response.url()).pathname}`)
  })

  await page.goto('/')
  await expect(page.getByLabel('지역 급지 목록').getByText('종로구')).toBeVisible()
  await expect(page.locator('.selection-summary')).toContainText('1급지')
  await expect(page.locator('.naver-map')).toBeVisible()
  await expect(page.locator('.naver-map > div')).not.toHaveCount(0, { timeout: 15_000 })
  await expect(page.getByText('지도를 불러오지 못했습니다.')).toHaveCount(0)
  expect(failedResponses.filter((entry) => entry.includes('/api/'))).toEqual([])
})
