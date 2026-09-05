<script setup lang="ts">
import type { Banner, CategoryResponse, Product } from '~/types'
import { categoryImage, homeAboutPoints, homeFeatures, homeStats, homeSteps, homeTestimonials } from '~/data/home'

const { request } = useApi()
const config = useRuntimeConfig()
const supportPhone = config.public.supportPhone as string

const { data: homeData, pending: loading } = await useAsyncData('home', async () => {
  const [banners, categories, products] = await Promise.all([
    request<Banner[]>('/api/banners/active'),
    request<CategoryResponse[]>('/api/categories'),
    request<Product[]>('/api/products/latest')
  ])
  return { banners, categories, products }
})

const banners = computed(() => homeData.value?.banners ?? [])
const categories = computed(() => homeData.value?.categories ?? [])
const products = computed(() => homeData.value?.products ?? [])

const stats = homeStats
const features = homeFeatures(supportPhone)
const steps = homeSteps
const testimonials = homeTestimonials
const aboutPoints = homeAboutPoints
</script>

<template>
  <div>
    <!-- Hero -->
    <section class="relative min-h-130 overflow-hidden bg-emerald-900"
      style="background-image: url('/images/hero-bg.jpg'); background-size: cover; background-position: center;">
      <div class="absolute inset-0 bg-emerald-900/50 backdrop-blur-sm"></div>
      <div class="relative z-10 mx-auto max-w-7xl px-4 py-20 sm:px-6 lg:py-28">
        <div class="max-w-2xl">
          <p
            class="mb-4 inline-flex items-center gap-2 rounded-full bg-white/15 px-4 py-1.5 text-sm font-medium text-white">
            <UIcon name="i-ph-storefront" class="h-4 w-4" />
            Thực phẩm tươi và đa dạng
          </p>
          <h1
            class="text-balance text-4xl font-extrabold leading-tight tracking-tight text-white sm:text-5xl lg:text-6xl">
            Mua sắm tiện lợi mỗi ngày
          </h1>
          <p class="mt-5 max-w-xl text-lg leading-relaxed text-emerald-50">
            EcoMart mang đến rau củ, trái cây, thực phẩm tươi sạch và đầy đủ cho mọi bữa ăn của gia đình bạn.
          </p>
          <div class="mt-8 flex flex-wrap items-center gap-3">
            <UButton to="/products" color="neutral" size="xl" class="bg-white! px-7! text-emerald-700!"
              icon="i-ph-shopping-bag">
              Mua sắm ngay
            </UButton>
          </div>
        </div>
      </div>
    </section>

    <!-- Stats -->
    <section class="border-b border-emerald-100 bg-white">
      <Reveal>
        <div class="mx-auto grid max-w-7xl grid-cols-3 divide-x divide-emerald-100 px-4 py-10 sm:px-6">
          <div v-for="s in stats" :key="s.label" class="px-4 text-center sm:px-8">
            <p class="text-3xl font-extrabold tracking-tight text-emerald-700 tabular-nums sm:text-4xl">{{ s.value }}
            </p>
            <p class="mt-1 text-xs text-gray-500 sm:text-sm">{{ s.label }}</p>
          </div>
        </div>
      </Reveal>
    </section>

    <!-- Banner carousel -->
    <section v-if="banners.length" class="mx-auto max-w-7xl px-4 pt-12 sm:px-6">
      <Reveal>
        <UCarousel v-slot="{ item }" loop arrows dots :autoplay="{ delay: 5000 }" :items="banners"
          :ui="{ item: 'basis-full' }">
          <NuxtLink :to="item.linkUrl || '/products'"
            class="group relative block h-64 overflow-hidden rounded-3xl ring-1 ring-emerald-900/5 sm:h-72">
            <img :src="item.imageUrl" :alt="item.title" loading="lazy"
              class="h-full w-full object-cover transition-transform duration-500 group-hover:scale-105" />
            <div class="absolute inset-0 bg-linear-to-t from-black/60 to-transparent"></div>
            <div class="absolute bottom-0 p-6 sm:p-8 text-white">
              <h3 class="text-xl font-bold sm:text-2xl">{{ item.title }}</h3>
              <p v-if="item.subtitle" class="mt-1 text-sm text-white/80">{{ item.subtitle }}</p>
            </div>
          </NuxtLink>
        </UCarousel>
      </Reveal>
    </section>

    <!-- Features -->
    <section class="mx-auto max-w-7xl px-4 py-14 sm:px-6">
      <Reveal>
        <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          <div v-for="f in features" :key="f.title"
            class="group relative overflow-hidden rounded-2xl border border-emerald-100 bg-white">
            <div class="relative h-40 overflow-hidden">
              <img :src="f.photo" :alt="f.title" loading="lazy"
                class="h-full w-full object-cover transition-transform duration-500 group-hover:scale-105" />
              <div class="absolute inset-0 bg-gradient-to-t from-emerald-900/35 to-transparent"></div>
            </div>
            <div class="p-5">
              <h3 class="text-lg font-bold tracking-tight text-gray-800">{{ f.title }}</h3>
              <p class="mt-1.5 text-sm leading-relaxed text-gray-500">{{ f.desc }}</p>
            </div>
          </div>
        </div>
      </Reveal>
    </section>

    <!-- Categories -->
    <section class="pb-14">
      <div class="mx-auto max-w-7xl px-4 sm:px-6">
        <Reveal>
          <SectionHeader title="Danh mục" />
          <div v-if="categories.length" class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
            <NuxtLink v-for="(c, idx) in categories" :key="c.id" :to="`/products?category=${c.slug}`"
              class="cat-card group relative flex h-full min-h-60 flex-col overflow-hidden rounded-2xl">
              <div class="relative h-40 shrink-0 overflow-hidden">
                <img :src="categoryImage(c.slug, idx)" :alt="c.name" loading="lazy"
                  class="absolute inset-0 h-full w-full object-cover transition-transform duration-500 group-hover:scale-105" />
                <div class="absolute inset-0 bg-gradient-to-t from-emerald-900/70 via-emerald-900/10 to-transparent"></div>
                <span class="absolute bottom-0 left-0 p-5">
                  <span class="block text-xl font-bold text-white drop-shadow-sm">{{ c.name }}</span>
                </span>
                <span
                  class="absolute right-4 top-4 grid h-8 w-8 place-items-center rounded-full bg-white/15 text-white backdrop-blur-sm transition-all duration-200 group-hover:bg-white/25 group-hover:scale-110">
                  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" class="h-4 w-4">
                    <path fill-rule="evenodd" d="M12.97 3.97a.75.75 0 0 1 1.06 0l7.5 7.5a.75.75 0 0 1 0 1.06l-7.5 7.5a.75.75 0 1 1-1.06-1.06l6.22-6.22H3a.75.75 0 0 1 0-1.5h16.19l-6.22-6.22a.75.75 0 0 1 0-1.06Z" clip-rule="evenodd" />
                  </svg>
                </span>
              </div>
              <div class="flex flex-1 flex-col justify-between gap-3 border-t border-emerald-50 bg-white p-5">
                <ul class="space-y-1.5">
                  <li v-for="sub in c.children.slice(0, 4)" :key="sub.id"
                    class="flex items-center gap-2 text-sm text-gray-500">
                    <span class="grid h-4 w-4 shrink-0 place-items-center rounded-full bg-emerald-100 text-emerald-700">
                      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" class="h-2.5 w-2.5">
                        <path fill-rule="evenodd" d="M16.704 4.153a.75.75 0 0 1 .143 1.052l-8 10.5a.75.75 0 0 1-1.127.075l-4.5-4.5a.75.75 0 0 1 1.06-1.06l3.894 3.893 7.48-9.817a.75.75 0 0 1 1.05-.143Z" clip-rule="evenodd" />
                      </svg>
                    </span>
                    {{ sub.name }}
                  </li>
                </ul>
                <span v-if="c.children.length"
                  class="inline-flex items-center gap-1 text-sm font-semibold text-emerald-600 transition-colors group-hover:text-emerald-700">
                  Xem {{ c.children.length }} mục nhỏ
                </span>
              </div>
            </NuxtLink>
          </div>
          <p v-else class="rounded-2xl border border-dashed border-emerald-200 py-10 text-center text-sm text-gray-400">
            Chưa có danh mục.</p>
        </Reveal>
      </div>
    </section>

    <!-- How it works -->
    <section class="py-14">
      <div class="mx-auto max-w-7xl px-4 sm:px-6">
        <Reveal>
          <div class="mx-auto max-w-2xl text-center">
            <h2 class="text-2xl font-extrabold tracking-tight text-gray-800">Cách đặt hàng</h2>
            <p class="mt-2 text-sm text-gray-500">Chỉ 4 bước đơn giản là mâm cơm xanh đã sẵn sàng</p>
          </div>
          <div class="relative mt-10">
            <div class="absolute left-1/4 right-1/4 top-6 hidden border-t-2 border-dashed border-emerald-200 lg:block"
              aria-hidden="true"></div>
            <div class="grid gap-5 sm:grid-cols-2 lg:grid-cols-4">
              <div v-for="(step, i) in steps" :key="step.title"
                class="group relative overflow-hidden rounded-xl border border-emerald-100 bg-white transition-all duration-200 hover:-translate-y-1 hover:shadow-lg hover:shadow-emerald-100">
                <div class="relative h-40 overflow-hidden">
                  <img :src="step.photo" :alt="step.title" loading="lazy"
                    class="h-full w-full object-cover transition-transform duration-500 group-hover:scale-105" />
                  <div class="absolute inset-0 bg-gradient-to-t from-emerald-900/45 via-emerald-900/5 to-transparent"></div>
                </div>
                <div class="flex items-center gap-3 p-4">
                  <span
                    class="grid h-9 w-9 shrink-0 place-items-center rounded-full bg-emerald-600 text-sm font-extrabold text-white shadow-sm shadow-emerald-200">
                    {{ i + 1 }}
                  </span>
                  <div>
                    <h3 class="text-sm font-bold tracking-tight text-gray-800">{{ step.title }}</h3>
                    <p class="mt-0.5 text-xs leading-relaxed text-gray-500">{{ step.desc }}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </Reveal>
      </div>
    </section>

    <!-- Latest products -->
    <section class="pb-14">
      <div class="mx-auto max-w-7xl px-4 sm:px-6">
        <Reveal>
          <SectionHeader title="Sản phẩm mới" />

          <div v-if="loading" class="grid grid-cols-2 gap-4 md:grid-cols-4">
            <USkeleton v-for="i in 4" :key="i" class="h-80 rounded-2xl" />
          </div>
          <div v-else-if="products.length" class="grid grid-cols-2 gap-4 md:grid-cols-3 lg:grid-cols-4">
            <ProductCard v-for="p in products" :key="p.id" :product="p" />
          </div>
          <div v-else
            class="rounded-2xl border border-dashed border-emerald-200 py-16 text-center text-sm text-gray-400">
            Chưa có sản phẩm.</div>
        </Reveal>
      </div>
    </section>

    <!-- About / mission -->
    <section id="about" class="py-16">
      <div class="mx-auto max-w-7xl px-4 sm:px-6">
        <Reveal>
          <div class="mx-auto max-w-2xl text-center">
            <p class="text-sm font-semibold uppercase tracking-wide text-emerald-600">Về EcoMart</p>
            <h2 class="mt-2 text-3xl font-extrabold tracking-tight text-gray-800">Tươi sạch mỗi ngày, giá cả hợp lý</h2>
            <p class="mx-auto mt-4 max-w-xl leading-relaxed text-gray-500">
              EcoMart cam kết mang đến sản phẩm có nguồn gốc rõ ràng, chất lượng đảm bảo với mức giá phù hợp.
              Đặt hàng nhanh chóng, giao tận nơi và thanh toán linh hoạt.
            </p>
            <ul class="mt-8 flex flex-wrap items-center justify-center gap-3">
              <li
                v-for="item in aboutPoints"
                :key="item"
                class="flex items-center gap-2 rounded-full border border-emerald-100 bg-white px-4 py-2 text-sm text-gray-700">
                <span class="grid h-5 w-5 place-items-center rounded-full bg-emerald-100 text-emerald-700">
                  <UIcon name="i-ph-check" class="h-3.5 w-3.5" />
                </span>
                {{ item }}
              </li>
            </ul>
            <UButton to="/products" color="primary" label="Khám phá sản phẩm" size="lg" class="mt-8"
              icon="i-ph-shopping-bag" />
          </div>
        </Reveal>
      </div>
    </section>

    <!-- Testimonials -->
    <section class="bg-emerald-50/40 py-14">
      <div class="mx-auto max-w-7xl px-4 sm:px-6">
        <Reveal>
          <div class="mb-8 text-center">
            <h2 class="text-2xl font-extrabold tracking-tight text-gray-800">Khách hàng nói gì</h2>
            <p class="mt-2 text-sm text-gray-500">Cảm nhận thực tế từ những người đã tin dùng EcoMart</p>
          </div>
          <div class="grid gap-6 md:grid-cols-3">
            <div v-for="t in testimonials" :key="t.name" class="rounded-2xl border border-emerald-100 bg-white p-6">
              <div class="flex gap-0.5">
                <UIcon v-for="s in 5" :key="s" name="i-ph-star-fill" class="h-4 w-4 text-yellow-400" />
              </div>
              <p class="mt-3 leading-relaxed text-gray-600">“{{ t.content }}”</p>
              <div class="mt-4 flex items-center gap-3">
                <span
                  class="grid h-11 w-11 place-items-center rounded-full bg-emerald-100 font-bold text-emerald-700">{{
                    t.initial }}</span>
                <div>
                  <p class="text-sm font-semibold text-gray-800">{{ t.name }}</p>
                  <p class="text-xs text-gray-400">{{ t.role }}</p>
                </div>
              </div>
            </div>
          </div>
        </Reveal>
      </div>
    </section>

    <!-- CTA -->
    <section class="mx-auto max-w-7xl px-4 py-16 sm:px-6">
      <Reveal>
        <div
          class="relative overflow-hidden rounded-3xl bg-gradient-to-br from-emerald-600 to-emerald-800 px-6 py-12 text-center sm:px-12">
          <UIcon name="i-ph-leaf" class="pointer-events-none absolute -left-10 -top-10 h-40 w-40 text-white/10"
            aria-hidden="true" />
          <UIcon name="i-ph-leaf" class="pointer-events-none absolute -bottom-12 -right-12 h-52 w-52 text-white/10"
            aria-hidden="true" />
          <h2 class="text-2xl font-extrabold text-white sm:text-3xl">Bắt đầu mua sắm xanh cùng EcoMart</h2>
          <p class="mx-auto mt-3 max-w-xl text-emerald-50">
            Đăng ký tài khoản miễn phí để nhận ưu đãi và đặt hàng thực phẩm tươi sạch mỗi ngày.
          </p>
          <UButton to="/products" color="neutral" size="lg" class="mt-6 bg-white! text-emerald-700!"
            icon="i-ph-shopping-bag">
            Mua sắm ngay
          </UButton>
      </div>
    </Reveal>
    </section>
  </div>
</template>