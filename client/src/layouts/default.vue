<script setup lang="ts">
import { useAuth } from '@/composables/useAuth'
import { useCart } from '@/composables/useCart'
const { isLoggedIn, isAdmin, session } = useAuth()
const { itemCount } = useCart()

const links = [
  { label: 'Trang chủ', to: '/' },
  { label: 'Sản phẩm', to: '/products' },
  { label: 'Về chúng tôi', to: '/#about' }
]
</script>

<template>
  <div class="flex min-h-screen flex-col">
    <header class="sticky top-0 z-40 border-b border-emerald-100/70 bg-white/90 backdrop-blur">
      <div class="mx-auto flex h-16 items-center justify-between px-4 sm:px-6">
        <RouterLink to="/" class="flex items-center gap-2">
          <img src="/favicon.svg" alt="EcoMart" class="h-9 w-9 rounded-xl" />
          <span class="text-xl font-extrabold tracking-tight text-emerald-800">EcoMart</span>
        </RouterLink>

        <nav class="hidden items-center gap-8 md:flex">
          <RouterLink v-for="link in links" :key="link.to" :to="link.to" class="text-sm font-medium text-gray-600 hover:text-emerald-700">
            {{ link.label }}
          </RouterLink>
        </nav>

        <div class="flex items-center gap-1">
          <UButton
            v-if="isLoggedIn && !isAdmin"
            color="neutral"
            variant="ghost"
            square
            aria-label="Giỏ hàng"
            :to="isLoggedIn ? '/cart' : '/login'"
            class="relative"
          >
            <UIcon name="i-ph-shopping-cart" class="h-5 w-5" />
            <span v-if="itemCount > 0" class="absolute right-0 top-0 grid h-4 min-w-4 place-items-center rounded-full bg-emerald-600 px-1 text-[10px] font-bold text-white">
              {{ itemCount }}
            </span>
          </UButton>

          <template v-if="isLoggedIn">
            <RouterLink to="/account" class="flex items-center gap-2 rounded-lg p-1 hover:bg-emerald-50">
              <UAvatar :src="session?.avatarUrl || undefined" :alt="session?.username" size="sm" />
              <span class="hidden text-sm font-medium sm:inline">{{ session?.username }}</span>
            </RouterLink>
          </template>
          <template v-else>
            <UButton :to="'/login'" color="neutral" variant="soft" size="lg">Đăng nhập</UButton>
            <UButton :to="'/register'" color="primary" size="lg" class="ml-1">Đăng ký</UButton>
          </template>
        </div>
      </div>
    </header>

    <main class="flex-1">
      <RouterView />
    </main>

    <FooterGlobal />
    <ChatWidget />
  </div>
</template>
