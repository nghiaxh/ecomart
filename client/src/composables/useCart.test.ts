import { ref } from 'vue'
import { describe, it, expect, vi, beforeEach } from 'vitest'

const { requestMock, toastMock, authMock } = vi.hoisted(() => ({
  requestMock: vi.fn(),
  toastMock: vi.fn(),
  authMock: { isLoggedIn: { value: true }, isAdmin: { value: false } }
}))

vi.mock('@/composables/useApi', () => ({
  useApi: () => ({ request: requestMock })
}))

vi.mock('@/composables/useAuth', () => ({
  useAuth: () => authMock
}))

vi.mock('@/composables/useToast', () => ({
  useToast: () => ({ add: toastMock })
}))

import { useCart } from './useCart'

const cart = {
  items: [
    { productId: 1, productName: 'Bơ', productSlug: 'bo', imageUrl: '', price: 25000, quantity: 2, stock: 10 }
  ],
  subtotal: 50000,
  itemCount: 2
}

describe('useCart', () => {
  beforeEach(() => {
    authMock.isLoggedIn = ref(true)
    authMock.isAdmin = ref(false)
    requestMock.mockReset()
    toastMock.mockReset()
    requestMock.mockResolvedValue(null)
  })

  it('exposes itemCount and subtotal with no cart', () => {
    const { cart: c, itemCount, subtotal } = useCart()
    c.value = null
    expect(itemCount.value).toBe(0)
    expect(subtotal.value).toBe(0)
  })

  it('add posts to API and updates cart', async () => {
    requestMock.mockResolvedValue(cart)
    const { add } = useCart()
    await add(1, 2)
    expect(requestMock).toHaveBeenCalledWith('/api/cart', { method: 'POST', body: { productId: 1, quantity: 2 } })
  })

  it('updateQuantity sends PUT and updates cart', async () => {
    requestMock.mockResolvedValue(cart)
    const { updateQuantity, cart: c } = useCart()
    await updateQuantity(1, 3)
    expect(requestMock).toHaveBeenCalledWith('/api/cart/1?quantity=3', { method: 'PUT' })
    expect(c.value).toEqual(cart)
  })

  it('remove sends DELETE', async () => {
    requestMock.mockResolvedValue(cart)
    const { remove } = useCart()
    await remove(1)
    expect(requestMock).toHaveBeenCalledWith('/api/cart/1', { method: 'DELETE' })
  })

  it('add surfaces a Vietnamese error toast and rethrows on failure', async () => {
    requestMock.mockRejectedValue({ data: { message: 'Hết hàng' } })
    const { add } = useCart()
    await expect(add(1, 2)).rejects.toBeTruthy()
    expect(toastMock).toHaveBeenCalledWith(expect.objectContaining({ severity: 'error' }))
  })

  it('fetchCart deduplicates concurrent calls into a single request', async () => {
    authMock.isLoggedIn.value = false
    const { fetchCart } = useCart()
    requestMock.mockResolvedValue(cart)
    authMock.isAdmin.value = true
    authMock.isLoggedIn.value = true
    const p1 = fetchCart()
    const p2 = fetchCart()
    const results = await Promise.all([p1, p2])
    expect(results[0]).toEqual(cart)
    expect(results[1]).toEqual(cart)
    expect(requestMock).toHaveBeenCalledTimes(1)
    expect(requestMock).toHaveBeenCalledWith('/api/cart')
  })

  it('fetchCart skips the API entirely when logged out', async () => {
    authMock.isLoggedIn.value = false
    const { fetchCart } = useCart()
    requestMock.mockResolvedValue(cart)
    await expect(fetchCart()).resolves.toBeNull()
    expect(requestMock).not.toHaveBeenCalled()
  })

  it('reset clears the cart state', async () => {
    requestMock.mockResolvedValue(cart)
    const { fetchCart, cart: c, reset } = useCart()
    await fetchCart()
    expect(c.value).toEqual(cart)
    reset()
    expect(c.value).toBeNull()
  })

  it('updateQuantity surfaces the server message on failure', async () => {
    requestMock.mockRejectedValue({ data: { message: 'Không thể cập nhật: vượt tồn kho' } })
    const { updateQuantity } = useCart()
    await expect(updateQuantity(1, 3)).rejects.toBeTruthy()
    expect(toastMock).toHaveBeenCalledWith(expect.objectContaining({ summary: 'Không thể cập nhật: vượt tồn kho' }))
  })

  it('remove surfaces the server message on failure', async () => {
    requestMock.mockRejectedValue({ data: { message: 'Không thể xóa sản phẩm khỏi giỏ hàng' } })
    const { remove } = useCart()
    await expect(remove(1)).rejects.toBeTruthy()
    expect(toastMock).toHaveBeenCalledWith(expect.objectContaining({ severity: 'error' }))
  })
})