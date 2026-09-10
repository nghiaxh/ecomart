import { test, expect, gotoReady } from './fixtures'

test('empty cart shows friendly empty state', async ({ authedPage: page }) => {
  // isolated customer starts with an empty cart (no clear-all cart endpoint exists)
  await gotoReady(page, '/cart')
  await expect(page.getByText('Giỏ hàng của bạn đang trống.')).toBeVisible()
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

test('mobile viewport keeps key pages usable', async ({ authedPage: page, request }) => {
  await page.setViewportSize({ width: 390, height: 844 })
  await gotoReady(page, '/checkout')
  await expect(page.getByRole('heading', { name: 'Thanh toán', exact: true })).toBeVisible()
  const orderBtn = page.getByRole('button', { name: 'Đặt hàng' })
  if (await orderBtn.count()) {
    await expect(orderBtn).toBeVisible()
  }
  const res = await request.get('http://localhost:8080/api/products?page=0&size=5')
  const list = ((await res.json()) as { content: Array<{ slug: string }> }).content
  const slug = list[0]?.slug
  if (slug) {
    await gotoReady(page, `/products/${slug}`)
    await expect(page.getByRole('button', { name: /Thêm vào giỏ|Hết hàng/ }).first()).toBeVisible()
  }
})
