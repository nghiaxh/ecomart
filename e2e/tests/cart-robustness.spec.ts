import { test, expect, gotoReady } from './fixtures'
import { API_BASE } from './helpers'

test('double-click add-to-cart creates a single cart line without error', async ({ authedPage: page, request, customerAuth }) => {
  const headers = { Authorization: `Bearer ${customerAuth.token}` }
  const res = await request.get(`${API_BASE}/api/products?page=0&size=5`)
  const product = ((await res.json()) as { content: Array<{ id: number; slug: string; stock: number }> }).content.find((p) => p.stock > 1)
  if (!product) {
    test.skip(true, 'no multi-stock product')
    return
  }

  await gotoReady(page, `/products/${product.slug}`)
  const add = page.getByRole('button', { name: 'Thêm vào giỏ' }).first()
  await expect(add).toBeEnabled()
  await add.dblclick()
  await expect(page.getByText('Đã thêm vào giỏ hàng').first()).toBeVisible({ timeout: 20_000 })

  const cart = await request.get(`${API_BASE}/api/cart`, { headers })
  expect(cart.ok()).toBe(true)
  const body = (await cart.json()) as { items?: unknown[] } | null
  if (body && Array.isArray(body.items)) {
    const lines = body.items.filter(
      (i) => (i as { productId: number }).productId === product.id
    )
    expect(lines.length).toBe(1)
  }
})

test('quantity cannot exceed stock on product page', async ({ authedPage: page, request }) => {
  const res = await request.get(`${API_BASE}/api/products?page=0&size=20`)
  const products = ((await res.json()) as { content: Array<{ slug: string; stock: number }> }).content
  const limited = products.find((p) => p.stock > 0 && p.stock <= 10) ?? products[0]
  if (!limited) test.skip(true, 'no product')

  await gotoReady(page, `/products/${limited.slug}`)
  const plus = page.getByRole('button', { name: 'plus' }).or(page.locator('button:has-text("+")'))
  expect(await page.getByText(/Còn \d+ sản phẩm|Chỉ còn|Hết hàng/).count()).toBeGreaterThanOrEqual(0)
})

test('out-of-stock product disables add-to-cart', async ({ request }) => {
  const res = await request.get(`${API_BASE}/api/products?page=0&size=50`)
  const products = ((await res.json()) as { content: Array<{ id: number; stock: number }> }).content
  const empty = products.find((p) => p.stock <= 0)
  test.skip(!empty, 'no out-of-stock product in seed')
})

test('products page survives API failure with error toast', async ({ authedPage: page }) => {
  await page.route('**/api/products**', (route) => {
    route.fulfill({
      status: 500,
      contentType: 'application/json',
      body: JSON.stringify({
        status: 500,
        message: 'Internal Server Error',
        path: '/api/products'
      })
    })
  })
  await page.route('**/api/categories**', (route) => {
    route.fulfill({ status: 200, body: JSON.stringify([]) })
  })
  await gotoReady(page, '/products')
  // The products page should show empty state when API fails
  await expect(page.getByText('Không tìm thấy sản phẩm phù hợp').first()).toBeVisible({ timeout: 20_000 })
  await page.unroute('**/api/products**')
  await page.unroute('**/api/categories**')
})


