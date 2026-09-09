<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useToast } from '@/composables/useToast'
import type { ProfileResponse, Address } from '@/types'
import { addressSchema, profileSchema, type AddressForm as AddressFormData } from '@/schemas'
import { useApi } from '@/composables/useApi'
import { useAuth } from '@/composables/useAuth'
import { useFormErrors } from '@/composables/useFormErrors'
import { useConfirm } from '@/composables/useConfirm'
import { useFormat } from '@/composables/useFormat'
import AddressForm from '@/components/AddressForm.vue'
import AddressCard from '@/components/AddressCard.vue'
import UiIcon from '@/components/UiIcon.vue'
import { NAvatar, NButton, NInput, NSkeleton } from 'naive-ui'

const { request } = useApi()
const toast = useToast()
const { session, updateSession, isAdmin, logout } = useAuth()
const { errors, applyIssues, clearErrors } = useFormErrors()
const { confirm } = useConfirm()
const { formatDate } = useFormat()

const profile = ref<ProfileResponse | null>(null)
const addresses = ref<Address[]>([])
const loading = ref(true)

const editMode = ref(false)
const saving = ref(false)
const profileForm = reactive({
  username: '', numberPhone: '', avatarUrl: '', currentPassword: '', newPassword: ''
})

const showAddressForm = ref(false)
const editingAddressId = ref<number | null>(null)
const savingAddress = ref(false)
const addressUIBusy = ref<Set<number>>(new Set())
const addressForm = ref<AddressFormData>({
  label: '', street: '', ward: '', district: '', city: '', receiverName: '', receiverPhone: '', isDefault: false
})

async function load() {
  loading.value = true
  try {
    const [p, a] = await Promise.all([
      request<ProfileResponse>('/api/profile'),
      request<Address[]>('/api/addresses')
    ])
    profile.value = p
    addresses.value = a
    resetProfileForm(p)
  } finally {
    loading.value = false
  }
}

function resetProfileForm(p = profile.value) {
  if (!p) return
  Object.assign(profileForm, { username: p.username, numberPhone: p.numberPhone, avatarUrl: p.avatarUrl || '', currentPassword: '', newPassword: '' })
}

function startEdit() {
  resetProfileForm()
  clearErrors()
  editMode.value = true
}

async function saveProfile() {
  clearErrors()
  const result = profileSchema.safeParse(profileForm)
  if (!result.success) {
    applyIssues(result.error)
    return
  }
  saving.value = true
  try {
    const updated = await request<ProfileResponse>('/api/profile', { method: 'PUT', body: profileForm })
    profile.value = updated
    updateSession({ username: updated.username, avatarUrl: updated.avatarUrl })
    editMode.value = false
    toast.add({ severity: 'success', summary: 'Cập nhật thành công', life: 4000 })
  } catch (e: any) {
    toast.add({ severity: 'error', summary: e?.data?.message || 'Cập nhật thất bại', life: 4000 })
  } finally {
    saving.value = false
  }
}

function resetAddressForm() {
  addressForm.value = { label: '', street: '', ward: '', district: '', city: '', receiverName: '', receiverPhone: '', isDefault: false }
  editingAddressId.value = null
  clearErrors()
}

function openCreateAddress() {
  resetAddressForm()
  showAddressForm.value = true
}

function openEditAddress(a: Address) {
  editingAddressId.value = a.id
  Object.assign(addressForm.value, {
    label: a.label, street: a.street, ward: a.ward, district: a.district, city: a.city,
    receiverName: a.receiverName, receiverPhone: a.receiverPhone, isDefault: a.isDefault
  })
  clearErrors()
  showAddressForm.value = true
}

async function saveAddress() {
  clearErrors()
  const result = addressSchema.safeParse(addressForm.value)
  if (!result.success) {
    applyIssues(result.error)
    return
  }
  savingAddress.value = true
  const payload = addressForm.value
  try {
    if (editingAddressId.value) {
      await request(`/api/addresses/${editingAddressId.value}`, { method: 'PUT', body: payload })
      toast.add({ severity: 'success', summary: 'Đã cập nhật địa chỉ', life: 4000 })
    } else {
      await request('/api/addresses', { method: 'POST', body: payload })
      toast.add({ severity: 'success', summary: 'Đã thêm địa chỉ', life: 4000 })
    }
    showAddressForm.value = false
    await load()
  } catch (e: any) {
    toast.add({ severity: 'error', summary: e?.data?.message || 'Không thể lưu địa chỉ', life: 4000 })
  } finally {
    savingAddress.value = false
  }
}

