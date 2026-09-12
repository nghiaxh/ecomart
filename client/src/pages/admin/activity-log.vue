<script setup lang="ts">
import { ref, computed, h, onMounted } from 'vue'
import { useToast } from '@/composables/useToast'
import type { ActivityLog, PageResponse } from '@/types'
import { useApi } from '@/composables/useApi'
import { useFormat } from '@/composables/useFormat'
import PaginationBar from '@/components/PaginationBar.vue'
import UiIcon from '@/components/UiIcon.vue'
import type { DataTableColumns } from 'naive-ui'
import { NButton, NDataTable, NInput, NTag } from 'naive-ui'

const { request } = useApi()
const toast = useToast()
const { formatDate } = useFormat()

const logs = ref<ActivityLog[]>([])
const page = ref(0)
const totalPages = ref(0)
const totalElements = ref(0)
const search = ref('')
const loading = ref(false)

const actionLabels: Record<string, string> = {
  CREATE_USER: 'Tạo người dùng',
  UPDATE_USER: 'Cập nhật người dùng',
  TOGGLE_USER_ACTIVE: 'Khóa/Kích hoạt người dùng',
  CREATE_PRODUCT: 'Tạo sản phẩm',
  UPDATE_PRODUCT: 'Cập nhật sản phẩm',
  DELETE_PRODUCT: 'Xóa sản phẩm',
  TOGGLE_PRODUCT_ACTIVE: 'Khóa/Kích hoạt sản phẩm',
  CREATE_CATEGORY: 'Tạo danh mục',
  UPDATE_CATEGORY: 'Cập nhật danh mục',
  DELETE_CATEGORY: 'Xóa danh mục',
  UPDATE_ORDER_STATUS: 'Cập nhật đơn hàng',
  CONFIRM_PAYMENT: 'Xác nhận thanh toán',
  TOGGLE_REVIEW_VISIBILITY: 'Ẩn/Hiện đánh giá'
}

const typeLabels: Record<string, string> = {
  USER: 'Người dùng',
  PRODUCT: 'Sản phẩm',
  CATEGORY: 'Danh mục',
  ORDER: 'Đơn hàng',
  REVIEW: 'Đánh giá'
}

const roleLabels: Record<string, string> = {
  ADMIN: 'Quản trị',
  STAFF: 'Nhân viên',
  CUSTOMER: 'Khách hàng'
}

async function load(keepPage = false) {
  if (!keepPage) page.value = 0
  loading.value = true
  try {
    const params = new URLSearchParams()
    params.set('page', String(page.value))
    params.set('size', '15')
    if (search.value.trim()) params.set('search', search.value.trim())
    const data = await request<PageResponse<ActivityLog>>(`/api/admin/activity-logs?${params.toString()}`)
    logs.value = data.content
    totalPages.value = data.totalPages
    totalElements.value = data.totalElements
  } catch (e: any) {
    toast.add({ severity: 'error', summary: e?.data?.message || 'Không thể tải nhật ký hệ thống', life: 4000 })
  } finally {
    loading.value = false
  }
}

const columns = computed<DataTableColumns<ActivityLog>>(() => [
  {
    key: 'createdAt',
    title: 'Thời gian',
    width: 170,
    render: (row) => h('span', { class: 'text-gray-500 tabular-nums' }, formatDate(row.createdAt))
  },
  {
    key: 'actor',
    title: 'Người thực hiện',
    render: (row) => h('div', { class: 'flex items-center gap-2' }, [
      h('span', { class: 'font-medium text-gray-700' }, row.username || '—'),
      row.role ? h(NTag, { size: 'small', type: 'info' }, { default: () => roleLabels[row.role!] ?? row.role }) : null
    ])
  },
  {
    key: 'action',
    title: 'Hành động',
    width: 200,
    render: (row) => h('span', { class: 'text-gray-600' }, actionLabels[row.action] ?? row.action)
  },
  {
    key: 'entity',
    title: 'Đối tượng',
    width: 120,
    render: (row) => h(NTag, { size: 'small', type: 'default' }, { default: () => typeLabels[row.entityType ?? ''] ?? row.entityType ?? '—' })
  },
  {
    key: 'entityName',
    title: 'Tên',
    render: (row) => h('span', { class: 'text-gray-700' }, row.entityName || '—')
  },
  {
    key: 'detail',
    title: 'Chi tiết',
    render: (row) => h('span', { class: 'text-gray-500' }, row.detail || '')
  }
])

onMounted(() => load(false))
</script>

<template>
  <div class="mx-auto max-w-7xl px-4 py-8 sm:px-6">
    <div class="mb-5 flex flex-wrap items-center justify-between gap-3">
      <div class="flex flex-wrap items-center gap-2">
        <div class="w-64">
          <NInput
            v-model:value="search"
            placeholder="Tìm theo tên, hành động, chi tiết..."
            clearable
            @keyup.enter="load(false)"
          >
            <template #prefix><UiIcon name="search" size="16" /></template>
          </NInput>
        </div>
        <NButton secondary @click="load(false)">
          <template #icon><UiIcon name="search" size="16" /></template>
          Tìm
        </NButton>
        <p v-if="!loading && totalElements > 0" class="text-sm text-gray-400">
          Tổng <span class="font-semibold tabular-nums">{{ totalElements }}</span> bản ghi
        </p>
      </div>
    </div>

    <div class="overflow-hidden rounded-2xl border border-gray-200 bg-white">
      <NDataTable :data="logs" :columns="columns" :loading="loading" striped>
        <template #empty>
          <div v-if="!loading" class="flex flex-col items-center py-8 text-gray-400">
            <UiIcon name="inbox" size="32" class="mb-2 text-gray-300" />
            <p class="text-sm">Chưa có bản ghi hoạt động trong hệ thống.</p>
          </div>
        </template>
      </NDataTable>
    </div>

    <PaginationBar
      v-if="totalPages > 1"
      :page="page"
      :total-pages="totalPages"
      @prev="page--; load(true)"
      @next="page++; load(true)"
    />
  </div>
</template>