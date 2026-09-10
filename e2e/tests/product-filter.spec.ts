import { test, expect } from '@playwright/test'
import { API_BASE, gotoReady } from './helpers'

test('homepage renders hero and shows latest products', async ({ page }) => {
  await gotoReady(page, '/')
  await expect(page.getByRole('heading', { name: 'Mua sắm tiện lợi mỗi ngày' })).toBeVisible()
  const grid = page.locator('a[href^="/products/"]')
  const empty = page.getByText('Chưa có sản phẩm.')
  await expect(grid.first().or(empty)).toBeVisible()
})

test('product list renders product cards', async ({ page }) => {
  await gotoReady(page, '/products')
  await expect(page.locator('a[href^="/products/"]').first()).toBeVisible()
})

test('product search filters results (gibberish and Vietnamese diacritics)', async ({ page }) => {
  await gotoReady(page, '/products')
  await expect(page.locator('a[href^="/products/"]').first()).toBeVisible()

  await page.getByPlaceholder('Tìm sản phẩm...').fill('zzz-khong-ton-tai-999')
  await page.getByRole('button', { name: 'Lọc', exact: true }).click()
  await expect(page.getByText('Không tìm thấy sản phẩm phù hợp.')).toBeVisible()

  await page.getByPlaceholder('Tìm sản phẩm...').fill('rau củ tươi')
  await page.getByRole('button', { name: 'Lọc', exact: true }).click()
  await expect(
    page.locator('a[href^="/products/"]').first().or(page.getByText('Không tìm thấy sản phẩm phù hợp.'))
  ).toBeVisible()
})

test('product detail page renders reviews section', async ({ page }) => {
  await gotoReady(page, '/products')
  const first = page.locator('a[href^="/products/"]').first()
  await expect(first).toBeVisible()
  await first.click()
  await expect(page.getByRole('heading', { name: 'Đánh giá' })).toBeVisible()
  await expect(page.getByRole('button', { name: 'Thêm vào giỏ' }).first()).toBeVisible()
})

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
