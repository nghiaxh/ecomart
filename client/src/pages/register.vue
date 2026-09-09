<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useToast } from '@/composables/useToast'
import { useRouter } from 'vue-router'
import { registerSchema } from '@/schemas'
import { useAuth } from '@/composables/useAuth'
import PasswordInput from '@/components/PasswordInput.vue'
import AuthShell from '@/components/AuthShell.vue'
import UiIcon from '@/components/UiIcon.vue'
import { NButton, NInput } from 'naive-ui'

const router = useRouter()
const { register } = useAuth()
const toast = useToast()

const form = reactive({ username: '', email: '', numberPhone: '', password: '' })
const loading = ref(false)

async function submit() {
  const result = registerSchema.safeParse(form)
  if (!result.success) {
    for (const issue of result.error.issues) {
      toast.add({ severity: 'error', summary: issue.message, life: 4000 })
    }
    return
  }
  loading.value = true
  try {
    const data = await register(form, { remember: true })
    if (data.role === 'ADMIN') {
      router.push('/admin/products')
    } else {
      router.push('/')
    }
  } catch (e: any) {
    toast.add({ severity: 'error', summary: e?.data?.message || 'Đăng ký thất bại', life: 4000 })
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <AuthShell title="Tạo tài khoản" subtitle="Bắt đầu mua sắm tiện lợi cùng EcoMart">
    <form class="w-full space-y-4" novalidate @submit.prevent="submit">
      <div>
        <label for="register-username" class="mb-1.5 block text-sm font-medium text-gray-700">Tên đăng nhập</label>
        <NInput id="register-username" v-model:value="form.username" autocomplete="username" placeholder="Nhập tên đăng nhập" size="large" class="w-full">
          <template #prefix><UiIcon name="user" size="18" /></template>
        </NInput>
      </div>
      <div>
        <label for="register-email" class="mb-1.5 block text-sm font-medium text-gray-700">Email</label>
        <NInput id="register-email" v-model:value="form.email" :input-props="{ type: 'email' }" autocomplete="email" placeholder="Nhập email" size="large" class="w-full">
          <template #prefix><UiIcon name="envelope" size="18" /></template>
        </NInput>
      </div>
      <div>
        <label for="register-phone" class="mb-1.5 block text-sm font-medium text-gray-700">Số điện thoại</label>
        <NInput id="register-phone" v-model:value="form.numberPhone" :input-props="{ type: 'tel' }" autocomplete="tel" placeholder="Nhập số điện thoại" size="large" class="w-full">
          <template #prefix><UiIcon name="phone" size="18" /></template>
        </NInput>
      </div>
      <div>
        <label for="register-password" class="mb-1.5 block text-sm font-medium text-gray-700">Mật khẩu</label>
        <PasswordInput
          id="register-password"
          v-model="form.password"
          autocomplete="new-password"
          placeholder="Nhập mật khẩu"
          icon="lock"
          size="large"
        />
      </div>
      <NButton type="primary" size="large" attr-type="submit" :loading="loading" block class="mt-2">Đăng ký</NButton>
    </form>

    <p class="mt-6 text-center text-sm text-gray-500">
      Đã có tài khoản?
      <RouterLink to="/login" class="font-semibold text-emerald-700 hover:underline">Đăng nhập</RouterLink>
    </p>
  </AuthShell>
</template>