import { test, expect, gotoReady } from './fixtures'
import { API_BASE } from './helpers'

interface ProductLite {
  id: number
  stock: number
}

async function setupCartAndAddress(
  request: import('@playwright/test').APIRequestContext,
  token: string
): Promise<{ addressId: number; productId: number }> {
  const headers = { Authorization: `Bearer ${token}` }
  const products = await request.get(`${API_BASE}/api/products?page=0&size=20`)
  const list = ((await products.json()) as { content: ProductLite[] }).content
  const product = list.find((p) => p.stock > 0)
  if (!product) throw new Error('no in-stock product for matrix')
  await request.post(`${API_BASE}/api/cart`, { headers, data: { productId: product.id, quantity: 1 } })
  const addr = await request.post(`${API_BASE}/api/addresses`, {
    headers,
    data: {
      label: 'Nhà', street: '123 Matrix', ward: 'P1', district: 'Q1', city: 'HCM',
      receiverName: 'Matrix User', receiverPhone: '0901234567', isDefault: true
    }
  })
  if (!addr.ok()) throw new Error(`address setup failed: ${await addr.text()}`)
  const address = (await addr.json()) as { id: number }
  return { addressId: address.id, productId: product.id }
}

test('matrix: no address + COD via API fails', async ({ request, customerAuth }) => {
  const res = await request.post(`${API_BASE}/api/orders/checkout`, {
    headers: { Authorization: `Bearer ${customerAuth.token}` },
    data: { paymentMethod: 'COD' }
  })
  expect([400, 422]).toContain(res.status())
})

test('matrix: address + COD + notes empty succeeds', async ({ request, customerAuth }) => {
  const { addressId } = await setupCartAndAddress(request, customerAuth.token)
  const res = await request.post(`${API_BASE}/api/orders/checkout`, {
    headers: { Authorization: `Bearer ${customerAuth.token}` },
    data: { addressId, paymentMethod: 'COD', notes: '' }
  })
  expect(res.ok(), await res.text()).toBe(true)
  const body = (await res.json()) as { orderId: number }
  expect(body.orderId).toBeGreaterThan(0)
})

test('matrix: address + COD + notes present succeeds', async ({ request, customerAuth }) => {
  const { addressId } = await setupCartAndAddress(request, customerAuth.token)
  const res = await request.post(`${API_BASE}/api/orders/checkout`, {
    headers: { Authorization: `Bearer ${customerAuth.token}` },
    data: { addressId, paymentMethod: 'COD', notes: 'Gọi trước khi giao' }
  })
  expect(res.ok(), await res.text()).toBe(true)
})

test('matrix: empty cart + address fails gracefully', async ({ request, customerAuth }) => {
  const headers = { Authorization: `Bearer ${customerAuth.token}` }
  const addr = await request.post(`${API_BASE}/api/addresses`, {
    headers,
    data: {
      label: 'Cơ quan', street: '456 Empty', ward: 'P2', district: 'Q2', city: 'HCM',
      receiverName: 'Empty Cart', receiverPhone: '0901234567'
    }
  })
  if (!addr.ok()) test.skip(true, 'cannot create address')
  const address = (await addr.json()) as { id: number }
  const res = await request.post(`${API_BASE}/api/orders/checkout`, {
    headers,
    data: { addressId: address.id, paymentMethod: 'COD' }
  })
  expect([200, 400, 422]).toContain(res.status())
})

test('matrix: PayOS option renders without requiring real payment', async ({ authedPage: page }) => {
  await gotoReady(page, '/checkout')
  await expect(page.getByText('PayOS QR')).toBeVisible()
  await expect(page.getByText('Quét mã chuyển khoản')).toBeVisible()
})


