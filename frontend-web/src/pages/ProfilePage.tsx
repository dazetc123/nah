import { useEffect, useState, type FormEvent } from 'react'
import { ArrowLeft, Camera, CircleAlert, KeyRound, LoaderCircle, Pencil } from 'lucide-react'
import PasswordInput from '../components/common/PasswordInput'
import StatusPill from '../components/common/StatusPill'
import { authApi, profileApi } from '../lib/api'
import { ApiError, errorText } from '../lib/api/client'
import { useToast } from '../lib/toast'
import type { TaiKhoan } from '../types/domain'
import UserAvatar from '../components/common/UserAvatar'

export default function ProfilePage() {
  const [profile, setProfile] = useState<TaiKhoan | null>(null)
  const [loadError, setLoadError] = useState('')
  const [editing, setEditing] = useState(false)
  const [changingPassword, setChangingPassword] = useState(false)

  useEffect(() => {
    profileApi.get().then(setProfile).catch((err) => setLoadError(errorText(err, 'Không thể tải hồ sơ')))
  }, [])

  return (
    <>
      <div className="page-head">
        <div><p className="eyebrow">HỒ SƠ TÀI KHOẢN</p><h1>Hồ sơ cá nhân</h1><p>Thông tin chi tiết và ảnh đại diện của tài khoản đang đăng nhập.</p></div>
      </div>
      {loadError && <div className="form-error" role="alert" style={{ marginBottom: 16 }}><CircleAlert size={16} />{loadError}</div>}
      {profile && <>
        <ProfileHeader profile={profile} editing={editing} changingPassword={changingPassword}
          onEdit={() => { setEditing((value) => !value); setChangingPassword(false) }}
          onChangePassword={() => { setChangingPassword((value) => !value); setEditing(false) }}
          onAvatarUpdated={setProfile} />
        {editing
          ? <ProfileEditForm profile={profile} onSaved={(next) => { setProfile(next); setEditing(false) }} onBack={() => setEditing(false)} />
          : changingPassword
            ? <PasswordChangeForm profile={profile} onDone={() => setChangingPassword(false)} onBack={() => setChangingPassword(false)} />
            : <ProfileDetails profile={profile} />}
      </>}
    </>
  )
}

function ProfileHeader({ profile, editing, changingPassword, onEdit, onChangePassword, onAvatarUpdated }: {
  profile: TaiKhoan
  editing: boolean
  changingPassword: boolean
  onEdit: () => void
  onChangePassword: () => void
  onAvatarUpdated: (profile: TaiKhoan) => void
}) {
  const toast = useToast()
  async function uploadAvatar(file: File) {
    try {
      await profileApi.uploadAvatar(file)
      const updated = await profileApi.get()
      const avatarUrl = updated.anhDaiDien || ''
      const separator = avatarUrl.includes('?') ? '&' : '?'
      localStorage.setItem('betong_avatar_url', avatarUrl ? `${avatarUrl}${separator}v=${Date.now()}` : '')
      window.dispatchEvent(new Event('betong-avatar-updated'))
      onAvatarUpdated(updated)
      toast.success('Đã cập nhật ảnh đại diện')
    } catch (err) {
      toast.error(errorText(err, 'Cập nhật ảnh đại diện thất bại'))
    }
  }

  return (
    <div className="profile-header-card">
      <div className="profile-identity">
        <UserAvatar key={profile.anhDaiDien ?? 'default-avatar'} name={profile.hoTen} src={profile.anhDaiDien} className="profile-avatar" preferStored />
        <label className="profile-avatar-upload" title="Đổi ảnh đại diện">
          <Camera size={16} />
          <input type="file" accept="image/*" hidden onChange={(e) => { const file = e.target.files?.[0]; if (file) uploadAvatar(file) }} />
        </label>
        <div className="profile-identity-copy">
          <h2>{profile.hoTen}</h2>
          <StatusPill tone="accent">{profile.tenVaiTro}</StatusPill>
          <p>{profile.email || 'Chưa cập nhật'}</p>
        </div>
      </div>
      <div className="profile-actions">
        <button className="btn btn-primary" onClick={onEdit}><Pencil size={15} />{editing ? 'Đóng cập nhật' : 'Cập nhật hồ sơ'}</button>
        <button className="btn" onClick={onChangePassword}><KeyRound size={15} />{changingPassword ? 'Đóng đổi mật khẩu' : 'Đổi mật khẩu'}</button>
      </div>
    </div>
  )
}