async function deleteAddress(a: Address) {
  if (addressUIBusy.value.has(a.id)) return
  if (!await confirm(`Xóa địa chỉ "${a.label}"?`, 'Xóa địa chỉ')) return
  addressUIBusy.value.add(a.id)
  try {
    await request(`/api/addresses/${a.id}`, { method: 'DELETE' })
    toast.add({ severity: 'success', summary: 'Đã xóa địa chỉ', life: 4000 })
    await load()
  } catch (e: any) {
    toast.add({ severity: 'error', summary: e?.data?.message || 'Không thể xóa địa chỉ', life: 4000 })
  } finally {
    addressUIBusy.value.delete(a.id)
  }
}

async function setDefaultAddress(a: Address) {
  if (addressUIBusy.value.has(a.id)) return
  addressUIBusy.value.add(a.id)
  try {
    await request(`/api/addresses/${a.id}/default`, { method: 'PATCH' })
    toast.add({ severity: 'success', summary: 'Đã đặt làm mặc định', life: 4000 })
    await load()
  } catch (e: any) {
    toast.add({ severity: 'error', summary: e?.data?.message || 'Không thể đặt mặc định', life: 4000 })
  } finally {
    addressUIBusy.value.delete(a.id)
  }
}

onMounted(load)
</script>

