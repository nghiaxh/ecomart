export default defineNuxtRouteMiddleware(() => {
  const { isLoggedIn, isAdmin } = useAuth()
  if (!isLoggedIn.value) {
    return navigateTo('/login')
  }
  if (isAdmin.value) {
    return navigateTo('/admin')
  }
})