function ProfileDetails({ profile }: { profile: TaiKhoan }) {
  return (
    <div className="panel profile-info-card">
      <div className="panel-head"><h2>Thông tin cá nhân</h2></div>
      <div className="panel-body">
        <div className="profile-info-grid">
          <div className="profile-field"><span>Họ và tên</span><strong>{profile.hoTen || 'Chưa cập nhật'}</strong></div>
          <div className="profile-field"><span>Vai trò</span><strong><StatusPill tone="accent">{profile.tenVaiTro || 'Chưa cập nhật'}</StatusPill></strong></div>
          <div className="profile-field"><span>Email</span><strong>{profile.email || 'Chưa cập nhật'}</strong></div>
          <div className="profile-field"><span>Số điện thoại</span><strong>{profile.sdt || 'Chưa cập nhật'}</strong></div>
          <div className="profile-field"><span>Ngày sinh</span><strong>{profile.ngaySinh || 'Chưa cập nhật'}</strong></div>
          <div className="profile-field"><span>Giới tính</span><strong>{profile.gioiTinh || 'Chưa cập nhật'}</strong></div>
          <div className="profile-field profile-field-wide"><span>Địa chỉ</span><strong>{profile.diaChi || profile.diaChiThuongTru || 'Chưa cập nhật'}</strong></div>
          {profile.soGPLX && <div className="profile-field"><span>Số GPLX</span><strong>{profile.soGPLX}</strong></div>}
        </div>
      </div>
    </div>
  )
}

function ProfileEditForm({ profile, onSaved, onBack }: { profile: TaiKhoan; onSaved: (profile: TaiKhoan) => void; onBack: () => void }) {
  const toast = useToast()
  const [hoTen, setHoTen] = useState(profile.hoTen)
  const [sdt, setSdt] = useState(profile.sdt ?? '')
  const [email, setEmail] = useState(profile.email ?? '')
  const [diaChi, setDiaChi] = useState(profile.diaChi ?? '')
  const [ngaySinh, setNgaySinh] = useState(profile.ngaySinh ?? '')
  const [gioiTinh, setGioiTinh] = useState(profile.gioiTinh ?? '')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  async function submit(e: FormEvent) {
    e.preventDefault()
    if (!hoTen.trim()) { setError('Vui lòng nhập họ tên'); return }
    if (sdt.trim() && !/^0[0-9]{9}$/.test(sdt.trim())) { setError('Số điện thoại phải gồm 10 số và bắt đầu bằng 0'); return }
    if (email.trim() && !/^\S+@\S+\.\S+$/.test(email.trim())) { setError('Email không đúng định dạng'); return }
    setError(''); setLoading(true)
    try {
      const updated = await profileApi.update({ hoTen: hoTen.trim(), sdt: sdt.trim(), email: email.trim(), diaChi: diaChi.trim(), ngaySinh: ngaySinh || undefined, gioiTinh: gioiTinh || undefined })
      onSaved(updated)
      toast.success(updated.thongBao ?? 'Cập nhật thông tin thành công')
    } catch (err) { setError(errorText(err, 'Cập nhật thông tin thất bại, yêu cầu thử lại sau')) } finally { setLoading(false) }
  }

  return <div className="panel"><div className="panel-head"><h2><button className="icon-btn profile-back" onClick={onBack} aria-label="Quay lại"><ArrowLeft size={17} /></button>Cập nhật thông tin</h2></div><div className="panel-body">
    <form className="form-stack" onSubmit={submit} noValidate>
      <div className="form-grid"><div className="field"><label>Họ và tên</label><input value={hoTen} onChange={(e) => setHoTen(e.target.value)} /></div><div className="field"><label>Email</label><input type="email" value={email} onChange={(e) => setEmail(e.target.value)} /></div></div>
      <div className="form-grid"><div className="field"><label>Số điện thoại</label><input value={sdt} onChange={(e) => setSdt(e.target.value)} /></div><div className="field"><label>Ngày sinh</label><input type="date" value={ngaySinh} onChange={(e) => setNgaySinh(e.target.value)} /></div></div>
      <div className="form-grid"><div className="field"><label>Giới tính</label><select value={gioiTinh} onChange={(e) => setGioiTinh(e.target.value)}><option value="">Chọn giới tính</option><option value="Nam">Nam</option><option value="Nữ">Nữ</option><option value="Khác">Khác</option></select></div><div className="field"><label>Địa chỉ</label><input value={diaChi} onChange={(e) => setDiaChi(e.target.value)} /></div></div>
      {error && <div className="form-error" role="alert"><CircleAlert size={16} />{error}</div>}
      <button className="btn btn-primary" disabled={loading}>{loading && <LoaderCircle size={15} className="spin" />}Lưu cập nhật</button>
    </form>
  </div></div>
}

