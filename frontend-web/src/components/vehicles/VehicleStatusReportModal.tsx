import { useState, type FormEvent } from 'react'
import { LoaderCircle, MapPin, Upload } from 'lucide-react'
import Field from '../common/Field'
import Modal from '../common/Modal'
import { vehicleApi, vehicleReportApi } from '../../lib/api'
import { errorText } from '../../lib/api/client'
import type { Xe } from '../../types/domain'

export default function VehicleStatusReportModal({ xe, onClose, onSaved }: { xe: Xe; onClose: () => void; onSaved: () => void }) {
  const [trangThaiXe, setTrangThaiXe] = useState(String(xe.trangThai))
  const [diaChiHu, setDiaChiHu] = useState('')
  const [soDienThoaiTaiXe, setSoDienThoaiTaiXe] = useState('')
  const [noiDung, setNoiDung] = useState('')
  const [anh, setAnh] = useState<File | undefined>()
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const [locating, setLocating] = useState(false)

  function useCurrentLocation() {
    if (!navigator.geolocation) { setError('Thiết bị không hỗ trợ định vị GPS'); return }
    setLocating(true); setError('')
    navigator.geolocation.getCurrentPosition(
      ({ coords }) => { setDiaChiHu(`Vị trí GPS: ${coords.latitude.toFixed(6)}, ${coords.longitude.toFixed(6)}`); setLocating(false) },
      () => { setError('Không lấy được vị trí GPS. Hãy cấp quyền định vị hoặc nhập địa chỉ thủ công.'); setLocating(false) },
      { enableHighAccuracy: true, timeout: 10000, maximumAge: 30000 },
    )
  }

  async function submit(e: FormEvent) {
    e.preventDefault()
    if (!diaChiHu.trim() || !soDienThoaiTaiXe.trim() || !noiDung.trim() || !anh) {
      setError('Vui lòng nhập đủ thông tin và chọn ảnh minh họa'); return
    }
    setError(''); setLoading(true)
    try {
      await vehicleApi.setStatus(xe.idXe, Number(trangThaiXe))
      await vehicleReportApi.create(xe.idXe, { trangThaiXe: Number(trangThaiXe), diaChiHu: diaChiHu.trim(), soDienThoaiTaiXe: soDienThoaiTaiXe.trim(), nguyenNhan: noiDung.trim(), noiDung: noiDung.trim(), anh })
      onSaved()
    } catch (err) { setError(errorText(err, 'Không thể cập nhật trạng thái và gửi báo cáo')) }
    finally { setLoading(false) }
  }

  return <Modal title={`Cập nhật trạng thái — ${xe.bienSo}`} onClose={onClose} width={680}
    footer={<><button className="btn" onClick={onClose} disabled={loading}>Hủy</button><button className="btn btn-primary" onClick={submit} disabled={loading}>{loading && <LoaderCircle size={15} className="spin" />}Cập nhật</button></>}>
    <form className="form-stack" onSubmit={submit} noValidate>
      <div className="form-grid">
        <Field label="Trạng thái xe"><select value={trangThaiXe} onChange={(e) => setTrangThaiXe(e.target.value)}><option value="1">Đang hoạt động</option><option value="0">Bảo trì</option></select></Field>
        <Field label="Số điện thoại tài xế"><input value={soDienThoaiTaiXe} onChange={(e) => setSoDienThoaiTaiXe(e.target.value)} placeholder="090..." /></Field>
      </div>
      <Field label="Vị trí xe gặp sự cố"><div className="row-actions"><input value={diaChiHu} onChange={(e) => setDiaChiHu(e.target.value)} placeholder="Nhập địa chỉ hoặc dùng GPS" /><button type="button" className="btn btn-sm" onClick={useCurrentLocation} disabled={locating}><MapPin size={15} />{locating ? 'Đang lấy vị trí…' : 'Vị trí hiện tại'}</button></div></Field>
      <Field label="Mô tả tình trạng / nguyên nhân"><textarea rows={4} value={noiDung} onChange={(e) => setNoiDung(e.target.value)} placeholder="Mô tả sự cố và nguyên nhân…" /></Field>
      <Field label="Ảnh minh họa (có thể chụp trực tiếp)"><label className="btn"><Upload size={15} />{anh ? anh.name : 'Chọn hoặc chụp ảnh'}<input type="file" accept="image/*" capture="environment" hidden onChange={(e) => setAnh(e.target.files?.[0])} /></label></Field>
      {error && <div className="form-error">{error}</div>}
    </form>
  </Modal>
}
