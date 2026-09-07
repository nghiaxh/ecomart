import ConfirmDialog from '@/components/ConfirmDialog.vue'

export const useConfirm = () => {
  const confirm = (message: string, title = 'Xác nhận') => {
    const overlay = useOverlay()
    return overlay.create(ConfirmDialog).open({ title, message }) as Promise<boolean>
  }

  return { confirm }
}
