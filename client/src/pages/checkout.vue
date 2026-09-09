<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from '@/composables/useToast'
import type { Address, CheckoutResult } from '@/types'
import { addressSchema, type AddressForm as AddressFormData } from '@/schemas'
import { useApi } from '@/composables/useApi'
import { useCart } from '@/composables/useCart'
import { useFormErrors } from '@/composables/useFormErrors'
import { useFormat } from '@/composables/useFormat'
import AddressForm from '@/components/AddressForm.vue'
import AddressCard from '@/components/AddressCard.vue'
import OrderSummaryCard from '@/components/OrderSummaryCard.vue'
import UiIcon from '@/components/UiIcon.vue'
import { NButton, NInput } from 'naive-ui'

const router = useRouter()
const { request } = useApi()
const { cart, fetchCart, reset } = useCart()
const { formatVND } = useFormat()
const { errors: addressErrors, applyIssues, clearErrors } = useFormErrors()
const toast = useToast()

const addresses = ref<Address[]>([])
const selectedAddressId = ref<number | null>(null)
const paymentMethod = ref<'PAYOS' | 'COD'>('COD')
const notes = ref('')
const loading = ref(false)
const showAddressForm = ref(false)
const savingAddress = ref(false)

const addressForm = ref<AddressFormData>({
  label: '', street: '', ward: '', district: '', city: '', receiverName: '', receiverPhone: '', isDefault: false
})

async function loadAddresses() {
  try {
    addresses.value = await request<Address[]>('/api/addresses')
    const def = addresses.value.find(a => a.isDefault)
    const first = addresses.value[0]
    selectedAddressId.value = def?.id ?? first?.id ?? null
  } catch (error: any) {
    toast.add({ severity: 'error', summary: error?.data?.message || 'Không thể tải địa chỉ', life: 4000 })
  }
}

async function loadCart() {
  try {
    await fetchCart()
  } catch {
    toast.add({ severity: 'error', summary: 'Không thể tải giỏ hàng', life: 4000 })
  }
}

function resetAddressForm() {
  addressForm.value = { label: '', street: '', ward: '', district: '', city: '', receiverName: '', receiverPhone: '', isDefault: false }
  clearErrors()
}

async function createAddress() {
  clearErrors()
  const result = addressSchema.safeParse(addressForm.value)
  if (!result.success) {
    applyIssues(result.error)
    return
  }
  savingAddress.value = true
  try {
    const created = await request<Address>('/api/addresses', { method: 'POST', body: addressForm.value })
    await loadAddresses()
    if (created.isDefault) await loadCart()
    showAddressForm.value = false
    resetAddressForm()
  } catch (error: any) {
    toast.add({ severity: 'error', summary: error?.data?.message || 'Không thể lưu địa chỉ', life: 4000 })
  } finally {
    savingAddress.value = false
  }
}

async function checkout() {
  if (!selectedAddressId.value) {
    toast.add({ severity: 'warn', summary: 'Vui lòng chọn địa chỉ giao hàng', life: 4000 })
    return
  }
  loading.value = true
  try {
    const result = await request<CheckoutResult>('/api/orders/checkout', {
      method: 'POST',
      body: { addressId: selectedAddressId.value, paymentMethod: paymentMethod.value, notes: notes.value }
    })
    if (result.payosCheckoutUrl) {
      window.open(result.payosCheckoutUrl, '_blank')
    }
    await reset()
    toast.add({ severity: 'success', summary: result.message || 'Đặt hàng thành công!', life: 4000 })
    router.push(`/orders/${result.orderId}`)
  } catch (e: any) {
    toast.add({ severity: 'error', summary: e?.data?.message || 'Đặt hàng thất bại', life: 4000 })
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadAddresses()
  loadCart()
})
</script>

