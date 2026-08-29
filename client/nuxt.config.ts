export default defineNuxtConfig({
  devtools: { enabled: true },
  modules: ['@nuxt/ui', '@vueuse/nuxt'],

  css: ['~/assets/css/main.css'],

  app: {
    pageTransition: { name: 'page', mode: 'out-in' },
    head: {
      htmlAttrs: { lang: 'vi' },
      title: 'EcoMart — Siêu thị xanh',
      meta: [
        { charset: 'utf-8' },
        { name: 'viewport', content: 'width=device-width, initial-scale=1' },
        { name: 'description', content: 'EcoMart — siêu thị thực phẩm sạch, bền vững. Tiêu dùng xanh mỗi ngày.' },
        { name: 'theme-color', content: '#16a34a' }
      ],
      link: [
        { rel: 'preconnect', href: 'https://fonts.googleapis.com' },
        { rel: 'preconnect', href: 'https://fonts.gstatic.com', crossorigin: '' },
        { rel: 'stylesheet', href: 'https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap' }
      ]
    }
  },

  runtimeConfig: {
    public: {
      apiBase: process.env.NUXT_PUBLIC_API_BASE || 'http://localhost:8080'
    }
  },

  colorMode: {
    preference: 'light'
  },

  ui: {
    global: true,
    icons: ['heroicons', 'ph', 'material-symbols']
  },

  typescript: {
    strict: true,
    typeCheck: false
  }
})
