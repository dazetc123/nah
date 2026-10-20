import { useEffect, useState } from 'react'
import { CircleAlert, XCircle } from 'lucide-react'
import Field from '../common/Field'
import { accountApi, driverApi } from '../../lib/api'
import { errorText } from '../../lib/api/client'
import { useToast } from '../../lib/toast'
import type { TaiXe, TaiKhoan } from '../../types/domain'

export default function DriverFormModal({ editing, onClose, onSaved }: { editing: TaiXe | 'new'; onClose: () => void; onSaved: () => void }) {
  const toast = useToast()
  const isNew = editing === 'new'
  const [hoTen, setHoTen] = useState(isNew ? '' : editing.hoTen)
  const [soGPLX, setSoGPLX] = useState(isNew ? '' : editing.soGPLX)
  const [sdt, setSdt] = useState(isNew ? '' : editing.sdt)
  const [idTK, setIdTK] = useState(isNew ? '' : String(editing.idTK))
  const [trangThai, setTrangThai] = useState(isNew ? 1 : editing.trangThai)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  
  const [accounts, setAccounts] = useState<TaiKhoan[]>([])

  useEffect(() => {
    if (isNew) {
      accountApi.allForRoles('', 1, 100).then((res) => {
        // Filter accounts that are Drivers (Tài xế)
        setAccounts(res.danhSach.filter(a => a.tenVaiTro === 'Tài xế'))
      }).catch(err => setError(errorText(err, 'Lỗi tải danh sách tài khoản')))
    }
  }, [isNew])

  async function save() {
    setError(''); setLoading(true)
    if (!hoTen.trim() || !soGPLX.trim() || !sdt.trim() || (isNew && !idTK)) {
      setError('Vui lòng điền đủ thông tin bắt buộc'); setLoading(false); return
    }
    
    try {
      if (isNew) {
        await driverApi.create({ hoTen: hoTen.trim(), soGPLX: soGPLX.trim(), sdt: sdt.trim(), idTK: Number(idTK), trangThai })
        toast.success('Thêm tài xế thành công')
      } else {
        await driverApi.update(editing.idTX, { hoTen: hoTen.trim(), soGPLX: soGPLX.trim(), sdt: sdt.trim(), trangThai })
        toast.success('Cập nhật tài xế thành công')
      }
      onSaved()
    } catch (err) {
      setError(errorText(err, 'Không thể lưu hồ sơ tài xế'))
    } finally {
      setLoading(false)
    }
  }

  return <div className="modal-backdrop"><div className="modal">
    <div className="modal-head"><h2>{isNew ? 'Thêm hồ sơ tài xế' : 'Sửa hồ sơ tài xế'}</h2><button className="icon-btn" onClick={onClose}><XCircle size={18} /></button></div>
    <div className="modal-body form-stack">
      {isNew && (
        <Field label="Tài khoản liên kết">
          <select value={idTK} onChange={e => setIdTK(e.target.value)}>
            <option value="">-- Chọn tài khoản Tài xế --</option>
            {accounts.map(acc => <option key={acc.idTK} value={acc.idTK}>{acc.hoTen} ({acc.tenDangNhap})</option>)}
          </select>
        </Field>
      )}
      <Field label="Họ tên"><input value={hoTen} onChange={e => setHoTen(e.target.value)} placeholder="Nguyễn Văn A" /></Field>
      <Field label="Số GPLX"><input value={soGPLX} onChange={e => setSoGPLX(e.target.value)} placeholder="VD: 790123456789" /></Field>
      <Field label="Số điện thoại"><input value={sdt} onChange={e => setSdt(e.target.value)} placeholder="09xxxx" /></Field>
      {!isNew && (
        <Field label="Trạng thái">
          <select value={String(trangThai)} onChange={(e) => setTrangThai(Number(e.target.value))}>
            <option value="1">Đang hoạt động</option>
            <option value="0">Tạm dừng</option>
          </select>
        </Field>
      )}
      {error && <div className="form-error"><CircleAlert size={16} />{error}</div>}
      <div className="profile-form-actions"><button className="btn" onClick={onClose}>Hủy</button><button className="btn btn-primary" disabled={loading} onClick={save}>Lưu tài xế</button></div>
    </div>
  </div></div>
}
