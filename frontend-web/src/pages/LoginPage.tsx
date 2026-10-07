import { useState, type FormEvent } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { CircleAlert, LoaderCircle } from 'lucide-react'
import AuthLayout from '../components/layout/AuthLayout'
import Field from '../components/common/Field'
import PasswordInput from '../components/common/PasswordInput'
import { useAuth } from '../lib/auth'
import { errorText } from '../lib/api/client'
import { googleLoginUrl } from '../lib/api/client'

/** Bảng 3.2 - Đăng nhập: chọn phương thức (tên đăng nhập/email hoặc Google) → nhập thông tin → kiểm tra → thông báo. */
export default function LoginPage() {
  const { session, login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [dinhDanh, setDinhDanh] = useState('')
  const [matKhau, setMatKhau] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const oauthState = location.state as { oauthCancelled?: boolean; oauthError?: string } | null
  const oauthCancelled = Boolean(oauthState?.oauthCancelled)

  if (session) return <Navigate to={session.phaiDoiMatKhau ? '/doi-mat-khau-lan-dau' : '/'} replace />

  async function submit(e: FormEvent) {
    e.preventDefault()
    setError('')
    if (!dinhDanh.trim() || !matKhau) { setError('Không được để trống tên đăng nhập hoặc mật khẩu'); return }  // nhánh 5.b
    if (matKhau.length < 8) { setError('Mật khẩu phải có ít nhất 8 ký tự'); return }                               // nhánh 5.a
    setLoading(true)
    try {
      const s = await login(dinhDanh.trim(), matKhau)
      // Tài khoản do Quản lý cấp (Điều phối/Tài xế) bắt buộc đổi mật khẩu ở lần đăng nhập đầu tiên.
      navigate(s.phaiDoiMatKhau ? '/doi-mat-khau-lan-dau' : '/', { replace: true })
    } catch (err) {
      setError(errorText(err, 'Đăng nhập thất bại'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <AuthLayout eyebrow="ĐIỀU HÀNH BÊ TÔNG" headline={<>Từ trạm trộn<br />đến công trình.</>}
      blurb="Quản lý tài khoản, đội xe và trạm trộn trên cùng một hệ thống, đồng bộ trực tiếp với dữ liệu thật.">
      <h1>Đăng nhập</h1>
      <p className="sub">Dùng tên đăng nhập hoặc email đã được cấp.</p>
      {oauthCancelled && <div className="form-error" role="alert">Bạn đã hủy đăng nhập Google. Phiên đăng nhập đã được đóng.</div>}
      <form className="form-stack" onSubmit={submit} noValidate>
        <Field label="Tên đăng nhập hoặc email">
          <input value={dinhDanh} onChange={(e) => setDinhDanh(e.target.value)} autoComplete="username" autoFocus placeholder="Nhập tên đăng nhập hoặc email" />
        </Field>
        <Field label="Mật khẩu">
          <PasswordInput value={matKhau} onChange={setMatKhau} autoComplete="current-password" placeholder="Nhập mật khẩu" required={false} />
        </Field>
        <div style={{ textAlign: 'right', marginTop: -6 }}><Link to="/quen-mat-khau" style={{ fontSize: 12.5 }}>Quên mật khẩu?</Link></div>
        {error && <div className="form-error" role="alert"><CircleAlert size={16} style={{ flexShrink: 0, marginTop: 1 }} />{error}</div>}
        <button className="btn btn-primary btn-block" disabled={loading}>
          {loading && <LoaderCircle size={16} className="spin" />}{loading ? 'Đang đăng nhập…' : 'Đăng nhập'}
        </button>
      </form>
      <div className="divider">hoặc</div>
      <button type="button" className="btn google-btn" onClick={() => window.location.assign(googleLoginUrl)}>
        <span className="google-logo">G</span>Đăng nhập bằng Google
      </button>
      <p className="switch">Chưa có tài khoản? <Link to="/dang-ky">Đăng ký tài khoản khách hàng</Link></p>
    </AuthLayout>
  )
}
