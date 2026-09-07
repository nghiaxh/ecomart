<script setup lang="ts">
withDefaults(
  defineProps<{
    src?: string | null
    alt?: string
    imgClass?: string
  }>(),
  { src: null, alt: '', imgClass: 'h-full w-full object-cover' }
)
</script>

<template>
  <img
    v-if="src"
    :src="src"
    :alt="alt"
    loading="lazy"
    :class="imgClass"
    @error="($event.target as HTMLImageElement).src = '/images/placeholder.svg'"
  />
  <div v-else class="grid h-full w-full place-items-center text-gray-200">
    <UIcon name="i-ph-image" class="h-12 w-12" />
  </div>
</template>
