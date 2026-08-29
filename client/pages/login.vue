<script setup lang="ts">
import { loginSchema } from '~/schemas'

definePageMeta({
  layout: 'auth'
})

const { login } = useAuth()
const toast = useToast()

const form = reactive({ email: '', password: '' })
const errors = ref<Record<string, string>>({})
const loading = ref(false)

async function submit() {
  errors.value = {}
  const result = loginSchema.safeParse(form)
  if (!result.success) {
    for (const issue of result.error.issues) {
      errors.value[String(issue.path[0])] = issue.message
    }
    return
  }
  loading.value = true
  try {
    const data = await login(form.email, form.password)
    if (data.role === 'ADMIN') {
      navigateTo('/admin')
    } else {
      navigateTo('/')
    }
  } catch (e: any) {
    toast.add({ title: e?.data?.message || 'Đăng nhập thất bại', color: 'red', icon: 'i-heroicons-exclamation-triangle' })
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="flex min-h-screen items-center justify-center bg-gradient-to-br from-green-50 to-green-100 p-4">
    <div class="w-full max-w-md">
      <NuxtLink to="/" class="mb-6 flex items-center justify-center gap-2">
        <span class="grid h-10 w-10 place-items-center rounded-xl bg-green-600 text-white">
          <UIcon name="i-heroicons-leaf" class="h-5 w-5" />
        </span>
        <span class="text-2xl font-extrabold text-green-800">EcoMart</span>
      </NuxtLink>

      <div class="rounded-2xl border border-green-100 bg-white p-8 shadow-lg shadow-green-100/50">
        <h1 class="text-2xl font-extrabold text-gray-800">Đăng nhập</h1>
        <p class="mt-1 text-sm text-gray-500">Chào mừng bạn quay trở lại</p>

        <form class="mt-6 space-y-4" @submit.prevent="submit">
          <div>
            <UInput v-model="form.email" type="email" placeholder="Email" icon="i-heroicons-envelope" size="lg" :trailing-icon="errors.email ? 'i-heroicons-exclamation-circle' : undefined" />
            <p v-if="errors.email" class="mt-1 text-xs text-red-600">{{ errors.email }}</p>
          </div>
          <div>
            <UInput v-model="form.password" type="password" placeholder="Mật khẩu" icon="i-heroicons-lock-closed" size="lg" />
            <p v-if="errors.password" class="mt-1 text-xs text-red-600">{{ errors.password }}</p>
          </div>
          <UButton type="submit" color="green" size="lg" block :loading="loading" label="Đăng nhập" />
        </form>

        <p class="mt-6 text-center text-sm text-gray-500">
          Chưa có tài khoản?
          <NuxtLink to="/register" class="font-semibold text-green-700 hover:underline">Đăng ký</NuxtLink>
        </p>
      </div>
    </div>
  </div>
</template>
