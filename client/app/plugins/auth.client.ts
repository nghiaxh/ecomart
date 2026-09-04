export default defineNuxtPlugin(() => {
  const { forceLogout } = useAuth()

  if (import.meta.client) {
    document.addEventListener('ecomart:unauthorized', () => {
      forceLogout()
    })
  }
})
