import { test, expect, gotoReady } from './fixtures'
import { setAuthStorage } from './helpers'

test('chat widget answers the shipping fee question on a public page', async ({ page }) => {
  await gotoReady(page, '/products')
  await page.getByRole('button', { name: 'Mở trợ lý EcoMart' }).click()
  await expect(page.getByText('Chào bạn! Mình là trợ lý ảo của EcoMart')).toBeVisible()

  await page.getByRole('button', { name: 'Phí giao hàng là bao nhiêu?' }).click()

  await expect(page.getByText(/Phí giao hàng hiện tại/)).toBeVisible()
})

test('chat widget is hidden on admin routes', async ({ page, adminAuth }) => {
  await setAuthStorage(page, adminAuth)
  await gotoReady(page, '/admin/products')

  await expect(page.getByRole('heading', { name: 'Sản phẩm' })).toBeVisible()
  await expect(page.getByRole('button', { name: 'Mở trợ lý EcoMart' })).toHaveCount(0)
})

test('logged-in customer can ask a question by typing', async ({ authedPage: page }) => {
  await gotoReady(page, '/cart')
  await page.getByRole('button', { name: 'Mở trợ lý EcoMart' }).click()

  const input = page.getByPlaceholder('Hỏi mình điều gì đó...')
  await input.fill('Phí giao hàng là bao nhiêu?')
  await input.press('Enter')

  await expect(page.getByText(/Phí giao hàng hiện tại/)).toBeVisible()
})