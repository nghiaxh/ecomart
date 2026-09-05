import { test, expect } from '@playwright/test'
import { gotoReady } from './helpers'

test('homepage renders hero heading', async ({ page }) => {
  await gotoReady(page, '/')
  await expect(page.getByRole('heading', { name: 'Mua sắm tiện lợi mỗi ngày' })).toBeVisible()
})

test('homepage shows latest products or empty state', async ({ page }) => {
  await gotoReady(page, '/')
  const grid = page.locator('a[href^="/products/"]')
  const empty = page.getByText('Chưa có sản phẩm.')
  await expect(grid.first().or(empty)).toBeVisible()
})

test('product list renders product cards', async ({ page }) => {
  await gotoReady(page, '/products')
  await expect(page.locator('a[href^="/products/"]').first()).toBeVisible()
})

test('product search filters results', async ({ page }) => {
  await gotoReady(page, '/products')
  await expect(page.locator('a[href^="/products/"]').first()).toBeVisible()
  await page.getByPlaceholder('Tìm sản phẩm...').fill('zzz-khong-ton-tai-999')
  await page.getByRole('button', { name: 'Lọc', exact: true }).click()
  await expect(page.getByText('Không tìm thấy sản phẩm phù hợp.')).toBeVisible()
})

test('product detail page renders reviews section', async ({ page }) => {
  await gotoReady(page, '/products')
  const first = page.locator('a[href^="/products/"]').first()
  await expect(first).toBeVisible()
  await first.click()
  await expect(page.getByRole('heading', { name: 'Đánh giá' })).toBeVisible()
  await expect(page.getByRole('button', { name: 'Thêm vào giỏ' }).first()).toBeVisible()
})



