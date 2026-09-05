<script setup lang="ts">
import type { ChatMessage, ChatResponse } from '~/types'

const { isLoggedIn } = useAuth()
const { request } = useApi()

const open = ref(false)
const messages = ref<ChatMessage[]>([])
const sessionId = ref<number | null>(null)
const input = ref('')
const sending = ref(false)
const greeting = 'Xin chào! Tôi là EcoBot. Bạn cần hỗ trợ gì hôm nay?'

function toggle() {
  open.value = !open.value
  if (open.value && !messages.value.length) {
    messages.value = [{ id: -1, role: 'BOT', content: greeting, createdAt: new Date().toISOString() }]
  }
}

async function send() {
  const msg = input.value.trim()
  if (!msg || sending.value) return
  sending.value = true
  try {
    const res = await request<ChatResponse>('/api/chat/send', {
      method: 'POST',
      body: { message: msg, sessionId: sessionId.value }
    })
    messages.value = res.messages
    sessionId.value = res.sessionId
    input.value = ''
  } catch {
    messages.value.push({ id: Date.now(), role: 'BOT', content: 'Đã có lỗi xảy ra, vui lòng thử lại sau.', createdAt: new Date().toISOString() })
  } finally {
    sending.value = false
  }
}

function goLogin() {
  navigateTo('/login')
}
</script>

<template>
  <Teleport to="body">
    <div class="fixed bottom-5 right-5 z-50 flex flex-col items-end gap-3">
      <transition name="chat-pop">
        <div v-if="open" class="flex h-[28rem] w-[calc(100vw-2.5rem)] max-w-sm flex-col overflow-hidden rounded-2xl border border-emerald-100 bg-white shadow-2xl shadow-emerald-900/10">
          <div class="flex items-center justify-between border-b border-emerald-100 bg-emerald-600 px-4 py-3">
            <div class="flex items-center gap-2 text-white">
              <span class="grid h-8 w-8 place-items-center rounded-full bg-white/20">
                <UIcon name="i-ph-chats-circle" class="h-4 w-4" />
              </span>
              <div class="leading-tight">
                <p class="text-sm font-semibold">EcoBot</p>
                <p class="text-[11px] text-emerald-100">Hỗ trợ tư vấn</p>
              </div>
            </div>
            <UButton color="neutral" variant="ghost" square icon="i-ph-x" class="text-white! hover:text-black!" @click="open = false" aria-label="Đóng chat" />
          </div>

          <div v-if="!isLoggedIn" class="flex flex-1 flex-col items-center justify-center gap-3 p-6 text-center">
            <UIcon name="i-ph-lock-key" class="h-10 w-10 text-emerald-300" />
            <p class="text-sm text-gray-500">Đăng nhập để trò chuyện cùng EcoBot</p>
            <UButton color="primary" size="sm" label="Đăng nhập" @click="goLogin" />
          </div>

          <template v-else>
            <div class="flex-1 space-y-3 overflow-y-auto p-4">
              <div
                v-for="m in messages"
                :key="m.id"
                class="flex"
                :class="m.role === 'USER' ? 'justify-end' : 'justify-start'"
              >
                <div
                  class="max-w-[80%] whitespace-pre-wrap rounded-2xl px-3 py-2 text-sm leading-relaxed"
                  :class="m.role === 'USER' ? 'bg-emerald-600 text-white' : 'bg-gray-100 text-gray-800'"
                >
                  {{ m.content }}
                </div>
              </div>
            </div>

            <form class="border-t border-emerald-100 p-3" @submit.prevent="send">
              <div class="flex gap-2">
                <UInput v-model="input" placeholder="Nhập tin nhắn..." size="md" class="flex-1" />
                <UButton type="submit" color="primary" icon="i-ph-paper-plane-tilt" :disabled="!input.trim() || sending" :loading="sending" aria-label="Gửi tin nhắn" />
              </div>
            </form>
          </template>
        </div>
      </transition>

      <button
        type="button"
        class="grid h-14 w-14 place-items-center rounded-full bg-emerald-600 text-white shadow-lg shadow-emerald-900/25"
        :aria-label="open ? 'Đóng chat' : 'Mở chat'"
        @click="toggle"
      >
        <UIcon :name="open ? 'i-ph-x' : 'i-ph-chats-circle'" class="h-6 w-6" />
      </button>
    </div>
  </Teleport>
</template>

<style scoped>
.chat-pop-enter-active,
.chat-pop-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}
.chat-pop-enter-from,
.chat-pop-leave-to {
  opacity: 0;
  transform: translateY(12px);
}
</style>
