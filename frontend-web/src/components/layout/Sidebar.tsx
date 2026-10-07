import { NavLink } from 'react-router-dom'
import { useEffect, useState } from 'react'
import { Factory, LayoutDashboard, LogOut, ShieldCheck, Truck, User, Users, X, Activity, Boxes, FileWarning, Building2, ShoppingCart } from 'lucide-react'
import type { Session } from '../../types/domain'
import { isManager } from '../../lib/auth'
import { profileApi } from '../../lib/api'
import UserAvatar from '../common/UserAvatar'

const initials = (name: string) => name.split(' ').filter(Boolean).map((p) => p[0]).slice(-2).join('').toUpperCase() || 'U'

export default function Sidebar({ session, open, onClose, onLogout }: { session: Session; open: boolean; onClose: () => void; onLogout: () => void }) {
  const manager = isManager(session)
  const [avatar, setAvatar] = useState<string | null>(null)
  useEffect(() => {
    let active = true
    profileApi.get().then((profile) => {
      if (!active) return
      setAvatar(profile.anhDaiDien || null)
      if (profile.anhDaiDien) localStorage.setItem('betong_avatar_url', profile.anhDaiDien)
    }).catch(() => undefined)
    const refresh = () => setAvatar(localStorage.getItem('betong_avatar_url'))
    window.addEventListener('betong-avatar-updated', refresh)
    return () => { active = false; window.removeEventListener('betong-avatar-updated', refresh) }
  }, [])
  const link = (to: string, label: string, Icon: typeof Users, end = false) => (
    <NavLink key={to} to={to} end={end} onClick={onClose} className={({ isActive }) => (isActive ? 'nav-item active' : 'nav-item')}>
      <Icon size={18} />{label}
    </NavLink>
  )
  return (
    <aside className={`sidebar ${open ? 'open' : ''}`}>
      <div className="brand">
        <span className="brand-mark"><Activity size={19} /></span>
        <div><strong>Betong<span>Ops</span></strong><small>ĐIỀU HÀNH BÊ TÔNG</small></div>
        <button className="icon-btn close-mobile" onClick={onClose} aria-label="Đóng menu"><X size={18} /></button>
      </div>
      <nav>
        <p className="nav-caption">CHUNG</p>
        {link('/', 'Tổng quan', LayoutDashboard, true)}
        {manager && <>
          <p className="nav-caption" style={{ marginTop: 18 }}>QUẢN LÝ</p>
          {link('/tai-khoan', 'Tài khoản', Users)}
          {link('/phan-quyen', 'Phân quyền', ShieldCheck)}
          {link('/xe', 'Đội xe', Truck)}
          {link('/tai-xe', 'Tài xế', Users)}
          {link('/tram-tron', 'Trạm trộn', Factory)}
          {link('/loai-be-tong', 'Loại bê tông', Boxes)}
          {link('/bao-cao-xe', 'Báo cáo sự cố', FileWarning)}
          {link('/cong-trinh', 'Công trình', Building2)}
          {link('/doanh-thu', 'Báo cáo hoạt động', Activity)}
        </>}
        {session.tenVaiTro === 'Khách hàng' && link('/cong-trinh', 'Công trình của tôi', Building2)}
        {session.tenVaiTro === 'Khách hàng' && link('/dat-be-tong', 'Đặt bê tông', ShoppingCart)}
        {session.tenVaiTro === 'Khách hàng' && link('/don-hang', 'Quản lý đơn hàng', ShoppingCart)}
        {session.tenVaiTro === 'Tài xế' && link('/lich-trinh', 'Lịch trình chuyến đi', Truck)}
        {session.tenVaiTro === 'Tài xế' && link('/bao-cao-xe', 'Báo cáo tình trạng xe', FileWarning)}
        {session.tenVaiTro === 'Nhân viên điều phối' && link('/bao-cao-xe', 'Báo cáo tình trạng xe', FileWarning)}
        <p className="nav-caption" style={{ marginTop: 18 }}>CÁ NHÂN</p>
        {link('/ho-so', 'Hồ sơ cá nhân', User)}
      </nav>
      <div className="sidebar-bottom">
        <div className="user-card">
          <UserAvatar name={session.hoTen} src={avatar || session.anhDaiDien} preferStored />
          <div className="who"><b>{session.hoTen}</b><small>{session.tenVaiTro}</small></div>
          <button className="icon-btn" onClick={onLogout} aria-label="Đăng xuất" title="Đăng xuất"><LogOut size={17} /></button>
        </div>
      </div>
    </aside>
  )
}
