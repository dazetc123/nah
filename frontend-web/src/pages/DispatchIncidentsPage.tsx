import { useEffect, useState } from 'react'
import { CheckCircle2, ExternalLink, Image, RefreshCw, Siren, Wrench } from 'lucide-react'
import ConfirmDialog from '../components/common/ConfirmDialog'
import Pagination from '../components/common/Pagination'
import StatusPill from '../components/common/StatusPill'
import TableState from '../components/common/TableState'
import { dispatchIncidentApi } from '../lib/api'
import { errorText } from '../lib/api/client'
import { usePagedList } from '../lib/hooks'
import { useToast } from '../lib/toast'
import type { SuCo } from '../types/domain'

const REFRESH_MS = 15_000
const TABS: { value: number | undefined; label: string }[] = [
  { value: 0, label: 'Mới tiếp nhận' },
  { value: 1, label: 'Đang xử lý' },
  { value: 2, label: 'Đã xử lý' },
  { value: undefined, label: 'Tất cả' },
]
const SERIOUS = ['Hỏng xe', 'Tai nạn']

const dateTime = (v?: string | null) => v ? new Date(v).toLocaleString('vi-VN', { hour: '2-digit', minute: '2-digit', day: '2-digit', month: '2-digit' }) : '—'

function priorityTone(muc?: number | null) {
  if (muc === 3) return 'critical' as const
  if (muc === 1) return 'neutral' as const
  return 'warning' as const
}
function statusTone(t?: number | null) {
  if (t === 2) return 'ok' as const
  if (t === 1) return 'info' as const
  return 'danger' as const
}

