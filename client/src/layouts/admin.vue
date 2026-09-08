<script setup lang="ts">
import { useAuth } from '@/composables/useAuth'
const { logout } = useAuth()

const mobileOpen = ref(false)

const nav = [
  { label: 'Tổng quan', icon: 'i-ph-squares-four', to: '/admin', exact: true },
  { label: 'Sản phẩm', icon: 'i-ph-shopping-bag', to: '/admin/products' },
  { label: 'Danh mục', icon: 'i-ph-tag', to: '/admin/categories' },
  { label: 'Đơn hàng', icon: 'i-ph-receipt', to: '/admin/orders' },
  { label: 'Người dùng', icon: 'i-ph-users', to: '/admin/users' }
]

const collapsed = ref(localStorage.getItem('ecomart_admin_sidebar') === 'collapsed')
watch(collapsed, (value) => localStorage.setItem('ecomart_admin_sidebar', value ? 'collapsed' : 'expanded'))

const route = useRoute()
const title = computed(() => (route.meta.title as string) || 'Bảng điều khiển')

function closeMobile() {
  mobileOpen.value = false
}
</script>

<template>
  <div class="flex min-h-screen bg-gray-50">
    <!-- Desktop sidebar -->
    <aside
      :class="collapsed ? 'w-16' : 'w-64'"
      class="sticky top-0 hidden h-screen flex-col border-r border-gray-200 bg-white transition-[width] duration-200 lg:flex"
    >
      <RouterLink to="/" class="flex h-16 items-center gap-2.5 overflow-hidden border-b border-gray-100 px-4" :title="collapsed ? 'EcoMart' : undefined">
        <img src="/favicon.svg" alt="EcoMart" class="h-8 w-8 shrink-0 rounded-lg" />
        <div v-if="!collapsed" class="min-w-0 leading-tight">
          <p class="truncate text-sm font-extrabold text-emerald-800">EcoMart</p>
          <p class="text-xs text-gray-400">Quản trị</p>
        </div>
      </RouterLink>

      <nav class="flex-1 space-y-1 p-3">
        <RouterLink
          v-for="item in nav"
          :key="item.to"
          :to="item.to"
          :title="collapsed ? item.label : undefined"
          class="group relative flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium text-gray-600 transition hover:bg-emerald-50 hover:text-emerald-700"
          :class="collapsed ? 'justify-center' : ''"
          :active-class="item.exact ? undefined : 'bg-emerald-600! text-white!'"
          :exact-active-class="item.exact ? 'bg-emerald-600! text-white!' : undefined"
        >
          <UIcon :name="item.icon" class="h-5 w-5 shrink-0" />
          <span v-if="!collapsed" class="truncate">{{ item.label }}</span>
        </RouterLink>
      </nav>

      <div class="flex flex-col gap-1 border-t border-gray-100 p-3">
        <UButton v-if="collapsed" color="neutral" variant="ghost" square icon="i-ph-arrow-left" aria-label="Về trang chủ" :to="'/' " />
        <UButton v-else color="neutral" variant="ghost" block icon="i-ph-arrow-left" label="Về trang chủ" :to="'/' " />
        <UButton v-if="collapsed" color="neutral" variant="soft" square icon="i-ph-sign-out" aria-label="Đăng xuất" @click="logout" />
        <UButton v-else class="mt-1" color="neutral" variant="soft" block icon="i-ph-sign-out" label="Đăng xuất" @click="logout" />
      </div>
    </aside>

    <!-- Mobile sidebar overlay -->
    <div v-if="mobileOpen" class="fixed inset-0 z-40 lg:hidden">
      <div class="absolute inset-0 bg-black/40" @click="mobileOpen = false"></div>
      <aside class="absolute inset-y-0 left-0 flex w-72 flex-col border-r border-gray-200 bg-white">
        <div class="flex h-16 items-center justify-between border-b border-gray-100 px-6">
          <div class="flex items-center gap-2">
            <img src="/favicon.svg" alt="EcoMart" class="h-8 w-8 rounded-lg" />
            <div class="leading-tight">
              <p class="text-sm font-extrabold text-emerald-800">EcoMart</p>
              <p class="text-xs text-gray-400">Quản trị</p>
            </div>
          </div>
          <UButton color="neutral" variant="ghost" square icon="i-ph-x" aria-label="Đóng menu" @click="mobileOpen = false" />
        </div>

        <nav class="flex-1 space-y-1 p-3">
          <RouterLink
            v-for="item in nav"
            :key="item.to"
            :to="item.to"
            class="flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium text-gray-600 transition hover:bg-emerald-50 hover:text-emerald-700"
            :active-class="item.exact ? undefined : 'bg-emerald-600! text-white!'"
            :exact-active-class="item.exact ? 'bg-emerald-600! text-white!' : undefined"
            @click="closeMobile"
          >
            <UIcon :name="item.icon" class="h-5 w-5" />
            {{ item.label }}
          </RouterLink>
        </nav>

        <div class="border-t border-gray-100 p-3">
          <UButton color="neutral" variant="ghost" block icon="i-ph-arrow-left" label="Về trang chủ" :to="'/'" @click="closeMobile" />
          <UButton class="mt-1" color="neutral" variant="soft" block icon="i-ph-sign-out" label="Đăng xuất" @click="logout" />
        </div>
      </aside>
    </div>

    <div class="flex min-h-screen flex-1 flex-col">
      <header class="sticky top-0 z-30 flex h-16 items-center justify-between border-b border-gray-200 bg-white px-4 sm:px-6">
        <div class="flex min-w-0 items-center gap-3">
          <UButton color="neutral" variant="ghost" square icon="i-ph-list" aria-label="Mở menu" class="lg:hidden" @click="mobileOpen = true" />
          <UButton
            color="neutral"
            variant="ghost"
            square
            :icon="collapsed ? 'i-ph-sidebar-simple' : 'i-ph-sidebar'"
            aria-label="Thu gọn sidebar"
            class="hidden lg:inline-flex"
            @click="collapsed = !collapsed"
          />
          <h1 class="truncate text-lg font-bold text-gray-800">{{ title }}</h1>
        </div>
      </header>

      <main class="flex-1 p-4 sm:p-6 lg:p-8">
        <RouterView />
      </main>
    </div>
  </div>
</template>