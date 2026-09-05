<script setup lang="ts">
import type { ChatResponse, ChatSession, ChatMessage } from '~/types'

definePageMeta({ middleware: 'customer' })

const { request } = useApi()
const { formatDate } = useFormat()
const toast = useToast()

const sessions = ref<ChatSession[]>([])
const currentSessionId = ref<number | null>(null)
const messages = ref<ChatMessage[]>([])
const sending = ref(false)

async function loadSessions() {
  try {
    sessions.value = await request<ChatSession[]>('/api/chat/sessions')
  } catch {
    toast.add({ title: 'Không thể tải cuộc trò chuyện', color: 'error' })
  }
}

async function send(msg: string) {
  sending.value = true
  try {
    const res = await request<ChatResponse>('/api/chat/send', {
      method: 'POST',
      body: { message: msg, sessionId: currentSessionId.value }
    })
    messages.value = res.messages
    currentSessionId.value = res.sessionId
    await loadSessions()
  } catch {
    messages.value.push({ id: Date.now(), role: 'BOT', content: 'Đã có lỗi xảy ra, vui lòng thử lại sau.', createdAt: new Date().toISOString() })
  } finally {
    sending.value = false
  }
}

function newSession() {
  currentSessionId.value = null
  messages.value = []
}

onMounted(loadSessions)
</script>

<template>
  <div class="mx-auto max-w-6xl px-4 py-8 sm:px-6">
    <h1 class="mb-6 text-3xl font-extrabold text-gray-800">Chat hỗ trợ</h1>

    <div class="grid gap-6 lg:grid-cols-4">
      <!-- Sessions sidebar -->
      <div class="rounded-2xl border border-emerald-100 bg-white p-4 lg:col-span-1">
        <UButton color="primary" size="md" block icon="i-ph-plus" label="Cuộc trò chuyện mới" @click="newSession" class="mb-3" />
        <div class="space-y-2">
          <button
            v-for="s in sessions"
            :key="s.id"
            class="w-full rounded-xl p-3 text-left transition"
            :class="currentSessionId === s.id ? 'bg-emerald-50 ring-1 ring-emerald-300' : 'hover:bg-gray-50'"
            @click="currentSessionId = s.id; messages = s.messages"
          >
            <p class="line-clamp-1 text-sm font-medium text-gray-800">{{ s.title }}</p>
            <p class="text-xs text-gray-400">{{ formatDate(s.createdAt) }}</p>
          </button>
          <p v-if="!sessions.length" class="py-4 text-center text-xs text-gray-400">Chưa có cuộc trò chuyện.</p>
        </div>
      </div>

      <!-- Chat area -->
      <div class="flex h-[600px] flex-col rounded-2xl border border-emerald-100 bg-white lg:col-span-3">
        <ChatThread :messages="messages" :sending="sending" @send="send" />
      </div>
    </div>
  </div>
</template>