function PasswordChangeForm({ profile, onDone, onBack }: { profile: TaiKhoan; onDone: () => void; onBack: () => void }) {
  const toast = useToast()
  const [mode, setMode] = useState<'current' | 'otp'>('current')
  const [verified, setVerified] = useState(false)
  const [currentPassword, setCurrentPassword] = useState('')
  const [otp, setOtp] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirm, setConfirm] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  async function continueWithCurrentPassword() {
    if (!currentPassword) { setError('Vui lòng nhập mật khẩu hiện tại'); return }
    setError(''); setLoading(true)
    try {
      await authApi.login(profile.tenDangNhap, currentPassword)
      setVerified(true)
    } catch (err) {
      setError(err instanceof ApiError && (err.status === 401 || err.status === 403)
        ? 'Mật khẩu hiện tại không đúng'
        : errorText(err, 'Không thể kiểm tra mật khẩu hiện tại'))
    } finally { setLoading(false) }
  }

  async function sendOtp() {
    setError(''); setLoading(true)
    try {
      await authApi.forgot(profile.email || profile.tenDangNhap, profile.email ? 'EMAIL' : 'SMS')
      setMode('otp')
      toast.success('Mã OTP đã được gửi. Vui lòng kiểm tra email hoặc SMS.')
    } catch (err) { setError(errorText(err, 'Không thể gửi OTP')) } finally { setLoading(false) }
  }

  async function submit(e: FormEvent) {
    e.preventDefault()
    if (mode === 'otp' && !/^\d{6}$/.test(otp)) { setError('Mã OTP phải gồm 6 chữ số'); return }
    if (newPassword.length < 8) { setError('Mật khẩu mới phải có ít nhất 8 ký tự'); return }
    if (newPassword !== confirm) { setError('Xác nhận mật khẩu không khớp'); return }
    setError(''); setLoading(true)
    try {
      if (mode === 'otp') {
        await authApi.reset({ dinhDanh: profile.email || profile.tenDangNhap, maOtp: otp, matKhauMoi: newPassword, xacNhanMatKhauMoi: confirm })
      } else {
        await profileApi.changePassword({ matKhauHienTai: currentPassword, matKhauMoi: newPassword, xacNhanMatKhauMoi: confirm })
      }
      toast.success('Đổi mật khẩu thành công')
      onDone()
    } catch (err) { setError(errorText(err, 'Đổi mật khẩu thất bại, yêu cầu thử lại sau')) } finally { setLoading(false) }
  }

  return <div className="panel"><div className="panel-head"><h2><button className="icon-btn profile-back" onClick={onBack} aria-label="Quay lại"><ArrowLeft size={17} /></button>Đổi mật khẩu</h2></div><div className="panel-body">
    {mode === 'current' && !verified
      ? <div className="form-stack">
        <div className="field"><label>Mật khẩu hiện tại</label><PasswordInput value={currentPassword} onChange={setCurrentPassword} required={false} /></div>
        {error && <div className="form-error" role="alert"><CircleAlert size={16} />{error}</div>}
        <div className="profile-form-actions"><button type="button" className="btn btn-primary" onClick={continueWithCurrentPassword} disabled={loading}>{loading && <LoaderCircle size={15} className="spin" />}Tiếp tục</button></div>
      </div>
      : <form className="form-stack" onSubmit={submit} noValidate>
        {mode === 'otp' && <div className="field"><label>Mã OTP</label><input inputMode="numeric" maxLength={6} value={otp} onChange={(e) => setOtp(e.target.value.replace(/\D/g, ''))} placeholder="Nhập 6 chữ số" /></div>}
        <div className="field"><label>Mật khẩu mới</label><PasswordInput value={newPassword} onChange={setNewPassword} required={false} /></div>
        <div className="field"><label>Xác nhận mật khẩu mới</label><PasswordInput value={confirm} onChange={setConfirm} required={false} /></div>
        {error && <div className="form-error" role="alert"><CircleAlert size={16} />{error}</div>}
        <div className="profile-form-actions"><button type="button" className="btn" onClick={() => { setError(''); mode === 'otp' ? setMode('current') : setVerified(false) }}>Quay lại</button>{mode === 'otp' ? <button type="button" className="btn" onClick={sendOtp} disabled={loading}>Gửi lại OTP</button> : <button type="button" className="btn" onClick={sendOtp} disabled={loading}>{loading && <LoaderCircle size={15} className="spin" />}Gửi mã OTP</button>}<button className="btn btn-primary" disabled={loading}>{loading && <LoaderCircle size={15} className="spin" />}Cập nhật mật khẩu</button></div>
      </form>}
  </div></div>
}
