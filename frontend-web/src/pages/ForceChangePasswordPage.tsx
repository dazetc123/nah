import { useState, type FormEvent } from 'react'
import { Navigate, useNavigate } from 'react-router-dom'
import { CircleAlert, LoaderCircle } from 'lucide-react'
import AuthLayout from '../components/layout/AuthLayout'
import Field from '../components/common/Field'
import PasswordInput from '../components/common/PasswordInput'
import { authApi } from '../lib/api'
import { errorText } from '../lib/api/client'
import { useAuth } from '../lib/auth'
import { useToast } from '../lib/toast'

/**
 * Tài khoản Nhân viên điều phối / Tài xế do Quản lý cấp (phaiDoiMatKhau = true) bắt buộc đổi mật khẩu
 * trước khi dùng hệ thống — mọi API khác bị backend chặn (403) cho tới khi hoàn tất bước này.
 */
export default function ForceChangePasswordPage() {
  const { session, applySession, logout } = useAuth()
  const navigate = useNavigate()
  const toast = useToast()
  const [mk, setMk] = useState('')
  const [xn, setXn] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  if (!session) return <Navigate to="/dang-nhap" replace />
  if (!session.phaiDoiMatKhau) return <Navigate to="/" replace />

  async function submit(e: FormEvent) {
    e.preventDefault()
    if (mk.length < 8) { setError('Mật khẩu phải có ít nhất 8 ký tự'); return }
    if (mk !== xn) { setError('Xác nhận mật khẩu mới không khớp'); return }
    setError(''); setLoading(true)
    try {
      applySession(await authApi.firstPassword(mk, xn)) // backend cấp token mới với phaiDoiMatKhau = false
      toast.success('Đổi mật khẩu thành công')
      navigate('/', { replace: true })
    } catch (err) { setError(errorText(err, 'Đổi mật khẩu thất bại, yêu cầu thử lại sau')) } finally { setLoading(false) }
  }

  return (
    <AuthLayout eyebrow="BẮT BUỘC TRƯỚC KHI TIẾP TỤC" headline={<>Đặt mật khẩu<br />của riêng bạn.</>}
      blurb="Tài khoản của bạn do Quản lý cấp với mật khẩu tạm. Vì lý do bảo mật, hãy đổi mật khẩu ngay lần đăng nhập đầu tiên.">
      <h1>Đổi mật khẩu lần đầu</h1>
      <p className="sub">Xin chào <b>{session.hoTen}</b> ({session.tenVaiTro}).</p>
      <form className="form-stack" onSubmit={submit} noValidate>
        <Field label="Mật khẩu mới" hint="Tối thiểu 8 ký tự"><PasswordInput value={mk} onChange={setMk} autoComplete="new-password" required={false} /></Field>
        <Field label="Xác nhận mật khẩu mới"><PasswordInput value={xn} onChange={setXn} autoComplete="new-password" required={false} /></Field>
        {error && <div className="form-error" role="alert"><CircleAlert size={16} style={{ flexShrink: 0, marginTop: 1 }} />{error}</div>}
        <button className="btn btn-primary btn-block" disabled={loading}>{loading && <LoaderCircle size={16} className="spin" />}Xác nhận và tiếp tục</button>
        <button type="button" className="btn btn-block" onClick={async () => { await logout(); navigate('/dang-nhap', { replace: true }) }}>Đăng xuất</button>
      </form>
    </AuthLayout>
  )
}
