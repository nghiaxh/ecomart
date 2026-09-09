<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useToast } from '@/composables/useToast'
import type { Order, PageResponse } from '@/types'
import { useApi } from '@/composables/useApi'
import { useFormat } from '@/composables/useFormat'
import { useStatusLabels } from '@/composables/useStatusLabels'
import PaginationBar from '@/components/PaginationBar.vue'
import UiIcon from '@/components/UiIcon.vue'
import { NButton, NSelect, NTag } from 'naive-ui'

const { request } = useApi()
const { formatVND, formatDate } = useFormat()
const { orderStatus, paymentStatus, badgeType } = useStatusLabels()
const toast = useToast()

const orders = ref<Order[]>([])
const page = ref(0)
const totalPages = ref(0)
const statusFilter = ref('ALL')
const loading = ref(false)
const loadError = ref(false)
const busyIds = ref<Set<number>>(new Set())

async function load() {
  loading.value = true
  loadError.value = false
  try {
    const params = new URLSearchParams()
    if (statusFilter.value && statusFilter.value !== 'ALL') params.set('status', statusFilter.value)
    params.set('page', String(page.value))
    params.set('size', '10')
    const data = await request<PageResponse<Order>>(`/api/orders?${params.toString()}`)
    orders.value = data.content
    totalPages.value = data.totalPages
  } catch (e: any) {
    toast.add({ severity: 'error', summary: e?.data?.message || 'Không thể tải đơn hàng', life: 4000 })
    loadError.value = true
  } finally {
    loading.value = false
  }
}

async function updateStatus(o: Order, status: string) {
  if (busyIds.value.has(o.id)) return
  if (status === o.status) return
  busyIds.value.add(o.id)
  try {
    await request(`/api/orders/${o.id}/status`, { method: 'PATCH', body: { status } })
    toast.add({ severity: 'success', summary: 'Đã cập nhật trạng thái', life: 4000 })
    await load()
  } catch (e: any) {
    toast.add({ severity: 'error', summary: e?.data?.message || 'Không thể cập nhật trạng thái', life: 4000 })
  } finally {
    busyIds.value.delete(o.id)
  }
}

async function confirmPayment(o: Order) {
  if (busyIds.value.has(o.id)) return
  busyIds.value.add(o.id)
  try {
    await request(`/api/orders/${o.id}/confirm-payment`, { method: 'POST' })
    toast.add({ severity: 'success', summary: 'Đã xác nhận thanh toán', life: 4000 })
    await load()
  } catch (e: any) {
    toast.add({ severity: 'error', summary: e?.data?.message || 'Không thể xác nhận thanh toán', life: 4000 })
  } finally {
    busyIds.value.delete(o.id)
  }
}

const statusOptions = Object.entries(orderStatus).map(([k, v]) => ({ label: v.label, value: k }))

onMounted(load)
watch(statusFilter, () => { page.value = 0; load() })
</script>

<template>
  <div class="mx-auto max-w-7xl px-4 py-8 sm:px-6">
    <div class="mb-5 flex items-center justify-between gap-3">
      <div v-if="!loading && orders.length" class="text-sm text-gray-500">Hiển thị <span class="font-semibold tabular-nums">{{ orders.length }}</span> đơn gần nhất</div>
      <div class="ml-auto">
        <NSelect
          v-model:value="statusFilter"
          :options="[{ label: 'Tất cả trạng thái', value: 'ALL' }, ...statusOptions]"
          class="w-48"
        />
      </div>
    </div>

    <div class="space-y-4">
      <div v-for="o in orders" :key="o.id" class="rounded-2xl border border-gray-200 bg-white p-5 transition hover:border-gray-300">
        <div class="flex flex-wrap items-center justify-between gap-4">
          <div>
            <div class="flex items-center gap-2">
              <span class="font-bold text-gray-700 tabular-nums">Đơn #{{ o.id }}</span>
              <NTag :type="badgeType[orderStatus[o.status].color]">{{ orderStatus[o.status].label }}</NTag>
              <NTag :type="badgeType[paymentStatus[o.payment.status].color]">{{ paymentStatus[o.payment.status].label }}</NTag>
            </div>
            <p class="mt-1 text-xs text-gray-400">{{ formatDate(o.createdAt) }} · {{ o.receiverName }} · {{ o.receiverPhone }}</p>
            <p class="mt-1 text-xs text-gray-500">{{ o.address }}</p>
          </div>
          <div class="flex flex-col items-end gap-2">
            <span class="text-lg font-bold text-emerald-700 tabular-nums">{{ formatVND(o.total) }}</span>
            <div class="flex gap-2">
              <NSelect
                :value="o.status"
                :options="statusOptions"
                size="small"
                class="w-40"
                :disabled="busyIds.has(o.id)"
                @update:value="(v: string | null) => updateStatus(o, v ?? '')"
              />
              <NButton v-if="o.payment.status === 'PENDING'" type="primary" secondary size="small" :loading="busyIds.has(o.id)" @click="confirmPayment(o)">
                Xác nhận thanh toán
              </NButton>
            </div>
          </div>
        </div>
      </div>
      <div v-if="!loading && !orders.length" class="rounded-2xl border border-dashed border-gray-200 py-16 text-center">
        <UiIcon name="inbox" size="32" class="mx-auto block text-gray-300" />
        <p class="mt-3 text-sm text-gray-400">Không có đơn hàng.</p>
      </div>
      <div v-if="loadError" class="rounded-2xl border border-dashed border-gray-200 py-16 text-center">
        <UiIcon name="exclamation-circle" size="32" class="mx-auto block text-gray-300" />
        <p class="mt-3 text-sm text-gray-400">Không thể tải đơn hàng. Vui lòng thử lại.</p>
      </div>
    </div>

    <PaginationBar
      v-if="totalPages > 1"
      :page="page"
      :total-pages="totalPages"
      @prev="page--; load()"
      @next="page++; load()"
    />
  </div>
</template>