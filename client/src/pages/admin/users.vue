<script setup lang="ts">
import { ref, reactive, computed, h, onMounted } from 'vue'
import { useToast } from '@/composables/useToast'
import type { UserSummary, PageResponse, CreateUserRequest, UpdateUserRequest } from '@/types'
import { createUserSchema, updateUserSchema } from '@/schemas'
import { useApi } from '@/composables/useApi'
import { useConfirm } from '@/composables/useConfirm'
import { useFormErrors } from '@/composables/useFormErrors'
import { useFormat } from '@/composables/useFormat'
import { useAuth } from '@/composables/useAuth'
import PaginationBar from '@/components/PaginationBar.vue'
import UiIcon from '@/components/UiIcon.vue'
import type { DataTableColumns } from 'naive-ui'
import { NAvatar, NButton, NDataTable, NInput, NModal, NSelect, NTag } from 'naive-ui'

const { request } = useApi()
const { errors, applyIssues, clearErrors } = useFormErrors()
const { confirm } = useConfirm()
const { formatDate } = useFormat()
const { session } = useAuth()
const toast = useToast()

const users = ref<UserSummary[]>([])
const page = ref(0)
const totalPages = ref(0)
const totalElements = ref(0)
const search = ref('')
const loading = ref(false)
const busyIds = ref<Set<number>>(new Set())

const roleOptions = [
  { label: 'Khách hàng', value: 'CUSTOMER' },
  { label: 'Nhân viên', value: 'STAFF' },
  { label: 'Quản trị', value: 'ADMIN' }
]

interface UserFormState {
  username: string
  email: string
  numberPhone: string
  password: string
  role: 'CUSTOMER' | 'STAFF' | 'ADMIN'
  hireDate: string
}

const showForm = ref(false)
const editingId = ref<number | null>(null)
const saving = ref(false)
const form = reactive<UserFormState>({ username: '', email: '', numberPhone: '', password: '', role: 'CUSTOMER', hireDate: '' })

const selfId = computed(() => session.value?.id ?? null)

const formTitle = computed(() => editingId.value ? 'Chỉnh sửa người dùng' : 'Thêm người dùng mới')

function resetForm() {
  Object.assign(form, { username: '', email: '', numberPhone: '', password: '', role: 'CUSTOMER', hireDate: '' })
  editingId.value = null
  clearErrors()
}

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
    toast.add({ severity: 'error', summary: e?.data?.message || 'Không thể tải danh sách người dùng', life: 4000 })
  } finally {
    loading.value = false
  }
}

function toggleSearch() {
  page.value = 0
  load()
}

function openCreate() {
  resetForm()
  showForm.value = true
}

function openEdit(u: UserSummary) {
  Object.assign(form, {
    username: u.username,
    email: u.email,
    numberPhone: u.numberPhone,
    password: '',
    role: u.role,
    hireDate: u.hireDate ?? ''
  })
  editingId.value = u.id
  clearErrors()
  showForm.value = true
}

function closeForm() {
  showForm.value = false
  resetForm()
}

async function submit() {
  clearErrors()
  const schema = editingId.value ? updateUserSchema : createUserSchema
  const result = schema.safeParse(form)
  if (!result.success) {
    applyIssues(result.error)
    return
  }
  saving.value = true
  try {
    if (editingId.value) {
      const payload: UpdateUserRequest = {
        username: form.username,
        email: form.email,
        numberPhone: form.numberPhone,
        role: form.role,
        password: form.password || undefined,
        hireDate: form.hireDate || undefined
      }
      await request(`/api/admin/users/${editingId.value}`, { method: 'PUT', body: payload })
      toast.add({ severity: 'success', summary: 'Đã cập nhật người dùng', life: 4000 })
    } else {
      const payload: CreateUserRequest = {
        username: form.username,
        email: form.email,
        numberPhone: form.numberPhone,
        password: form.password,
        role: form.role,
        hireDate: form.hireDate || undefined
      }
      await request('/api/admin/users', { method: 'POST', body: payload })
      toast.add({ severity: 'success', summary: form.role === 'ADMIN' ? 'Đã tạo tài khoản quản trị' : form.role === 'STAFF' ? 'Đã tạo tài khoản nhân viên' : 'Đã tạo tài khoản khách hàng', life: 4000 })
    }
    showForm.value = false
    resetForm()
    await load()
  } catch (e: any) {
    toast.add({ severity: 'error', summary: e?.data?.message || 'Không thể lưu người dùng', life: 4000 })
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
    toast.add({ severity: 'success', summary: `Đã ${action} tài khoản`, life: 4000 })
    await load()
  } catch (e: any) {
    toast.add({ severity: 'error', summary: e?.data?.message || 'Không thể cập nhật', life: 4000 })
  } finally {
    busyIds.value.delete(u.id)
  }
}

