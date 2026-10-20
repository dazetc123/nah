import { useState, type FormEvent } from 'react'
import { CircleAlert, LoaderCircle } from 'lucide-react'
import Modal from '../common/Modal'
import Field from '../common/Field'
import PasswordInput from '../common/PasswordInput'
import { accountApi, driverApi } from '../../lib/api'
import { errorText } from '../../lib/api/client'
import type { TaiKhoan } from '../../types/domain'

/** Bảng 3.4 - Thêm tài khoản (mật khẩu do Quản lý chỉ định, chỉ hiển thị lại đúng 1 lần ở phản hồi tạo).
 *  Bảng 3.5 - Sửa tài khoản (không đổi tên đăng nhập / mật khẩu / vai trò ở đây). */
export default function AccountFormModal({ editing, onClose, onSaved }: {
  editing: TaiKhoan | null; onClose: () => void; onSaved: (created?: { tenDangNhap: string; matKhau: string }) => void
}) {
  const isEdit = Boolean(editing)
  const [f, setF] = useState({
    tenDangNhap: editing?.tenDangNhap ?? '',
    matKhau: '',
    hoTen: editing?.hoTen ?? '',
    email: editing?.email ?? '',
    sdt: editing?.sdt ?? '',
    tenVaiTro: (editing?.tenVaiTro as 'Nhân viên điều phối' | 'Tài xế') ?? 'Nhân viên điều phối',
    soGPLX: editing?.soGPLX ?? '',
  })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const set = <K extends keyof typeof f>(k: K) => (v: typeof f[K]) => setF((p) => ({ ...p, [k]: v }))

  function validate(): string {
    if (!f.hoTen.trim()) return 'Vui lòng nhập họ tên'
    if (f.email.trim() && !/^\S+@\S+\.\S+$/.test(f.email)) return 'Email không đúng định dạng'
    if (!isEdit) {
      if (!/^[a-zA-Z0-9_.]{4,50}$/.test(f.tenDangNhap)) return 'Tên đăng nhập từ 4–50 ký tự: chữ, số, dấu chấm, gạch dưới'
      if (f.matKhau.length < 8) return 'Mật khẩu phải có ít nhất 8 ký tự'
      if (!f.email.trim() && !f.sdt.trim()) return 'Cần cung cấp email hoặc số điện thoại để gửi mật khẩu tạm'
      if (f.tenVaiTro === 'Tài xế' && !f.sdt.trim()) return 'Tài khoản Tài xế cần có số điện thoại để nhận mật khẩu'
      if (f.tenVaiTro === 'Tài xế' && !/^0[0-9]{9}$/.test(f.sdt.trim())) return 'Số điện thoại tài xế phải gồm 10 số và bắt đầu bằng 0'
    }
    if (f.tenVaiTro === 'Tài xế' && !f.soGPLX.trim()) return 'Vui lòng nhập số giấy phép lái xe cho tài khoản Tài xế'
    return ''
  }

  async function submit(e: FormEvent) {
    e.preventDefault()
    const v = validate()
    if (v) { setError(v); return }
    setError(''); setLoading(true)
    try {
      if (isEdit && editing) {
        await accountApi.update(editing.idTK, { hoTen: f.hoTen.trim(), email: f.email.trim(), sdt: f.sdt.trim(), soGPLX: f.soGPLX.trim() })
        onSaved()
      } else {
        const res = await accountApi.create({ ...f, hoTen: f.hoTen.trim(), tenDangNhap: f.tenDangNhap.trim(), email: f.email.trim(), sdt: f.sdt.trim() })
        if (f.tenVaiTro === 'Tài xế') {
          try {
            await driverApi.create({
              idTK: res.idTK,
              hoTen: f.hoTen.trim(),
              soGPLX: f.soGPLX.trim(),
              sdt: f.sdt.trim(),
              trangThai: 1,
            })
          } catch (driverError) {
            throw new Error(`Tài khoản đã được tạo nhưng chưa tạo được hồ sơ tài xế: ${errorText(driverError, 'lỗi không xác định')}`)
          }
        }
        onSaved({ tenDangNhap: res.tenDangNhap, matKhau: f.matKhau })
      }
    } catch (err) {
      setError(errorText(err, isEdit ? 'Cập nhật thất bại' : 'Tạo tài khoản thất bại'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <Modal title={isEdit ? `Sửa tài khoản — ${editing?.hoTen}` : 'Cấp tài khoản nhân viên'} onClose={onClose}
      footer={<>
        <button className="btn" onClick={onClose} disabled={loading}>Hủy</button>
        <button className="btn btn-primary" onClick={submit} disabled={loading}>
          {loading && <LoaderCircle size={15} className="spin" />}{isEdit ? 'Lưu thay đổi' : 'Cấp tài khoản'}
        </button>
      </>}>
      <form className="form-stack" onSubmit={submit} noValidate>
        {!isEdit && (
          <Field label="Vai trò">
            <select value={f.tenVaiTro} onChange={(e) => set('tenVaiTro')(e.target.value as typeof f.tenVaiTro)}>
              <option value="Nhân viên điều phối">Nhân viên điều phối</option>
              <option value="Tài xế">Tài xế</option>
            </select>
          </Field>
        )}
        <Field label="Họ tên"><input value={f.hoTen} onChange={(e) => set('hoTen')(e.target.value)} autoFocus /></Field>
        <div className="form-grid">
          <Field label="Số điện thoại"><input value={f.sdt} onChange={(e) => set('sdt')(e.target.value)} /></Field>
          <Field label="Email"><input type="email" value={f.email} onChange={(e) => set('email')(e.target.value)} /></Field>
        </div>
        {!isEdit && (
          <>
            <Field label="Tên đăng nhập / mã tài xế" hint="Có thể dùng số điện thoại hoặc mã công nhân/mã tài xế"><input value={f.tenDangNhap} onChange={(e) => set('tenDangNhap')(e.target.value)} /></Field>
            <Field label="Mật khẩu ban đầu" hint="Tối thiểu 8 ký tự — chỉ hiển thị lại đúng 1 lần sau khi tạo">
              <PasswordInput value={f.matKhau} onChange={set('matKhau')} autoComplete="new-password" required={false} />
            </Field>
          </>
        )}
        {f.tenVaiTro === 'Tài xế' && (
          <Field label="Số giấy phép lái xe"><input value={f.soGPLX} onChange={(e) => set('soGPLX')(e.target.value)} /></Field>
        )}
        {error && <div className="form-error" role="alert"><CircleAlert size={16} style={{ flexShrink: 0, marginTop: 1 }} />{error}</div>}
      </form>
    </Modal>
  )
}
