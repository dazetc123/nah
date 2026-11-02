import { useEffect, useState } from 'react'
import { ExternalLink, Image, MapPin, Pause, Play, RefreshCw, TriangleAlert } from 'lucide-react'
import Pagination from '../components/common/Pagination'
import StatusPill from '../components/common/StatusPill'
import TableState from '../components/common/TableState'
import { dispatchTripApi } from '../lib/api'
import { usePagedList } from '../lib/hooks'
import type { TheoDoiXe } from '../types/domain'

const REFRESH_MS = 10_000
/** Hằng số ngoài component để reload() giữ nguyên tham chiếu, bộ đếm không bị khởi động lại. */
const NO_FILTERS = {}

const time = (v?: string | null) => v ? new Date(v).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }) : null

/** "12 giây trước" / "3 phút trước" — để điều phối biết vị trí xe còn mới không. */
function ago(v?: string | null): string {
  if (!v) return ''
  const s = Math.max(0, Math.round((Date.now() - new Date(v).getTime()) / 1000))
  if (s < 60) return `${s} giây trước`
  if (s < 3600) return `${Math.floor(s / 60)} phút trước`
  return `${Math.floor(s / 3600)} giờ trước`
}

function tone(trangThai: number) {
  if (trangThai === 0) return 'warning' as const
  if (trangThai === 4) return 'ok' as const
  return 'info' as const
}

/** Mục 2.2.2 / 2.2.3 phía điều phối: theo dõi các chuyến đang hoạt động và mọi cập nhật tài xế gửi về. */
export default function DispatchTrackingPage() {
  const [auto, setAuto] = useState(true)
  const [updatedAt, setUpdatedAt] = useState<Date | null>(null)
  const { data, rows, setTrang, loading, error, reload } = usePagedList(
    async (page) => { const res = await dispatchTripApi.tracking(page, 20); setUpdatedAt(new Date()); return res }, NO_FILTERS,
  )

  useEffect(() => {
    if (!auto) return
    const timer = window.setInterval(() => { void reload() }, REFRESH_MS)
    return () => window.clearInterval(timer)
  }, [auto, reload])

  return (
    <>
      <div className="page-head">
        <div>
          <p className="eyebrow">ĐIỀU PHỐI</p>
          <h1>Theo dõi xe</h1>
          <p>Các chuyến đang hoạt động: trạng thái, vị trí GPS mới nhất và tiến độ tài xế cập nhật từ app. Chuyến hoàn thành sẽ tự rời khỏi danh sách.</p>
        </div>
        <div className="row-actions">
          <button className="btn" onClick={() => setAuto((a) => !a)}>{auto ? <><Pause size={16} />Tạm dừng tự làm mới</> : <><Play size={16} />Bật tự làm mới</>}</button>
          <button className="btn btn-primary" onClick={reload}><RefreshCw size={16} />Tải lại</button>
        </div>
      </div>

      <div className="panel">
        <div className="toolbar">
          <span className="hint">
            {auto ? `Tự làm mới mỗi ${REFRESH_MS / 1000} giây` : 'Đã tạm dừng tự làm mới'}
            {updatedAt && ` · Cập nhật lúc ${updatedAt.toLocaleTimeString('vi-VN')}`}
          </span>
        </div>
        <div className="table-wrap">
          <table>
            <thead><tr><th>Chuyến</th><th>Xe · Tài xế</th><th>Trạng thái</th><th>Vị trí GPS mới nhất</th><th>Tiến độ tài xế gửi về</th></tr></thead>
            <tbody>
              <TableState cols={5} loading={loading} error={error} empty={rows.length === 0} searching={false}
                emptyText="Hiện không có chuyến nào đang hoạt động." noMatchText="" />
              {rows.map((c) => <TripRow key={c.idChuyen} c={c} />)}
            </tbody>
          </table>
        </div>
        <Pagination page={data} onChange={setTrang} />
      </div>
    </>
  )
}

function TripRow({ c }: { c: TheoDoiXe }) {
  const hasGps = c.viDo != null && c.kinhDo != null
  return (
    <tr>
      <td><b>#{c.idChuyen}</b><br /><span className="hint">Đơn #{c.idDH} · {c.tenTram}</span></td>
      <td><span className="plate">{c.bienSo}</span><br />{c.tenTaiXe}</td>
      <td><StatusPill tone={tone(c.trangThaiChuyen)}>{c.tenTrangThai}</StatusPill></td>
      <td>
        {hasGps ? <>
          <a href={`https://www.google.com/maps?q=${c.viDo},${c.kinhDo}`} target="_blank" rel="noreferrer">
            <MapPin size={14} style={{ verticalAlign: '-2px' }} /> {c.viDo!.toFixed(5)}, {c.kinhDo!.toFixed(5)} <ExternalLink size={12} />
          </a>
          <br /><span className="hint">{ago(c.thoiDiemGPS)}{c.tocDo != null ? ` · ${(c.tocDo * 3.6).toFixed(0)} km/h` : ''}</span>
        </> : <span className="hint">{c.trangThaiChuyen >= 2 ? 'Chưa nhận được vị trí từ xe' : 'Chưa bắt đầu chuyến'}</span>}
      </td>
      <td style={{ fontSize: 13, lineHeight: 1.6 }}>
        {c.thoiGianXuatPhat && <div>Xuất phát: <b>{time(c.thoiGianXuatPhat)}</b></div>}
        {c.thoiGianDen && <div>
          Đến công trình: <b>{time(c.thoiGianDen)}</b>
          {c.khoangCachDen != null && ` · cách ${Math.round(c.khoangCachDen)} m`}
          {c.canKiemTraDen && <div><StatusPill tone="critical">Cần kiểm tra vị trí</StatusPill></div>}
          {c.ghiChuDen && <div className="hint"><TriangleAlert size={12} style={{ verticalAlign: '-2px' }} /> {c.ghiChuDen}</div>}
        </div>}
        {c.khoiLuongThucGiao != null && <div>
          Đã giao: <b>{c.khoiLuongThucGiao} m³</b> lúc {time(c.thoiGianGiaoXong)}
          {c.anhMinhChung && <> · <a href={c.anhMinhChung} target="_blank" rel="noreferrer"><Image size={12} style={{ verticalAlign: '-2px' }} /> Ảnh minh chứng</a></>}
        </div>}
        {!c.thoiGianXuatPhat && !c.thoiGianDen && c.khoiLuongThucGiao == null && <span className="hint">Chưa có cập nhật</span>}
      </td>
    </tr>
  )
}
