import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { CircleAlert, LoaderCircle } from 'lucide-react'
import AuthLayout from '../components/layout/AuthLayout'
import Field from '../components/common/Field'
import PasswordInput from '../components/common/PasswordInput'
import { authApi } from '../lib/api'
import { errorText, googleLoginUrl } from '../lib/api/client'
import { useToast } from '../lib/toast'

const EMPTY = { hoTen: '', sdt: '', tenDangNhap: '', email: '', matKhau: '', xacNhanMatKhau: '' }

/** Bảng 3.1 - Đăng ký tài khoản (Khách hàng). Đăng ký xong quay về trang đăng nhập. */
export default function RegisterPage() {
  const navigate = useNavigate()
  const toast = useToast()
  const [f, setF] = useState(EMPTY)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const set = (k: keyof typeof EMPTY) => (v: string) => setF((p) => ({ ...p, [k]: v }))

  function validate(): string {
    if (Object.values(f).some((v) => !v.trim())) return 'Không được để trống dữ liệu'                              // nhánh 5.b
    if (!/^0[0-9]{9}$/.test(f.sdt)) return 'Số điện thoại không đúng định dạng (10 số, bắt đầu bằng 0)'          // nhánh 5.a
    if (!/^[a-zA-Z0-9_.]{4,50}$/.test(f.tenDangNhap)) return 'Tên đăng nhập từ 4 đến 50 ký tự, chỉ gồm chữ, số, dấu chấm hoặc gạch dưới'
    if (!/^\S+@\S+\.\S+$/.test(f.email)) return 'Email không đúng định dạng'
    if (f.matKhau.length < 6) return 'Mật khẩu phải có ít nhất 6 ký tự'
    if (f.matKhau !== f.xacNhanMatKhau) return 'Xác nhận mật khẩu không khớp'
    return ''
  }

  async function submit(e: FormEvent) {
    e.preventDefault()
    const v = validate()
    if (v) { setError(v); return }
    setError(''); setLoading(true)
    try {
      await authApi.register({ ...f, hoTen: f.hoTen.trim(), tenDangNhap: f.tenDangNhap.trim(), email: f.email.trim() })
      toast.success('Đăng ký thành công, vui lòng đăng nhập')
      navigate('/dang-nhap', { replace: true })
    } catch (err) {
      setError(errorText(err, 'Đăng ký thất bại'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <AuthLayout eyebrow="DÀNH CHO KHÁCH HÀNG" headline={<>Đặt bê tông,<br />theo dõi từng chuyến.</>}
      blurb="Tạo tài khoản khách hàng để quản lý công trình và đơn hàng của bạn.">
      <h1>Đăng ký tài khoản</h1>
      <p className="sub">Chỉ dành cho khách hàng. Tài khoản nhân viên do Quản lý cấp.</p>
      <form className="form-stack" onSubmit={submit} noValidate>
        <div className="form-grid">
          <Field label="Họ tên"><input value={f.hoTen} onChange={(e) => set('hoTen')(e.target.value)} autoFocus /></Field>
          <Field label="Số điện thoại"><input value={f.sdt} onChange={(e) => set('sdt')(e.target.value)} inputMode="tel" placeholder="0912345678" /></Field>
        </div>
        <Field label="Tên đăng nhập" hint="4–50 ký tự: chữ, số, dấu chấm, gạch dưới"><input value={f.tenDangNhap} onChange={(e) => set('tenDangNhap')(e.target.value)} autoComplete="username" /></Field>
        <Field label="Email"><input type="email" value={f.email} onChange={(e) => set('email')(e.target.value)} autoComplete="email" /></Field>
        <div className="form-grid">
          <Field label="Mật khẩu" hint="Tối thiểu 6 ký tự"><PasswordInput value={f.matKhau} onChange={set('matKhau')} autoComplete="new-password" required={false} /></Field>
          <Field label="Xác nhận mật khẩu"><PasswordInput value={f.xacNhanMatKhau} onChange={set('xacNhanMatKhau')} autoComplete="new-password" required={false} /></Field>
        </div>
        {error && <div className="form-error" role="alert"><CircleAlert size={16} style={{ flexShrink: 0, marginTop: 1 }} />{error}</div>}
        <button className="btn btn-primary btn-block" disabled={loading}>
          {loading && <LoaderCircle size={16} className="spin" />}{loading ? 'Đang tạo tài khoản…' : 'Tạo tài khoản'}
        </button>
      </form>
      <div className="divider">hoặc</div>
      <button type="button" className="btn google-btn" onClick={() => window.location.assign(googleLoginUrl)}>
        <span className="google-logo">G</span>Đăng ký bằng Google
      </button>
      <p className="switch">Đã có tài khoản? <Link to="/dang-nhap">Quay lại đăng nhập</Link></p>
    </AuthLayout>
  )
}
