import { Navigate, Outlet } from 'react-router-dom'
import { isManager, useAuth } from '../../lib/auth'

/** Các chức năng Bảng 3.3–3.22 đều chỉ dành cho Quản lý (backend cũng chặn ở /api/quan-ly/**). */
export default function RequireManager() {
  const { session } = useAuth()
  return isManager(session) ? <Outlet /> : <Navigate to="/" replace />
}
