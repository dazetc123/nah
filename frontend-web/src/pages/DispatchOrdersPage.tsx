import { useEffect, useState } from 'react'
import { CheckCircle2, Factory, LoaderCircle, RefreshCw, Truck, XCircle } from 'lucide-react'
import Modal from '../components/common/Modal'
import Field from '../components/common/Field'
import ConfirmDialog from '../components/common/ConfirmDialog'
import Pagination from '../components/common/Pagination'
import StatusPill from '../components/common/StatusPill'
import TableState from '../components/common/TableState'
import { dispatchOrderApi, dispatchTripApi } from '../lib/api'
import { errorText } from '../lib/api/client'
import { usePagedList } from '../lib/hooks'
import { useToast } from '../lib/toast'
import type { DonHangDieuPhoi, TaiXe, TramKhaDung, Xe } from '../types/domain'

/** Các bước duyệt đơn của điều phối: Chờ xử lý (0) → Đã xác nhận (1) → Đã phân bổ trạm (6) → Tạo chuyến. */
const TABS = [
  { trangThai: 0, label: 'Chờ xử lý', hint: 'Đơn khách hàng vừa đặt trên web: xác nhận hoặc từ chối' },
  { trangThai: 1, label: 'Đã xác nhận', hint: 'Phân bổ trạm trộn cho đơn' },
  { trangThai: 6, label: 'Đã phân bổ trạm', hint: 'Tạo chuyến và giao cho tài xế' },
]

const money = (v?: number | null) => v == null ? '—' : new Intl.NumberFormat('vi-VN').format(v) + ' đ'
const dateTime = (v?: string | null) => v ? new Date(v).toLocaleString('vi-VN', { hour: '2-digit', minute: '2-digit', day: '2-digit', month: '2-digit' }) : '—'

