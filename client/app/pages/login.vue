<script setup lang="ts">
import { loginSchema } from '~/schemas'

definePageMeta({
  layout: false
})

const { login } = useAuth()
const toast = useToast()

const form = reactive({ identifier: '', password: '' })
const loading = ref(false)
const remember = ref(false)

async function submit() {
  const result = loginSchema.safeParse(form)
  if (!result.success) {
    for (const issue of result.error.issues) {
      toast.add({ title: issue.message, color: 'error', icon: 'i-ph-warning' })
    }
    return
  }
  loading.value = true
  try {
    const data = await login(form.identifier, form.password, { remember: remember.value })
    if (data.role === 'ADMIN') {
      navigateTo('/admin')
    } else {
      navigateTo('/')
    }
  } catch (e: any) {
    toast.add({ title: e?.data?.message || 'Đăng nhập thất bại', color: 'error', icon: 'i-ph-warning' })
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <AuthShell title="Đăng nhập" subtitle="Chào mừng bạn quay trở lại">
    <form class="w-full space-y-4" novalidate @submit.prevent="submit">
      <div>
        <label for="login-identifier" class="mb-1.5 block text-sm font-medium text-gray-700">Tên đăng nhập hoặc email</label>
        <UInput
          id="login-identifier"
          v-model="form.identifier"
          autocomplete="username"
          placeholder="Nhập tên đăng nhập hoặc email"
          icon="i-ph-user"
          size="lg"
          class="w-full"
        />
      </div>
      <div class="mb-6">
        <label for="login-password" class="mb-1.5 block text-sm font-medium text-gray-700">Mật khẩu</label>
        <PasswordInput
          id="login-password"
          v-model="form.password"
          autocomplete="current-password"
          placeholder="Nhập mật khẩu"
          icon="i-ph-lock"
          size="lg"
        />
      </div>
      <div class="flex items-center justify-between">
        <UCheckbox v-model="remember" label="Ghi nhớ đăng nhập" />
      </div>
      <UButton type="submit" color="primary" size="xl" block :loading="loading" label="Đăng nhập" class="mt-2" />
    </form>

    <p class="mt-6 text-center text-sm text-gray-500">
      Chưa có tài khoản?
      <NuxtLink to="/register" class="font-semibold text-emerald-700 hover:underline">Đăng ký</NuxtLink>
    </p>
  </AuthShell>
</template>