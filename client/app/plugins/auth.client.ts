export default defineNuxtPlugin(() => {
  const { restore, forceLogout } = useAuth()

  // Restore synchronously here (before route middleware runs). Relying on
  // layout onMounted is too late: middleware would see a null session on
  // full page loads and bounce the user to /login despite a valid session.
  restore()

  if (import.meta.client) {
    document.addEventListener('ecomart:unauthorized', () => {
      forceLogout()
    })
  }
})
