<script setup lang="ts">
import type { ChatMessage } from '~/types'

const props = withDefaults(
  defineProps<{
    messages: ChatMessage[]
    sending?: boolean
    emptyHint?: string
    compact?: boolean
  }>(),
  { sending: false, emptyHint: 'Xin chào! Bạn cần hỗ trợ gì?', compact: false }
)

const emit = defineEmits<{ send: [message: string] }>()

const input = ref('')

function submit() {
  const msg = input.value.trim()
  if (!msg || props.sending) return
  emit('send', msg)
  input.value = ''
}
</script>

<template>
  <div class="flex min-h-0 flex-1 flex-col">
    <div class="min-h-0 flex-1 space-y-4 overflow-y-auto" :class="compact ? 'p-4' : 'p-6'">
      <div v-if="!messages.length" class="grid h-full place-items-center text-gray-300">
        <div class="text-center">
          <UIcon name="i-ph-chats-circle" class="mx-auto h-12 w-12" />
          <p class="mt-2 text-sm">{{ emptyHint }}</p>
        </div>
      </div>
      <div
        v-for="m in messages"
        :key="m.id"
        class="flex"
        :class="m.role === 'USER' ? 'justify-end' : 'justify-start'"
      >
        <div
          class="whitespace-pre-wrap rounded-2xl leading-relaxed"
          :class="[
            compact ? 'max-w-[80%] px-3 py-2 text-sm' : 'max-w-[70%] px-4 py-3 text-sm',
            m.role === 'USER' ? 'bg-emerald-600 text-white' : 'bg-gray-100 text-gray-800'
          ]"
        >
          {{ m.content }}
        </div>
      </div>
    </div>

    <form class="border-t border-emerald-100" :class="compact ? 'p-3' : 'p-4'" @submit.prevent="submit">
      <div class="flex gap-2">
        <UInput v-model="input" placeholder="Nhập tin nhắn..." class="flex-1" :size="compact ? 'md' : 'lg'" />
        <UButton
          type="submit"
          color="primary"
          icon="i-ph-paper-plane-tilt"
          :disabled="!input.trim() || sending"
          :loading="sending"
          aria-label="Gửi tin nhắn"
        />
      </div>
    </form>
  </div>
</template>
