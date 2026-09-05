import { test, expect, gotoReady } from './fixtures'
import { API_BASE } from './helpers'

test('review API rejects overlong content and invalid rating', async ({ request, customerAuth }) => {
  const headers = { Authorization: `Bearer ${customerAuth.token}` }
  const products = await request.get(`${API_BASE}/api/products?page=0&size=5`)
  const list = ((await products.json()) as { content: Array<{ id: number }> }).content
  const productId = list[0]?.id
  if (!productId) test.skip(true, 'no product to review')

  const tooLong = await request.post(`${API_BASE}/api/reviews`, {
    headers,
    data: { productId, rating: 5, content: 'x'.repeat(1001) }
  })
  expect([400, 422]).toContain(tooLong.status())

  const badRating = await request.post(`${API_BASE}/api/reviews`, {
    headers,
    data: { productId, rating: 9, content: 'ok' }
  })
  expect([400, 422]).toContain(badRating.status())
})

test('logged-in user can open review form and submit', async ({ authedPage: page, request }) => {
  const res = await request.get(`${API_BASE}/api/products?page=0&size=5`)
  const list = ((await res.json()) as { content: Array<{ slug: string }> }).content
  const slug = list[0]?.slug
  if (!slug) test.skip(true, 'no product to review')

  await gotoReady(page, `/products/${slug}`)
  await expect(page.getByRole('heading', { name: 'Đánh giá' })).toBeVisible()
  const toggle = page.getByRole('button', { name: /Viết đánh giá|Đóng/ })
  if (await toggle.count()) {
    await toggle.first().click()
    await expect(page.getByPlaceholder('Chia sẻ trải nghiệm của bạn...')).toBeVisible()
  }
})

test('guest sees no review form toggle', async ({ page, request }) => {
  const res = await request.get(`${API_BASE}/api/products?page=0&size=5`)
  const list = ((await res.json()) as { content: Array<{ slug: string }> }).content
  const slug = list[0]?.slug
  if (!slug) test.skip(true, 'no product to review')

  await gotoReady(page, `/products/${slug}`)
  await expect(page.getByRole('button', { name: 'Viết đánh giá' })).toHaveCount(0)
})


