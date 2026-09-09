<script setup lang="ts">
import { ref, reactive, computed, h, onMounted } from 'vue'
import { useToast } from '@/composables/useToast'
import type { CategoryRequest, CategoryResponse } from '@/types'
import { categorySchema } from '@/schemas'
import { useApi } from '@/composables/useApi'
import { useConfirm } from '@/composables/useConfirm'
import { useFormErrors } from '@/composables/useFormErrors'
import UiIcon from '@/components/UiIcon.vue'
import type { DataTableColumns } from 'naive-ui'
import { NButton, NCheckbox, NDataTable, NInput, NInputNumber, NModal, NTag } from 'naive-ui'

const { request } = useApi()
const { errors, applyIssues, clearErrors } = useFormErrors()
const { confirm } = useConfirm()
const toast = useToast()

const categories = ref<CategoryResponse[]>([])
const loading = ref(true)
const busyIds = ref<Set<number>>(new Set())

const categoryIconMap: Record<string, string> = {
  leaf: 'leaf',
  apple: 'apple',
  box: 'box',
  carrot: 'tag',
  tag: 'tag'
}

function categoryIcon(icon?: string | null) {
  if (!icon) return 'tag'
  return categoryIconMap[icon] || 'tag'
}

interface CategoryRow {
  item: CategoryResponse
  level: 1 | 2
}

const categoryRows = computed<CategoryRow[]>(() =>
  categories.value.flatMap((c) => [
    { item: c, level: 1 as const },
    ...c.children.map((child) => ({ item: child, level: 2 as const }))
  ])
)

const showForm = ref(false)
const editingId = ref<number | null>(null)
const saving = ref(false)
const form = reactive({ name: '', slug: '', icon: '', parentId: null as number | null, displayOrder: 0, active: true })

const formTitle = computed(() => editingId.value ? 'Chỉnh sửa danh mục' : 'Thêm danh mục mới')

