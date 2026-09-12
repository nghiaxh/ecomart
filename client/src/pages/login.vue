<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useToast } from '@/composables/useToast'
import { useRouter } from 'vue-router'
import { loginSchema } from '@/schemas'
import { useAuth } from '@/composables/useAuth'
import PasswordInput from '@/components/PasswordInput.vue'
import AuthShell from '@/components/AuthShell.vue'
import UiIcon from '@/components/UiIcon.vue'
import { NButton, NCheckbox, NInput } from 'naive-ui'

const router = useRouter()
const { login } = useAuth()
const toast = useToast()

const form = reactive({ identifier: '', password: '' })
const loading = ref(false)
const remember = ref(false)

async function submit() {
  const result = loginSchema.safeParse(form)
  if (!result.success) {
    for (const issue of result.error.issues) {
      toast.add({ severity: 'error', summary: issue.message, life: 4000 })
    }
    return
  }
  loading.value = true
  try {
    const data = await login(form.identifier, form.password, { remember: remember.value })
    router.push(data.role === 'CUSTOMER' ? '/' : '/admin/products')
  } catch (e: any) {
    toast.add({ severity: 'error', summary: e?.data?.message || 'Đăng nhập thất bại', life: 4000 })
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
        <NInput
          id="login-identifier"
          v-model:value="form.identifier"
          autocomplete="username"
          placeholder="Nhập tên đăng nhập hoặc email"
          size="large"
          class="w-full"
        >
          <template #prefix><UiIcon name="user" size="18" /></template>
        </NInput>
      </div>
      <div class="mb-6">
        <label for="login-password" class="mb-1.5 block text-sm font-medium text-gray-700">Mật khẩu</label>
        <PasswordInput
          id="login-password"
          v-model="form.password"
          autocomplete="current-password"
          placeholder="Nhập mật khẩu"
          icon="lock"
          size="large"
        />
      </div>
      <div class="flex items-center justify-between">
        <NCheckbox v-model:checked="remember">
          <label class="text-sm text-gray-700">Ghi nhớ đăng nhập</label>
        </NCheckbox>
      </div>
      <NButton type="primary" size="large" attr-type="submit" :loading="loading" block class="mt-2">Đăng nhập</NButton>
    </form>

    <p class="mt-6 text-center text-sm text-gray-500">
      Chưa có tài khoản?
      <RouterLink to="/register" class="font-semibold text-emerald-700 hover:underline">Đăng ký</RouterLink>
    </p>
  </AuthShell>
</template>