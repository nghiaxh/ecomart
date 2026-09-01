<script setup lang="ts">
import type { Banner, CategoryResponse, Product } from '~/types'

const { request } = useApi()
const { isLoggedIn } = useAuth()

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
  { value: '5,000+', label: 'Sản phẩm đa dạng' },
  { value: '12,000', label: 'Khách hàng tin dùng' },
]

const features = [
  { icon: 'i-ph-leaf', title: 'Sạch & an toàn', desc: 'Sản phẩm có nguồn gốc rõ ràng, đảm bảo vệ sinh thực phẩm.' },
  { icon: 'i-ph-truck', title: 'Giao nhanh', desc: 'Nội thành nhận hàng trong ngày, toàn quốc 2–4 ngày.' },
  { icon: 'i-ph-wallet', title: 'Thanh toán linh hoạt', desc: 'COD hoặc quét mã QR qua PayOS tiện lợi.' },
  { icon: 'i-ph-headset', title: 'Hỗ trợ tận tâm', desc: 'Hotline 0900 000 000 hỗ trợ 8h–20h mỗi ngày.' },
]

const steps = [
  { icon: 'i-ph-magnifying-glass', title: 'Chọn sản phẩm', desc: 'Duyệt danh mục và chọn món bạn cần.' },
  { icon: 'i-ph-shopping-cart-simple', title: 'Thêm vào giỏ', desc: 'Đặt số lượng và thêm vào giỏ hàng.' },
  { icon: 'i-ph-credit-card', title: 'Thanh toán', desc: 'Chọn COD hoặc quét mã QR PayOS.' },
  { icon: 'i-ph-package', title: 'Nhận hàng', desc: 'Nhận hàng tận nơi, kiểm tra và thưởng thức.' },
]

const testimonials = [
  { name: 'Chị Thu Hà', role: 'Quận 7, TP.HCM', avatar: '', content: 'Rau củ luôn tươi, đóng gói cẩn thận. Giao hàng đúng giờ, tôi đặt hằng tuần cho cả gia đình.' },
  { name: 'Anh Minh Quang', role: 'Đà Nẵng', avatar: '', content: 'Đặt xoài và trái cây giờ chỉ quen EcoMart. Giá hợp lý, chất lượng ổn định, thanh toán rất dễ.' },
  { name: 'Chị Ngọc Anh', role: 'Thanh Xuân, Hà Nội', avatar: '', content: 'Nhân viên hỗ trợ nhanh và thân thiện. Có lần giao sai, bên mình đổi ngay trong ngày.' },
]

const categoryIconMap: Record<string, string> = {
  leaf: 'i-ph-leaf',
  apple: 'i-ph-apple-logo',
  box: 'i-ph-package',
  carrot: 'i-ph-carrot',
  tag: 'i-ph-tag-simple'
}

function categoryIcon(icon?: string | null) {
  if (!icon) return 'i-ph-tag-simple'
  return categoryIconMap[icon] || `i-ph-${icon}`
}
</script>

