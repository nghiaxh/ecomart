<script setup lang="ts">

import type { UserSummary, PageResponse, CreateAdminRequest } from '@/types'
import { createAdminSchema } from '@/schemas'
import { useApi } from '@/composables/useApi'
import { useConfirm } from '@/composables/useConfirm'
import { useFormErrors } from '@/composables/useFormErrors'
import { useFormat } from '@/composables/useFormat'

const { request } = useApi()
const { errors, applyIssues, clearErrors } = useFormErrors()
const { confirm } = useConfirm()
const { formatDate } = useFormat()
const toast = useToast()

const users = ref<UserSummary[]>([])
const page = ref(0)
const totalPages = ref(0)
const totalElements = ref(0)
const search = ref('')
const loading = ref(false)
const busyIds = ref<Set<number>>(new Set())

const showCreateForm = ref(false)
const saving = ref(false)
const createForm = reactive({ username: '', email: '', numberPhone: '', password: '', hireDate: '' })

async function load() {
  loading.value = true
  try {
    const params = new URLSearchParams()
    params.set('page', String(page.value))
    params.set('size', '10')
    if (search.value.trim()) params.set('search', search.value.trim())
    const data = await request<PageResponse<UserSummary>>(`/api/admin/users?${params.toString()}`)
    users.value = data.content
    totalPages.value = data.totalPages
    totalElements.value = data.totalElements
  } catch (e: any) {
    toast.add({ title: e?.data?.message || 'Không thể tải danh sách người dùng', color: 'error' })
  } finally {
    loading.value = false
  }
}

function toggleSearch() {
  page.value = 0
  load()
}

function openCreate() {
  Object.assign(createForm, { username: '', email: '', numberPhone: '', password: '', hireDate: '' })
  clearErrors()
  showCreateForm.value = true
}

async function submitCreate() {
  clearErrors()
  const result = createAdminSchema.safeParse(createForm)
  if (!result.success) {
    applyIssues(result.error)
    return
  }
  saving.value = true
  try {
    const payload: CreateAdminRequest = {
      username: createForm.username,
      email: createForm.email,
      numberPhone: createForm.numberPhone,
      password: createForm.password,
      hireDate: createForm.hireDate || undefined
    }
    await request('/api/admin/users/admin', { method: 'POST', body: payload })
    toast.add({ title: 'Đã tạo tài khoản quản trị', color: 'success' })
    showCreateForm.value = false
    await load()
  } catch (e: any) {
    toast.add({ title: e?.data?.message || 'Không thể tạo tài khoản', color: 'error' })
  } finally {
    saving.value = false
  }
}

async function toggleActive(u: UserSummary) {
  if (busyIds.value.has(u.id)) return
  const action = u.isActive ? 'vô hiệu hóa' : 'kích hoạt'
  if (!await confirm(`Bạn có chắc muốn ${action} tài khoản "${u.username}"?`, 'Xác nhận')) return
  busyIds.value.add(u.id)
  try {
    await request(`/api/admin/users/${u.id}/toggle-active`, { method: 'PATCH' })
    toast.add({ title: `Đã ${action} tài khoản`, color: 'success' })
    await load()
  } catch (e: any) {
    toast.add({ title: e?.data?.message || 'Không thể cập nhật', color: 'error' })
  } finally {
    busyIds.value.delete(u.id)
  }
}

onMounted(load)
</script>

