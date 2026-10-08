import { useState, type FormEvent } from 'react'
import { CircleAlert, LoaderCircle } from 'lucide-react'
import Modal from '../common/Modal'
import Field from '../common/Field'
import { plantApi } from '../../lib/api'
import { errorText } from '../../lib/api/client'
import type { TramTron } from '../../types/domain'

/** Bảng 3.21 - Thêm trạm trộn, Bảng 3.23 - Sửa trạm trộn. */
export default function PlantFormModal({ editing, onClose, onSaved }: { editing: TramTron | null; onClose: () => void; onSaved: () => void }) {
  const isEdit = Boolean(editing)
  const [tenTram, setTenTram] = useState(editing?.tenTram ?? '')
  const [diaChi, setDiaChi] = useState(editing?.diaChi ?? '')
  const [sdt, setSdt] = useState(editing?.sdt ?? '')
  const [congSuat, setCongSuat] = useState(String(editing?.congSuat ?? ''))
  const [trangThai, setTrangThai] = useState(editing?.trangThai ?? 1)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  function validate(): string {
    if (!tenTram.trim()) return 'Vui lòng nhập tên trạm trộn'
    if (!diaChi.trim()) return 'Vui lòng nhập địa chỉ'
    if (!/^0[0-9]{9}$/.test(sdt.trim())) return 'Số điện thoại không đúng định dạng (10 số, bắt đầu bằng 0)'
    const c = Number(congSuat)
    if (!congSuat.trim() || Number.isNaN(c) || c <= 0) return 'Công suất phải là số lớn hơn 0 (m³/giờ)'
    return ''
  }

  async function submit(e: FormEvent) {
    e.preventDefault()
    const v = validate()
    if (v) { setError(v); return }
    setError(''); setLoading(true)
    const body = { tenTram: tenTram.trim(), diaChi: diaChi.trim(), sdt: sdt.trim(), congSuat: Number(congSuat), trangThai }
    try {
      if (isEdit && editing) await plantApi.update(editing.idTram, body)
      else await plantApi.create(body)
      onSaved()
    } catch (err) {
      setError(errorText(err, isEdit ? 'Cập nhật trạm trộn thất bại' : 'Thêm trạm trộn thất bại'))
    } finally { setLoading(false) }
  }

  return (
    <Modal title={isEdit ? `Sửa trạm trộn — ${editing?.tenTram}` : 'Thêm trạm trộn'} onClose={onClose}
      footer={<>
        <button className="btn" onClick={onClose} disabled={loading}>Hủy</button>
        <button className="btn btn-primary" onClick={submit} disabled={loading}>{loading && <LoaderCircle size={15} className="spin" />}Lưu</button>
      </>}>
      <form className="form-stack" onSubmit={submit} noValidate>
        <Field label="Tên trạm trộn"><input value={tenTram} onChange={(e) => setTenTram(e.target.value)} autoFocus /></Field>
        <Field label="Địa chỉ"><input value={diaChi} onChange={(e) => setDiaChi(e.target.value)} /></Field>
        <div className="form-grid">
          <Field label="Số điện thoại"><input value={sdt} onChange={(e) => setSdt(e.target.value)} /></Field>
          <Field label="Công suất (m³/giờ)"><input type="number" min={1} value={congSuat} onChange={(e) => setCongSuat(e.target.value)} /></Field>
        </div>
        <Field label="Trạng thái">
          <select value={trangThai} onChange={(e) => setTrangThai(Number(e.target.value))}>
            <option value={1}>Hoạt động</option><option value={0}>Ngừng hoạt động</option>
          </select>
        </Field>
        {error && <div className="form-error" role="alert"><CircleAlert size={16} style={{ flexShrink: 0, marginTop: 1 }} />{error}</div>}
      </form>
    </Modal>
  )
}
