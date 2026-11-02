import { ChevronLeft, ChevronRight } from 'lucide-react'
import type { PageResponse } from '../../types/domain'

export default function Pagination({ page, onChange }: { page: PageResponse<unknown> | null; onChange: (trang: number) => void }) {
  if (!page || page.tongSoPhanTu === 0) return null
  const { trangHienTai, tongSoTrang, tongSoPhanTu } = page
  return (
    <div className="pager">
      <span>Trang <b>{trangHienTai}</b> / {Math.max(tongSoTrang, 1)} · Tổng <b>{tongSoPhanTu}</b> bản ghi</span>
      <div className="btns">
        <button className="btn btn-sm" disabled={trangHienTai <= 1} onClick={() => onChange(trangHienTai - 1)}><ChevronLeft size={14} />Trước</button>
        <button className="btn btn-sm" disabled={trangHienTai >= tongSoTrang} onClick={() => onChange(trangHienTai + 1)}>Sau<ChevronRight size={14} /></button>
      </div>
    </div>
  )
}