async function load() {
  loading.value = true
  try {
    categories.value = await request<CategoryResponse[]>('/api/categories')
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  Object.assign(form, { name: '', slug: '', icon: '', parentId: null, displayOrder: 0, active: true })
  clearErrors()
  showForm.value = true
}

function openEdit(c: CategoryResponse) {
  editingId.value = c.id
  Object.assign(form, { name: c.name, slug: c.slug, icon: c.icon || '', parentId: null, displayOrder: c.displayOrder, active: c.active })
  clearErrors()
  showForm.value = true
}

async function submit() {
  clearErrors()
  const result = categorySchema.safeParse(form)
  if (!result.success) {
    applyIssues(result.error)
    return
  }
  saving.value = true
  const payload: CategoryRequest = { name: form.name, slug: form.slug, icon: form.icon, displayOrder: Number(form.displayOrder), active: form.active }
  try {
    if (editingId.value) {
      await request(`/api/categories/${editingId.value}`, { method: 'PUT', body: payload })
    } else {
      await request('/api/categories', { method: 'POST', body: payload })
    }
    toast.add({ severity: 'success', summary: 'Đã lưu danh mục', life: 4000 })
    showForm.value = false
    await load()
  } catch (e: any) {
    toast.add({ severity: 'error', summary: e?.data?.message || 'Lưu thất bại', life: 4000 })
  } finally {
    saving.value = false
  }
}

async function remove(c: CategoryResponse) {
  if (busyIds.value.has(c.id)) return
  if (!await confirm(`Xóa danh mục "${c.name}"?`, 'Xóa danh mục')) return
  busyIds.value.add(c.id)
  try {
    await request(`/api/categories/${c.id}`, { method: 'DELETE' })
    toast.add({ severity: 'success', summary: 'Đã xóa danh mục', life: 4000 })
    await load()
  } catch (e: any) {
    toast.add({ severity: 'error', summary: e?.data?.message || 'Không thể xóa danh mục', life: 4000 })
  } finally {
    busyIds.value.delete(c.id)
  }
}

const columns = computed<DataTableColumns<CategoryRow>>(() => [
  {
    key: 'name',
    title: 'Danh mục',
    render: (row) =>
      row.level === 2
        ? h('div', { class: 'flex items-center gap-2' }, [
            h(UiIcon, { name: 'arrow-down-right', size: 16, class: 'shrink-0 text-emerald-500' }),
            h('span', { class: 'text-gray-700' }, row.item.name)
          ])
        : h('div', { class: 'flex items-center gap-2' }, [
            row.item.icon && categoryIcon(row.item.icon) !== 'tag'
              ? h(UiIcon, { name: categoryIcon(row.item.icon), size: 16, class: 'text-emerald-600' })
              : null,
            h('span', { class: 'font-medium text-gray-700' }, row.item.name)
          ])
  },
  {
    key: 'slug',
    title: 'Slug',
    render: (row) => h('span', { class: 'text-gray-500' }, row.item.slug)
  },
  {
    key: 'level',
    title: 'Cấp',
    render: (row) => h(NTag, { type: 'default' }, { default: () => row.level === 2 ? 'Cấp 2' : 'Cấp 1' })
  },
  {
    key: 'active',
    title: 'Trạng thái',
    render: (row) => h(NTag, { type: row.item.active ? 'success' : 'default' }, { default: () => row.item.active ? 'Hiển thị' : 'Ẩn' })
  },
  {
    key: 'actions',
    title: 'Thao tác',
    render: (row) => h('div', { class: 'flex justify-end gap-1' }, [
      h(NButton, { quaternary: true, size: 'small', onClick: () => openEdit(row.item) }, { icon: () => h(UiIcon, { name: 'pencil', size: 16 }) }),
      row.level === 1
        ? h(NButton, { quaternary: true, size: 'small', loading: busyIds.value.has(row.item.id), onClick: () => remove(row.item) }, { icon: () => h(UiIcon, { name: 'trash', size: 16 }) })
        : null
    ])
  }
])

onMounted(load)
</script>

<template>
  <div class="mx-auto max-w-7xl px-4 py-8 sm:px-6">
    <div class="mb-5 flex items-center justify-end">
      <NButton type="primary" @click="openCreate">
        <template #icon><UiIcon name="plus" size="16" /></template>
        Thêm danh mục
      </NButton>
    </div>

    <NModal
      v-model:show="showForm"
      preset="card"
      :title="formTitle"
      :style="{ width: '720px', maxWidth: '95vw' }"
    >
      <form class="grid gap-4 md:grid-cols-3" @submit.prevent="submit">
        <div>
          <label class="mb-1 block text-sm text-gray-500">Tên</label>
          <NInput v-model:value="form.name" />
          <p v-if="errors.name" class="text-xs text-red-600">{{ errors.name }}</p>
        </div>
        <div>
          <label class="mb-1 block text-sm text-gray-500">Slug</label>
          <NInput v-model:value="form.slug" />
          <p v-if="errors.slug" class="text-xs text-red-600">{{ errors.slug }}</p>
        </div>
        <div>
          <label class="mb-1 block text-sm text-gray-500">Icon (tên icon)</label>
          <NInput v-model:value="form.icon" placeholder="ex: carrot, apple, tag" />
        </div>
        <div>
          <label class="mb-1 block text-sm text-gray-500">Thứ tự</label>
          <NInputNumber v-model:value="form.displayOrder" :min="0" :show-button="false" />
        </div>
        <div class="flex items-end pb-1">
          <NCheckbox v-model:checked="form.active">Hiển thị</NCheckbox>
        </div>
      </form>
      <template #footer>
        <div class="flex justify-end gap-2">
          <NButton quaternary @click="showForm = false">Hủy</NButton>
          <NButton type="primary" :loading="saving" @click="submit">Lưu</NButton>
        </div>
      </template>
    </NModal>

    <div class="overflow-hidden rounded-2xl border border-gray-200 bg-white">
      <NDataTable :data="categoryRows" :columns="columns" :loading="loading" :row-key="(row) => row.item.id">
        <template #empty>
          <div v-if="!loading" class="flex flex-col items-center py-8 text-gray-400">
            <UiIcon name="folder" size="32" class="mb-2 text-gray-300" />
            <p class="text-sm">Không có danh mục.</p>
          </div>
        </template>
      </NDataTable>
    </div>
  </div>
</template>