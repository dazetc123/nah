import { useCallback, useEffect, useRef, useState } from 'react'
import { errorText } from './api/client'
import type { PageResponse } from '../types/domain'

export function useDebounced<T>(value: T, delay = 350): T {
  const [v, setV] = useState(value)
  useEffect(() => {
    const t = window.setTimeout(() => setV(value), delay)
    return () => window.clearTimeout(t)
  }, [value, delay])
  return v
}

/**
 * Danh sách phân trang phía server (khớp PageResponse của backend).
 * Đổi bộ lọc/từ khoá -> tự về trang 1; bỏ qua kết quả của request cũ nếu đã có request mới hơn.
 */
export function usePagedList<T, F>(
  fetcher: (trang: number, filters: F) => Promise<PageResponse<T>>,
  filters: F,
) {
  const [trang, setTrang] = useState(1)
  const [data, setData] = useState<PageResponse<T> | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const ticket = useRef(0)
  const fetcherRef = useRef(fetcher)
  fetcherRef.current = fetcher
  const key = JSON.stringify(filters)
  const lastKey = useRef(key)

  const load = useCallback(async (page: number, f: F) => {
    const mine = ++ticket.current
    setLoading(true)
    try {
      const res = await fetcherRef.current(page, f)
      if (mine !== ticket.current) return
      setData(res)
      setError('')
    } catch (err) {
      if (mine !== ticket.current) return
      setError(errorText(err, 'Không thể tải dữ liệu, vui lòng kiểm tra kết nối'))
    } finally {
      if (mine === ticket.current) setLoading(false)
    }
  }, [])

  useEffect(() => {
    const filterChanged = lastKey.current !== key
    lastKey.current = key
    const page = filterChanged ? 1 : trang
    if (filterChanged && trang !== 1) { setTrang(1); return }
    load(page, filters)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [trang, key, load])

  const reload = useCallback(() => load(trang, filters), [load, trang, filters])

  return { data, rows: data?.danhSach ?? [], trang, setTrang, loading, error, reload }
}
