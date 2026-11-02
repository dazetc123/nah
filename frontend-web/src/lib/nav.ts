import {
  Activity, Boxes, Building2, ClipboardList, Factory, FileWarning, LayoutDashboard,
  Navigation, Route, ShieldCheck, ShoppingCart, Siren, Truck, User, UserCog, Users, type LucideIcon,
} from 'lucide-react'
import type { Session } from '../types/domain'

export interface NavItem {
  to: string
  label: string
  desc: string
  icon: LucideIcon
  end?: boolean
}

export interface NavGroup {
  caption: string
  items: NavItem[]
}

const MANAGER: NavItem[] = [
  { to: '/tai-khoan', label: 'Tài khoản', desc: 'Cấp, sửa, khoá/mở tài khoản nhân viên & khách hàng', icon: Users },
  { to: '/phan-quyen', label: 'Phân quyền', desc: 'Đổi vai trò giữa Điều phối và Tài xế', icon: ShieldCheck },
  { to: '/xe', label: 'Đội xe', desc: 'Xe bồn, trạng thái vận hành và gán tài xế', icon: Truck },
  { to: '/tai-xe', label: 'Tài xế', desc: 'Hồ sơ tài xế và giấy phép lái xe', icon: UserCog },
  { to: '/tram-tron', label: 'Trạm trộn', desc: 'Trạm trộn và công suất sản xuất', icon: Factory },
  { to: '/loai-be-tong', label: 'Loại bê tông', desc: 'Mác, thành phần và đơn giá', icon: Boxes },
  { to: '/cong-trinh', label: 'Công trình', desc: 'Công trình của khách hàng', icon: Building2 },
]

const MANAGER_OPS: NavItem[] = [
  { to: '/bao-cao-xe', label: 'Báo cáo sự cố', desc: 'Sự cố xe do tài xế/điều phối gửi lên', icon: FileWarning },
  { to: '/doanh-thu', label: 'Báo cáo hoạt động', desc: 'Doanh thu, sản lượng, xuất Excel/PDF', icon: Activity },
]

const CUSTOMER: NavItem[] = [
  { to: '/dat-be-tong', label: 'Đặt bê tông', desc: 'Chọn mác, khối lượng và thời gian giao', icon: ShoppingCart },
  { to: '/don-hang', label: 'Đơn hàng', desc: 'Theo dõi, xem chi tiết và hủy đơn', icon: ClipboardList },
  { to: '/cong-trinh', label: 'Công trình của tôi', desc: 'Địa điểm nhận bê tông', icon: Building2 },
]

const DRIVER: NavItem[] = [
  { to: '/lich-trinh', label: 'Lịch trình', desc: 'Các chuyến được phân công cho bạn', icon: Route },
  { to: '/bao-cao-xe', label: 'Báo cáo tình trạng xe', desc: 'Gửi sự cố kèm vị trí và ảnh', icon: FileWarning },
]

const DISPATCHER: NavItem[] = [
  { to: '/dieu-phoi/don-hang', label: 'Đơn hàng & tạo chuyến', desc: 'Duyệt đơn, phân bổ trạm, giao chuyến cho tài xế', icon: ClipboardList },
  { to: '/dieu-phoi/theo-doi-xe', label: 'Theo dõi xe', desc: 'Trạng thái, vị trí GPS và tiến độ các chuyến', icon: Navigation },
  { to: '/dieu-phoi/su-co', label: 'Sự cố từ tài xế', desc: 'Tiếp nhận và xử lý sự cố tài xế báo lên', icon: Siren },
]

export const HOME: NavItem = { to: '/', label: 'Tổng quan', desc: 'Trang chủ', icon: LayoutDashboard, end: true }
export const PROFILE: NavItem = { to: '/ho-so', label: 'Hồ sơ cá nhân', desc: 'Thông tin cá nhân và đổi mật khẩu', icon: User }

/** Menu theo vai trò — khớp với các route & quyền trong App.tsx. */
export function navFor(session: Session): NavGroup[] {
  const groups: NavGroup[] = [{ caption: 'Chung', items: [HOME] }]
  switch (session.tenVaiTro) {
    case 'Quản lý':
      groups.push({ caption: 'Quản lý', items: MANAGER }, { caption: 'Vận hành', items: MANAGER_OPS })
      break
    case 'Khách hàng':
      groups.push({ caption: 'Khách hàng', items: CUSTOMER })
      break
    case 'Tài xế':
      groups.push({ caption: 'Công việc', items: DRIVER })
      break
    case 'Nhân viên điều phối':
      groups.push({ caption: 'Điều phối', items: DISPATCHER })
      break
  }
  groups.push({ caption: 'Cá nhân', items: [PROFILE] })
  return groups
}

/** Lối tắt cho trang chủ (bỏ "Tổng quan" và "Hồ sơ"). */
export function quickLinksFor(session: Session): NavItem[] {
  return navFor(session).flatMap((g) => g.items).filter((i) => i !== HOME)
}

const ALL: NavItem[] = [HOME, PROFILE, ...MANAGER, ...MANAGER_OPS, ...CUSTOMER, ...DRIVER, ...DISPATCHER]

export function titleFor(pathname: string, session: Session): string {
  const own = navFor(session).flatMap((g) => g.items).find((i) => i.to === pathname)
  return (own ?? ALL.find((i) => i.to === pathname))?.label ?? 'Tổng quan'
}
