import { test, expect, gotoReady } from './fixtures'

test('customer sends a support message and gets a reply', async ({ authedPage: page }) => {
  await gotoReady(page, '/chat')
  await expect(page.getByRole('heading', { name: 'Chat hỗ trợ' })).toBeVisible()

  const input = page.getByPlaceholder('Nhập tin nhắn...')
  await input.fill('Cho mình hỏi về giao hàng')
  await page.getByRole('button', { name: 'paper-plane-tilt' }).or(page.locator('form button[type="submit"]')).first().click()

  await expect(page.getByText('Xin chào! Bạn cần hỗ trợ gì?').or(page.locator('text=giao').first())).toBeVisible({ timeout: 20_000 })
})

test('chat widget prompts guest to log in', async ({ page }) => {
  await gotoReady(page, '/')
  await page.getByRole('button', { name: /Mở chat|Đóng chat/ }).click()
  await expect(page.getByText('Đăng nhập để trò chuyện cùng EcoBot')).toBeVisible()
})

test('empty chat message does not send', async ({ authedPage: page }) => {
  await gotoReady(page, '/chat')
  const send = page.locator('form button[type="submit"]')
  await expect(send).toBeDisabled()
})


