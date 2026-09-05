import { test, expect } from '@playwright/test'
import { API_BASE, gotoReady } from './helpers'

test('minPrice greater than maxPrice yields empty state, clear restores', async ({ page }) => {
  await gotoReady(page, '/products')
  await expect(page.locator('a[href^="/products/"]').first()).toBeVisible()

  await page.getByPlaceholder('Giá tối thiểu (₫)').fill('1000000')
  await page.getByPlaceholder('Giá tối đa (₫)').fill('1000')
  await page.getByRole('button', { name: 'Lọc', exact: true }).click()
  await expect(page.getByText('Không tìm thấy sản phẩm phù hợp.')).toBeVisible()

  await page.getByRole('button', { name: 'Bỏ lọc' }).first().click()
  await expect(page.locator('a[href^="/products/"]').first()).toBeVisible()
})

test('negative price and text input do not crash the list', async ({ page }) => {
  await gotoReady(page, '/products')
  await page.getByPlaceholder('Giá tối thiểu (₫)').fill('-5')
  await page.getByRole('button', { name: 'Lọc', exact: true }).click()
  await expect(
    page.locator('a[href^="/products/"]').first().or(page.getByText('Không tìm thấy sản phẩm phù hợp.'))
  ).toBeVisible()
})

test('vietnamese search with diacritics works or shows empty state', async ({ page }) => {
  await gotoReady(page, '/products')
  await page.getByPlaceholder('Tìm sản phẩm...').fill('rau củ tươi')
  await page.getByRole('button', { name: 'Lọc', exact: true }).click()
  await expect(
    page.locator('a[href^="/products/"]').first().or(page.getByText('Không tìm thấy sản phẩm phù hợp.'))
  ).toBeVisible()
})

test('products API rejects invalid pagination shape gracefully', async ({ request }) => {
  const res = await request.get(`${API_BASE}/api/products?page=-1&size=12`)
  expect([200, 400]).toContain(res.status())
})



