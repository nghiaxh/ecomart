import { test, expect, gotoReady } from './fixtures'
import { API_BASE } from './helpers'

test('checkout without address warns instead of ordering', async ({ authedPage: page }) => {
  await gotoReady(page, '/checkout')
  await expect(page.getByRole('heading', { name: 'Thanh toán', exact: true })).toBeVisible()
  const orderBtn = page.getByRole('button', { name: 'Đặt hàng' })
  if (await orderBtn.count()) {
await orderBtn.click()
    await expect(
      page.getByText(/Vui lòng chọn địa chỉ giao hàng|Chưa có địa chỉ nào/).first()
    ).toBeVisible({ timeout: 15_000 })
    await expect(page).toHaveURL('/checkout')
  }
})

test('checkout API rejects missing addressId', async ({ request, customerAuth }) => {
  const res = await request.post(`${API_BASE}/api/orders/checkout`, {
    headers: { Authorization: `Bearer ${customerAuth.token}` },
    data: { paymentMethod: 'COD' }
  })
  expect([400, 422]).toContain(res.status())
})

test('checkout API rejects overlong notes', async ({ request, customerAuth }) => {
  const headers = { Authorization: `Bearer ${customerAuth.token}` }
  const products = await request.get(`${API_BASE}/api/products?page=0&size=5`)
  const product = ((await products.json()) as { content: Array<{ id: number }> }).content[0]
  if (product) {
    await request.post(`${API_BASE}/api/cart`, { headers, data: { productId: product.id, quantity: 1 } })
  }
  const addr = await request.post(`${API_BASE}/api/addresses`, {
    headers,
    data: {
      label: 'Nhà', street: '1 ABC', ward: 'P1', district: 'Q1', city: 'HCM',
      receiverName: 'E2E', receiverPhone: '0901234567', isDefault: true
    }
  })
  if (!addr.ok()) test.skip(true, 'cannot create address')
  const address = (await addr.json()) as { id: number }
  const res = await request.post(`${API_BASE}/api/orders/checkout`, {
    headers,
    data: { addressId: address.id, paymentMethod: 'COD', notes: 'n'.repeat(501) }
  })
  expect([400, 422]).toContain(res.status())
})

test('viewing another user order id shows not-found state', async ({ authedPage: page }) => {
  await gotoReady(page, '/orders/999999999')
  await expect(page.getByText('Không tìm thấy đơn hàng.')).toBeVisible({ timeout: 20_000 })
})

test('chat send failure does not hang the send button', async ({ authedPage: page }) => {
  await page.route('**/api/chat/send', (route) => route.abort())
  await gotoReady(page, '/chat')
  await page.getByPlaceholder('Nhập tin nhắn...').fill('hello e2e')
  await page.locator('form button[type="submit"]').click()
  await expect(page.getByPlaceholder('Nhập tin nhắn...')).toBeVisible()
  await page.unroute('**/api/chat/send')
})


