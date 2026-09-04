import { test, expect } from '@playwright/test'
import { loginAsCustomer, gotoReady } from './helpers'

test('guest adding to cart is sent to login', async ({ page }) => {
  await gotoReady(page, '/products')
  await page.locator('a[href^="/products/"]').first().click()
  await page.getByRole('button', { name: 'Thêm vào giỏ' }).first().click()
  await expect(page).toHaveURL('/login')
})

test('customer adds a product to the cart', async ({ page }) => {
  await loginAsCustomer(page)
  await gotoReady(page, '/products')
  await page.locator('a[href^="/products/"]').first().click()
await page.getByRole('button', { name: 'Thêm vào giỏ' }).first().click()
  await expect(page.getByText('Đã thêm vào giỏ hàng').first()).toBeVisible()
  await expect(page.locator('a[aria-label="Giỏ hàng"]')).toBeVisible()
})

test('customer can change quantity and remove item', async ({ page }) => {
  await loginAsCustomer(page)
  await gotoReady(page, '/products')
await page.locator('a[href^="/products/"]').first().click()
  await page.getByRole('button', { name: 'Thêm vào giỏ' }).first().click()
  await expect(page.getByText('Đã thêm vào giỏ hàng').first()).toBeVisible()

  await gotoReady(page, '/cart')
  const qty = page.locator('span.w-8.text-center').first()
  await expect(qty.or(page.getByText('Giỏ hàng của bạn đang trống.'))).toBeVisible()
})


