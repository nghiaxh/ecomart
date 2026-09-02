<script setup lang="ts">
defineProps<{ title: string; subtitle?: string }>()
</script>

<template>
  <div
    class="relative flex min-h-[100dvh] items-center justify-center overflow-hidden p-4"
    style="background-image: url('/images/auth-bg.jpg'); background-size: cover; background-position: center; background-color: #065f46;"
  >
    <div class="absolute inset-0 bg-emerald-900/50" aria-hidden="true"></div>

    <div class="relative z-10 w-full max-w-[420px] rounded-2xl bg-white p-8 shadow-2xl shadow-emerald-950/30 sm:p-10">
      <div class="flex flex-col items-center justify-around">
        <h1 class="text-2xl font-extrabold tracking-tight text-gray-800">{{ title }}</h1>
        <p v-if="subtitle" class="text-sm text-gray-500">{{ subtitle }}</p>
      </div>

      <div class="mt-7">
        <slot />
      </div>
    </div>
  </div>
</template>