/** Mục 2.2.3 "Báo cáo sự cố" bước 6 và luồng 6.a phía điều phối: nhận sự cố tài xế gửi lên và cập nhật xử lý. */
export default function DispatchIncidentsPage() {
  const toast = useToast()
  const [tab, setTab] = useState<number | undefined>(0)
  const { data, rows, setTrang, loading, error, reload } = usePagedList(
    (page, f: { tab: number | undefined }) => dispatchIncidentApi.list(f.tab, page, 15), { tab },
  )
  const [changing, setChanging] = useState<{ suCo: SuCo; next: 1 | 2 } | null>(null)

  // Tự làm mới để sự cố mới từ tài xế hiện lên mà không cần bấm tải lại
  useEffect(() => {
    const timer = window.setInterval(() => { void reload() }, REFRESH_MS)
    return () => window.clearInterval(timer)
  }, [reload])

  async function changeStatus() {
    if (!changing) return
    try {
      const res = await dispatchIncidentApi.setStatus(changing.suCo.idSuCo, changing.next)
      toast.success(`Sự cố #${res.idSuCo}: ${res.tenTrangThai}`)
      setChanging(null); reload()
    } catch (err) { toast.error(errorText(err)); setChanging(null); reload() }
  }

  const urgentNew = tab === 0 ? rows.filter((s) => s.mucDoUuTien === 3).length : 0

  return (
    <>
      <div className="page-head">
        <div>
          <p className="eyebrow">ĐIỀU PHỐI</p>
          <h1>Sự cố từ tài xế</h1>
          <p>Sự cố tài xế báo từ app trong lúc chạy chuyến. Mức ưu tiên cao xếp trước; "Hỏng xe" và "Tai nạn" cần điều động xe thay thế.</p>
        </div>
        <button className="btn" onClick={reload}><RefreshCw size={16} />Tải lại</button>
      </div>

      {urgentNew > 0 && <div className="form-error" style={{ marginBottom: 12 }}>
        <Siren size={16} /> Có {urgentNew} sự cố mức ưu tiên Cao đang chờ tiếp nhận.
      </div>}

      <div className="panel">
        <div className="toolbar" style={{ flexWrap: 'wrap', gap: 8 }}>
          {TABS.map((t) => (
            <button key={t.label} className={tab === t.value ? 'btn btn-sm btn-primary' : 'btn btn-sm'} onClick={() => setTab(t.value)}>{t.label}</button>
          ))}
          <span className="hint" style={{ marginLeft: 8 }}>Tự làm mới mỗi {REFRESH_MS / 1000} giây</span>
        </div>
        <div className="table-wrap">
          <table>
            <thead><tr><th>Mã</th><th>Thời điểm</th><th>Chuyến · Xe · Tài xế</th><th>Loại sự cố</th><th>Mô tả</th><th>Vị trí · Ảnh</th><th>Trạng thái</th><th></th></tr></thead>
            <tbody>
              <TableState cols={8} loading={loading} error={error} empty={rows.length === 0} searching={false}
                emptyText="Không có sự cố nào." noMatchText="" />
              {rows.map((s) => {
                const coords = s.viTri?.match(/^\s*(-?\d+(\.\d+)?)\s*,\s*(-?\d+(\.\d+)?)\s*$/)
                return (
                  <tr key={s.idSuCo}>
                    <td>#{s.idSuCo}</td>
                    <td>{dateTime(s.thoiDiem)}</td>
                    <td>Chuyến #{s.idChuyen ?? '—'}<br /><span className="plate">{s.bienSo ?? '—'}</span> · {s.tenTaiXe ?? s.nguoiBaoCao ?? '—'}</td>
                    <td>
                      <b>{s.loaiSuCo}</b><br />
                      <StatusPill tone={priorityTone(s.mucDoUuTien)}>{`Ưu tiên ${s.tenMucDoUuTien ?? 'Trung bình'}`}</StatusPill>
                      {SERIOUS.includes(s.loaiSuCo) && s.trangThai !== 2 && <div className="hint" style={{ color: 'var(--danger)' }}>Cần điều động xe thay thế</div>}
                    </td>
                    <td style={{ maxWidth: 260 }}>{s.moTa}</td>
                    <td style={{ fontSize: 13 }}>
                      {coords ? <a href={`https://www.google.com/maps?q=${coords[1]},${coords[3]}`} target="_blank" rel="noreferrer">Xem vị trí <ExternalLink size={12} /></a>
                        : <span className="hint">{s.viTri ?? 'Không có vị trí'}</span>}
                      {s.anhMinhChung && <><br /><a href={s.anhMinhChung} target="_blank" rel="noreferrer"><Image size={12} style={{ verticalAlign: '-2px' }} /> Xem ảnh</a></>}
                    </td>
                    <td><StatusPill tone={statusTone(s.trangThai)}>{s.tenTrangThai ?? 'Mới tiếp nhận'}</StatusPill></td>
                    <td className="actions">
                      <div className="row-actions">
                        {(s.trangThai ?? 0) === 0 && <button className="btn btn-sm btn-primary" onClick={() => setChanging({ suCo: s, next: 1 })}><Wrench size={14} />Nhận xử lý</button>}
                        {(s.trangThai ?? 0) < 2 && <button className="btn btn-sm" onClick={() => setChanging({ suCo: s, next: 2 })}><CheckCircle2 size={14} />Đã xử lý</button>}
                      </div>
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </div>
        <Pagination page={data} onChange={setTrang} />
      </div>

      {changing && <ConfirmDialog
        title={changing.next === 1 ? 'Nhận xử lý sự cố' : 'Đánh dấu đã xử lý'}
        confirmLabel={changing.next === 1 ? 'Nhận xử lý' : 'Đã xử lý'}
        message={`Sự cố #${changing.suCo.idSuCo} (${changing.suCo.loaiSuCo}) của xe ${changing.suCo.bienSo ?? ''} sẽ chuyển sang "${changing.next === 1 ? 'Đang xử lý' : 'Đã xử lý'}". Tài xế sẽ thấy trạng thái mới.`}
        onConfirm={changeStatus} onClose={() => setChanging(null)} />}
    </>
  )
}
