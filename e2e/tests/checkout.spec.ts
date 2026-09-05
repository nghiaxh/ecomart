import { test, expect } from '@playwright/test'
import { loginAsCustomer, gotoReady } from './helpers'

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

test('checkout shows address and COD/PayOS options', async ({ page }) => {
  await loginAsCustomer(page)
  await gotoReady(page, '/checkout')
await expect(page.getByRole('heading', { name: 'Thanh toán', exact: true })).toBeVisible()
  await expect(page.getByRole('heading', { name: 'Địa chỉ giao hàng' })).toBeVisible()
  await expect(page.getByText('Phương thức thanh toán')).toBeVisible()
  await expect(page.getByText('COD')).toBeVisible()
  await expect(page.getByText('PayOS QR')).toBeVisible()
})


