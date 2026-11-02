import { getToken } from '../session'

export const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'
const CLIENT_KEY_STORAGE_KEY = 'betong_client_key'
export const googleLoginUrl = `${API_URL}/api/auth/dang-nhap-google`

function getClientKey(): string | null {
  const configuredKey = import.meta.env.VITE_CLIENT_KEY?.trim()
  if (configuredKey) return configuredKey
  return localStorage.getItem(CLIENT_KEY_STORAGE_KEY)?.trim() || 'betong-web-local'
}

export class ApiError extends Error {
  status: number
  constructor(message: string, status: number) {
    super(message)
    this.status = status
  }
}

let unauthorizedHandler: (() => void) | null = null
/** AuthProvider đăng ký hàm này để tự đăng xuất khi token hết hạn / bị thu hồi. */
export function setUnauthorizedHandler(fn: (() => void) | null) {
  unauthorizedHandler = fn
}

function defaultMessage(status: number): string {
  if (status === 0) return 'Không kết nối được máy chủ, vui lòng kiểm tra lại kết nối'
  if (status === 400) return 'Dữ liệu nhập sai hoặc còn thiếu'
  if (status === 401) return 'Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại'
  if (status === 403) return 'Không đúng quyền hoặc chưa đổi mật khẩu lần đầu'
  if (status === 404) return 'Không tìm thấy dữ liệu yêu cầu'
  if (status === 415) return 'Lỗi 415 - Unsupported Media Type: máy chủ không chấp nhận định dạng dữ liệu gửi lên. Theo API mới, các thông tin báo cáo phải nằm trên query và phần multipart chỉ chứa ảnh.'
  if (status === 409) return 'Dữ liệu đang có liên quan hoặc xung đột'
  if (status >= 500) return 'Lỗi server/cơ sở dữ liệu, vui lòng thử lại sau'
  return `Yêu cầu thất bại (mã ${status})`
}

/**
 * Backend trả lỗi dạng { "message": "..." } (ErrorResponse). Chỉ lấy đúng phần
 * message này để hiển thị — không bao giờ đẩy nguyên chuỗi JSON lên giao diện.
 * Các lỗi do Spring tự sinh (không có message) sẽ dùng câu tiếng Việt mặc định.
 */
async function readMessage(res: Response): Promise<string> {
  const text = await res.text().catch(() => '')
  if (text) {
    try {
      const data = JSON.parse(text) as { message?: unknown }
      if (typeof data?.message === 'string' && data.message.trim()) return data.message.trim()
    } catch {
      const plain = text.trim()
      if (plain && plain.length <= 160 && !plain.startsWith('<') && !plain.startsWith('{')) return plain
    }
  }
  return defaultMessage(res.status)
}

interface Options {
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  body?: unknown
  /** false cho các API công khai (đăng nhập, đăng ký, quên mật khẩu). */
  auth?: boolean
}

export async function request<T>(path: string, options: Options = {}): Promise<T> {
  const { method = 'GET', body, auth = true } = options
  const headers: Record<string, string> = {}
  if (!(body instanceof FormData)) headers['Content-Type'] = 'application/json'
  const clientKey = getClientKey()
  if (clientKey) headers.ClientKey = clientKey
  const token = auth ? getToken() : null
  if (token) headers.Authorization = `Bearer ${token}`

  let res: Response
  try {
    res = await fetch(`${API_URL}${path}`, {
      method,
      headers,
      body: body === undefined ? undefined : body instanceof FormData ? body : JSON.stringify(body),
    })
  } catch {
    throw new ApiError(defaultMessage(0), 0)
  }

  if (!res.ok) {
    if (res.status === 401 && auth) unauthorizedHandler?.()
    throw new ApiError(await readMessage(res), res.status)
  }
  if (res.status === 204) return undefined as T
  const text = await res.text()
  if (!text.trim()) return undefined as T
  try {
    return JSON.parse(text) as T
  } catch {
    const status = res.redirected ? 401 : res.status
    throw new ApiError(defaultMessage(status), status)
  }
}

export function query(params: Record<string, string | number | undefined | null>): string {
  const usp = new URLSearchParams()
  Object.entries(params).forEach(([k, v]) => {
    if (v !== undefined && v !== null && String(v).trim() !== '') usp.set(k, String(v).trim())
  })
  const s = usp.toString()
  return s ? `?${s}` : ''
}

/** Lấy message hiển thị từ mọi loại lỗi. */
export function errorText(err: unknown, fallback = 'Có lỗi xảy ra, vui lòng thử lại'): string {
  return err instanceof Error && err.message ? err.message : fallback
}
