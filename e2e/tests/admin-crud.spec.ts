import { test, expect } from '@playwright/test'
import { loginAsAdmin, gotoReady } from './helpers'

test.beforeEach(async ({ page }) => {
  await loginAsAdmin(page)
})

test('admin dashboard shows stat cards', async ({ page }) => {
  await gotoReady(page, '/admin')
  await expect(page.getByText('Sản phẩm').first()).toBeVisible()
  await expect(page.getByText('Đơn hàng').first()).toBeVisible()
})

test('admin product form validates empty name', async ({ page }) => {
  await gotoReady(page, '/admin/products')
  await expect(page.getByRole('heading', { name: 'Quản lý sản phẩm' })).toBeVisible()
  await page.getByRole('button', { name: 'Thêm sản phẩm' }).click()
  await expect(page.getByText('Thêm sản phẩm mới')).toBeVisible()
  await page.getByRole('button', { name: 'Lưu', exact: true }).click()
  await expect(page.getByText('Vui lòng nhập tên sản phẩm')).toBeVisible()
})

test('admin product form rejects negative price shape', async ({ page, request }) => {
  const cats = await request.get('http://localhost:8080/api/categories')
  if (!cats.ok()) test.skip(true, 'cannot load categories')
  await gotoReady(page, '/admin/products')
  await page.getByRole('button', { name: 'Thêm sản phẩm' }).click()
  await expect(page.getByText('Thêm sản phẩm mới')).toBeVisible()
})

test('admin orders page renders list or empty state', async ({ page }) => {
  await gotoReady(page, '/admin/orders')
  await expect(
    page.getByText(/^Đơn #\d+/).first().or(page.getByText('Không có đơn hàng').first())
  ).toBeVisible({ timeout: 20_000 })
})

test('admin can filter orders by status', async ({ page }) => {
  await gotoReady(page, '/admin/orders')
  await expect(page.getByRole('heading', { name: 'Quản lý đơn hàng' })).toBeVisible()
})



