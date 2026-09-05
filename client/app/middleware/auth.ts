export default defineNuxtRouteMiddleware(() => {
  if (process.server) return
  const { isLoggedIn } = useAuth()
  if (!isLoggedIn.value) {
    return navigateTo('/login')
  }
})