<template>
  <div class="mx-auto max-w-7xl px-4 py-8 sm:px-6">
    <h1 class="text-3xl font-extrabold text-gray-700">Thanh toán</h1>

    <div class="mt-8 grid gap-8 lg:grid-cols-3">
      <div class="lg:col-span-2 space-y-6">
        <!-- Address -->
        <section class="rounded-2xl border border-emerald-100 bg-white p-6">
          <div class="flex items-center justify-between">
            <h2 class="text-lg font-bold text-gray-700">Địa chỉ giao hàng</h2>
            <NButton quaternary type="primary" size="small" @click="showAddressForm = !showAddressForm">
              <template #icon><UiIcon name="plus" size="16" /></template>
              Thêm mới
            </NButton>
          </div>

          <AddressForm
            v-if="showAddressForm"
            v-model="addressForm"
            :errors="addressErrors"
            :saving="savingAddress"
            @submit="createAddress"
            @cancel="showAddressForm = false; resetAddressForm()"
          />

          <div v-else class="mt-4 space-y-3">
            <AddressCard
              v-for="a in addresses"
              :key="a.id"
              :address="a"
              selectable
              :selected="selectedAddressId === a.id"
              @select="selectedAddressId = $event"
            />
            <p v-if="!addresses.length" class="py-4 text-center text-gray-400">Chưa có địa chỉ nào. Vui lòng thêm địa chỉ giao hàng.</p>
          </div>
        </section>

        <!-- Payment -->
        <section class="rounded-2xl border border-emerald-100 bg-white p-6">
          <h2 class="text-lg font-bold text-gray-700">Phương thức thanh toán</h2>
          <div class="mt-4 grid gap-3 sm:grid-cols-2">
            <label class="flex items-center gap-3 rounded-xl border p-4 transition" :class="paymentMethod === 'COD' ? 'border-emerald-500 bg-emerald-50' : 'border-gray-200'" >
              <input type="radio" v-model="paymentMethod" value="COD" class="accent-emerald-600" />
              <span class="grid h-9 w-9 place-items-center rounded-lg bg-emerald-100 text-emerald-700"><UiIcon name="money-bill" size="18" /></span>
              <div>
                <p class="font-semibold text-gray-700">COD</p>
                <p class="text-xs text-gray-400">Thanh toán khi nhận hàng</p>
              </div>
            </label>
            <label class="flex items-center gap-3 rounded-xl border p-4 transition" :class="paymentMethod === 'PAYOS' ? 'border-emerald-500 bg-emerald-50' : 'border-gray-200'" >
              <input type="radio" v-model="paymentMethod" value="PAYOS" class="accent-emerald-600" />
              <span class="grid h-9 w-9 place-items-center rounded-lg bg-emerald-100 text-emerald-700"><UiIcon name="qrcode" size="18" /></span>
              <div>
                <p class="font-semibold text-gray-700">PayOS QR</p>
                <p class="text-xs text-gray-400">Quét mã chuyển khoản</p>
              </div>
            </label>
          </div>
        </section>
      </div>

      <!-- Summary -->
      <OrderSummaryCard
        v-if="cart"
        :subtotal="cart.subtotal"
        :item-count="cart.itemCount"
        :title="`Đơn hàng (${cart.itemCount} món)`"
      >
        <template #items>
          <div class="max-h-64 space-y-3 overflow-auto">
            <div v-for="item in cart.items" :key="item.productId" class="flex justify-between text-sm">
              <span class="text-gray-600">{{ item.productName }} <span class="text-gray-400">× {{ item.quantity }}</span></span>
              <span class="font-medium text-gray-700">{{ formatVND(item.price * item.quantity) }}</span>
            </div>
          </div>
        </template>
        <template #actions>
          <NInput type="textarea" v-model:value="notes" placeholder="Ghi chú cho đơn hàng (tùy chọn)..." :rows="2" />
          <NButton type="primary" size="large" block class="mt-4" :loading="loading" @click="checkout">
            <template #icon><UiIcon name="check-circle" size="18" /></template>
            Đặt hàng
          </NButton>
        </template>
      </OrderSummaryCard>
    </div>
  </div>
</template>
