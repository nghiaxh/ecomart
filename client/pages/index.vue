<script setup lang="ts">
import type { Banner, CategoryResponse, Product } from '~/types'

const { request } = useApi()
const { formatVND } = useFormat()

const banners = useState<Banner[]>('home_banners', () => [])
const categories = useState<CategoryResponse[]>('home_categories', () => [])
const products = useState<Product[]>('home_products', () => [])
const loading = ref(true)

onMounted(async () => {
  try {
    const [b, c, p] = await Promise.all([
      request<Banner[]>('/api/banners/active'),
      request<CategoryResponse[]>('/api/categories'),
      request<Product[]>('/api/products/latest')
    ])
    banners.value = b
    categories.value = c
    products.value = p
  } finally {
    loading.value = false
  }
})

const stats = [
  { value: '100%', label: 'Nguồn gốc rõ ràng' },
  { value: '5,000+', label: 'Sản phẩm sạch' },
  { value: '12,000', label: 'Khách hàng xanh' },
]
</script>

<template>
  <div>
    <!-- Hero -->
    <section class="relative overflow-hidden bg-gradient-to-br from-green-700 via-green-600 to-green-500">
      <div class="absolute inset-0 opacity-10" style="background-image: radial-gradient(circle at 20% 30%, white 1px, transparent 1px); background-size: 24px 24px"></div>
      <div class="relative mx-auto max-w-7xl px-4 py-20 sm:px-6 lg:py-28">
        <div class="max-w-2xl">
          <p class="mb-4 inline-flex items-center gap-2 rounded-full bg-white/15 px-4 py-1.5 text-sm font-medium text-white backdrop-blur">
            <UIcon name="i-heroicons-leaf" class="h-4 w-4" />
            Thực phẩm sạch · Bền vững
          </p>
          <h1 class="text-balance text-4xl font-extrabold leading-tight tracking-tight text-white sm:text-5xl lg:text-6xl">
            Chọn xanh cho bữa ăn của bạn
          </h1>
          <p class="mt-5 max-w-xl text-lg leading-relaxed text-green-50">
            EcoMart mang đến nông sản hữu cơ và thực phẩm bền vững, an toàn cho sức khỏe và thân thiện với môi trường.
          </p>
          <div class="mt-8 flex flex-wrap gap-3">
            <UButton to="/products" color="white" size="lg" class="!text-green-700" icon="i-heroicons-shopping-bag">
              Mua sắm ngay
            </UButton>
            <UButton to="/chat" color="green" variant="outline" size="lg" class="!border-white/40 !bg-transparent !text-white" icon="i-heroicons-chat-bubble-left-right">
              Tư vấn cùng EcoBot
            </UButton>
          </div>
        </div>
      </div>
    </section>

    <!-- Stats -->
    <section class="border-b border-green-100 bg-white">
      <div class="mx-auto grid max-w-7xl grid-cols-3 divide-x divide-green-50 px-4 py-8 sm:px-6">
        <div v-for="s in stats" :key="s.label" class="px-4 text-center sm:px-8">
          <p class="text-2xl font-extrabold text-green-700 sm:text-3xl">{{ s.value }}</p>
          <p class="mt-1 text-xs text-gray-500 sm:text-sm">{{ s.label }}</p>
        </div>
      </div>
    </section>

    <!-- Banners -->
    <section v-if="banners.length" class="mx-auto max-w-7xl px-4 pt-12 sm:px-6">
      <div class="grid gap-4 md:grid-cols-2">
        <NuxtLink
          v-for="b in banners"
          :key="b.id"
          :to="b.linkUrl || '/products'"
          class="group relative h-56 overflow-hidden rounded-2xl"
        >
          <img :src="b.imageUrl" :alt="b.title" loading="lazy" class="h-full w-full object-cover transition duration-500 group-hover:scale-105" />
          <div class="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent"></div>
          <div class="absolute bottom-0 p-5 text-white">
            <h3 class="text-lg font-bold">{{ b.title }}</h3>
            <p v-if="b.subtitle" class="text-sm text-white/80">{{ b.subtitle }}</p>
          </div>
        </NuxtLink>
      </div>
    </section>

    <!-- Categories -->
    <section class="mx-auto max-w-7xl px-4 py-12 sm:px-6">
      <div class="mb-6 flex items-center justify-between">
        <h2 class="text-2xl font-extrabold text-gray-800">Danh mục</h2>
        <UButton to="/products" color="green" variant="ghost" label="Xem tất cả" trailing-icon="i-heroicons-arrow-right" />
      </div>
      <div class="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-4">
        <NuxtLink
          v-for="c in categories"
          :key="c.id"
          :to="`/products?category=${c.slug}`"
          class="group rounded-2xl border border-green-100 bg-white p-5 transition hover:border-green-300 hover:shadow-md"
        >
          <span class="grid h-11 w-11 place-items-center rounded-xl bg-green-50 text-green-600 transition group-hover:bg-green-600 group-hover:text-white">
            <UIcon :name="c.icon ? `i-heroicons-${c.icon}` : 'i-heroicons-tag'" class="h-5 w-5" />
          </span>
          <h3 class="mt-3 font-semibold text-gray-800 group-hover:text-green-700">{{ c.name }}</h3>
          <p v-if="c.children.length" class="text-xs text-gray-400">{{ c.children.length }} mục</p>
        </NuxtLink>
      </div>
    </section>

    <!-- Latest products -->
    <section class="bg-green-50/40 py-12">
      <div class="mx-auto max-w-7xl px-4 sm:px-6">
        <div class="mb-6 flex items-center justify-between">
          <h2 class="text-2xl font-extrabold text-gray-800">Sản phẩm mới</h2>
          <UButton to="/products" color="green" variant="ghost" label="Xem tất cả" trailing-icon="i-heroicons-arrow-right" />
        </div>

        <div v-if="loading" class="grid grid-cols-2 gap-4 md:grid-cols-4">
          <USkeleton v-for="i in 4" :key="i" class="h-72 rounded-2xl" />
        </div>
        <div v-else-if="products.length" class="grid grid-cols-2 gap-4 md:grid-cols-3 lg:grid-cols-4">
          <ProductCard v-for="p in products" :key="p.id" :product="p" />
        </div>
        <div v-else class="py-16 text-center text-gray-400">Chưa có sản phẩm.</div>
      </div>
    </section>

    <!-- About / mission -->
    <section id="about" class="mx-auto max-w-7xl px-4 py-16 sm:px-6">
      <div class="grid items-center gap-10 md:grid-cols-2">
        <div>
          <p class="text-sm font-semibold uppercase tracking-wide text-green-600">Sứ mệnh</p>
          <h2 class="mt-2 text-3xl font-extrabold text-gray-800">Tiêu dùng xanh, một lựa chọn mỗi ngày</h2>
          <p class="mt-4 leading-relaxed text-gray-500">
            Mỗi sản phẩm EcoMart đều được kiểm soát nguồn gốc, giảm thiểu bao bì nhựa và ưu tiên vật liệu tái chế, hữu cơ.
            Khi bạn mua sắm, hệ thống ghi nhận lượng CO₂ tiết kiệm và tích lũy điểm Eco để đổi quà.
          </p>
          <ul class="mt-6 space-y-3">
            <li v-for="item in ['Nông sản hữu cơ, truy xuất nguồn gốc', 'Bao bì thân thiện môi trường', 'Tích điểm Eco — tiết kiệm CO₂']" :key="item" class="flex items-center gap-3 text-gray-700">
              <span class="grid h-6 w-6 place-items-center rounded-full bg-green-100 text-green-700">
                <UIcon name="i-heroicons-check" class="h-4 w-4" />
              </span>
              <span class="text-sm">{{ item }}</span>
            </li>
          </ul>
        </div>
        <div class="rounded-2xl bg-green-50 p-8">
          <div class="text-center">
            <span class="mx-auto grid h-16 w-16 place-items-center rounded-2xl bg-green-600 text-white">
              <UIcon name="i-heroicons-bolt" class="h-8 w-8" />
            </span>
            <h3 class="mt-4 text-2xl font-extrabold text-green-700">Tích điểm Eco</h3>
            <p class="mt-2 text-sm text-gray-500">Cộng dồn điểm khi mua các sản phẩm thân thiện môi trường và đổi lấy mã giảm giá.</p>
            <UButton to="/wallet" color="green" label="Xem ví Eco" class="mt-5" icon="i-heroicons-wallet" />
          </div>
        </div>
      </div>
    </section>
  </div>
</template>
