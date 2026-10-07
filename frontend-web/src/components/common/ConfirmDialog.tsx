import { useState } from 'react'
import { LoaderCircle } from 'lucide-react'
import Modal from './Modal'

/** Hộp thoại xác nhận theo đúng kịch bản báo cáo (vd: "Bạn có chắc chắn muốn xóa ... này?"). */
export default function ConfirmDialog({ title, message, confirmLabel = 'Đồng ý', danger, onConfirm, onClose }: {
  title: string; message: string; confirmLabel?: string; danger?: boolean
  onConfirm: () => Promise<void>; onClose: () => void
}) {
  const [busy, setBusy] = useState(false)
  async function run() {
    setBusy(true)
    try { await onConfirm() } finally { setBusy(false) }
  }
  return (
    <Modal title={title} onClose={busy ? () => undefined : onClose} width={440}
      footer={<>
        <button className="btn" onClick={onClose} disabled={busy}>Hủy</button>
        <button className={danger ? 'btn btn-danger-solid' : 'btn btn-primary'} onClick={run} disabled={busy}>
          {busy && <LoaderCircle size={15} className="spin" />}{confirmLabel}
        </button>
      </>}>
      <p className="confirm-text">{message}</p>
    </Modal>
  )
}
