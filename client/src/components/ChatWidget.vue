<script setup lang="ts">
import { computed, nextTick, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { NButton, NInput } from 'naive-ui'
import UiIcon from '@/components/UiIcon.vue'
import { useChat } from '@/composables/useChat'
import { useToast } from '@/composables/useToast'
import type { ChatMessage } from '@/types'

const SUGGESTIONS = ['Phí giao hàng là bao nhiêu?', 'EcoMart có sản phẩm mới không?', 'Hướng dẫn thanh toán']

const SOURCE_LABELS: Record<string, string> = {
  local: 'Trợ lý EcoMart',
  bm25: 'Kiến thức EcoMart',
  gemini: 'AI nâng cao',
  default: 'Hỗ trợ EcoMart'
}

const route = useRoute()
const { send } = useChat()
const toast = useToast()

const open = ref(false)
const isAdminRoute = computed(() => route.path.startsWith('/admin'))
const messages = ref<ChatMessage[]>([
  { role: 'assistant', content: 'Chào bạn! Mình là trợ lý ảo của EcoMart. Hỏi mình về sản phẩm, phí giao hàng hay thanh toán nhé.' }
])
const input = ref('')
const streaming = ref(false)
const source = ref<string | null>(null)
const controller = ref<AbortController | null>(null)
const listRef = ref<HTMLDivElement | null>(null)

watch(messages, async () => {
  await nextTick()
  listRef.value?.scrollTo({ top: listRef.value.scrollHeight })
}, { deep: true })

const scrollToBottom = async () => {
  await nextTick()
  listRef.value?.scrollTo({ top: listRef.value.scrollHeight })
}

const sendMessage = async (raw?: string) => {
  const text = (raw ?? input.value).trim()
  if (!text || streaming.value) return
  input.value = ''
  source.value = null

  messages.value.push({ role: 'user', content: text })
  const draft: ChatMessage = { role: 'assistant', content: '' }
  messages.value.push(draft)
  scrollToBottom()

  const ctrl = new AbortController()
  controller.value = ctrl
  streaming.value = true
  try {
    await send(
      messages.value.slice(0, -1),
      {
        onText: (chunk) => { draft.content += chunk },
        onSource: (value) => { source.value = value },
        onDone: () => { draft.content = draft.content.trim() }
      },
      ctrl.signal
    )
  } catch (error: any) {
    if (error?.name !== 'AbortError') {
      if (!draft.content) draft.content = 'Xin lỗi, mình chưa thể kết nối ngay. Bạn thử lại sau nhé.'
      toast.add({ severity: 'error', summary: 'Không thể liên hệ trợ lý EcoMart', life: 4000 })
    } else {
      draft.content = draft.content.trim()
    }
  } finally {
    streaming.value = false
    controller.value = null
  }
}

const close = () => {
  controller.value?.abort()
  open.value = false
}
</script>

<template>
  <div v-if="!isAdminRoute" class="fixed bottom-5 right-5 z-40 flex flex-col items-end gap-3">
    <Transition name="chat-pop">
      <div
        v-if="open"
        class="flex h-[480px] w-[360px] max-w-[calc(100vw-2.5rem)] flex-col overflow-hidden rounded-2xl border border-emerald-100 bg-white shadow-2xl shadow-emerald-900/10"
      >
        <div class="flex items-center gap-3 bg-emerald-600 px-4 py-3">
          <div class="flex h-9 w-9 items-center justify-center rounded-full bg-white/20">
            <UiIcon name="leaf" color="#fff" />
          </div>
          <div class="flex-1">
            <p class="text-sm font-semibold text-white">Trợ lý EcoMart</p>
            <p class="text-xs text-emerald-100">Siêu thị xanh · hỗ trợ 24/7</p>
          </div>
          <button aria-label="Đóng trợ lý EcoMart" class="rounded-full p-1 text-emerald-100 transition hover:bg-white/10 hover:text-white" @click="close">
            <UiIcon name="times" />
          </button>
        </div>

        <div ref="listRef" class="flex-1 space-y-3 overflow-y-auto bg-gray-50/60 p-4">
          <div v-for="(m, index) in messages" :key="index" class="flex" :class="m.role === 'user' ? 'justify-end' : 'justify-start'">
            <div class="max-w-[85%]">
              <div
                class="rounded-2xl px-3 py-2 text-sm whitespace-pre-wrap"
                :class="m.role === 'user' ? 'rounded-br-sm bg-emerald-600 text-white' : 'rounded-bl-sm bg-white text-gray-700 shadow-sm'"
              >
                <span v-if="m.content">{{ m.content }}</span>
                <span v-else-if="streaming && m.role === 'assistant' && index === messages.length - 1" class="inline-block h-4 w-4 animate-spin rounded-full border-2 border-emerald-600 border-t-transparent align-middle" />
              </div>
              <p v-if="m.role === 'assistant' && index === messages.length - 1 && source" class="mt-1 text-right text-[11px] text-gray-400">
                {{ SOURCE_LABELS[source] ?? source }}
              </p>
            </div>
          </div>

          <div v-if="!streaming" class="flex flex-wrap gap-2">
            <button
              v-for="s in SUGGESTIONS"
              :key="s"
              class="rounded-full border border-emerald-200 bg-white px-3 py-1.5 text-xs text-emerald-700 transition hover:bg-emerald-50"
              @click="sendMessage(s)"
            >
              {{ s }}
            </button>
          </div>
        </div>

        <div class="border-t border-gray-100 bg-white p-3">
          <div class="flex items-center gap-2">
            <NInput
              v-model:value="input"
              class="flex-1"
              round
              placeholder="Hỏi mình điều gì đó..."
              :disabled="streaming"
              @keyup.enter.prevent="sendMessage()"
            />
            <NButton round type="primary" :disabled="!input.trim() || streaming" @click="sendMessage()">
              <template #icon><UiIcon name="send" /></template>
            </NButton>
          </div>
        </div>
      </div>
    </Transition>

    <NButton round circle type="primary" size="large" aria-label="Mở trợ lý EcoMart" class="shadow-lg shadow-emerald-900/20" @click="open = !open">
      <template #icon><UiIcon :name="open ? 'times' : 'chat'" size="24" /></template>
    </NButton>
  </div>
</template>

<style scoped>
.chat-pop-enter-active,
.chat-pop-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}
.chat-pop-enter-from,
.chat-pop-leave-to {
  opacity: 0;
  transform: translateY(12px) scale(0.97);
}
</style>