const isSelf = (u: UserSummary) => selfId.value === u.id

const editingUser = computed(() => editingId.value === null
  ? null
  : users.value.find(u => u.id === editingId.value) ?? null)

const editingSelf = computed(() => editingUser.value ? isSelf(editingUser.value) : false)

function roleLabel(role: string) {
  return role === 'ADMIN' ? 'Quản trị' : role === 'STAFF' ? 'Nhân viên' : 'Khách hàng'
}

const columns = computed<DataTableColumns<UserSummary>>(() => [
  {
    key: 'user',
    title: 'Người dùng',
    render: (row) => h('div', { class: 'flex items-center gap-3' }, [
      h(NAvatar, { src: row.avatarUrl || undefined, size: 36 }, { default: () => row.username.charAt(0).toUpperCase() }),
      h('div', null, [
        h('p', { class: 'font-medium text-gray-700' }, row.username),
        h('p', { class: 'text-xs text-gray-400' }, row.email)
      ])
    ])
  },
  {
    key: 'numberPhone',
    title: 'Số điện thoại',
    render: (row) => h('span', { class: 'text-gray-500 tabular-nums' }, row.numberPhone)
  },
  {
    key: 'role',
    title: 'Vai trò',
    render: (row) => h(NTag, { type: row.role === 'ADMIN' ? 'default' : row.role === 'STAFF' ? 'info' : 'success' }, { default: () => roleLabel(row.role) })
  },
  {
    key: 'isActive',
    title: 'Trạng thái',
    render: (row) => h(NTag, { type: row.isActive ? 'success' : 'default' }, { default: () => row.isActive ? 'Hoạt động' : 'Đã khóa' })
  },
  {
    key: 'createdAt',
    title: 'Ngày tạo',
    render: (row) => h('span', { class: 'text-gray-500' }, formatDate(row.createdAt))
  },
  {
    key: 'hireDate',
    title: 'Ngày tuyển',
    render: (row) => h('span', { class: 'text-gray-500' }, row.hireDate ? formatDate(row.hireDate) : '—')
  },
  {
    key: 'actions',
    title: 'Thao tác',
    render: (row) => h('div', { class: 'flex justify-end gap-1' }, [
      h(NButton, { quaternary: true, size: 'small', onClick: () => openEdit(row) }, { icon: () => h(UiIcon, { name: 'pencil', size: 16 }), default: () => 'Sửa' }),
      h(NButton, {
        quaternary: true,
        size: 'small',
        loading: busyIds.value.has(row.id),
        disabled: isSelf(row),
        onClick: () => toggleActive(row)
      }, { icon: () => h(UiIcon, { name: row.isActive ? 'lock' : 'lock-open', size: 16 }), default: () => row.isActive ? 'Khóa' : 'Mở khóa' })
    ])
  }
])

onMounted(load)
</script>

