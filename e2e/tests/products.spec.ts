import { test, expect } from '@playwright/test'

test('homepage renders a heading', async ({ page }) => {
  await page.goto('/')
  await expect(page.getByRole('heading', { level: 1 }).first()).toBeVisible()
})

test('product list renders product cards', async ({ page }) => {
  await page.goto('/products')
  await expect(page.locator('a[href^="/products/"]').first()).toBeVisible()
})

test('product detail page renders a product', async ({ page }) => {
  await page.goto('/products')
  const first = page.locator('a[href^="/products/"]').first()
  await expect(first).toBeVisible()
  await first.click()
  await expect(page.getByRole('heading', { name: 'Đánh giá' })).toBeVisible()
})