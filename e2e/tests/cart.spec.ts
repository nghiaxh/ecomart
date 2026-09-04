import { test, expect } from '@playwright/test'
import { loginAsCustomer } from './helpers'

test('customer adds a product to the cart', async ({ page }) => {
  await loginAsCustomer(page)
  await page.goto('/products')
  await page.locator('a[href^="/products/"]').first().click()
  await page.getByRole('button', { name: 'Thêm vào giỏ' }).click()
  await expect(page.locator('a[aria-label="Giỏ hàng"]')).toBeVisible()
})