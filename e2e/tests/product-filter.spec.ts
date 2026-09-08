import { test, expect } from '@playwright/test'
import { API_BASE, gotoReady } from './helpers'

test('price range filter applies and clear restores', async ({ page }) => {
  await gotoReady(page, '/products')
  await expect(page.locator('a[href^="/products/"]').first()).toBeVisible()

  await page.getByRole('combobox').first().click()
  await page.getByRole('option', { name: 'Trên 100.000 ₫' }).click()
  await page.getByRole('button', { name: 'Lọc', exact: true }).click()
  await expect(page.getByText('Không tìm thấy sản phẩm phù hợp.')).toBeHidden()
  expect(page.url()).toContain('priceRange=100000-')

  await page.getByRole('button', { name: 'Bỏ lọc' }).first().click()
  await expect(page.locator('a[href^="/products/"]').first()).toBeVisible()
})

test('sort by price dropdown updates URL and keeps the list working', async ({ page }) => {
  await gotoReady(page, '/products')
  await page.getByRole('combobox').nth(1).click()
  await page.getByRole('option', { name: 'Giá cao đến thấp' }).click()
  await page.getByRole('button', { name: 'Lọc', exact: true }).click()
  await expect(page.url()).toContain('sort=price_desc')
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

test('parent categories show products from child categories', async ({ page }) => {
  for (const slug of ['rau-cu-sach', 'trai-cay-tuoi', 'thuc-pham-kho']) {
    await gotoReady(page, `/products?category=${slug}`)
    await expect(page.locator('a[href^="/products/"]').first()).toBeVisible({ timeout: 15000 })
    await expect(page.getByText('Không tìm thấy sản phẩm phù hợp.')).toBeHidden()
  }
})

test('products API rejects invalid pagination shape gracefully', async ({ request }) => {
  const res = await request.get(`${API_BASE}/api/products?page=-1&size=12`)
  expect([200, 400]).toContain(res.status())
})



