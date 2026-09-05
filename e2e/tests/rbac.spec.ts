import { test, expect } from '@playwright/test'
import { API_BASE, loginAsAdmin, loginAsCustomer, gotoReady } from './helpers'

const customerRoutes = ['/cart', '/checkout', '/orders', '/chat', '/account']
const adminRoutes = ['/admin', '/admin/products', '/admin/orders', '/admin/categories', '/admin/banners']

test('guest is sent to login on customer routes', async ({ page }) => {
  test.setTimeout(180_000)
  for (const route of customerRoutes) {
    await gotoReady(page, route)
    await expect(page, `guest ${route} should go to /login`).toHaveURL('/login')
  }
})

test('guest is sent to login on admin routes', async ({ page }) => {
  test.setTimeout(180_000)
  for (const route of adminRoutes) {
    await gotoReady(page, route)
    await expect(page, `guest ${route} should go to /login`).toHaveURL('/login')
  }
})

test('customer can open customer routes but not admin routes', async ({ page }) => {
  test.setTimeout(180_000)
  await loginAsCustomer(page)
  for (const route of customerRoutes) {
    await gotoReady(page, route)
    await expect(page, `customer ${route} should stay`).not.toHaveURL('/login')
  }
  for (const route of adminRoutes) {
    await gotoReady(page, route)
    await expect(page, `customer ${route} must not stay`).not.toHaveURL(new RegExp(route.replace(/\//g, '\\/') + '$'))
  }
})

test('admin can open admin routes', async ({ page }) => {
  await loginAsAdmin(page)
  await gotoReady(page, '/admin/products')
  await expect(page.getByRole('heading', { name: 'Quản lý sản phẩm' })).toBeVisible()
  await gotoReady(page, '/admin/orders')
  await expect(page.getByRole('heading', { name: 'Quản lý đơn hàng' })).toBeVisible()
})

test('unauthenticated API calls to cart/orders are rejected', async ({ request }) => {
  const cart = await request.get(`${API_BASE}/api/cart`)
  expect([401, 403]).toContain(cart.status())
  const orders = await request.get(`${API_BASE}/api/orders/mine?page=0&size=5`)
  expect([401, 403]).toContain(orders.status())
  const chat = await request.get(`${API_BASE}/api/chat/sessions`)
  expect([401, 403]).toContain(chat.status())
})


