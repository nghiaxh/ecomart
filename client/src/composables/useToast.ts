import { useMessage } from 'naive-ui'

export const useToast = () => {
  const message = useMessage()

  const add = (opts: { severity?: 'success' | 'info' | 'warn' | 'error'; summary: string; detail?: string; life?: number }) => {
    const content = opts.detail ? `${opts.summary} — ${opts.detail}` : opts.summary
    const duration = opts.life ?? 4000
    switch (opts.severity) {
      case 'error':
        return message.error(content, { duration })
      case 'success':
        return message.success(content, { duration })
      case 'warn':
        return message.warning(content, { duration })
      default:
        return message.info(content, { duration })
    }
  }

  return { add }
}