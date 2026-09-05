import type { Cart } from '~/types'

export const useCart = () => {
  const cart = useState<Cart | null>('ecomart_cart', () => null)
  const { request } = useApi()
  const { isLoggedIn } = useAuth()
  const toast = useToast()

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

  watch(isLoggedIn, (loggedIn) => {
    if (loggedIn) {
      fetchCart()
    } else {
      cart.value = null
    }
  }, { immediate: true })

  const notifyError = (error: any, fallback: string) => {
    toast.add({
      title: error?.data?.message || fallback,
      color: 'error'
    })
  }

  const add = async (productId: number, quantity = 1) => {
    try {
      cart.value = await request<Cart>('/api/cart', {
        method: 'POST',
        body: { productId, quantity }
      })
      return cart.value
    } catch (error: any) {
      notifyError(error, 'Không thể thêm sản phẩm vào giỏ hàng')
      throw error
    }
  }

  const updateQuantity = async (productId: number, quantity: number) => {
    try {
      cart.value = await request<Cart>(`/api/cart/${productId}?quantity=${quantity}`, {
        method: 'PUT'
      })
      return cart.value
    } catch (error: any) {
      notifyError(error, 'Không thể cập nhật giỏ hàng')
      throw error
    }
  }

  const remove = async (productId: number) => {
    try {
      cart.value = await request<Cart>(`/api/cart/${productId}`, {
        method: 'DELETE'
      })
      return cart.value
    } catch (error: any) {
      notifyError(error, 'Không thể xóa sản phẩm khỏi giỏ hàng')
      throw error
    }
  }

  const reset = () => {
    cart.value = null
  }

  return { cart, itemCount, subtotal, fetchCart, add, updateQuantity, remove, reset }
}
