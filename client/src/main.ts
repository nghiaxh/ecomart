import { createApp } from 'vue'
import ui from '@nuxt/ui/vue-plugin'
import App from './App.vue'
import { router } from './router'
import { useAuth } from './composables/useAuth'
import { UNAUTHORIZED_EVENT } from './utils/session-storage'
import './assets/css/main.css'

const app = createApp(App)

const auth = useAuth()
auth.restore()

document.addEventListener(UNAUTHORIZED_EVENT, () => {
  auth.forceLogout()
})

app.use(router)
app.use(ui)

app.mount('#app')
