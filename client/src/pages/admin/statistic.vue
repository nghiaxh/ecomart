<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useApi } from '@/composables/useApi'
import { useFormat } from '@/composables/useFormat'
import { useStatusLabels } from '@/composables/useStatusLabels'
import { Line, Doughnut } from 'vue-chartjs'
import { Chart as ChartJS, CategoryScale, LinearScale, PointElement, LineElement, ArcElement, Tooltip, Legend, Filler } from 'chart.js'
import type { AdminDashboard, AdminStatistics, OrderStatus } from '@/types'
import UiIcon from '@/components/UiIcon.vue'
import { NSkeleton } from 'naive-ui'

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, ArcElement, Tooltip, Legend, Filler)

const { request } = useApi()
const { formatVND } = useFormat()
const { orderStatus } = useStatusLabels()

const dashboard = ref<AdminDashboard | null>(null)
const stats = ref<AdminStatistics | null>(null)
const loading = ref(true)
const loadError = ref(false)

onMounted(async () => {
  try {
    const [d, s] = await Promise.all([
      request<AdminDashboard>('/api/admin/dashboard'),
      request<AdminStatistics>('/api/admin/statistics?days=30')
    ])
    dashboard.value = d
    stats.value = s
  } catch {
    loadError.value = true
  } finally {
    loading.value = false
  }
})

const cards = computed(() => [
  { label: 'Sản phẩm', value: dashboard.value?.productCount ?? 0, icon: 'shopping-bag' },
  { label: 'Khách hàng', value: dashboard.value?.customerCount ?? 0, icon: 'users' },
  { label: 'Đơn hàng', value: dashboard.value?.orderCount ?? 0, icon: 'receipt' },
  { label: 'Doanh thu', value: dashboard.value ? formatVND(dashboard.value.revenue) : '0', icon: 'money-bill' }
])

const revenueChartData = computed(() => ({
  labels: stats.value?.revenueSeries.map((d) => d.date.slice(5)) ?? [],
  datasets: [
    {
      label: 'Doanh thu',
      data: stats.value?.revenueSeries.map((d) => d.revenue) ?? [],
      borderColor: '#059669',
      backgroundColor: 'rgba(5, 150, 105, 0.12)',
      fill: true,
      tension: 0.35,
      pointRadius: 2,
      pointHoverRadius: 5,
      borderWidth: 2,
      yAxisID: 'y'
    },
    {
      label: 'Số đơn',
      data: stats.value?.revenueSeries.map((d) => d.orderCount) ?? [],
      borderColor: '#94a3b8',
      backgroundColor: 'rgba(148, 163, 184, 0.15)',
      tension: 0.35,
      pointRadius: 2,
      pointHoverRadius: 5,
      borderWidth: 2,
      yAxisID: 'y1'
    }
  ]
}))

const compactVND = (n: number) => {
  if (n >= 1_000_000_000) return `${(n / 1_000_000_000).toFixed(1).replace(/\.0$/, '')} tỷ`
  if (n >= 1_000_000) return `${(n / 1_000_000).toFixed(1).replace(/\.0$/, '')} triệu`
  if (n >= 1_000) return `${Math.round(n / 1_000)}K`
  return `${n}`
}

const revenueChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  interaction: { mode: 'index' as const, intersect: false },
  plugins: {
    legend: { display: true, position: 'bottom' as const, labels: { boxWidth: 12, boxHeight: 12, padding: 16 } }
  },
  scales: {
    y: {
      type: 'linear' as const,
      position: 'left' as const,
      beginAtZero: true,
      grid: { color: '#f1f5f9' },
      ticks: { callback: (v: number | string) => compactVND(Number(v)) }
    },
    y1: {
      type: 'linear' as const,
      position: 'right' as const,
      beginAtZero: true,
      grid: { drawOnChartArea: false },
      ticks: { precision: 0 }
    },
    x: {
      grid: { display: false },
      ticks: { maxTicksLimit: 12 }
    }
  }
}

const chartColors = ['#10b981', '#0ea5e9', '#f59e0b', '#8b5cf6', '#f43f5e']

const statusChartData = computed(() => {
  const statuses = Object.keys(orderStatus) as OrderStatus[]
  return {
    labels: statuses.map((s) => orderStatus[s].label),
    datasets: [
      {
        data: statuses.map((s) => stats.value?.ordersByStatus[s] ?? 0),
        backgroundColor: chartColors,
        borderWidth: 0,
        hoverOffset: 4
      }
    ]
  }
})

