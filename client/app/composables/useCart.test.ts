import { describe, it, expect, vi, beforeEach } from 'vitest'

const { requestMock, toastMock, authMock } = vi.hoisted(() => ({
  requestMock: vi.fn(),
  toastMock: vi.fn(),
  authMock: { isLoggedIn: { value: true } }
}))

vi.mock('~/composables/useApi', () => ({
  useApi: () => ({ request: requestMock })
}))

vi.mock('~/composables/useAuth', () => ({
  useAuth: () => authMock
}))

vi.mock('@nuxt/ui/composables/useToast', () => ({
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
    expect(toastMock).toHaveBeenCalledWith(expect.objectContaining({ color: 'error' }))
  })
})