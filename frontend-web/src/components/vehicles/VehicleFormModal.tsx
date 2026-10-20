import { useState, type FormEvent } from 'react'
import { CircleAlert, LoaderCircle } from 'lucide-react'
import Modal from '../common/Modal'
import Field from '../common/Field'
import { vehicleApi } from '../../lib/api'
import { errorText } from '../../lib/api/client'
import type { Xe } from '../../types/domain'

const PLATE_RE = /^[0-9]{2}[A-Z]{1,2}[0-9]{0,1}-[0-9]{3}\.[0-9]{2}$|^[0-9]{2}[A-Z]-[0-9]{4,5}$/

/** Bảng 3.17 - Thêm xe, Bảng 3.18 - Sửa xe. */
export default function VehicleFormModal({ editing, onClose, onSaved }: { editing: Xe | null; onClose: () => void; onSaved: () => void }) {
  const isEdit = Boolean(editing)
  const [bienSo, setBienSo] = useState(editing?.bienSo ?? '')
  const [trongTai, setTrongTai] = useState(String(editing?.trongTai ?? ''))
  const [trangThai, setTrangThai] = useState(editing?.trangThai ?? 1)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  function validate(): string {
    if (!bienSo.trim()) return 'Vui lòng nhập biển số xe'
    if (!PLATE_RE.test(bienSo.trim().toUpperCase())) return 'Biển số không đúng định dạng (VD: 51C-123.45)'
    const t = Number(trongTai)
    if (!trongTai.trim() || Number.isNaN(t) || t <= 0) return 'Trọng tải phải là số lớn hơn 0'
    return ''
  }

  async function submit(e: FormEvent) {
    e.preventDefault()
    const v = validate()
    if (v) { setError(v); return }
    setError(''); setLoading(true)
    const body = { bienSo: bienSo.trim().toUpperCase(), trongTai: Number(trongTai), trangThai }
    try {
      if (isEdit && editing) await vehicleApi.update(editing.idXe, body)
      else await vehicleApi.create(body)
      onSaved()
    } catch (err) {
      setError(errorText(err, isEdit ? 'Cập nhật xe thất bại' : 'Thêm xe thất bại'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <Modal title={isEdit ? `Sửa xe — ${editing?.bienSo}` : 'Thêm xe'} onClose={onClose}
      footer={<>
        <button className="btn" onClick={onClose} disabled={loading}>Hủy</button>
        <button className="btn btn-primary" onClick={submit} disabled={loading}>{loading && <LoaderCircle size={15} className="spin" />}Lưu</button>
      </>}>
      <form className="form-stack" onSubmit={submit} noValidate>
        <Field label="Biển số xe" hint="VD: 51C-123.45"><input value={bienSo} onChange={(e) => setBienSo(e.target.value)} autoFocus /></Field>
        <div className="form-grid">
          <Field label="Trọng tải (m³)"><input type="number" min={1} step={0.5} value={trongTai} onChange={(e) => setTrongTai(e.target.value)} /></Field>
          <Field label="Trạng thái">
            <select value={trangThai} onChange={(e) => setTrangThai(Number(e.target.value))}>
              <option value={1}>Đang hoạt động</option><option value={0}>Bảo trì</option>
            </select>
          </Field>
        </div>
        {error && <div className="form-error" role="alert"><CircleAlert size={16} style={{ flexShrink: 0, marginTop: 1 }} />{error}</div>}
      </form>
    </Modal>
  )
}
