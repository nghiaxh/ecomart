<script setup lang="ts">
definePageMeta({ middleware: 'admin', layout: 'admin' })

import type { AdminDashboard } from '~/types'

const { request } = useApi()
const { formatVND } = useFormat()

const stats = ref<AdminDashboard | null>(null)
const loading = ref(true)

onMounted(async () => {
  try {
    stats.value = await request<AdminDashboard>('/api/admin/dashboard')
  } finally {
    loading.value = false
  }
})

const cards = computed(() => [
  { label: 'Sản phẩm', value: stats.value?.productCount ?? 0, icon: 'i-ph-shopping-bag', color: 'text-blue-600' },
  { label: 'Khách hàng', value: stats.value?.customerCount ?? 0, icon: 'i-ph-users', color: 'text-green-600' },
  { label: 'Đơn hàng', value: stats.value?.orderCount ?? 0, icon: 'i-ph-receipt', color: 'text-orange-600' },
  { label: 'Doanh thu', value: stats.value ? formatVND(stats.value.revenue) : '0', icon: 'i-ph-money', color: 'text-purple-600' }
])
</script>

<template>
  <div>
    <div v-if="loading" class="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
      <USkeleton v-for="i in 4" :key="i" class="h-32 rounded-2xl" />
    </div>
    <div v-else class="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
      <div v-for="c in cards" :key="c.label" class="rounded-2xl border border-gray-200 bg-white p-6">
        <span class="grid h-10 w-10 place-items-center rounded-xl bg-gray-50" :class="c.color">
          <UIcon :name="c.icon" class="h-5 w-5" />
        </span>
        <p class="mt-4 text-2xl font-extrabold text-gray-800">{{ c.value }}</p>
        <p class="text-sm text-gray-400">{{ c.label }}</p>
      </div>
    </div>

    <div class="mt-8 grid gap-4 lg:grid-cols-2">
      <NuxtLink to="/admin/orders" class="rounded-2xl border border-green-100 bg-gradient-to-br from-green-50 to-white p-6 transition hover:shadow-md">
        <div class="flex items-center justify-between">
          <div>
            <h3 class="font-bold text-gray-800">Quản lý đơn hàng</h3>
            <p class="mt-1 text-sm text-gray-500">Cập nhật trạng thái và xác nhận thanh toán</p>
          </div>
          <span class="grid h-12 w-12 place-items-center rounded-2xl bg-green-600 text-white"><UIcon name="i-ph-arrow-right" class="h-6 w-6" /></span>
        </div>
      </NuxtLink>
      <NuxtLink to="/admin/products" class="rounded-2xl border border-green-100 bg-gradient-to-br from-green-50 to-white p-6 transition hover:shadow-md">
        <div class="flex items-center justify-between">
          <div>
            <h3 class="font-bold text-gray-800">Quản lý sản phẩm</h3>
            <p class="mt-1 text-sm text-gray-500">Thêm, sửa sản phẩm và chuyển bật/tắt</p>
          </div>
          <span class="grid h-12 w-12 place-items-center rounded-2xl bg-green-600 text-white"><UIcon name="i-ph-arrow-right" class="h-6 w-6" /></span>
        </div>
      </NuxtLink>
    </div>
  </div>
</template>