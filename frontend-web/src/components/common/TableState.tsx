import { CircleAlert } from 'lucide-react'

/** Hàng thông báo trong bảng: lỗi tải dữ liệu / chưa có dữ liệu / không có kết quả tìm kiếm. */
export default function TableState({ cols, loading, error, empty, searching, emptyText, noMatchText }: {
  cols: number; loading: boolean; error: string; empty: boolean; searching: boolean; emptyText: string; noMatchText: string
}) {
  if (error) return <tr><td colSpan={cols} className="empty-state" style={{ color: 'var(--danger)' }}><CircleAlert size={16} style={{ verticalAlign: '-3px', marginRight: 6 }} />{error}</td></tr>
  if (loading && empty) return <tr><td colSpan={cols} className="empty-state">Đang tải dữ liệu…</td></tr>
  if (empty) return <tr><td colSpan={cols} className="empty-state">{searching ? noMatchText : emptyText}</td></tr>
  return null
}
