<script setup lang="ts">
import { NInput } from 'naive-ui'
import UiIcon from '@/components/UiIcon.vue'

const props = withDefaults(
  defineProps<{
    modelValue: string
    id?: string
    autocomplete?: string
    label?: string
    placeholder?: string
    icon?: string
    size?: 'small' | 'medium' | 'large'
  }>(),
  { id: undefined, autocomplete: undefined, label: undefined, placeholder: undefined, icon: undefined, size: 'medium' }
)

const emit = defineEmits<{ 'update:modelValue': [value: string] }>()
</script>

<template>
  <NInput
    :id="props.id"
    :value="props.modelValue"
    type="password"
    show-password-on="click"
    :autocomplete="props.autocomplete"
    :placeholder="props.placeholder"
    :size="props.size"
    class="w-full"
    @update:value="emit('update:modelValue', $event)"
  >
    <template v-if="props.icon" #prefix>
      <UiIcon :name="props.icon" size="18" />
    </template>
  </NInput>
</template>