<template>
  <div class="mx-auto max-w-5xl px-4 py-8 sm:px-6">
    <!-- Loading skeleton -->
    <div v-if="loading" class="mt-8 grid gap-8 lg:grid-cols-3">
      <div class="lg:col-span-2 space-y-4">
        <NSkeleton class="h-52 rounded-2xl" />
      </div>
      <div class="space-y-4">
        <NSkeleton class="h-36 rounded-2xl" />
        <NSkeleton class="h-48 rounded-2xl" />
      </div>
    </div>

    <div v-else class="mt-8 grid gap-8 lg:grid-cols-3">
      <!-- Profile -->
      <div class="lg:col-span-2 space-y-6">
        <div class="rounded-2xl border border-emerald-100 bg-white p-6">
          <div class="flex items-center justify-between">
            <h2 class="text-lg font-bold text-gray-700">Thông tin cá nhân</h2>
            <NButton v-if="!editMode" quaternary type="primary" size="small" @click="startEdit">
              <template #icon><UiIcon name="pencil" size="16" /></template>
              Chỉnh sửa
            </NButton>
          </div>

          <form v-if="editMode" class="mt-4 space-y-3" @submit.prevent="saveProfile">
            <div class="flex items-center gap-4">
              <NAvatar :src="profileForm.avatarUrl || undefined" :size="48">{{ profileForm.username?.charAt(0)?.toUpperCase() || '?' }}</NAvatar>
              <div class="flex-1 space-y-1">
                <NInput v-model:value="profileForm.avatarUrl" placeholder="https://..." />
                <p class="text-xs text-gray-400">Dán URL ảnh đại diện để xem trước.</p>
              </div>
            </div>
            <div>
              <NInput v-model:value="profileForm.username" placeholder="Tên đăng nhập" />
              <p v-if="errors.username" class="text-xs text-red-600">{{ errors.username }}</p>
            </div>
            <div>
              <NInput v-model:value="profileForm.numberPhone" placeholder="Số điện thoại" />
              <p v-if="errors.numberPhone" class="text-xs text-red-600">{{ errors.numberPhone }}</p>
            </div>
            <hr class="border-emerald-100" />
            <p class="text-sm font-semibold text-gray-500">Đổi mật khẩu</p>
            <div>
              <NInput v-model:value="profileForm.currentPassword" type="password" placeholder="(để trống nếu không đổi)" />
            </div>
            <div>
              <NInput v-model:value="profileForm.newPassword" type="password" placeholder="(để trống nếu không đổi)" />
              <p v-if="errors.newPassword" class="text-xs text-red-600">{{ errors.newPassword }}</p>
            </div>
            <div class="flex justify-end gap-2 pt-2">
              <NButton quaternary @click="editMode = false">Hủy</NButton>
              <NButton type="primary" attr-type="submit" :loading="saving">Lưu</NButton>
            </div>
          </form>

          <div v-else-if="profile" class="mt-4">
            <div class="flex items-center gap-4">
              <NAvatar :src="profile.avatarUrl ?? undefined" :size="48">{{ profile.username?.charAt(0)?.toUpperCase() || '?' }}</NAvatar>
              <div>
                <div class="flex items-center gap-2">
                  <p class="text-lg font-bold text-gray-700">{{ profile.username }}</p>
                  <span
                    class="rounded-full px-2.5 py-0.5 text-xs font-semibold"
                    :class="isAdmin ? 'bg-slate-100 text-slate-700' : 'bg-emerald-100 text-emerald-700'"
                  >{{ isAdmin ? 'Quản trị' : 'Khách hàng' }}</span>
                </div>
                <div class="mt-1 space-y-0.5 text-sm text-gray-400">
                  <p class="flex items-center gap-1.5"><UiIcon name="envelope" size="16" /> {{ profile.email }}</p>
                  <p v-if="profile.numberPhone" class="flex items-center gap-1.5"><UiIcon name="phone" size="16" /> {{ profile.numberPhone }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Addresses -->
        <div class="rounded-2xl border border-emerald-100 bg-white p-6">
          <div class="flex items-center justify-between">
            <h2 class="text-lg font-bold text-gray-700">Địa chỉ của tôi</h2>
            <NButton quaternary type="primary" size="small" @click="openCreateAddress">
              <template #icon><UiIcon name="plus" size="16" /></template>
              Thêm địa chỉ
            </NButton>
          </div>

          <AddressForm
            v-if="showAddressForm"
            v-model="addressForm"
            :errors="errors"
            :saving="savingAddress"
            @submit="saveAddress"
            @cancel="showAddressForm = false; resetAddressForm()"
          />

          <div v-else-if="addresses.length" class="mt-4 space-y-3">
            <AddressCard
              v-for="a in addresses"
              :key="a.id"
              :address="a"
              editable
              @edit="openEditAddress"
              @delete="deleteAddress"
              @set-default="setDefaultAddress"
            />
          </div>
          <div v-else class="mt-4 rounded-xl border border-dashed border-emerald-200 bg-emerald-50/40 p-6 text-center">
            <UiIcon name="map-marker" size="32" class="mx-auto mb-2 block text-emerald-300" />
            <p class="text-sm text-gray-500">Chưa có địa chỉ. Thêm địa chỉ để tiết kiệm thời gian khi đặt hàng.</p>
          </div>
        </div>
      </div>

      <!-- Sidebar -->
      <div class="space-y-6">
        <div class="rounded-2xl border border-emerald-100 bg-white p-6">
          <h3 class="font-semibold text-gray-700">Đường dẫn</h3>
          <div class="mt-3 space-y-2">
            <template v-if="!isAdmin">
              <RouterLink to="/orders" class="flex items-center gap-2 rounded-lg px-3 py-2 text-sm text-gray-600 hover:bg-emerald-50 hover:text-emerald-700">
                <UiIcon name="receipt" size="16" /> Đơn hàng
              </RouterLink>
              <RouterLink to="/checkout" class="flex items-center gap-2 rounded-lg px-3 py-2 text-sm text-gray-600 hover:bg-emerald-50 hover:text-emerald-700">
                <UiIcon name="shopping-cart" size="16" /> Giỏ hàng
              </RouterLink>
            </template>
            <RouterLink v-if="isAdmin" to="/admin/products" class="flex items-center gap-2 rounded-lg px-3 py-2 text-sm text-gray-600 hover:bg-emerald-50 hover:text-emerald-700">
              <UiIcon name="th-large" size="16" /> Quản trị
            </RouterLink>
            <NButton quaternary type="error" block @click="logout">
              <template #icon><UiIcon name="sign-out" size="16" /></template>
              Đăng xuất
            </NButton>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
