import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'
import ui from '@nuxt/ui/vite'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  plugins: [
    vue(),
    ui({
      autoImport: {
        imports: ['vue', 'vue-router', '@vueuse/core']
      },
      components: {
        dirs: [fileURLToPath(new URL('./src/components', import.meta.url))]
      }
    })
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  test: {
    include: ['src/**/*.{test,spec}.ts'],
    environment: 'happy-dom',
    globals: true,
    restoreMocks: true
  }
})