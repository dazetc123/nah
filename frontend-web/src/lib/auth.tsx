import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import { authApi } from './api'
import { setUnauthorizedHandler } from './api/client'
import { clearSession, loadSession, saveSession } from './session'
import type { LoginResponse, Session } from '../types/domain'

interface AuthState {
  session: Session | null
  login: (dinhDanh: string, matKhau: string) => Promise<Session>
  /** Dùng sau đăng nhập Google hoặc đổi mật khẩu lần đầu. */
  applySession: (res: LoginResponse) => Session
  logout: () => Promise<void>
}

const AuthContext = createContext<AuthState | null>(null)

function toSession(res: LoginResponse): Session {
  return {
    accessToken: res.accessToken,
    idTK: res.idTK,
    hoTen: res.hoTen,
    tenVaiTro: res.tenVaiTro,
    phaiDoiMatKhau: Boolean(res.phaiDoiMatKhau),
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<Session | null>(() => loadSession())

  const applySession = useCallback((res: LoginResponse) => {
    const s = toSession(res)
    saveSession(s)
    setSession(s)
    return s
  }, [])

  const login = useCallback(async (dinhDanh: string, matKhau: string) => {
    return applySession(await authApi.login(dinhDanh, matKhau))
  }, [applySession])

  const logout = useCallback(async () => {
    // Backend thu hồi token (TokenRevocationService); lỗi mạng không được chặn việc đăng xuất ở phía giao diện.
    try { await authApi.logout() } catch { /* bỏ qua */ }
    clearSession()
    setSession(null)
  }, [])

  // Token hết hạn / bị thu hồi -> API trả 401 -> tự đưa về trang đăng nhập.
  useEffect(() => {
    setUnauthorizedHandler(() => { clearSession(); setSession(null) })
    return () => setUnauthorizedHandler(null)
  }, [])

  const value = useMemo(() => ({ session, login, applySession, logout }), [session, login, applySession, logout])
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth phải nằm trong <AuthProvider>')
  return ctx
}

export const isManager = (s: Session | null) => s?.tenVaiTro === 'Quản lý'
export const isDispatcher = (s: Session | null) => s?.tenVaiTro === 'Nhân viên điều phối'
