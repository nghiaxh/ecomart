<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuth } from '@/composables/useAuth'
import { useCart } from '@/composables/useCart'
import { useApi } from '@/composables/useApi'
import type { CategoryResponse } from '@/types'
import FooterGlobal from '@/components/FooterGlobal.vue'
import UiIcon from '@/components/UiIcon.vue'
import { NAvatar, NButton, NDropdown, type DropdownOption } from 'naive-ui'

const { isLoggedIn, isAdmin, session } = useAuth()
const { itemCount } = useCart()
const { request } = useApi()

const route = useRoute()
const router = useRouter()

const links = [
  { label: 'Trang chủ', to: '/' },
  { label: 'Sản phẩm', to: '/products' },
  { label: 'Về chúng tôi', to: '/#about' },
  { label: 'Liên hệ', to: '/#contact' }
]

const adminLinks = [
  { label: 'Sản phẩm', to: '/admin/products' },
  { label: 'Danh mục', to: '/admin/categories' },
  { label: 'Đơn hàng', to: '/admin/orders' },
  { label: 'Người dùng', to: '/admin/users' },
  { label: 'Thống kê', to: '/admin/statistic' }
]

const userLinks = computed(() =>
  isLoggedIn.value && !isAdmin.value ? [...links, { label: 'Đơn hàng', to: '/orders' }] : links
)

const categoryOptions = ref<DropdownOption[]>([])

onMounted(async () => {
  if (isAdmin.value) return
  try {
    const categories = await request<CategoryResponse[]>('/api/categories')
    categoryOptions.value = categories.map((c) => ({
      label: c.name,
      key: c.slug,
      children: c.children?.length
        ? c.children.map((ch) => ({ label: ch.name, key: ch.slug }))
        : undefined
    }))
  } catch {
    categoryOptions.value = []
  }
})

function onCategorySelect(slug: string) {
  router.push(`/products?category=${slug}`)
}

function scrollHome() {
  if (route.name === 'home' && !route.hash) {
    const behavior = window.matchMedia('(prefers-reduced-motion: reduce)').matches ? 'auto' : 'smooth'
    window.scrollTo({ top: 0, behavior })
  }
}
</script>

<template>
  <div class="flex min-h-screen flex-col">
    <header class="sticky top-0 z-40 bg-white/90 backdrop-blur-xl shadow-2xs">
      <div class="mx-auto grid h-16 grid-cols-[1fr_auto_1fr] items-center px-4 sm:px-6">
        <RouterLink to="/" class="flex items-center gap-2 justify-self-start">
          <span class="text-lg font-extrabold tracking-tight text-emerald-800">EcoMart</span>
        </RouterLink>

        <nav class="hidden items-center gap-6 lg:gap-8 md:flex justify-self-center">
          <RouterLink v-for="link in (isAdmin ? adminLinks : userLinks)" :key="link.to" :to="link.to"
            class="text-sm font-medium text-gray-600 hover:text-emerald-700"
            @click="scrollHome">
            {{ link.label }}
          </RouterLink>
          <NDropdown
            v-if="!isAdmin"
            :options="categoryOptions"
            trigger="click"
            placement="bottom"
            @select="onCategorySelect"
          >
            <span class="flex cursor-pointer items-center gap-1 text-sm font-medium text-gray-600 hover:text-emerald-700">
              Danh mục
              <UiIcon name="caret-down" size="14" />
            </span>
          </NDropdown>
        </nav>

        <div class="flex items-center gap-1 justify-self-end">
          <NButton
            v-if="isLoggedIn && !isAdmin"
            quaternary
            aria-label="Giỏ hàng"
            class="relative"
            @click="router.push(isLoggedIn ? '/cart' : '/login')"
          >
            <template #icon><UiIcon name="shopping-cart" size="22" /></template>
            <span v-if="itemCount > 0" class="absolute right-0 top-0 grid h-4 min-w-4 place-items-center rounded-full bg-emerald-600 px-1 text-[10px] font-bold text-white">
              {{ itemCount }}
            </span>
          </NButton>

          <template v-if="isLoggedIn">
            <RouterLink to="/account" class="flex items-center gap-2 rounded-lg p-1 hover:bg-emerald-50">
              <NAvatar round :src="session?.avatarUrl || undefined" size="medium">
                {{ session?.username?.charAt(0)?.toUpperCase() || undefined }}
              </NAvatar>
              <span class="hidden text-sm font-medium sm:inline">{{ session?.username }}</span>
            </RouterLink>
          </template>
          <template v-else>
            <NButton quaternary size="small" @click="router.push('/login')">Đăng nhập</NButton>
            <NButton type="primary" size="small" class="ml-1" @click="router.push('/register')">Đăng ký</NButton>
          </template>
        </div>
      </div>
    </header>

    <main class="flex-1">
      <RouterView />
    </main>

    <FooterGlobal v-if="route.name === 'home'" />
  </div>
</template>