<template>
  <div>
    <!-- Hero -->
    <section class="relative min-h-[520px] overflow-hidden bg-emerald-900" style="background-image: url('/images/hero-bg.jpg'); background-size: cover; background-position: center;">
      <div class="absolute inset-0 bg-emerald-900/50 backdrop-blur-sm"></div>
      <div class="relative z-10 mx-auto max-w-7xl px-4 py-20 sm:px-6 lg:py-28">
        <div class="max-w-2xl">
          <p class="mb-4 inline-flex items-center gap-2 rounded-full bg-white/15 px-4 py-1.5 text-sm font-medium text-white">
            <UIcon name="i-ph-storefront" class="h-4 w-4" />
            Thực phẩm tươi · Đa dạng
          </p>
          <h1 class="text-balance text-4xl font-extrabold leading-tight tracking-tight text-white sm:text-5xl lg:text-6xl">
            Mua sắm tiện lợi mỗi ngày
          </h1>
          <p class="mt-5 max-w-xl text-lg leading-relaxed text-emerald-50">
            EcoMart mang đến rau củ, trái cây, thực phẩm tươi sạch và đầy đủ cho mọi bữa ăn của gia đình bạn.
          </p>
          <div class="mt-8 flex flex-wrap items-center gap-3">
            <UButton to="/products" color="neutral" size="xl" class="bg-white! px-7! text-emerald-700!" icon="i-ph-shopping-bag">
              Mua sắm ngay
            </UButton>
            <template v-if="!isLoggedIn">
              <UButton to="/login" color="primary" variant="outline" size="xl" class="border-white/60! bg-transparent! px-6! text-white!">
                Đăng nhập
              </UButton>
              <UButton to="/register" color="neutral" size="xl" class="border-white/80! bg-white/20! px-6! text-white!">
                Đăng ký
              </UButton>
            </template>
          </div>
        </div>
      </div>
    </section>

    <!-- Stats -->
    <section class="border-b border-emerald-100 bg-white">
      <div class="mx-auto grid max-w-7xl grid-cols-3 divide-x divide-emerald-50 px-4 py-8 sm:px-6">
        <div v-for="s in stats" :key="s.label" class="px-4 text-center sm:px-8">
          <p class="text-2xl font-extrabold text-emerald-700 sm:text-3xl">{{ s.value }}</p>
          <p class="mt-1 text-xs text-gray-500 sm:text-sm">{{ s.label }}</p>
        </div>
      </div>
    </section>

    <!-- Banner carousel -->
    <section v-if="banners.length" class="relative mx-auto max-w-7xl px-4 pb-14 pt-12 sm:px-6">
      <div class="pointer-events-none absolute -left-20 top-0 h-64 w-64 rounded-full bg-emerald-200/40 blur-3xl" aria-hidden="true"></div>
      <div class="pointer-events-none absolute -right-20 bottom-0 h-64 w-64 rounded-full bg-primary-200/40 blur-3xl" aria-hidden="true"></div>
      <div class="relative">
        <UCarousel
          v-slot="{ item }"
          loop
          arrows
          dots
          :autoplay="{ delay: 5000 }"
          :items="banners"
          :ui="{ item: 'basis-full', container: '-ms-0' }"
        >
          <NuxtLink :to="item.linkUrl || '/products'" class="group relative block h-64 overflow-hidden rounded-2xl sm:h-72">
            <img :src="item.imageUrl" :alt="item.title" loading="lazy" class="h-full w-full object-cover" />
            <div class="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent"></div>
            <div class="absolute bottom-0 p-6 text-white">
              <h3 class="text-xl font-bold sm:text-2xl">{{ item.title }}</h3>
              <p v-if="item.subtitle" class="mt-1 text-sm text-white/80">{{ item.subtitle }}</p>
            </div>
          </NuxtLink>
        </UCarousel>
      </div>
    </section>

    <!-- Features -->
    <section class="mx-auto max-w-7xl px-4 py-12 sm:px-6">
      <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <div v-for="f in features" :key="f.title" class="rounded-2xl border border-emerald-100 bg-white p-6">
          <span class="grid h-11 w-11 place-items-center rounded-xl bg-emerald-50 text-emerald-600">
            <UIcon :name="f.icon" class="h-5 w-5" />
          </span>
          <h3 class="mt-3 font-semibold text-gray-800">{{ f.title }}</h3>
          <p class="mt-1 text-sm text-gray-500">{{ f.desc }}</p>
        </div>
      </div>
    </section>

    <!-- Categories -->
    <section class="mx-auto max-w-7xl px-4 pb-12 sm:px-6">
      <div class="mb-6 flex items-center justify-between">
        <h2 class="text-2xl font-extrabold text-gray-800">Danh mục</h2>
        <UButton to="/products" color="primary" variant="ghost" label="Xem tất cả" trailing-icon="i-ph-arrow-right" />
      </div>
      <div class="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-4">
        <NuxtLink
          v-for="c in categories"
          :key="c.id"
          :to="`/products?category=${c.slug}`"
          class="rounded-2xl border border-emerald-100 bg-white p-5 hover:border-emerald-300"
        >
          <span class="grid h-11 w-11 place-items-center rounded-xl bg-emerald-50 text-emerald-600">
            <UIcon :name="categoryIcon(c.icon)" class="h-5 w-5" />
          </span>
          <h3 class="mt-3 font-semibold text-gray-800">{{ c.name }}</h3>
          <p v-if="c.children.length" class="text-xs text-gray-400">{{ c.children.length }} mục</p>
        </NuxtLink>
      </div>
    </section>

    <!-- How it works -->
    <section class="bg-emerald-50/40 py-12">
      <div class="mx-auto max-w-7xl px-4 sm:px-6">
        <h2 class="text-center text-2xl font-extrabold text-gray-800">Cách đặt hàng</h2>
        <p class="mt-2 text-center text-sm text-gray-500">Chỉ 4 bước đơn giản là mâm cơm xanh đã sẵn sàng</p>
        <div class="mt-8 grid gap-6 sm:grid-cols-2 lg:grid-cols-4">
          <div v-for="(step, i) in steps" :key="step.title" class="relative rounded-2xl border border-emerald-100 bg-white p-6 pt-7">
            <span class="absolute -top-3 left-6 grid h-7 w-7 place-items-center rounded-full bg-emerald-600 text-sm font-bold text-white">{{ i + 1 }}</span>
            <span class="grid h-11 w-11 place-items-center rounded-xl bg-emerald-50 text-emerald-600">
              <UIcon :name="step.icon" class="h-5 w-5" />
            </span>
            <h3 class="mt-3 font-semibold text-gray-800">{{ step.title }}</h3>
            <p class="mt-1 text-sm text-gray-500">{{ step.desc }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Latest products -->
    <section class="py-12">
      <div class="mx-auto max-w-7xl px-4 sm:px-6">
        <div class="mb-6 flex items-center justify-between">
          <h2 class="text-2xl font-extrabold text-gray-800">Sản phẩm mới</h2>
          <UButton to="/products" color="primary" variant="ghost" label="Xem tất cả" trailing-icon="i-ph-arrow-right" />
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
          <p class="text-sm font-semibold uppercase tracking-wide text-emerald-600">Về EcoMart</p>
          <h2 class="mt-2 text-3xl font-extrabold text-gray-800">Tươi sạch mỗi ngày, giá cả hợp lý</h2>
          <p class="mt-4 leading-relaxed text-gray-500">
            EcoMart cam kết mang đến sản phẩm có nguồn gốc rõ ràng, chất lượng đảm bảo với mức giá phù hợp.
            Đặt hàng nhanh chóng, giao tận nơi và thanh toán linh hoạt.
          </p>
          <ul class="mt-6 space-y-3">
            <li v-for="item in ['Sản phẩm đa dạng, nguồn gốc rõ ràng', 'Giao hàng nhanh, thanh toán linh hoạt', 'Chăm sóc khách hàng tận tâm', 'Đóng gói thân thiện môi trường']" :key="item" class="flex items-center gap-3 text-gray-700">
              <span class="grid h-6 w-6 place-items-center rounded-full bg-emerald-100 text-emerald-700">
                <UIcon name="i-ph-check" class="h-4 w-4" />
              </span>
              <span class="text-sm">{{ item }}</span>
            </li>
          </ul>
        </div>
        <div class="rounded-2xl bg-emerald-50 p-8">
          <div class="text-center">
            <img src="/favicon.svg" alt="EcoMart" class="mx-auto h-16 w-16 rounded-2xl" />
            <h3 class="mt-4 text-2xl font-extrabold text-emerald-700">Mua sắm dễ dàng</h3>
            <p class="mt-2 text-sm text-gray-500">Chọn sản phẩm, đặt hàng nhanh chóng và nhận tận nơi trên toàn quốc.</p>
            <UButton to="/products" color="primary" label="Bắt đầu mua sắm" class="mt-5" icon="i-ph-shopping-bag" />
          </div>
        </div>
      </div>
    </section>

    <!-- Testimonials -->
    <section class="bg-emerald-50/40 py-12">
      <div class="mx-auto max-w-7xl px-4 sm:px-6">
        <div class="mb-8 text-center">
          <h2 class="text-2xl font-extrabold text-gray-800">Khách hàng nói gì</h2>
          <p class="mt-2 text-sm text-gray-500">Cảm nhận thực tế từ những người đã tin dùng EcoMart</p>
        </div>
        <div class="grid gap-6 md:grid-cols-3">
          <div v-for="t in testimonials" :key="t.name" class="rounded-2xl border border-emerald-100 bg-white p-6">
            <div class="flex gap-0.5">
              <UIcon v-for="s in 5" :key="s" name="i-ph-star-fill" class="h-4 w-4 text-yellow-400" />
            </div>
            <p class="mt-3 leading-relaxed text-gray-600">“{{ t.content }}”</p>
            <div class="mt-4 flex items-center gap-3">
              <UAvatar :src="t.avatar || undefined" :alt="t.name" size="md" />
              <div>
                <p class="text-sm font-semibold text-gray-800">{{ t.name }}</p>
                <p class="text-xs text-gray-400">{{ t.role }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- CTA -->
    <section class="mx-auto max-w-7xl px-4 py-16 sm:px-6">
      <div class="rounded-3xl bg-emerald-700 px-6 py-12 text-center sm:px-12">
        <h2 class="text-2xl font-extrabold text-white sm:text-3xl">Bắt đầu mua sắm xanh cùng EcoMart</h2>
        <p class="mx-auto mt-3 max-w-xl text-emerald-50">
          Đăng ký tài khoản miễn phí để nhận ưu đãi và đặt hàng thực phẩm tươi sạch mỗi ngày.
        </p>
        <UButton to="/products" color="neutral" size="lg" class="mt-6 bg-white! text-emerald-700!" icon="i-ph-shopping-bag">
          Mua sắm ngay
        </UButton>
      </div>
    </section>
  </div>
</template>