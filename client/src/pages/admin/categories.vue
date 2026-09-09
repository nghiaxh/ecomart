<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useToast } from '@/composables/useToast'
import type { CategoryRequest, CategoryResponse } from '@/types'
import { categorySchema } from '@/schemas'
import { useApi } from '@/composables/useApi'
import { useConfirm } from '@/composables/useConfirm'
import { useFormErrors } from '@/composables/useFormErrors'
import UiIcon from '@/components/UiIcon.vue'
import { NButton, NCheckbox, NInput, NInputNumber, NModal, NSelect, NTag } from 'naive-ui'

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

const showForm = ref(false)
const editingId = ref<number | null>(null)
const saving = ref(false)
const form = reactive({ name: '', slug: '', icon: '', parentId: null as number | null, displayOrder: 0, active: true })

const formTitle = computed(() => editingId.value ? 'Chỉnh sửa danh mục' : 'Thêm danh mục mới')

const parentOptions = computed(() =>
  categories.value
    .filter((c) => c.id !== editingId.value)
    .map((c) => ({ label: c.name, value: c.id }))
)

async function load() {
  loading.value = true
  try {
    categories.value = await request<CategoryResponse[]>('/api/categories')
  } finally {
    loading.value = false
  }
}

function openCreate(parentId: number | null = null) {
  editingId.value = null
  Object.assign(form, { name: '', slug: '', icon: '', parentId, displayOrder: 0, active: true })
  clearErrors()
  showForm.value = true
}

function openEdit(c: CategoryResponse, parentId: number | null = null) {
  editingId.value = c.id
  Object.assign(form, { name: c.name, slug: c.slug, icon: c.icon || '', parentId, displayOrder: c.displayOrder, active: c.active })
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
  const payload: CategoryRequest = {
    name: form.name,
    slug: form.slug || undefined,
    icon: form.icon,
    parentId: form.parentId ?? undefined,
    displayOrder: Number(form.displayOrder || 0),
    active: form.active
  }
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

onMounted(load)
</script>

<template>
  <div class="mx-auto max-w-7xl px-4 py-8 sm:px-6">
    <div class="mb-5 flex items-center justify-between">
      <p v-if="!loading && categories.length" class="text-sm text-gray-400">
        Tổng <span class="font-semibold tabular-nums">{{ categories.length }}</span> danh mục cấp 1
      </p>
      <NButton type="primary" class="ml-auto" @click="openCreate()">
        <template #icon><UiIcon name="plus" size="16" /></template>
        Thêm danh mục
      </NButton>
    </div>

    <div class="space-y-3">
      <template v-for="c in categories" :key="c.id">
        <div class="overflow-hidden rounded-2xl border border-gray-200 bg-white">
          <div class="flex items-center justify-between gap-3 px-4 py-3">
            <div class="flex min-w-0 items-center gap-2">
              <UiIcon :name="categoryIcon(c.icon)" size="18" class="shrink-0 text-emerald-600" />
              <span class="truncate font-medium text-gray-700">{{ c.name }}</span>
              <NTag :type="c.active ? 'success' : 'default'" size="small">{{ c.active ? 'Hiển thị' : 'Ẩn' }}</NTag>
            </div>
            <div class="flex shrink-0 items-center gap-1">
              <NButton quaternary size="small" @click="openEdit(c)">Sửa</NButton>
              <NButton quaternary size="small" @click="openCreate(c.id)">Thêm con</NButton>
              <NButton quaternary size="small" :loading="busyIds.has(c.id)" @click="remove(c)">Xóa</NButton>
            </div>
          </div>
          <div v-if="c.children.length" class="border-t border-gray-100 px-4 py-2">
            <div v-for="ch in c.children" :key="ch.id" class="flex items-center justify-between gap-3 py-1.5">
              <div class="flex min-w-0 items-center gap-2 pl-5">
                <UiIcon name="arrow-down-right" size="16" class="shrink-0 text-emerald-500" />
                <span class="truncate text-gray-600">{{ ch.name }}</span>
                <NTag :type="ch.active ? 'success' : 'default'" size="small">{{ ch.active ? 'Hiển thị' : 'Ẩn' }}</NTag>
              </div>
              <div class="flex shrink-0 items-center gap-1">
                <NButton quaternary size="small" @click="openEdit(ch, c.id)">Sửa</NButton>
                <NButton quaternary size="small" :loading="busyIds.has(ch.id)" @click="remove(ch)">Xóa</NButton>
              </div>
            </div>
          </div>
        </div>
      </template>

      <div v-if="loading" class="space-y-3">
        <div v-for="i in 3" :key="i" class="h-14 animate-pulse rounded-2xl bg-gray-100" />
      </div>
      <div v-else-if="!categories.length"
        class="flex flex-col items-center rounded-2xl border border-dashed border-gray-200 py-16 text-gray-400">
        <UiIcon name="folder" size="32" class="mb-2 text-gray-300" />
        <p class="text-sm">Không có danh mục.</p>
      </div>
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
          <NInput v-model:value="form.name" placeholder="vd: Rau củ sạch" />
          <p v-if="errors.name" class="text-xs text-red-600">{{ errors.name }}</p>
        </div>
        <div>
          <label class="mb-1 block text-sm text-gray-500">Danh mục cha</label>
          <NSelect v-model:value="form.parentId" :options="parentOptions" clearable placeholder="Gốc (cấp 1)" />
        </div>
        <div>
          <label class="mb-1 block text-sm text-gray-500">Icon (tên icon)</label>
          <NInput v-model:value="form.icon" placeholder="ex: carrot, apple, tag" />
        </div>
        <div>
          <label class="mb-1 block text-sm text-gray-500">Thứ tự</label>
          <NInputNumber v-model:value="form.displayOrder" :min="0" :show-button="false" placeholder="vd: 1" />
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
  </div>
</template>