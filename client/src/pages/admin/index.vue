<script setup lang="ts">
import { useApi } from '@/composables/useApi'
import { useFormat } from '@/composables/useFormat'
import type { AdminDashboard } from '@/types'

const { request } = useApi()
const { formatVND } = useFormat()

const stats = ref<AdminDashboard | null>(null)
const loading = ref(true)
const loadError = ref(false)

onMounted(async () => {
  try {
    stats.value = await request<AdminDashboard>('/api/admin/dashboard')
  } catch {
    loadError.value = true
  } finally {
    loading.value = false
  }
})

const cards = computed(() => [
  { label: 'Sản phẩm', value: stats.value?.productCount ?? 0, icon: 'i-ph-shopping-bag' },
  { label: 'Khách hàng', value: stats.value?.customerCount ?? 0, icon: 'i-ph-users' },
  { label: 'Đơn hàng', value: stats.value?.orderCount ?? 0, icon: 'i-ph-receipt' },
  { label: 'Doanh thu', value: stats.value ? formatVND(stats.value.revenue) : '0', icon: 'i-ph-money' }
])

const quickLinks = [
  { label: 'Đơn hàng', desc: 'Cập nhật trạng thái và xác nhận thanh toán', icon: 'i-ph-receipt', to: '/admin/orders' },
  { label: 'Sản phẩm', desc: 'Thêm, sửa, bật/tắt trạng thái bán', icon: 'i-ph-shopping-bag', to: '/admin/products' },
  { label: 'Người dùng', desc: 'Tạo tài khoản quản trị, khóa/mở khóa', icon: 'i-ph-users', to: '/admin/users' }
]
</script>

<template>
  <div class="mx-auto max-w-7xl space-y-8 px-4 py-8 sm:px-6">
    <div v-if="loading" class="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
      <USkeleton v-for="i in 4" :key="i" class="h-32 rounded-2xl" />
    </div>
    <div v-else-if="loadError" class="rounded-2xl border border-dashed border-gray-200 py-16 text-center">
      <UIcon name="i-ph-warning-circle" class="mx-auto h-8 w-8 text-gray-300" />
      <p class="mt-3 text-sm text-gray-400">Không thể tải số liệu. Vui lòng thử lại.</p>
    </div>
    <div v-else class="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
      <div v-for="c in cards" :key="c.label" class="rounded-2xl border border-gray-200 bg-white p-5">
        <div class="flex items-center justify-between">
          <span class="grid h-10 w-10 place-items-center rounded-xl bg-emerald-50 text-emerald-700">
            <UIcon :name="c.icon" class="h-5 w-5" />
          </span>
        </div>
        <p class="mt-4 text-2xl font-extrabold tracking-tight text-gray-800 tabular-nums">{{ c.value }}</p>
        <p class="mt-1 text-sm text-gray-500">{{ c.label }}</p>
      </div>
    </div>

    <div>
      <h2 class="text-sm font-semibold text-gray-500">Quản lý nhanh</h2>
      <div class="mt-3 grid gap-3 lg:grid-cols-3">
        <RouterLink
          v-for="link in quickLinks"
          :key="link.to"
          :to="link.to"
          class="group flex items-center justify-between rounded-2xl border border-gray-200 bg-white p-5 transition hover:border-emerald-200 hover:bg-emerald-50/40"
        >
          <div class="flex items-center gap-3">
            <span class="grid h-10 w-10 place-items-center rounded-xl bg-gray-100 text-gray-600 transition group-hover:bg-emerald-100 group-hover:text-emerald-700">
              <UIcon :name="link.icon" class="h-5 w-5" />
            </span>
            <div>
              <p class="font-semibold text-gray-700">{{ link.label }}</p>
              <p class="mt-0.5 text-sm text-gray-500">{{ link.desc }}</p>
            </div>
          </div>
          <UIcon name="i-ph-arrow-right" class="h-5 w-5 text-gray-300 transition group-hover:text-emerald-600" />
        </RouterLink>
      </div>
    </div>
  </div>
</template>