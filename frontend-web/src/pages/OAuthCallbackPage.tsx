import { useEffect, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../lib/auth'
import { clearSession } from '../lib/session'

/**
 * Nhận kết quả đăng nhập Google từ backend qua URL fragment (#accessToken=...&...).
 * Fragment không được gửi lên máy chủ và bị xoá khỏi thanh địa chỉ ngay sau khi đọc.
 * Cần backend đã áp dụng backend-patches/GoogleOAuth2SuccessHandler.java.
 */
export default function OAuthCallbackPage() {
  const { applySession } = useAuth()
  const navigate = useNavigate()
  const handledRef = useRef(false)

  useEffect(() => {
    if (handledRef.current) return
    handledRef.current = true

    const hashParams = new URLSearchParams(window.location.hash.replace(/^#/, ''))
    const queryParams = new URLSearchParams(window.location.search)
    const params = new URLSearchParams(hashParams)
    queryParams.forEach((value, key) => params.set(key, value))
    const err = params.get('error') || params.get('error_description')
    const token = params.get('accessToken')

    // Remove the token from the address bar only after it has been read.
    window.history.replaceState(null, '', '/oauth2/callback')

    if (err) {
      clearSession()
      navigate('/dang-nhap', {
        replace: true,
        state: { oauthCancelled: true, oauthError: err },
      })
      return
    }

    // React Strict Mode may mount this page twice in development. On the
    // second pass the fragment is already gone, so never clear a session that
    // was successfully persisted by the first pass.
    if (!token) {
      navigate('/dang-nhap', { replace: true })
      return
    }

    const s = applySession({
      accessToken: token,
      idTK: Number(params.get('idTK') ?? 0),
      hoTen: params.get('hoTen') ?? '',
      tenVaiTro: params.get('tenVaiTro') ?? '',
      phaiDoiMatKhau: params.get('phaiDoiMatKhau') === 'true',
    })
    navigate(s.phaiDoiMatKhau ? '/doi-mat-khau-lan-dau' : '/', { replace: true })
  }, [applySession, navigate])

  return <div className="loading-screen">Đang hoàn tất đăng nhập Google…</div>
}
