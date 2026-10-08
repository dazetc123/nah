import { Navigate, Outlet } from 'react-router-dom'
import { isDispatcher, useAuth } from '../../lib/auth'

/** Các trang điều phối (backend cũng chặn ở /api/dieu-phoi/**). */
export default function RequireDispatcher() {
  const { session } = useAuth()
  return isDispatcher(session) ? <Outlet /> : <Navigate to="/" replace />
}
