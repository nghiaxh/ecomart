import { test, expect, gotoReady } from './fixtures'

test('checkout address form validates each required field', async ({ authedPage: page }) => {
  await gotoReady(page, '/checkout')
  await expect(page.getByRole('heading', { name: 'Thanh toán', exact: true })).toBeVisible()
  await page.getByRole('button', { name: 'Thêm mới' }).click()
  await page.getByRole('button', { name: 'Lưu địa chỉ' }).click()
  await expect(page.getByText('Vui lòng nhập nhãn địa chỉ')).toBeVisible()
  await expect(page.getByText('Vui lòng nhập tên người nhận')).toBeVisible()
})

test('checkout address form rejects short phone', async ({ authedPage: page }) => {
  await gotoReady(page, '/checkout')
  await page.getByRole('button', { name: 'Thêm mới' }).click()
  await page.getByPlaceholder('Nhãn (Nhà, Cơ quan...)').fill('Nhà')
  await page.getByPlaceholder('Người nhận').fill('Test User')
  await page.getByPlaceholder('Số điện thoại').fill('123')
  await page.getByPlaceholder('Số nhà, đường, thôn/xóm').fill('123 ABC')
  await page.getByPlaceholder('Phường/Xã').fill('P1')
  await page.getByPlaceholder('Quận/Huyện').fill('Q1')
  await page.getByPlaceholder('Tỉnh/Thành phố').fill('HCM')
  await page.getByRole('button', { name: 'Lưu địa chỉ' }).click()
  await expect(page.getByText('Số điện thoại không hợp lệ')).toBeVisible()
})


