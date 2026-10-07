import type { Session } from '../types/domain'

const KEY = 'betong_session'

export function loadSession(): Session | null {
  try {
    const raw = localStorage.getItem(KEY)
    return raw ? (JSON.parse(raw) as Session) : null
  } catch {
    localStorage.removeItem(KEY)
    return null
  }
}

export function saveSession(session: Session) {
  localStorage.setItem(KEY, JSON.stringify(session))
  localStorage.setItem('accessToken', session.accessToken)
}

export function clearSession() {
  localStorage.removeItem(KEY)
  localStorage.removeItem('accessToken')
}

export function getToken(): string | null {
  return loadSession()?.accessToken ?? localStorage.getItem('accessToken')
}
