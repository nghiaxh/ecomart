<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useToast } from '@/composables/useToast'
import { useIntervalFn } from '@vueuse/core'
import type { Order } from '@/types'
import { useApi } from '@/composables/useApi'
import { useFormat } from '@/composables/useFormat'
import { useStatusLabels } from '@/composables/useStatusLabels'
import UiImg from '@/components/UiImg.vue'
import UiIcon from '@/components/UiIcon.vue'
import { NButton, NSkeleton, NTag } from 'naive-ui'

const { request } = useApi()
const { formatVND, formatDate } = useFormat()
const { orderStatus, paymentStatus, paymentMethod, badgeType } = useStatusLabels()
const route = useRoute()
const router = useRouter()
const toast = useToast()

const order = ref<Order | null>(null)
const loading = ref(true)
const polling = ref(false)

const MAX_POLLS = 36
let attempts = 0

const shouldPoll = (o: Order | null) => o?.payment.method === 'PAYOS' && o.payment.status === 'PENDING'

const { pause, resume } = useIntervalFn(async () => {
  attempts++
  await pollOnce()
  if (attempts >= MAX_POLLS) {
    pause()
    polling.value = false
  }
}, 5000, { immediate: false })

async function pollOnce() {
  try {
    const next = await request<Order>(`/api/orders/${route.params.id}`)
    order.value = next
    if (!shouldPoll(next)) {
      pause()
      polling.value = false
      if (next.payment.status === 'PAID') {
        toast.add({ severity: 'success', summary: 'Thanh toán đã được xác nhận', life: 4000 })
      }
    }
  } catch {
    pause()
    polling.value = false
  }
}

onMounted(async () => {
  try {
    order.value = await request<Order>(`/api/orders/${route.params.id}`)
    if (shouldPoll(order.value)) {
      attempts = 0
      polling.value = true
      resume()
    }
  } catch (error: any) {
    toast.add({ severity: 'error', summary: error?.data?.message || 'Không thể tải đơn hàng', life: 4000 })
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="mx-auto max-w-3xl px-4 py-8 sm:px-6">
    <div v-if="order">
      <div class="flex items-center gap-3">
        <NButton quaternary @click="router.push('/orders')">
          <template #icon><UiIcon name="arrow-left" size="18" /></template>
        </NButton>
        <div>
          <h1 class="text-2xl font-extrabold text-gray-700">Đơn hàng #{{ order.id }}</h1>
          <p class="text-sm text-gray-400">{{ formatDate(order.createdAt) }}</p>
        </div>
      </div>

      <div class="mt-8 grid gap-6 md:grid-cols-2">
        <section class="rounded-2xl border border-emerald-100 bg-white p-6">
          <h2 class="font-semibold text-gray-700">Trạng thái</h2>
          <div class="mt-3 space-y-3">
            <div class="flex items-center gap-2">
              <span class="text-sm text-gray-500">Đơn hàng:</span>
              <NTag :type="badgeType[orderStatus[order.status].color]">{{ orderStatus[order.status].label }}</NTag>
            </div>
            <div class="flex items-center gap-2">
              <span class="text-sm text-gray-500">Thanh toán:</span>
              <span class="text-sm font-medium">{{ paymentStatus[order.payment.status].label }}</span>
            </div>
            <p v-if="polling" class="text-xs font-medium text-amber-600">Đang chờ xác nhận thanh toán...</p>
            <div class="flex items-center gap-2">
              <span class="text-sm text-gray-500">Phương thức:</span>
              <span class="text-sm font-medium">{{ paymentMethod[order.payment.method] }}</span>
            </div>
          </div>
        </section>

        <section class="rounded-2xl border border-emerald-100 bg-white p-6">
          <h2 class="font-semibold text-gray-700">Giao hàng đến</h2>
          <div class="mt-3 space-y-1 text-sm">
            <p class="font-medium text-gray-700">{{ order.receiverName }} · {{ order.receiverPhone }}</p>
            <p class="text-gray-500">{{ order.address }}</p>
          </div>
        </section>
      </div>

      <section class="mt-6 rounded-2xl border border-emerald-100 bg-white p-6">
        <h2 class="font-semibold text-gray-700">Sản phẩm</h2>
        <div class="mt-4 space-y-4">
          <div v-for="item in order.items" :key="item.productId" class="flex items-center gap-4">
            <UiImg :src="item.imageUrl" :alt="item.productName" img-class="h-16 w-16 rounded-xl object-cover" />
            <div class="flex-1">
              <p class="font-medium text-gray-700">{{ item.productName }}</p>
              <p class="text-sm text-gray-400">{{ formatVND(item.unitPrice) }} × {{ item.quantity }}</p>
            </div>
            <span class="font-semibold text-gray-700">{{ formatVND(item.unitPrice * item.quantity) }}</span>
          </div>
        </div>
        <div class="mt-6 space-y-2 border-t border-emerald-50 pt-4 text-sm">
          <div class="flex justify-between text-gray-500"><span>Tạm tính</span><span>{{ formatVND(order.subtotal) }}</span></div>
          <div class="flex justify-between text-gray-500"><span>Phí giao hàng</span><span>{{ formatVND(order.shippingFee) }}</span></div>
          <div class="flex justify-between text-lg pt-2"><span class="font-semibold text-gray-700">Tổng cộng</span><span class="font-bold text-emerald-700">{{ formatVND(order.total) }}</span></div>
        </div>
        <p v-if="order.notes" class="mt-4 rounded-xl bg-emerald-50 p-3 text-sm text-gray-600">
          <span class="font-medium">Ghi chú:</span> {{ order.notes }}
        </p>
      </section>
    </div>

    <div v-else-if="loading" class="py-12">
      <NSkeleton class="h-64 rounded-2xl" />
    </div>
    <div v-else class="py-24 text-center text-gray-400">Không tìm thấy đơn hàng.</div>
  </div>
</template>
