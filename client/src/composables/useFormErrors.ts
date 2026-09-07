import type { ZodError } from 'zod'

export const useFormErrors = () => {
  const errors = ref<Record<string, string>>({})

  const applyIssues = (error: ZodError) => {
    errors.value = {}
    for (const issue of error.issues) {
      const key = String(issue.path[0] ?? 'form')
      if (!errors.value[key]) {
        errors.value[key] = issue.message
      }
    }
    return errors.value
  }

  const clearErrors = () => {
    errors.value = {}
  }

  return { errors, applyIssues, clearErrors }
}
