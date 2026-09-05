import { UNAUTHORIZED_EVENT } from '~/utils/session-storage'

export default defineNuxtPlugin(() => {
  const { restore, forceLogout } = useAuth()

  // Restore synchronously here (before route middleware runs). Relying on
  // layout onMounted is too late: middleware would see a null session on
  // full page loads and bounce the user to /login despite a valid session.
  restore()

  document.addEventListener(UNAUTHORIZED_EVENT, () => {
    forceLogout()
  })
})
