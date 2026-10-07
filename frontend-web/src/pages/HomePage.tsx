import { Link } from 'react-router-dom'
import { Factory, ShieldCheck, Truck, User, Users, Info, Boxes, FileWarning, Building2, ShoppingCart } from 'lucide-react'
import { useAuth, isManager } from '../lib/auth'

const CARDS = [
  { to: '/tai-khoan', icon: Users, title: 'Tài khoản', desc: 'Cấp, sửa, khoá/mở tài khoản nhân viên điều phối & tài xế' },
  { to: '/phan-quyen', icon: ShieldCheck, title: 'Phân quyền', desc: 'Đổi vai trò giữa Nhân viên điều phối và Tài xế' },
  { to: '/xe', icon: Truck, title: 'Đội xe', desc: 'Quản lý xe bồn, trạng thái vận hành và gán tài xế' },
  { to: '/tram-tron', icon: Factory, title: 'Trạm trộn', desc: 'Quản lý trạm trộn và công suất sản xuất' },
  { to: '/loai-be-tong', icon: Boxes, title: 'Loại bê tông', desc: 'Quản lý mác, thành phần và đơn giá' },
  { to: '/bao-cao-xe', icon: FileWarning, title: 'Báo cáo xe', desc: 'Gửi báo cáo tình trạng xe' },
  { to: '/cong-trinh', icon: Building2, title: 'Công trình', desc: 'Quản lý công trình của khách hàng' },
]

export default function HomePage() {
  const { session } = useAuth()
  if (!session) return null

  return (
    <>
      <div className="page-head">
        <div><p className="eyebrow">TỔNG QUAN</p><h1>Xin chào, {session.hoTen}</h1><p>Vai trò hiện tại: {session.tenVaiTro}</p></div>
      </div>
      {isManager(session) && (
        <div className="quick-grid">
          {CARDS.map(({ to, icon: Icon, title, desc }) => (
            <Link key={to} to={to} className="quick"><Icon size={22} /><b>{title}</b><span>{desc}</span></Link>
          ))}
        </div>
      )}
      {session.tenVaiTro === 'Tài xế' && <div className="quick-grid" style={{ marginTop: 14 }}><Link to="/bao-cao-xe" className="quick"><FileWarning size={22} /><b>Báo cáo tình trạng xe</b><span>Gửi nội dung và ảnh minh chứng cho xe đang phụ trách</span></Link></div>}
      {session.tenVaiTro === 'Khách hàng' && <div className="quick-grid" style={{ marginTop: 14 }}><Link to="/dat-be-tong" className="quick"><ShoppingCart size={22} /><b>Đặt bê tông</b><span>Chọn mác, khối lượng và thời gian giao hàng</span></Link></div>}
      <div className="quick-grid" style={{ marginTop: 14 }}>
        <Link to="/ho-so" className="quick"><User size={22} /><b>Hồ sơ cá nhân</b><span>Xem, cập nhật thông tin và đổi mật khẩu</span></Link>
      </div>
    </>
  )
}
