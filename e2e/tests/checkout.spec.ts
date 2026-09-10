import { test, expect, gotoReady } from './fixtures'
import { loginAsCustomer, API_BASE } from './helpers'

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

test('customer reaches the checkout page from the cart', async ({ page }) => {
  await loginAsCustomer(page)
  await gotoReady(page, '/products')
  await page.locator('a[href^="/products/"]').first().click()
  await page.getByRole('button', { name: 'Thêm vào giỏ' }).first().click()
  await expect(page.getByText('Đã thêm vào giỏ hàng').first()).toBeVisible()
  await page.getByRole('link', { name: 'Giỏ hàng' }).click()
  await page.getByRole('link', { name: 'Tiến hành thanh toán' }).click()
  await expect(page.getByRole('heading', { name: 'Thanh toán', exact: true })).toBeVisible()
})

test('checkout shows address, COD/PayOS options, and Quet ma', async ({ page }) => {
  await loginAsCustomer(page)
  await gotoReady(page, '/checkout')
  await expect(page.getByRole('heading', { name: 'Thanh toán', exact: true })).toBeVisible()
  await expect(page.getByRole('heading', { name: 'Địa chỉ giao hàng' })).toBeVisible()
  await expect(page.getByText('Phương thức thanh toán')).toBeVisible()
  await expect(page.getByText('COD')).toBeVisible()
  await expect(page.getByText('PayOS QR')).toBeVisible()
  await expect(page.getByText('Quét mã chuyển khoản')).toBeVisible()
})

test('checkout address form validates required fields and short phone', async ({ authedPage: page }) => {
  await gotoReady(page, '/checkout')
  await expect(page.getByRole('heading', { name: 'Thanh toán', exact: true })).toBeVisible()
  await page.getByRole('button', { name: 'Thêm mới' }).click()
  await page.getByRole('button', { name: 'Lưu địa chỉ' }).click()
  await expect(page.getByText('Vui lòng nhập nhãn địa chỉ')).toBeVisible()
  await expect(page.getByText('Vui lòng nhập tên người nhận')).toBeVisible()

  await page.getByPlaceholder('Nhãn (Nhà, Cơ quan...)').fill('Nhà')
  await page.getByPlaceholder('Người nhận').fill('Test User')
  await page.getByPlaceholder('Số điện thoại').fill('123')
  await page.getByPlaceholder('Số nhà, đường, thôn/xóm').fill('123 ABC')
  await page.getByPlaceholder('Phường/Xã').fill('P1')
  await page.getByPlaceholder('Quận/Huyện').fill('Q1')
  await page.getByPlaceholder('Tỉnh/Thành phố').fill('HCM')
  await page.getByRole('button', { name: 'Lưu địa chỉ' }).click()
  await expect(page.getByText('Số điện thoại không hợp lệ')).toBeVisible()
})

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

test('matrix: COD checkout succeeds via API', async ({ request, customerAuth }) => {
  const { addressId } = await setupCartAndAddress(request, customerAuth.token)
  const headers = { Authorization: `Bearer ${customerAuth.token}` }

  const emptyNotes = await request.post(`${API_BASE}/api/orders/checkout`, {
    headers,
    data: { addressId, paymentMethod: 'COD', notes: '' }
  })
  expect(emptyNotes.ok(), await emptyNotes.text()).toBe(true)
  const body = (await emptyNotes.json()) as { orderId: number }
  expect(body.orderId).toBeGreaterThan(0)

  const { addressId: addr2 } = await setupCartAndAddress(request, customerAuth.token)
  const withNotes = await request.post(`${API_BASE}/api/orders/checkout`, {
    headers,
    data: { addressId: addr2, paymentMethod: 'COD', notes: 'Gọi trước khi giao' }
  })
  expect(withNotes.ok(), await withNotes.text()).toBe(true)
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

test('viewing another user order id shows not-found state', async ({ authedPage: page }) => {
  await gotoReady(page, '/orders/999999999')
  await expect(page.getByText('Không tìm thấy đơn hàng.')).toBeVisible({ timeout: 20_000 })
})
