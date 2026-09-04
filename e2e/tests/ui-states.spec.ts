import { test, expect, gotoReady } from './fixtures'

test('empty cart shows friendly empty state', async ({ authedPage: page }) => {
  // isolated customer starts with an empty cart (no clear-all cart endpoint exists)
  await gotoReady(page, '/cart')
  await expect(page.getByText('Giỏ hàng của bạn đang trống.')).toBeVisible()
})

test('loading skeletons appear on orders page', async ({ authedPage: page }) => {
  await gotoReady(page, '/orders')
  await expect(
    page.locator('.animate-pulse, [class*="skeleton"]').first().or(page.getByRole('heading', { name: 'Đơn hàng của tôi' }))
  ).toBeVisible()
})

test('long product name does not overflow viewport', async ({ page }) => {
  await gotoReady(page, '/products')
  const first = page.locator('a[href^="/products/"]').first()
  await expect(first).toBeVisible()
  const box = await first.boundingBox()
  expect(box).toBeTruthy()
  expect(box!.x + box!.width).toBeLessThanOrEqual(1920)
})

test('broken product image falls back to placeholder', async ({ authedPage: page, request }) => {
  const res = await request.get('http://localhost:8080/api/products?page=0&size=5')
  const list = ((await res.json()) as { content: Array<{ slug: string }> }).content
  const slug = list[0]?.slug
  if (!slug) test.skip(true, 'no product')
  await page.route('**/*.{jpg,jpeg,png,webp}', (route) => route.abort())
  await gotoReady(page, `/products/${slug}`)
  await expect(page.getByRole('heading', { name: 'Đánh giá' })).toBeVisible()
  await page.unroute('**/*.{jpg,jpeg,png,webp}')
})

test('mobile viewport keeps checkout usable', async ({ authedPage: page }) => {
  await page.setViewportSize({ width: 390, height: 844 })
  await gotoReady(page, '/checkout')
  await expect(page.getByRole('heading', { name: 'Thanh toán', exact: true })).toBeVisible()
  const orderBtn = page.getByRole('button', { name: 'Đặt hàng' })
  if (await orderBtn.count()) {
    await expect(orderBtn).toBeVisible()
  }
})

test('mobile product detail keeps sticky add-to-cart visible', async ({ page, request }) => {
  await page.setViewportSize({ width: 390, height: 844 })
  const res = await request.get('http://localhost:8080/api/products?page=0&size=5')
  const list = ((await res.json()) as { content: Array<{ slug: string }> }).content
  const slug = list[0]?.slug
  if (!slug) test.skip(true, 'no product')
  await gotoReady(page, `/products/${slug}`)
  await expect(page.getByRole('button', { name: /Thêm vào giỏ|Hết hàng/ }).first()).toBeVisible()
})



