export default defineNuxtRouteMiddleware(() => {
  if (process.server) return
  const { isLoggedIn, isAdmin } = useAuth()
  if (!isLoggedIn.value) {
    return navigateTo('/login')
  }
  if (!isAdmin.value) {
    return navigateTo('/')
  }
})