const statusCounts = computed(() => {
  const statuses = Object.keys(orderStatus) as OrderStatus[]
  return statuses.map((s, i) => ({
    status: s,
    label: orderStatus[s].label,
    count: stats.value?.ordersByStatus[s] ?? 0,
    color: chartColors[i % chartColors.length]
  }))
})

const totalOrders = computed(() =>
  (statusCounts.value ?? []).reduce((sum, c) => sum + c.count, 0)
)

const statusChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  cutout: '65%',
  plugins: {
    legend: { display: false }
  }
}
</script>

<template>
  <div class="mx-auto max-w-7xl space-y-6 px-4 py-8 sm:px-6">
    <div v-if="loading" class="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
      <NSkeleton v-for="i in 4" :key="i" class="h-32 rounded-2xl" />
    </div>
    <div v-else-if="loadError" class="rounded-2xl border border-dashed border-gray-200 py-16 text-center">
      <UiIcon name="exclamation-circle" size="32" class="mx-auto block text-gray-300" />
      <p class="mt-3 text-sm text-gray-400">Không thể tải số liệu. Vui lòng thử lại.</p>
    </div>
    <template v-else>
      <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <div v-for="c in cards" :key="c.label" class="rounded-2xl border border-gray-200 bg-white p-5">
          <div class="flex items-center justify-between">
            <span class="grid h-10 w-10 place-items-center rounded-xl bg-emerald-50 text-emerald-700">
              <UiIcon :name="c.icon" size="18" />
            </span>
          </div>
          <p class="mt-4 text-2xl font-extrabold tracking-tight text-gray-800 tabular-nums">{{ c.value }}</p>
          <p class="mt-1 text-sm text-gray-500">{{ c.label }}</p>
        </div>
      </div>

      <div class="grid gap-6 lg:grid-cols-5">
        <div class="rounded-2xl border border-gray-200 bg-white p-5 lg:col-span-3">
          <h2 class="mb-4 font-semibold text-gray-700">Doanh thu &amp; đơn hàng 30 ngày</h2>
          <div class="h-72">
            <Line :data="revenueChartData" :options="revenueChartOptions" />
          </div>
        </div>

        <div class="rounded-2xl border border-gray-200 bg-white p-5 lg:col-span-2">
          <h2 class="mb-4 font-semibold text-gray-700">Trạng thái đơn hàng</h2>
          <div class="h-56">
            <Doughnut v-if="totalOrders > 0" :data="statusChartData" :options="statusChartOptions" />
            <div v-else class="grid h-full place-items-center text-sm text-gray-400">Chưa có đơn hàng</div>
          </div>
          <ul class="mt-4 space-y-2">
            <li v-for="c in statusCounts" :key="c.status" class="flex items-center gap-2 text-sm">
              <span class="h-2.5 w-2.5 rounded-full" :style="{ backgroundColor: c.color }" />
              <span class="text-gray-500">{{ c.label }}</span>
              <span class="ml-auto font-semibold text-gray-700 tabular-nums">{{ c.count }}</span>
            </li>
          </ul>
        </div>
      </div>

      <div class="rounded-2xl border border-gray-200 bg-white">
        <div class="px-5 pt-5">
          <h2 class="font-semibold text-gray-700">Sản phẩm bán chạy</h2>
        </div>
        <div class="mt-3 overflow-x-auto">
          <table class="w-full text-sm">
            <thead class="text-left text-xs font-medium uppercase tracking-wide text-gray-500">
              <tr>
                <th class="px-5 pb-3">Sản phẩm</th>
                <th class="px-5 pb-3 text-right">Đã bán</th>
                <th class="px-5 pb-3 text-right">Doanh thu</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-100">
              <tr v-for="p in stats?.topProducts ?? []" :key="p.productId">
                <td class="px-5 py-3 font-medium text-gray-700">{{ p.name }}</td>
                <td class="px-5 py-3 text-right text-gray-500 tabular-nums">{{ p.quantity }}</td>
                <td class="px-5 py-3 text-right text-gray-700 tabular-nums">{{ formatVND(p.revenue) }}</td>
              </tr>
              <tr v-if="!stats?.topProducts || stats.topProducts.length === 0">
                <td colspan="3" class="px-5 py-10 text-center text-gray-400">Chưa có số liệu bán hàng.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </template>
  </div>
</template>