<template>
  <div class="mx-auto max-w-7xl px-4 py-8 sm:px-6">
    <div class="mb-5 flex flex-wrap items-center justify-between gap-3">
      <div class="flex flex-wrap items-center gap-2">
        <div class="flex flex-wrap items-center gap-2">
          <div class="w-64">
            <NInput v-model:value="search" placeholder="Tìm theo tên hoặc email..." clearable @keyup.enter="toggleSearch">
              <template #prefix><UiIcon name="search" size="16" /></template>
            </NInput>
          </div>
          <NButton secondary @click="toggleSearch">
            <template #icon><UiIcon name="search" size="16" /></template>
            Tìm
          </NButton>
        </div>
        <p v-if="!loading && totalElements > 0" class="text-sm text-gray-400">Tổng <span class="font-semibold tabular-nums">{{ totalElements }}</span> người dùng</p>
      </div>
      <NButton type="primary" @click="openCreate">
        <template #icon><UiIcon name="plus" size="16" /></template>
        Thêm người dùng
      </NButton>
    </div>

    <div class="overflow-hidden rounded-2xl border border-gray-200 bg-white">
      <NDataTable :data="users" :columns="columns" :loading="loading" striped>
        <template #empty>
          <div v-if="!loading" class="flex flex-col items-center py-8 text-gray-400">
            <UiIcon name="users" size="32" class="mb-2 text-gray-300" />
            <p class="text-sm">Không tìm thấy người dùng.</p>
          </div>
        </template>
      </NDataTable>
    </div>

    <PaginationBar
      v-if="totalPages > 1"
      :page="page"
      :total-pages="totalPages"
      @prev="page--; load()"
      @next="page++; load()"
    />

    <NModal
      v-model:show="showForm"
      preset="card"
      :title="formTitle"
      :style="{ width: '720px', maxWidth: '95vw' }"
    >
      <form class="grid gap-4 md:grid-cols-2" @submit.prevent="submit">
        <div>
          <label class="mb-1 block text-sm text-gray-500">Tên đăng nhập</label>
          <NInput v-model:value="form.username" placeholder="vd: quanly1" />
          <p v-if="errors.username" class="text-xs text-red-600">{{ errors.username }}</p>
        </div>
        <div>
          <label class="mb-1 block text-sm text-gray-500">Email</label>
          <NInput v-model:value="form.email" :input-props="{ type: 'email' }" placeholder="quanly@ecomart.vn" />
          <p v-if="errors.email" class="text-xs text-red-600">{{ errors.email }}</p>
        </div>
        <div>
          <label class="mb-1 block text-sm text-gray-500">Số điện thoại</label>
          <NInput v-model:value="form.numberPhone" placeholder="0900000000" />
          <p v-if="errors.numberPhone" class="text-xs text-red-600">{{ errors.numberPhone }}</p>
        </div>
        <div>
          <label class="mb-1 block text-sm text-gray-500">Mật khẩu{{ editingId ? ' (để trống nếu không đổi)' : '' }}</label>
          <NInput v-model:value="form.password" type="password" placeholder="Tối thiểu 6 ký tự" show-password-on="click" />
          <p v-if="errors.password" class="text-xs text-red-600">{{ errors.password }}</p>
        </div>
        <div>
          <label class="mb-1 block text-sm text-gray-500">Vai trò</label>
          <NSelect v-model:value="form.role" :options="roleOptions" :disabled="editingSelf" />
          <p v-if="errors.role" class="text-xs text-red-600">{{ errors.role }}</p>
          <p v-if="editingSelf" class="mt-1 text-xs text-gray-400">Không thể thay đổi vai trò của chính mình</p>
        </div>
        <div v-if="form.role === 'ADMIN' || form.role === 'STAFF'">
          <label class="mb-1 block text-sm text-gray-500">Ngày tuyển dụng (tùy chọn)</label>
          <NInput v-model:value="form.hireDate" :input-props="{ type: 'date' }" placeholder="yyyy-mm-dd" />
        </div>
      </form>
      <template #footer>
        <div class="flex justify-end gap-2">
          <NButton quaternary @click="closeForm">Hủy</NButton>
          <NButton type="primary" :loading="saving" @click="submit">Lưu</NButton>
        </div>
      </template>
    </NModal>
  </div>
</template>