import { useEffect, useState } from 'react'
import { CircleAlert, Eye, LoaderCircle, MapPin, Search, XCircle } from 'lucide-react'
import ConfirmDialog from '../components/common/ConfirmDialog'
import { customerOrderApi } from '../lib/api'
import { errorText } from '../lib/api/client'
import { useToast } from '../lib/toast'
import type { DonHang } from '../types/domain'

const money = (v?: number | null) => v == null ? '—' : new Intl.NumberFormat('vi-VN').format(v) + ' đ'

export default function CustomerOrdersPage() {
  const toast = useToast()
  const [orders, setOrders] = useState<DonHang[]>([])
  const [keyword, setKeyword] = useState('')
  const [search, setSearch] = useState('')
  const [selected, setSelected] = useState<DonHang | null>(null)
  const [tracking, setTracking] = useState<DonHang | null>(null)
  const [cancelId, setCancelId] = useState<number | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  async function load() {
    setLoading(true); setError('')
    try { setOrders((await customerOrderApi.list(search, 1, 100)).danhSach) }
    catch (err) { setError(errorText(err, 'Không thể tải danh sách đơn hàng')) }
    finally { setLoading(false) }
  }
  useEffect(() => { void load() }, [search])
  useEffect(() => {
    if (!tracking) return
    const timer = window.setInterval(async () => {
      try { setTracking(await customerOrderApi.track(tracking.idDH)) } catch { /* giữ dữ liệu hiện tại */ }
    }, 10000)
    return () => window.clearInterval(timer)
  }, [tracking?.idDH])

  async function cancel() {
    if (cancelId == null) return
    try { const response = await customerOrderApi.cancel(cancelId); toast.success(response.thongBao ?? 'Hủy đơn hàng thành công'); setCancelId(null); await load() }
    catch (err) { toast.error(errorText(err, 'Không thể hủy đơn hàng')); setCancelId(null) }
  }

  return <>
    <div className="page-head"><div><p className="eyebrow">KHÁCH HÀNG</p><h1>Quản lý đơn hàng</h1><p>Xem chi tiết, theo dõi và hủy đơn hàng đang chờ xử lý.</p></div></div>
    
    <div className="toolbar" style={{ flexWrap: 'wrap' }}>
      <div className="order-steps" style={{ margin: 0, padding: 0, border: 'none', background: 'transparent' }}>
        <span className={search === '' ? 'active' : ''} onClick={() => { setKeyword(''); setSearch('') }} style={{ cursor: 'pointer' }}>Tất cả</span>
        <span className={search === '0' ? 'active' : ''} onClick={() => { setKeyword('0'); setSearch('0') }} style={{ cursor: 'pointer' }}>Chờ duyệt</span>
        <span className={search === '3' ? 'active' : ''} onClick={() => { setKeyword('3'); setSearch('3') }} style={{ cursor: 'pointer' }}>Đang giao</span>
        <span className={search === '4' ? 'active' : ''} onClick={() => { setKeyword('4'); setSearch('4') }} style={{ cursor: 'pointer' }}>Hoàn thành</span>
      </div>
      <div style={{ flex: 1 }} />
    </div>

    {error && <div className="form-error"><CircleAlert size={16} />{error}</div>}
    <div className="panel"><div className="panel-body">
      {loading ? <div className="empty-state"><LoaderCircle className="spin" />Đang tải đơn hàng...</div> : orders.length === 0 ? <div className="empty-state">Không có kết quả</div> :
        <div className="table-wrap"><table><thead><tr><th>Mã đơn</th><th>Loại bê tông</th><th>Ngày đặt</th><th>Tổng tiền</th><th>Trạng thái</th><th>Thao tác</th></tr></thead><tbody>{orders.filter(o => search === '' || String(o.trangThai) === search).map(order => <tr key={order.idDH}><td>#{order.idDH}</td><td>{order.macBeTong ?? '—'} ({order.khoiLuong} m³)</td><td>{order.ngayDat ?? '—'}</td><td>{money(order.tongTien)}</td><td><span className="status-badge">{order.tenTrangThai}</span></td><td><div className="row-actions"><button className="btn btn-sm" title="Xem chi tiết" onClick={async () => setSelected(await customerOrderApi.detail(order.idDH))}><Eye size={14} /> Chi tiết</button>{[0, 1, 3].includes(order.trangThai) && <button className="btn btn-sm btn-primary" title="Theo dõi" onClick={async () => setTracking(await customerOrderApi.track(order.idDH))}><MapPin size={14} /> Theo dõi</button>}{order.trangThai === 0 && <button className="btn btn-sm" style={{ color: 'var(--danger)' }} title="Hủy đơn" onClick={() => setCancelId(order.idDH)}><XCircle size={14} /> Hủy</button>}</div></td></tr>)}</tbody></table></div>}
    </div></div>
    {selected && <OrderDialog order={selected} title={`Chi tiết đơn hàng #${selected.idDH}`} onClose={() => setSelected(null)} />}
    {tracking && <OrderDialog order={tracking} title={`Theo dõi đơn hàng #${tracking.idDH}`} tracking onClose={() => setTracking(null)} />}
    {cancelId != null && <ConfirmDialog title="Hủy đơn hàng" message="Bạn có chắc chắn muốn hủy đơn hàng này?" confirmLabel="Xác nhận hủy" danger onConfirm={cancel} onClose={() => setCancelId(null)} />}
  </>
}

function OrderDialog({ order, title, tracking, onClose }: { order: DonHang; title: string; tracking?: boolean; onClose: () => void }) {
  return <div className="modal-backdrop"><div className="modal"><div className="modal-head"><h2>{title}</h2><button className="icon-btn" onClick={onClose}><XCircle size={18} /></button></div><div className="modal-body"><p><b>Trạng thái:</b> {order.tenTrangThai}</p><p><b>Loại bê tông:</b> {order.macBeTong ?? '—'} — {order.khoiLuong} m³</p><p><b>Địa chỉ giao:</b> {order.diaChiGiao}</p><p><b>Thời gian giao:</b> {order.thoiGianGiao ? new Date(order.thoiGianGiao).toLocaleString('vi-VN') : '—'}</p><p><b>Tổng tiền:</b> {money(order.tongTien)}</p>{order.ghiChu && <p><b>Ghi chú:</b> {order.ghiChu}</p>}{order.lyDoTuChoi && <p><b>Lý do:</b> {order.lyDoTuChoi}</p>}{tracking && order.viDo != null && order.kinhDo != null ? <p><b>Vị trí xe {order.bienSoXe ?? ''}:</b> {order.viDo}, {order.kinhDo} — <a href={`https://www.google.com/maps?q=${order.viDo},${order.kinhDo}`} target="_blank" rel="noreferrer">Mở bản đồ</a></p> : tracking && <div className="notice">Chưa có thông tin vị trí xe.</div>}</div></div></div>
}