<template>
  <div>
    <div class="mb-5 flex flex-wrap items-center justify-between gap-3">
      <div class="flex flex-wrap items-center gap-2">
        <UInput v-model="search" placeholder="Tìm theo tên hoặc email..." icon="i-ph-magnifying-glass" class="w-64" @keyup.enter="toggleSearch" />
        <UButton color="neutral" variant="soft" icon="i-ph-magnifying-glass" label="Tìm" @click="toggleSearch" />
        <p v-if="!loading && totalElements > 0" class="text-sm text-gray-400">Tổng <span class="font-semibold tabular-nums">{{ totalElements }}</span> người dùng</p>
      </div>
      <UButton color="primary" icon="i-ph-plus" label="Tạo quản trị viên" @click="openCreate" />
    </div>

    <div v-if="showCreateForm" class="mb-6 rounded-2xl border border-gray-200 bg-white p-6">
      <h2 class="mb-4 font-bold text-gray-700">Tạo tài khoản quản trị viên</h2>
      <form class="grid gap-4 md:grid-cols-2" @submit.prevent="submitCreate">
        <div>
          <label class="mb-1 block text-sm text-gray-500">Tên đăng nhập</label>
          <UInput v-model="createForm.username" placeholder="vd: quanly1" />
          <p v-if="errors.username" class="text-xs text-red-600">{{ errors.username }}</p>
        </div>
        <div>
          <label class="mb-1 block text-sm text-gray-500">Email</label>
          <UInput v-model="createForm.email" type="email" placeholder="quanly@ecomart.vn" />
          <p v-if="errors.email" class="text-xs text-red-600">{{ errors.email }}</p>
        </div>
        <div>
          <label class="mb-1 block text-sm text-gray-500">Số điện thoại</label>
          <UInput v-model="createForm.numberPhone" placeholder="0900000000" />
          <p v-if="errors.numberPhone" class="text-xs text-red-600">{{ errors.numberPhone }}</p>
        </div>
        <div>
          <label class="mb-1 block text-sm text-gray-500">Mật khẩu</label>
          <UInput v-model="createForm.password" type="password" placeholder="Tối thiểu 6 ký tự" />
          <p v-if="errors.password" class="text-xs text-red-600">{{ errors.password }}</p>
        </div>
        <div>
          <label class="mb-1 block text-sm text-gray-500">Ngày tuyển dụng (tùy chọn)</label>
          <UInput v-model="createForm.hireDate" type="date" />
        </div>
        <div class="flex items-end justify-end gap-2">
          <UButton color="neutral" variant="ghost" label="Hủy" @click="showCreateForm = false" />
          <UButton type="submit" color="primary" label="Tạo tài khoản" :loading="saving" />
        </div>
      </form>
    </div>

    <div class="overflow-hidden rounded-2xl border border-gray-200 bg-white">
      <table class="w-full text-sm">
        <thead class="sticky top-0 z-10 bg-gray-50 text-left text-xs font-medium uppercase tracking-wide text-gray-500">
          <tr>
            <th class="px-4 py-3">Người dùng</th>
            <th class="px-4 py-3">Số điện thoại</th>
            <th class="px-4 py-3">Vai trò</th>
            <th class="px-4 py-3">Trạng thái</th>
            <th class="px-4 py-3">Ngày tạo</th>
            <th class="px-4 py-3 text-right">Thao tác</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-100">
          <tr v-for="u in users" :key="u.id" class="transition hover:bg-gray-50/60">
            <td class="px-4 py-3">
              <div class="flex items-center gap-3">
                <UAvatar :src="u.avatarUrl || undefined" :alt="u.username" size="sm" />
                <div>
                  <p class="font-medium text-gray-700">{{ u.username }}</p>
                  <p class="text-xs text-gray-400">{{ u.email }}</p>
                </div>
              </div>
            </td>
            <td class="px-4 py-3 text-gray-500 tabular-nums">{{ u.numberPhone }}</td>
            <td class="px-4 py-3">
              <UBadge :color="u.role === 'ADMIN' ? 'slate' : 'emerald'" :label="u.role === 'ADMIN' ? 'Quản trị' : 'Khách hàng'" size="sm" />
            </td>
            <td class="px-4 py-3">
              <UBadge :color="u.isActive ? 'success' : 'neutral'" :label="u.isActive ? 'Hoạt động' : 'Đã khóa'" size="sm" />
            </td>
            <td class="px-4 py-3 text-gray-500">{{ formatDate(u.createdAt) }}</td>
            <td class="px-4 py-3">
              <div class="flex justify-end">
                <UButton
                  color="neutral"
                  variant="ghost"
                  :icon="u.isActive ? 'i-ph-lock-simple' : 'i-ph-lock-simple-open'"
                  :label="u.isActive ? 'Khóa' : 'Mở khóa'"
                  :loading="busyIds.has(u.id)"
                  @click="toggleActive(u)"
                />
              </div>
            </td>
          </tr>
          <tr v-if="!loading && !users.length">
            <td colspan="6" class="px-4 py-16 text-center text-gray-400">Không tìm thấy người dùng.</td>
          </tr>
        </tbody>
      </table>
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
