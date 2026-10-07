import { useState } from 'react'
import { Navigate, Outlet, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../../lib/auth'
import Sidebar from './Sidebar'
import Topbar from './Topbar'

const TITLES: Record<string, string> = {
  '/': 'Tổng quan', '/tai-khoan': 'Tài khoản', '/phan-quyen': 'Phân quyền',
  '/xe': 'Đội xe', '/tram-tron': 'Trạm trộn', '/ho-so': 'Hồ sơ cá nhân',
  '/loai-be-tong': 'Loại bê tông', '/bao-cao-xe': 'Báo cáo xe', '/cong-trinh': 'Công trình', '/dat-be-tong': 'Đặt bê tông', '/don-hang': 'Quản lý đơn hàng',
}

/** Khung cho khu vực đã đăng nhập; chặn người chưa đăng nhập hoặc còn bị buộc đổi mật khẩu lần đầu. */
export default function AppShell() {
  const { session, logout } = useAuth()
  const [open, setOpen] = useState(false)
  const { pathname } = useLocation()
  const navigate = useNavigate()

  if (!session) return <Navigate to="/dang-nhap" replace />
  if (session.phaiDoiMatKhau) return <Navigate to="/doi-mat-khau-lan-dau" replace />

  return (
    <div className="app">
      <Sidebar session={session} open={open} onClose={() => setOpen(false)}
        onLogout={async () => { await logout(); navigate('/dang-nhap', { replace: true }) }} />
      <main className="main">
        <Topbar title={TITLES[pathname] ?? 'Tổng quan'} onMenu={() => setOpen(true)} />
        <section className="content"><Outlet /></section>
      </main>
    </div>
  )
}
