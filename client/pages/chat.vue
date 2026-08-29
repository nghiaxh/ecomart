<script setup lang="ts">
import type { ChatResponse, ChatSession, ChatMessage } from '~/types'

definePageMeta({ middleware: 'auth' })

const { request } = useApi()
const { formatDate } = useFormat()

const sessions = ref<ChatSession[]>([])
const currentSessionId = ref<number | null>(null)
const messages = ref<ChatMessage[]>([])
const input = ref('')
const sending = ref(false)

async function loadSessions() {
  sessions.value = await request<ChatSession[]>('/api/chat/sessions')
}

async function send() {
  const msg = input.value.trim()
  if (!msg) return
  sending.value = true
  try {
    const res = await request<ChatResponse>('/api/chat/send', {
      method: 'POST',
      body: { message: msg, sessionId: currentSessionId.value }
    })
    messages.value = res.messages
    currentSessionId.value = res.sessionId
    await loadSessions()
  } finally {
    input.value = ''
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
      <div class="rounded-2xl border border-green-100 bg-white p-4 lg:col-span-1">
        <UButton color="green" size="sm" block icon="i-heroicons-plus" label="Cuộc trò chuyện mới" @click="newSession" class="mb-3" />
        <div class="space-y-2">
          <button
            v-for="s in sessions"
            :key="s.id"
            class="w-full rounded-xl p-3 text-left transition"
            :class="currentSessionId === s.id ? 'bg-green-50 ring-1 ring-green-300' : 'hover:bg-gray-50'"
            @click="currentSessionId = s.id; messages = s.messages"
          >
            <p class="line-clamp-1 text-sm font-medium text-gray-800">{{ s.title }}</p>
            <p class="text-xs text-gray-400">{{ formatDate(s.createdAt) }}</p>
          </button>
          <p v-if="!sessions.length" class="py-4 text-center text-xs text-gray-400">Chưa có cuộc trò chuyện.</p>
        </div>
      </div>

      <!-- Chat area -->
      <div class="flex h-[600px] flex-col rounded-2xl border border-green-100 bg-white lg:col-span-3">
        <div class="flex-1 overflow-y-auto p-6 space-y-4">
          <div v-if="!messages.length" class="grid h-full place-items-center text-gray-300">
            <div class="text-center">
              <UIcon name="i-heroicons-chat-bubble-left-right" class="mx-auto h-12 w-12" />
              <p class="mt-2 text-sm">Xin chào! Bạn cần hỗ trợ gì?</p>
            </div>
          </div>
          <div
            v-for="m in messages"
            :key="m.id"
            class="flex"
            :class="m.role === 'USER' ? 'justify-end' : 'justify-start'"
          >
            <div
              class="max-w-[70%] rounded-2xl px-4 py-3"
              :class="m.role === 'USER' ? 'bg-green-600 text-white' : 'bg-gray-100 text-gray-800'"
            >
              <p class="whitespace-pre-wrap text-sm leading-relaxed">{{ m.content }}</p>
            </div>
          </div>
        </div>

        <div class="border-t border-green-100 p-4">
          <form class="flex gap-2" @submit.prevent="send">
            <UInput v-model="input" placeholder="Nhập tin nhắn..." class="flex-1" size="lg" />
            <UButton type="submit" color="green" icon="i-heroicons-paper-airplane" :disabled="!input.trim()" :loading="sending" />
          </form>
        </div>
      </div>
    </div>
  </div>
</template>