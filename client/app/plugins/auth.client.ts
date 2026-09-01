export default defineNuxtPlugin(() => {
  const { restore } = useAuth()
  restore()
})
