export default defineNuxtConfig({
  compatibilityDate: '2026-09-02',
  devtools: {
    enabled: true,
    componentInspector: true
  },
  modules: ['@nuxt/ui', '@vueuse/nuxt'],

  devServer: {
    port: 5173
  },

  css: ['~/assets/css/main.css'],

  app: {
    head: {
      htmlAttrs: { lang: 'vi' },
      title: 'EcoMart',
      meta: [
        { charset: 'utf-8' },
        { name: 'viewport', content: 'width=device-width, initial-scale=1' },
        { name: 'description', content: 'EcoMart, siêu thị thực phẩm trực tuyến với sản phẩm đa dạng và tươi sạch.' },
        { name: 'theme-color', content: '#059669' },
        { property: 'og:title', content: 'EcoMart' },
        { property: 'og:description', content: 'Siêu thị xanh, sản phẩm tươi sạch mỗi ngày cho gia đình bạn.' },
        { property: 'og:type', content: 'website' },
        { property: 'og:image', content: '/favicon.svg' }
      ],
      link: [
        { rel: 'icon', href: '/favicon.svg' },
        { rel: 'apple-touch-icon', href: '/favicon.svg' },
        { rel: 'preconnect', href: 'https://fonts.googleapis.com' },
        { rel: 'preconnect', href: 'https://fonts.gstatic.com', crossorigin: '' },
        { rel: 'stylesheet', href: 'https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap' }
      ]
    }
  },

  runtimeConfig: {
    apiTarget: process.env.API_PROXY_TARGET || 'http://localhost:8080',
    public: {
      apiBase: process.env.NUXT_PUBLIC_API_BASE || '',
      supportPhone: process.env.NUXT_PUBLIC_SUPPORT_PHONE || '0900 000 000',
      supportEmail: process.env.NUXT_PUBLIC_SUPPORT_EMAIL || 'contact@ecomart.vn'
    }
  },

  colorMode: {
    preference: 'light'
  },

  typescript: {
    strict: true,
    typeCheck: false
  }
})
