<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { useAuth } from '@/composables/useAuth'
import { useCart } from '@/composables/useCart'
import FooterGlobal from '@/components/FooterGlobal.vue'
import UiIcon from '@/components/UiIcon.vue'
import { NAvatar, NButton } from 'naive-ui'

const { isLoggedIn, isAdmin, session } = useAuth()
const { itemCount } = useCart()

const route = useRoute()
const router = useRouter()

const links = [
  { label: 'Trang chủ', to: '/' },
  { label: 'Sản phẩm', to: '/products' },
  { label: 'Về chúng tôi', to: '/#about' }
]

const adminLinks = [
  { label: 'Sản phẩm', to: '/admin/products' },
  { label: 'Danh mục', to: '/admin/categories' },
  { label: 'Đơn hàng', to: '/admin/orders' },
  { label: 'Người dùng', to: '/admin/users' },
  { label: 'Thống kê', to: '/admin/statistic' }
]

function scrollHome() {
  if (route.name === 'home' && !route.hash) {
    const behavior = window.matchMedia('(prefers-reduced-motion: reduce)').matches ? 'auto' : 'smooth'
    window.scrollTo({ top: 0, behavior })
  }
}
</script>

<template>
  <div class="flex min-h-screen flex-col">
    <header class="sticky top-0 z-40 border-b border-emerald-100/70 bg-white/90 backdrop-blur">
      <div class="mx-auto flex h-16 items-center justify-between px-4 sm:px-6">
        <RouterLink to="/" class="flex items-center gap-2">
          <span class="text-lg font-extrabold tracking-tight text-emerald-800">EcoMart</span>
        </RouterLink>

        <nav class="hidden items-center gap-8 md:flex">
          <RouterLink v-for="link in (isAdmin ? adminLinks : links)" :key="link.to" :to="link.to"
            class="text-sm font-medium text-gray-600 hover:text-emerald-700"
            @click="scrollHome">
            {{ link.label }}
          </RouterLink>
        </nav>

        <div class="flex items-center gap-1">
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
