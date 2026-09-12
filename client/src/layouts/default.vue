<script setup lang="ts">
import { useRoute } from 'vue-router'
import FooterGlobal from '@/components/FooterGlobal.vue'
import HeaderGlobal from '@/components/HeaderGlobal.vue'
import ChatWidget from '@/components/ChatWidget.vue'

const route = useRoute()
</script>

<template>
  <div class="flex min-h-screen flex-col">
    <HeaderGlobal />

    <main class="flex-1">
      <RouterView />
    </main>

    <FooterGlobal v-if="route.name === 'home'" />
    <ChatWidget />
  </div>
</template>