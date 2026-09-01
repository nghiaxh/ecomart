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
const showPassword = ref(false)

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
  <div class="relative flex min-h-screen items-center justify-center overflow-hidden p-4" style="background-image: url('/images/auth-bg.jpg'); background-size: cover; background-position: center; background-color: #065f46;">
    <div class="absolute inset-0 bg-emerald-900/30 backdrop-blur-md" aria-hidden="true"></div>
    <div class="pointer-events-none absolute -left-24 -top-24 h-72 w-72 rounded-full bg-emerald-400/30 blur-3xl" aria-hidden="true"></div>
    <div class="pointer-events-none absolute -bottom-24 -right-24 h-72 w-72 rounded-full bg-primary-300/30 blur-3xl" aria-hidden="true"></div>

    <div class="relative z-10 w-full max-w-md rounded-2xl bg-white/90 p-8 shadow-2xl shadow-emerald-950/40 backdrop-blur-lg sm:p-10">
      <h1 class="text-2xl font-extrabold text-gray-800">Tạo tài khoản</h1>
      <p class="mt-1 text-sm text-gray-500">Bắt đầu mua sắm tiện lợi cùng EcoMart</p>

      <form class="mt-8 space-y-5" @submit.prevent="submit">
        <div>
          <UInput v-model="form.username" placeholder="Tên đăng nhập" icon="i-ph-user" size="lg" />
          <div class="h-5"><p v-if="errors.username" class="text-xs text-red-600">{{ errors.username }}</p></div>
        </div>
        <div>
          <UInput v-model="form.email" type="email" placeholder="Email" icon="i-ph-envelope" size="lg" />
          <div class="h-5"><p v-if="errors.email" class="text-xs text-red-600">{{ errors.email }}</p></div>
        </div>
        <div>
          <UInput v-model="form.numberPhone" placeholder="Số điện thoại" icon="i-ph-phone" size="lg" />
          <div class="h-5"><p v-if="errors.numberPhone" class="text-xs text-red-600">{{ errors.numberPhone }}</p></div>
        </div>
        <div>
          <UInput
            v-model="form.password"
            :type="showPassword ? 'text' : 'password'"
            placeholder="Mật khẩu"
            icon="i-ph-lock"
            size="lg"
            :ui="{ trailing: 'pe-1' }"
          >
            <template #trailing>
              <UButton
                color="neutral"
                variant="link"
                size="sm"
                :icon="showPassword ? 'i-ph-eye-slash' : 'i-ph-eye'"
                :aria-label="showPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'"
                :aria-pressed="showPassword"
                @click="showPassword = !showPassword"
              />
            </template>
          </UInput>
          <div class="h-5"><p v-if="errors.password" class="text-xs text-red-600">{{ errors.password }}</p></div>
        </div>
        <UButton type="submit" color="primary" size="lg" block :loading="loading" label="Đăng ký" class="mt-2" />
      </form>

      <p class="mt-6 text-center text-sm text-gray-500">
        Đã có tài khoản?
        <NuxtLink to="/login" class="font-semibold text-emerald-700 hover:underline">Đăng nhập</NuxtLink>
      </p>
    </div>
  </div>
</template>
