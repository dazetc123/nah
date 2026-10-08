import { useState, type FormEvent } from 'react'
import { CircleAlert, LoaderCircle } from 'lucide-react'
import Modal from '../common/Modal'
import Field from '../common/Field'
import { plantApi } from '../../lib/api'
import { errorText } from '../../lib/api/client'
import type { TramTron } from '../../types/domain'

/** Bảng 3.25 - Quản lý công suất trạm trộn (tách riêng khỏi Sửa trạm trộn theo đúng usecase báo cáo). */
export default function CapacityModal({ plant, onClose, onSaved }: { plant: TramTron; onClose: () => void; onSaved: () => void }) {
  const [congSuat, setCongSuat] = useState(String(plant.congSuat))
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  async function submit(e: FormEvent) {
    e.preventDefault()
    const c = Number(congSuat)
    if (!congSuat.trim() || Number.isNaN(c) || c <= 0) { setError('Công suất phải là số lớn hơn 0'); return }
    setError(''); setLoading(true)
    try { await plantApi.setCapacity(plant.idTram, c); onSaved() }
    catch (err) { setError(errorText(err, 'Cập nhật công suất thất bại')) }
    finally { setLoading(false) }
  }

  return (
    <Modal title={`Cập nhật công suất — ${plant.tenTram}`} onClose={onClose}
      footer={<>
        <button className="btn" onClick={onClose} disabled={loading}>Hủy</button>
        <button className="btn btn-primary" onClick={submit} disabled={loading}>{loading && <LoaderCircle size={15} className="spin" />}Lưu công suất</button>
      </>}>
      <form className="form-stack" onSubmit={submit} noValidate>
        <Field label="Công suất mới (m³/giờ)" hint={`Hiện tại: ${plant.congSuat} m³/giờ`}>
          <input type="number" min={1} value={congSuat} onChange={(e) => setCongSuat(e.target.value)} autoFocus />
        </Field>
        {error && <div className="form-error" role="alert"><CircleAlert size={16} style={{ flexShrink: 0, marginTop: 1 }} />{error}</div>}
      </form>
    </Modal>
  )
}
