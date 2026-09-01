<script setup lang="ts">
import type { CategoryResponse, PageResponse, Product } from '~/types'

const { request } = useApi()
const { formatVND } = useFormat()
const route = useRoute()

const products = ref<Product[]>([])
const categories = ref<CategoryResponse[]>([])
const total = ref(0)
const totalPages = ref(0)
const page = ref(0)
const loading = ref(false)
const size = 12

const filters = reactive({
  q: (route.query.q as string) || '',
  category: (route.query.category as string) || '',
  minPrice: '',
  maxPrice: ''
})

const activeCategory = computed(() => {
  if (!filters.category) return null
  const find = (list: CategoryResponse[]): CategoryResponse | null => {
    for (const c of list) {
      if (c.slug === filters.category) return c
      const child = find(c.children)
      if (child) return child
    }
    return null
  }
  return find(categories.value)
})

async function loadCats() {
  categories.value = await request<CategoryResponse[]>('/api/categories')
}

async function load() {
  loading.value = true
  try {
    const params = new URLSearchParams()
    if (filters.q) params.set('q', filters.q)
    if (filters.category) {
      const cat = activeCategory.value
      if (cat && !cat.children.length) {
        params.set('category', String(cat.id))
      } else {
        params.set('category', filters.category)
      }
    }
    if (filters.minPrice) params.set('minPrice', filters.minPrice)
    if (filters.maxPrice) params.set('maxPrice', filters.maxPrice)
    params.set('page', String(page.value))
    params.set('size', String(size))

    const data = await request<PageResponse<Product>>(`/api/products?${params.toString()}`)
    products.value = data.content
    total.value = data.totalElements
    totalPages.value = data.totalPages
  } finally {
    loading.value = false
  }
}

function applyFilters() {
  page.value = 0
  load()
}

function setCategory(slug?: string) {
  filters.category = slug || ''
  applyFilters()
}

function clearFilters() {
  filters.q = ''
  filters.category = ''
  filters.minPrice = ''
  filters.maxPrice = ''
  applyFilters()
}

const debouncedApply = useDebounceFn(applyFilters, 500)

onMounted(() => loadCats())
watch(() => filters.q, debouncedApply)
</script>

<template>
  <div class="mx-auto max-w-7xl px-4 py-8 sm:px-6">
    <div class="mb-8">
      <h1 class="text-3xl font-extrabold text-gray-800">Sản phẩm</h1>
      <p class="mt-1 text-gray-500">{{ total }} sản phẩm thân thiện với môi trường</p>
    </div>

    <!-- Category chips -->
    <div class="mb-6 flex flex-wrap gap-2">
      <UButton
        :color="!filters.category ? 'primary' : 'neutral'"
        :variant="!filters.category ? 'solid' : 'soft'"
        size="sm"
        label="Tất cả"
        @click="setCategory()"
      />
      <template v-for="c in categories" :key="c.id">
        <UButton
          :color="filters.category === c.slug ? 'primary' : 'neutral'"
          :variant="filters.category === c.slug ? 'solid' : 'soft'"
          size="sm"
          :label="c.name"
          @click="setCategory(c.slug)"
        />
        <UButton
          v-for="child in c.children"
          :key="child.id"
          :color="filters.category === child.slug ? 'primary' : 'neutral'"
          :variant="filters.category === child.slug ? 'solid' : 'soft'"
          size="sm"
          :label="child.name"
          @click="setCategory(child.slug)"
        />
      </template>
    </div>

    <!-- Filters -->
    <div class="mb-8 grid gap-3 rounded-2xl border border-emerald-100 bg-white p-4 md:grid-cols-5">
      <UInput v-model="filters.q" icon="i-ph-magnifying-glass" placeholder="Tìm sản phẩm..." />
      <UInput v-model="filters.minPrice" type="number" placeholder="Giá tối thiểu (₫)" class="md:col-span-1" />
      <UInput v-model="filters.maxPrice" type="number" placeholder="Giá tối đa (₫)" class="md:col-span-1" />
      <UButton color="primary" icon="i-ph-funnel" label="Lọc" @click="applyFilters" />
      <UButton color="neutral" variant="ghost" label="Bỏ lọc" @click="clearFilters" />
    </div>

    <!-- Product grid -->
    <div v-if="loading" class="grid grid-cols-2 gap-4 md:grid-cols-3 lg:grid-cols-4">
      <USkeleton v-for="i in 8" :key="i" class="h-72 rounded-2xl" />
    </div>
    <div v-else-if="products.length" class="grid grid-cols-2 gap-4 md:grid-cols-3 lg:grid-cols-4">
      <ProductCard v-for="p in products" :key="p.id" :product="p" />
    </div>
    <div v-else class="py-20 text-center text-gray-400">
      <UIcon name="i-ph-tray" class="mx-auto h-12 w-12 mb-3" />
      <p>Không tìm thấy sản phẩm phù hợp.</p>
      <UButton class="mt-4" color="primary" variant="soft" label="Bỏ lọc" @click="clearFilters" />
    </div>

    <!-- Pagination -->
    <div v-if="totalPages > 1" class="mt-10 flex items-center justify-center gap-2">
      <UButton color="neutral" variant="soft" icon="i-ph-caret-left" :disabled="page === 0" @click="page--; load()" />
      <span class="px-3 text-sm text-gray-600">Trang {{ page + 1 }} / {{ totalPages }}</span>
      <UButton color="neutral" variant="soft" icon="i-ph-caret-right" :disabled="page >= totalPages - 1" @click="page++; load()" />
    </div>
  </div>
</template>
