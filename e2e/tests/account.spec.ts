import { test, expect, gotoReady } from './fixtures'

test('customer views profile and addresses', async ({ authedPage: page, newCustomer }) => {
  await gotoReady(page, '/account')
  await expect(page.getByRole('heading', { name: 'Tài khoản' })).toBeVisible()
  await expect(page.getByText(newCustomer.username).first()).toBeVisible()
  await expect(page.getByText('Địa chỉ của tôi')).toBeVisible()
})

test('customer updates username successfully', async ({ authedPage: page }) => {
  await gotoReady(page, '/account')
  await page.getByRole('button', { name: 'Chỉnh sửa' }).click()
  const input = page.getByPlaceholder('Tên đăng nhập')
  await input.fill(`edited_${Date.now().toString().slice(-6)}`)
await page.getByRole('button', { name: 'Lưu' }).click()
  await expect(page.getByText('Cập nhật thành công').first()).toBeVisible()
})

test('logout clears session and returns home', async ({ authedPage: page }) => {
  await gotoReady(page, '/account')
  await page.getByRole('button', { name: 'Đăng xuất' }).click()
  await expect(page).toHaveURL('/')
  const session = await page.evaluate(() => localStorage.getItem('ecomart_session'))
  expect(session).toBeNull()
})


