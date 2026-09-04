import { test, expect } from '@playwright/test'
import { API_BASE, clearAuth, loginAsNewCustomer, makeUniqueUser, gotoReady } from './helpers'

test('clearing session mid-flow redirects protected pages to login', async ({ page, request }) => {
  await loginAsNewCustomer(page, request)
  await clearAuth(page)
  await gotoReady(page, '/cart')
  await expect(page).toHaveURL('/login')
  await gotoReady(page, '/checkout')
  await expect(page).toHaveURL('/login')
  await gotoReady(page, '/orders')
  await expect(page).toHaveURL('/login')
})

test('forged token cannot access protected API', async ({ request }) => {
  const res = await request.get(`${API_BASE}/api/cart`, {
    headers: { Authorization: 'Bearer forged.invalid.token' }
  })
  expect([401, 403]).toContain(res.status())
})

test('expired-style empty token cannot checkout', async ({ request }) => {
  const res = await request.post(`${API_BASE}/api/orders/checkout`, {
    data: { addressId: 1, paymentMethod: 'COD' }
  })
  expect([401, 403, 400]).toContain(res.status())
})

test('duplicate register via API is rejected', async ({ request }) => {
  const user = makeUniqueUser('dupapi')
  const first = await request.post(`${API_BASE}/api/auth/register`, { data: user })
  expect(first.ok()).toBe(true)
  const second = await request.post(`${API_BASE}/api/auth/register`, {
    data: { ...user, username: `${user.username}x` }
  })
  expect([400, 409, 422]).toContain(second.status())
})


