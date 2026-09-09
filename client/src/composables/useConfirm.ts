import { useDialog } from 'naive-ui'

export const useConfirm = () => {
  const dialog = useDialog()

  const confirm = (message: string, title = 'Xác nhận', options?: { confirmLabel?: string; cancelLabel?: string }): Promise<boolean> => {
    return new Promise<boolean>((resolve) => {
      dialog.warning({
        title,
        content: message,
        positiveText: options?.confirmLabel ?? 'Xác nhận',
        negativeText: options?.cancelLabel ?? 'Hủy',
        onPositiveClick: () => { resolve(true); return true },
        onNegativeClick: () => { resolve(false); return true },
        onClose: () => resolve(false),
        onMaskClick: () => resolve(false),
        onEsc: () => resolve(false)
      })
    })
  }

  return { confirm }
}