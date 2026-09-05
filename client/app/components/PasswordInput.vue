<script setup lang="ts">
const props = withDefaults(
  defineProps<{
    modelValue: string
    label?: string
    placeholder?: string
    icon?: string
    size?: 'md' | 'lg' | 'xl'
  }>(),
  { label: undefined, placeholder: undefined, icon: undefined, size: 'lg' }
)

const emit = defineEmits<{ 'update:modelValue': [value: string] }>()

const show = ref(false)

function toggle() {
  show.value = !show.value
}
</script>

<template>
  <UInput
    :model-value="props.modelValue"
    :type="show ? 'text' : 'password'"
    :label="label"
    :placeholder="placeholder"
    :icon="icon"
    :size="size"
    class="w-full"
    :ui="{ trailing: 'pe-1' }"
    @update:model-value="emit('update:modelValue', $event as string)"
  >
    <template #trailing>
      <UButton
        color="neutral"
        variant="link"
        size="sm"
        :icon="show ? 'i-ph-eye-slash' : 'i-ph-eye'"
        :aria-label="show ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'"
        @click="toggle"
      />
    </template>
  </UInput>
</template>