export default function DispatchOrdersPage() {
  const toast = useToast()
  const [tab, setTab] = useState(0)
  const { data, rows, setTrang, loading, error, reload } = usePagedList(
    (page, f: { tab: number }) => dispatchOrderApi.list(f.tab, page, 10), { tab },
  )
  const [confirming, setConfirming] = useState<DonHangDieuPhoi | null>(null)
  const [rejecting, setRejecting] = useState<DonHangDieuPhoi | null>(null)
  const [assigning, setAssigning] = useState<DonHangDieuPhoi | null>(null)
  const [creating, setCreating] = useState<DonHangDieuPhoi | null>(null)

  async function confirm() {
    if (!confirming) return
    try {
      const res = await dispatchOrderApi.confirm(confirming.idDH)
      toast.success(res.thongBao ?? 'Xác nhận đơn hàng thành công')
      setConfirming(null); reload()
    } catch (err) { toast.error(errorText(err)); setConfirming(null); reload() }
  }

  const current = TABS.find((t) => t.trangThai === tab)!

  return (
    <>
      <div className="page-head">
        <div>
          <p className="eyebrow">ĐIỀU PHỐI</p>
          <h1>Đơn hàng & tạo chuyến</h1>
          <p>Duyệt đơn, phân bổ trạm trộn và giao chuyến cho tài xế. Chuyến tạo xong sẽ hiện ngay trên app tài xế ở trạng thái "Chờ nhận".</p>
        </div>
        <button className="btn" onClick={reload}><RefreshCw size={16} />Tải lại</button>
      </div>

      <div className="panel">
        <div className="toolbar" style={{ flexWrap: 'wrap', gap: 8 }}>
          {TABS.map((t) => (
            <button key={t.trangThai} className={tab === t.trangThai ? 'btn btn-sm btn-primary' : 'btn btn-sm'} onClick={() => setTab(t.trangThai)}>
              {t.label}
            </button>
          ))}
          <span className="hint" style={{ marginLeft: 8 }}>{current.hint}</span>
        </div>
        <div className="table-wrap">
          <table>
            <thead><tr><th>Mã đơn</th><th>Công trình / địa chỉ giao</th><th>Bê tông</th><th>Giao lúc</th><th>Tổng tiền</th><th>Ghi chú</th>{tab === 6 && <th>Trạm trộn</th>}<th></th></tr></thead>
            <tbody>
              <TableState cols={tab === 6 ? 8 : 7} loading={loading} error={error} empty={rows.length === 0} searching={false}
                emptyText={`Không có đơn hàng nào ở trạng thái "${current.label}".`} noMatchText="" />
              {rows.map((o) => (
                <tr key={o.idDH}>
                  <td>#{o.idDH}</td>
                  <td><b>{o.tenCongTrinh ?? '—'}</b><br /><span className="hint">{o.diaChiGiao}</span></td>
                  <td>{o.macBeTong ?? '—'} · {o.khoiLuong} m³</td>
                  <td>{dateTime(o.thoiGianGiao)}</td>
                  <td>{money(o.tongTien)}</td>
                  <td>{o.ghiChu ?? '—'}</td>
                  {tab === 6 && <td>{o.tenTram ?? '—'}</td>}
                  <td className="actions">
                    <div className="row-actions">
                      {tab === 0 && <>
                        <button className="btn btn-sm btn-primary" onClick={() => setConfirming(o)}><CheckCircle2 size={14} />Xác nhận</button>
                        <button className="btn btn-sm" style={{ color: 'var(--danger)' }} onClick={() => setRejecting(o)}><XCircle size={14} />Từ chối</button>
                      </>}
                      {tab === 1 && <button className="btn btn-sm btn-primary" onClick={() => setAssigning(o)}><Factory size={14} />Phân bổ trạm</button>}
                      {tab === 6 && <button className="btn btn-sm btn-primary" onClick={() => setCreating(o)}><Truck size={14} />Tạo chuyến</button>}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <Pagination page={data} onChange={setTrang} />
      </div>

      {confirming && <ConfirmDialog title="Xác nhận đơn hàng" confirmLabel="Xác nhận"
        message={`Xác nhận đơn hàng #${confirming.idDH} (${confirming.khoiLuong} m³, ${confirming.tenCongTrinh ?? confirming.diaChiGiao})?`}
        onConfirm={confirm} onClose={() => setConfirming(null)} />}
      {rejecting && <RejectModal order={rejecting} onClose={() => setRejecting(null)}
        onDone={(msg) => { setRejecting(null); toast.success(msg); reload() }} />}
      {assigning && <AssignPlantModal order={assigning} onClose={() => setAssigning(null)}
        onDone={(msg) => { setAssigning(null); toast.success(msg); reload() }} />}
      {creating && <CreateTripModal order={creating} onClose={() => setCreating(null)}
        onDone={(msg) => { setCreating(null); toast.success(msg); reload() }} />}
    </>
  )
}

function RejectModal({ order, onClose, onDone }: { order: DonHangDieuPhoi; onClose: () => void; onDone: (msg: string) => void }) {
  const [lyDo, setLyDo] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  async function submit() {
    if (!lyDo.trim()) { setError('Vui lòng nhập lý do từ chối'); return }
    setBusy(true); setError('')
    try { const res = await dispatchOrderApi.reject(order.idDH, lyDo.trim()); onDone(res.thongBao ?? 'Từ chối đơn hàng thành công') }
    catch (err) { setError(errorText(err)) }
    finally { setBusy(false) }
  }
  return (
    <Modal title={`Từ chối đơn hàng #${order.idDH}`} onClose={onClose} width={480}
      footer={<><button className="btn" onClick={onClose} disabled={busy}>Hủy</button>
        <button className="btn btn-danger-solid" onClick={submit} disabled={busy}>{busy && <LoaderCircle size={15} className="spin" />}Từ chối đơn</button></>}>
      <Field label="Lý do từ chối" hint="Khách hàng sẽ thấy lý do này">
        <textarea rows={3} value={lyDo} onChange={(e) => setLyDo(e.target.value)} placeholder="Ví dụ: Trạm trộn hết công suất trong ngày" />
      </Field>
      {error && <div className="form-error">{error}</div>}
    </Modal>
  )
}

function AssignPlantModal({ order, onClose, onDone }: { order: DonHangDieuPhoi; onClose: () => void; onDone: (msg: string) => void }) {
  const [plants, setPlants] = useState<TramKhaDung[] | null>(null)
  const [idTram, setIdTram] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  useEffect(() => {
    dispatchOrderApi.plants(order.idDH)
      .then((list) => { setPlants(list); if (list.length > 0) setIdTram(String(list[0].idTram)) })
      .catch((err) => { setPlants([]); setError(errorText(err)) })
  }, [order.idDH])
  async function submit() {
    if (!idTram) { setError('Vui lòng chọn trạm trộn'); return }
    setBusy(true); setError('')
    try { const res = await dispatchOrderApi.assignPlant(order.idDH, Number(idTram)); onDone(res.thongBao ?? 'Phân bổ trạm trộn thành công') }
    catch (err) { setError(errorText(err)) }
    finally { setBusy(false) }
  }
  return (
    <Modal title={`Phân bổ trạm trộn cho đơn #${order.idDH}`} onClose={onClose} width={520}
      footer={<><button className="btn" onClick={onClose} disabled={busy}>Hủy</button>
        <button className="btn btn-primary" onClick={submit} disabled={busy || !plants?.length}>{busy && <LoaderCircle size={15} className="spin" />}Phân bổ</button></>}>
      <p>Đơn cần <b>{order.khoiLuong} m³</b> bê tông {order.macBeTong ?? ''}.</p>
      {plants === null ? <div className="empty-state"><LoaderCircle className="spin" />Đang tìm trạm trộn khả dụng…</div>
        : plants.length === 0 ? <div className="notice">Không có trạm trộn nào đang hoạt động và đủ công suất cho đơn này.</div>
        : <Field label="Trạm trộn khả dụng">
            <select value={idTram} onChange={(e) => setIdTram(e.target.value)}>
              {plants.map((p) => <option key={p.idTram} value={p.idTram}>{p.tenTram} — {p.diaChi ?? ''} (công suất {p.congSuat ?? '?'} m³)</option>)}
            </select>
          </Field>}
      {error && <div className="form-error">{error}</div>}
    </Modal>
  )
}

function CreateTripModal({ order, onClose, onDone }: { order: DonHangDieuPhoi; onClose: () => void; onDone: (msg: string) => void }) {
  const [vehicles, setVehicles] = useState<Xe[] | null>(null)
  const [drivers, setDrivers] = useState<TaiXe[] | null>(null)
  const [idXe, setIdXe] = useState('')
  const [idTX, setIdTX] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    Promise.all([dispatchTripApi.freeVehicles(), dispatchTripApi.freeDrivers()])
      .then(([xe, tx]) => { setVehicles(xe.danhSach); setDrivers(tx.danhSach) })
      .catch((err) => { setVehicles([]); setDrivers([]); setError(errorText(err)) })
  }, [])

  /** Chọn xe thì tự chọn luôn tài xế đang được gán cho xe đó (nếu tài xế đang rảnh). */
  function chooseVehicle(value: string) {
    setIdXe(value); setError('')
    const xe = vehicles?.find((v) => String(v.idXe) === value)
    if (xe?.idTX && drivers?.some((d) => d.idTX === xe.idTX)) setIdTX(String(xe.idTX))
  }

  async function submit() {
    if (!idXe || !idTX) { setError('Vui lòng chọn xe và tài xế'); return }
    if (!order.idTram) { setError('Đơn hàng chưa có trạm trộn'); return }
    setBusy(true); setError('')
    try {
      const res = await dispatchTripApi.create({ idDH: order.idDH, idTram: order.idTram, idXe: Number(idXe), idTX: Number(idTX) })
      onDone(`${res.thongBao ?? 'Tạo chuyến thành công'} — Chuyến #${res.idChuyen}`)
    } catch (err) { setError(errorText(err)) }
    finally { setBusy(false) }
  }

  const loadingLists = vehicles === null || drivers === null
  return (
    <Modal title={`Tạo chuyến cho đơn #${order.idDH}`} onClose={onClose} width={560}
      footer={<><button className="btn" onClick={onClose} disabled={busy}>Hủy</button>
        <button className="btn btn-primary" onClick={submit} disabled={busy || loadingLists}>{busy && <LoaderCircle size={15} className="spin" />}Tạo chuyến</button></>}>
      <p>{order.tenCongTrinh ?? order.diaChiGiao} · <b>{order.khoiLuong} m³</b> {order.macBeTong ?? ''} · Trạm: <b>{order.tenTram ?? '—'}</b> · Giao lúc {dateTime(order.thoiGianGiao)}</p>
      {loadingLists ? <div className="empty-state"><LoaderCircle className="spin" />Đang tải xe và tài xế rảnh…</div> : <>
        <Field label="Xe rảnh">
          <select value={idXe} onChange={(e) => chooseVehicle(e.target.value)}>
            <option value="">— Chọn xe —</option>
            {vehicles!.map((v) => <option key={v.idXe} value={v.idXe}>{v.bienSo} ({v.trongTai} m³)</option>)}
          </select>
        </Field>
        <Field label="Tài xế rảnh" hint="Tài xế đang có chuyến chưa hoàn thành sẽ không hiện ở đây">
          <select value={idTX} onChange={(e) => { setIdTX(e.target.value); setError('') }}>
            <option value="">— Chọn tài xế —</option>
            {drivers!.map((d) => <option key={d.idTX} value={d.idTX}>{d.hoTen} · {d.sdt}{d.bienSoXeDangGan ? ` · xe ${d.bienSoXeDangGan}` : ''}</option>)}
          </select>
        </Field>
        {(vehicles!.length === 0 || drivers!.length === 0) && <div className="form-error">
          {vehicles!.length === 0 && <div>Không có xe rảnh: xe đang có chuyến chưa hoàn thành hoặc đang bảo trì.</div>}
          {drivers!.length === 0 && <div>Không có tài xế rảnh: tài xế đang có chuyến chưa hoàn thành (kể cả chuyến "Chờ nhận") thì không được giao thêm. Hãy hoàn thành chuyến cũ trên app, hoặc chạy du_lieu_test_dieu_phoi.sql để có tài xế rảnh (taixe03).</div>}
        </div>}
        <div className="notice"><StatusPill tone="info">Chờ nhận</StatusPill> Chuyến mới sẽ ở trạng thái Chờ nhận cho tới khi tài xế bấm "Nhận chuyến" trên app.</div>
      </>}
      {error && <div className="form-error">{error}</div>}
    </Modal>
  )
}
