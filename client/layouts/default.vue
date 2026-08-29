<script setup lang="ts">
const { isLoggedIn, isAdmin, session, logout } = useAuth()
const cart = useCart()
const { formatVND } = useFormat()

const links = [
  { label: 'Trang chủ', to: '/' },
  { label: 'Sản phẩm', to: '/products' },
  { label: 'Về chúng tôi', to: '/#about' }
]

const userMenu = computed(() => {
  const items = [{ label: 'Tài khoản', icon: 'i-heroicons-user', to: '/account' }]
  if (isAdmin.value) {
    items.push({ label: 'Quản trị', icon: 'i-heroicons-squares-2x2', to: '/admin' })
  }
  items.push({ label: 'Đăng xuất', icon: 'i-heroicons-arrow-right-on-rectangle', click: logout })
  return items
})
</script>

<template>
  <div class="flex min-h-screen flex-col">
    <header class="sticky top-0 z-40 border-b border-green-100/70 bg-white/90 backdrop-blur">
      <div class="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-6">
        <NuxtLink to="/" class="flex items-center gap-2">
          <span class="grid h-9 w-9 place-items-center rounded-xl bg-green-600 text-white">
            <UIcon name="i-heroicons-leaf" class="h-5 w-5" />
          </span>
          <span class="text-xl font-extrabold tracking-tight text-green-800">EcoMart</span>
        </NuxtLink>

        <nav class="hidden items-center gap-8 md:flex">
          <NuxtLink v-for="link in links" :key="link.to" :to="link.to" class="text-sm font-medium text-gray-600 transition hover:text-green-700">
            {{ link.label }}
          </NuxtLink>
        </nav>

        <div class="flex items-center gap-1">
          <UButton icon="i-heroicons-chat-bubble-left-right" color="gray" variant="ghost" :to="'/chat'" square aria-label="Chat trợ giúp" />

          <UButton
            color="gray"
            variant="ghost"
            square
            aria-label="Giỏ hàng"
            :to="isLoggedIn ? '/cart' : '/login'"
            class="relative"
          >
            <UIcon name="i-heroicons-shopping-cart" class="h-5 w-5" />
            <span v-if="cart.itemCount > 0" class="absolute right-0 top-0 grid h-4 min-w-4 place-items-center rounded-full bg-green-600 px-1 text-[10px] font-bold text-white">
              {{ cart.itemCount }}
            </span>
          </UButton>

          <template v-if="isLoggedIn">
            <UDropdown :items="userMenu" :popper="{ placement: 'bottom-end' }">
              <UButton color="gray" variant="ghost" class="gap-2">
                <UAvatar :src="session?.avatarUrl || undefined" :alt="session?.username" size="sm" />
                <span class="hidden text-sm font-medium sm:inline">{{ session?.username }}</span>
              </UButton>
            </UDropdown>
          </template>
          <template v-else>
            <UButton :to="'/login'" color="gray" variant="soft" size="sm">Đăng nhập</UButton>
            <UButton :to="'/register'" color="green" size="sm" class="ml-1">Đăng ký</UButton>
          </template>
        </div>
      </div>
    </header>

    <main class="flex-1">
      <slot />
    </main>

    <FooterGlobal />
  </div>
</template>
