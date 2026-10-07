import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { CircleAlert, LoaderCircle } from 'lucide-react'
import AuthLayout from '../components/layout/AuthLayout'
import Field from '../components/common/Field'
import PasswordInput from '../components/common/PasswordInput'
import { authApi } from '../lib/api'
import { errorText } from '../lib/api/client'
import { useToast } from '../lib/toast'

/** Quên mật khẩu: bước 1 gửi mã OTP (email/SMS) → bước 2 nhập OTP + mật khẩu mới. */
export default function ForgotPasswordPage() {
  const navigate = useNavigate()
  const toast = useToast()
  const [step, setStep] = useState<1 | 2>(1)
  const [dinhDanh, setDinhDanh] = useState('')
  const [kenh, setKenh] = useState<'EMAIL' | 'SMS'>('EMAIL')
  const [maOtp, setMaOtp] = useState('')
  const [matKhauMoi, setMatKhauMoi] = useState('')
  const [xacNhan, setXacNhan] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  async function sendOtp(e: FormEvent) {
    e.preventDefault()
    if (!dinhDanh.trim()) { setError('Vui lòng nhập email, số điện thoại hoặc tên đăng nhập'); return }
    setError(''); setLoading(true)
    try {
      const res = await authApi.forgot(dinhDanh.trim(), kenh)
      toast.success(res.message)
      setStep(2)
    } catch (err) { setError(errorText(err)) } finally { setLoading(false) }
  }

  async function reset(e: FormEvent) {
    e.preventDefault()
    if (!/^[0-9]{6}$/.test(maOtp)) { setError('Mã OTP phải gồm 6 chữ số'); return }
    if (matKhauMoi.length < 8) { setError('Mật khẩu phải có ít nhất 8 ký tự'); return }
    if (matKhauMoi !== xacNhan) { setError('Xác nhận mật khẩu mới không khớp'); return }
    setError(''); setLoading(true)
    try {
      const res = await authApi.reset({ dinhDanh: dinhDanh.trim(), maOtp, matKhauMoi, xacNhanMatKhauMoi: xacNhan })
      toast.success(res.message)
      navigate('/dang-nhap', { replace: true })
    } catch (err) { setError(errorText(err)) } finally { setLoading(false) }
  }

  return (
    <AuthLayout eyebrow="KHÔI PHỤC TÀI KHOẢN" headline={<>Lấy lại quyền<br />truy cập.</>}
      blurb="Mã OTP gồm 6 chữ số sẽ được gửi qua email hoặc SMS đã đăng ký với tài khoản.">
      <h1>Quên mật khẩu</h1>
      <p className="sub">{step === 1 ? 'Nhập thông tin tài khoản để nhận mã OTP.' : `Nhập mã OTP đã gửi cho "${dinhDanh}" và đặt mật khẩu mới.`}</p>
      {step === 1 ? (
        <form className="form-stack" onSubmit={sendOtp} noValidate>
          <Field label="Email, số điện thoại hoặc tên đăng nhập"><input value={dinhDanh} onChange={(e) => setDinhDanh(e.target.value)} autoFocus /></Field>
          <Field label="Nhận mã qua">
            <select value={kenh} onChange={(e) => setKenh(e.target.value as 'EMAIL' | 'SMS')}>
              <option value="EMAIL">Email</option><option value="SMS">SMS</option>
            </select>
          </Field>
          {error && <div className="form-error" role="alert"><CircleAlert size={16} style={{ flexShrink: 0, marginTop: 1 }} />{error}</div>}
          <button className="btn btn-primary btn-block" disabled={loading}>{loading && <LoaderCircle size={16} className="spin" />}Gửi mã OTP</button>
        </form>
      ) : (
        <form className="form-stack" onSubmit={reset} noValidate>
          <Field label="Mã OTP (6 chữ số)"><input value={maOtp} onChange={(e) => setMaOtp(e.target.value.replace(/\D/g, '').slice(0, 6))} inputMode="numeric" autoFocus /></Field>
          <Field label="Mật khẩu mới" hint="Tối thiểu 8 ký tự"><PasswordInput value={matKhauMoi} onChange={setMatKhauMoi} autoComplete="new-password" required={false} /></Field>
          <Field label="Xác nhận mật khẩu mới"><PasswordInput value={xacNhan} onChange={setXacNhan} autoComplete="new-password" required={false} /></Field>
          {error && <div className="form-error" role="alert"><CircleAlert size={16} style={{ flexShrink: 0, marginTop: 1 }} />{error}</div>}
          <button className="btn btn-primary btn-block" disabled={loading}>{loading && <LoaderCircle size={16} className="spin" />}Đặt lại mật khẩu</button>
          <button type="button" className="btn btn-block" onClick={() => { setStep(1); setError('') }}>← Nhập lại thông tin</button>
        </form>
      )}
      <p className="switch"><Link to="/dang-nhap">Quay lại đăng nhập</Link></p>
    </AuthLayout>
  )
}
