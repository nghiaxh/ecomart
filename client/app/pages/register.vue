<script setup lang="ts">
import { registerSchema } from '~/schemas'

definePageMeta({
  layout: 'auth'
})

const { register } = useAuth()
const toast = useToast()

const form = reactive({ username: '', email: '', numberPhone: '', password: '' })
const errors = ref<Record<string, string>>({})
const loading = ref(false)

async function submit() {
  errors.value = {}
  const result = registerSchema.safeParse(form)
  if (!result.success) {
    for (const issue of result.error.issues) {
      errors.value[String(issue.path[0])] = issue.message
    }
    return
  }
  loading.value = true
  try {
    const data = await register(form)
    if (data.role === 'ADMIN') {
      navigateTo('/admin')
    } else {
      navigateTo('/')
    }
  } catch (e: any) {
    toast.add({ title: e?.data?.message || 'Đăng ký thất bại', color: 'error', icon: 'i-ph-warning' })
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="relative flex min-h-screen">
    <div class="relative hidden w-1/2 lg:block" style="background-image: url('/images/auth-bg.jpg'); background-size: cover; background-position: center; background-color: #065f46;">
      <div class="absolute inset-0 bg-emerald-900/30 backdrop-blur-sm"></div>
      <div class="relative z-10 flex h-full flex-col items-center justify-center p-12 text-white">
        <NuxtLink to="/" class="mb-8 flex items-center gap-3">
          <span class="grid h-12 w-12 place-items-center rounded-2xl bg-white/15 text-white backdrop-blur">
            <UIcon name="i-ph-storefront" class="h-6 w-6" />
          </span>
          <span class="text-3xl font-extrabold">EcoMart</span>
        </NuxtLink>
        <p class="max-w-xs text-center text-lg leading-relaxed text-emerald-50/90">
          Siêu thị xanh — sản phẩm tươi sạch mỗi ngày cho gia đình bạn
        </p>
      </div>
    </div>

    <div class="flex w-full items-center justify-center bg-emerald-50/30 p-4 lg:w-1/2 lg:bg-white">
      <div class="w-full max-w-md">
        <NuxtLink to="/" class="mb-8 flex items-center justify-center gap-2 lg:hidden">
          <span class="grid h-10 w-10 place-items-center rounded-xl bg-emerald-700 text-white">
            <UIcon name="i-ph-storefront" class="h-5 w-5" />
          </span>
          <span class="text-2xl font-extrabold text-emerald-800">EcoMart</span>
        </NuxtLink>

        <div class="rounded-2xl border border-emerald-100/60 bg-white/80 p-8 shadow-xl shadow-emerald-900/5 backdrop-blur-sm sm:p-10">
          <h1 class="text-2xl font-extrabold text-gray-800">Tạo tài khoản</h1>
          <p class="mt-1 text-sm text-gray-500">Bắt đầu mua sắm tiện lợi cùng EcoMart</p>

          <form class="mt-6 space-y-5" @submit.prevent="submit">
            <div>
              <UInput v-model="form.username" placeholder="Tên đăng nhập" icon="i-ph-user" size="lg" />
              <p v-if="errors.username" class="mt-1 text-xs text-red-600">{{ errors.username }}</p>
            </div>
            <div>
              <UInput v-model="form.email" type="email" placeholder="Email" icon="i-ph-envelope" size="lg" />
              <p v-if="errors.email" class="mt-1 text-xs text-red-600">{{ errors.email }}</p>
            </div>
            <div>
              <UInput v-model="form.numberPhone" placeholder="Số điện thoại" icon="i-ph-phone" size="lg" />
              <p v-if="errors.numberPhone" class="mt-1 text-xs text-red-600">{{ errors.numberPhone }}</p>
            </div>
            <div>
              <UInput v-model="form.password" type="password" placeholder="Mật khẩu" icon="i-ph-lock" size="lg" />
              <p v-if="errors.password" class="mt-1 text-xs text-red-600">{{ errors.password }}</p>
            </div>
            <UButton type="submit" color="primary" size="lg" block :loading="loading" label="Đăng ký" />
          </form>

          <p class="mt-6 text-center text-sm text-gray-500">
            Đã có tài khoản?
            <NuxtLink to="/login" class="font-semibold text-emerald-700 hover:underline">Đăng nhập</NuxtLink>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>
