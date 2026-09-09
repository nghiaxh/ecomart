<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useToast } from '@/composables/useToast'
import { useCart } from '@/composables/useCart'
import { useConfirm } from '@/composables/useConfirm'
import { useFormat } from '@/composables/useFormat'
import UiImg from '@/components/UiImg.vue'
import UiIcon from '@/components/UiIcon.vue'
import OrderSummaryCard from '@/components/OrderSummaryCard.vue'
import { NButton, NSkeleton } from 'naive-ui'
import { useRouter } from 'vue-router'

const { cart, fetchCart, updateQuantity, remove } = useCart()
const { formatVND } = useFormat()
const { confirm } = useConfirm()
const toast = useToast()
const router = useRouter()
const loading = ref(true)
const busyProductIds = ref<Set<number>>(new Set())

onMounted(async () => {
  try {
    await fetchCart()
  } finally {
    loading.value = false
  }
})

async function changeQuantity(productId: number, quantity: number) {
  if (busyProductIds.value.has(productId)) return
  busyProductIds.value.add(productId)
  try {
    await updateQuantity(productId, quantity)
  } catch {
    await fetchCart()
  } finally {
    busyProductIds.value.delete(productId)
  }
}

async function deleteItem(productId: number) {
  if (busyProductIds.value.has(productId)) return
  const item = cart.value?.items.find(i => i.productId === productId)
  const confirmed = await confirm(`Xóa "${item?.productName ?? 'sản phẩm'}" khỏi giỏ hàng?`, 'Xóa sản phẩm')
  if (!confirmed) return
  busyProductIds.value.add(productId)
  try {
    await remove(productId)
    toast.add({ severity: 'success', summary: 'Đã xóa sản phẩm khỏi giỏ hàng', life: 4000 })
  } catch {
    await fetchCart()
  } finally {
    busyProductIds.value.delete(productId)
  }
}
</script>

<template>
  <div class="mx-auto max-w-7xl px-4 py-8 sm:px-6">
    <h1 class="text-3xl font-extrabold text-gray-700">Giỏ hàng</h1>

    <div v-if="loading" class="mt-8 grid gap-6 lg:grid-cols-3">
      <div class="space-y-4 lg:col-span-2">
        <NSkeleton v-for="i in 2" :key="i" class="h-28 rounded-2xl" />
      </div>
      <NSkeleton class="h-64 rounded-2xl" />
    </div>

    <div v-else-if="cart && cart.items.length" class="mt-8 grid gap-8 lg:grid-cols-3">
      <div class="lg:col-span-2 space-y-4">
        <div v-for="item in cart.items" :key="item.productId" class="flex gap-4 rounded-2xl border border-emerald-100 bg-white p-4">
          <RouterLink :to="`/products/${item.productSlug}`" class="h-24 w-24 shrink-0 overflow-hidden rounded-xl bg-emerald-50">
            <UiImg :src="item.imageUrl" :alt="item.productName" img-class="h-full w-full object-cover" />
          </RouterLink>
          <div class="flex flex-1 flex-col">
            <div class="flex items-start justify-between gap-2">
              <div>
                <RouterLink :to="`/products/${item.productSlug}`" class="font-semibold text-gray-700 hover:text-emerald-700">{{ item.productName }}</RouterLink>
              </div>
              <button class="text-gray-300 hover:text-red-500" :aria-label="`Xóa ${item.productName}`" @click="deleteItem(item.productId)">
                <UiIcon name="trash" size="20" />
              </button>
            </div>
            <div class="mt-auto flex items-center justify-between pt-3">
              <div class="flex items-center rounded-lg border border-emerald-200">
                <NButton quaternary size="small" :disabled="item.quantity <= 1 || busyProductIds.has(item.productId)" :loading="busyProductIds.has(item.productId)" @click="changeQuantity(item.productId, item.quantity - 1)">
                  <template #icon><UiIcon name="minus" size="16" /></template>
                </NButton>
                <span class="w-8 text-center text-sm font-semibold">{{ item.quantity }}</span>
                <NButton quaternary size="small" :disabled="item.quantity >= item.stock || busyProductIds.has(item.productId)" :loading="busyProductIds.has(item.productId)" @click="changeQuantity(item.productId, item.quantity + 1)">
                  <template #icon><UiIcon name="plus" size="16" /></template>
                </NButton>
              </div>
              <span class="font-bold text-emerald-700">{{ formatVND(item.price * item.quantity) }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Summary -->
      <OrderSummaryCard :subtotal="cart.subtotal" :item-count="cart.itemCount" title="Tóm tắt đơn hàng">
        <template #actions>
          <NButton type="primary" size="large" block @click="router.push('/checkout')">Tiến hành thanh toán</NButton>
          <NButton quaternary size="large" block class="mt-2" @click="router.push('/products')">Tiếp tục mua sắm</NButton>
        </template>
      </OrderSummaryCard>
    </div>

    <div v-else class="py-24 text-center">
      <UiIcon name="shopping-cart" size="48" class="mx-auto mb-4 block text-emerald-200" />
      <p class="text-gray-500">Giỏ hàng của bạn đang trống.</p>
      <NButton type="primary" size="large" class="mt-4" @click="router.push('/products')">
        <template #icon><UiIcon name="shopping-bag" /></template>
        Mua sắm ngay
      </NButton>
    </div>
  </div>
</template>
