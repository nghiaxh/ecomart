<script setup lang="ts">
import type { Address, CheckoutResult } from '~/types'
import { addressSchema, type AddressForm } from '~/schemas'

definePageMeta({ middleware: 'customer' })

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

const addressForm = ref<AddressForm>({
  label: '', street: '', ward: '', district: '', city: '', receiverName: '', receiverPhone: '', isDefault: false
})

async function loadAddresses() {
  try {
    addresses.value = await request<Address[]>('/api/addresses')
    const def = addresses.value.find(a => a.isDefault)
    const first = addresses.value[0]
    selectedAddressId.value = def?.id ?? first?.id ?? null
  } catch (error: any) {
    toast.add({ title: error?.data?.message || 'Không thể tải địa chỉ', color: 'error' })
  }
}

async function loadCart() {
  try {
    await fetchCart()
  } catch {
    toast.add({ title: 'Không thể tải giỏ hàng', color: 'error' })
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
    toast.add({ title: error?.data?.message || 'Không thể lưu địa chỉ', color: 'error' })
  } finally {
    savingAddress.value = false
  }
}

async function checkout() {
  if (!selectedAddressId.value) {
    toast.add({ title: 'Vui lòng chọn địa chỉ giao hàng', color: 'warning' })
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
    toast.add({ title: result.message || 'Đặt hàng thành công!', color: 'success' })
    navigateTo(`/orders/${result.orderId}`)
  } catch (e: any) {
    toast.add({ title: e?.data?.message || 'Đặt hàng thất bại', color: 'error' })
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
    <h1 class="text-3xl font-extrabold text-gray-800">Thanh toán</h1>

    <div class="mt-8 grid gap-8 lg:grid-cols-3">
      <div class="lg:col-span-2 space-y-6">
        <!-- Address -->
        <section class="rounded-2xl border border-emerald-100 bg-white p-6">
          <div class="flex items-center justify-between">
            <h2 class="text-lg font-bold text-gray-800">Địa chỉ giao hàng</h2>
            <UButton color="primary" variant="soft" size="md" icon="i-ph-plus" label="Thêm mới" @click="showAddressForm = !showAddressForm" />
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
          <h2 class="text-lg font-bold text-gray-800">Phương thức thanh toán</h2>
          <div class="mt-4 grid gap-3 sm:grid-cols-2">
            <label class="flex items-center gap-3 rounded-xl border p-4 transition" :class="paymentMethod === 'COD' ? 'border-emerald-500 bg-emerald-50' : 'border-gray-200'" >
              <input type="radio" v-model="paymentMethod" value="COD" class="accent-emerald-600" />
              <span class="grid h-9 w-9 place-items-center rounded-lg bg-emerald-100 text-emerald-700"><UIcon name="i-ph-money" class="h-5 w-5" /></span>
              <div>
                <p class="font-semibold text-gray-800">COD</p>
                <p class="text-xs text-gray-400">Thanh toán khi nhận hàng</p>
              </div>
            </label>
            <label class="flex items-center gap-3 rounded-xl border p-4 transition" :class="paymentMethod === 'PAYOS' ? 'border-emerald-500 bg-emerald-50' : 'border-gray-200'" >
              <input type="radio" v-model="paymentMethod" value="PAYOS" class="accent-emerald-600" />
              <span class="grid h-9 w-9 place-items-center rounded-lg bg-emerald-100 text-emerald-700"><UIcon name="i-ph-qr-code" class="h-5 w-5" /></span>
              <div>
                <p class="font-semibold text-gray-800">PayOS QR</p>
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
          <UTextarea v-model="notes" placeholder="Ghi chú cho đơn hàng (tùy chọn)..." :rows="2" />
          <UButton color="primary" size="lg" block class="mt-4" :loading="loading" label="Đặt hàng" icon="i-ph-check-circle" @click="checkout" />
        </template>
      </OrderSummaryCard>
    </div>
  </div>
</template>
