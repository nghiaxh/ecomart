import type { Cart } from '~/types'

export const useCart = () => {
  const cart = useState<Cart | null>('ecomart_cart', () => null)
  const { request } = useApi()
  const { isLoggedIn } = useAuth()

  const itemCount = computed(() => cart.value?.itemCount ?? 0)
  const subtotal = computed(() => cart.value?.subtotal ?? 0)

  const fetchCart = async () => {
    if (!isLoggedIn.value) return null
    try {
      cart.value = await request<Cart>('/api/cart')
      return cart.value
    } catch {
      cart.value = null
      return null
    }
  }

  const add = async (productId: number, quantity = 1) => {
    cart.value = await request<Cart>('/api/cart', {
      method: 'POST',
      body: { productId, quantity }
    })
    return cart.value
  }

  const updateQuantity = async (productId: number, quantity: number) => {
    cart.value = await request<Cart>(`/api/cart/${productId}?quantity=${quantity}`, {
      method: 'PUT'
    })
    return cart.value
  }

  const remove = async (productId: number) => {
    cart.value = await request<Cart>(`/api/cart/${productId}`, {
      method: 'DELETE'
    })
    return cart.value
  }

  const reset = () => {
    cart.value = null
  }

  return { cart, itemCount, subtotal, fetchCart, add, updateQuantity, remove, reset }
}
