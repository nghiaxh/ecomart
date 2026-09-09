import { ref, computed, watch } from 'vue'
import { useToast } from '@/composables/useToast'
import type { Cart } from '@/types'
import { useApi } from '@/composables/useApi'
import { useAuth } from '@/composables/useAuth'

const cart = ref<Cart | null>(null)

let pendingFetch: Promise<Cart | null> | null = null

export const useCart = () => {
  const { request } = useApi()
  const { isLoggedIn } = useAuth()
  const toast = useToast()

  const itemCount = computed(() => cart.value?.itemCount ?? 0)
  const subtotal = computed(() => cart.value?.subtotal ?? 0)

  const load = async () => {
    try {
      cart.value = await request<Cart>('/api/cart')
      return cart.value
    } catch {
      cart.value = null
      return null
    }
  }

  const fetchCart = () => {
    if (!isLoggedIn.value) return Promise.resolve(null)
    if (!pendingFetch) {
      pendingFetch = load()
      pendingFetch.finally(() => { pendingFetch = null })
    }
    return pendingFetch
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
      severity: 'error',
      summary: error?.data?.message || fallback,
      life: 4000
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
