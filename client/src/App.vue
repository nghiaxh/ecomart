<script setup lang="ts">
import { enUS, dateEnUS, NConfigProvider, NDialogProvider, NGlobalStyle, NMessageProvider } from 'naive-ui'
</script>

<template>
  <NConfigProvider :locale="enUS" :date-locale="dateEnUS">
    <NMessageProvider placement="top" :max="3">
      <NDialogProvider>
        <NGlobalStyle />
        <RouterView />
      </NDialogProvider>
    </NMessageProvider>
  </NConfigProvider>
</template>