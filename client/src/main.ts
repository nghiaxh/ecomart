import { createApp } from 'vue'
import { createHead } from '@unhead/vue/client'
import App from './App.vue'
import { router } from './router'
import { useAuth } from './composables/useAuth'
import { UNAUTHORIZED_EVENT } from './utils/session-storage'
import './assets/css/main.css'

const app = createApp(App)
const head = createHead()

const auth = useAuth()
auth.restore()

document.addEventListener(UNAUTHORIZED_EVENT, () => {
  auth.forceLogout()
  const noAuthPage = ['/login', '/register'].includes(router.currentRoute.value.path)
  if (!noAuthPage) {
    router.push('/login')
  }
})

app.use(head)
app.use(router)

app.mount('#app')
