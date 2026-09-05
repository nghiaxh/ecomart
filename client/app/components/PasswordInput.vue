<script setup lang="ts">
const props = withDefaults(
  defineProps<{
    modelValue: string
    id?: string
    autocomplete?: string
    label?: string
    placeholder?: string
    icon?: string
    size?: 'md' | 'lg' | 'xl'
  }>(),
  { id: undefined, autocomplete: undefined, label: undefined, placeholder: undefined, icon: undefined, size: 'lg' }
)

const emit = defineEmits<{ 'update:modelValue': [value: string] }>()

const show = ref(false)

function toggle() {
  show.value = !show.value
}
</script>

<template>
  <UInput
    :id="props.id"
    :model-value="props.modelValue"
    :type="show ? 'text' : 'password'"
    :autocomplete="props.autocomplete"
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
