import { useEffect, useState } from 'react'
import { Pencil, Plus, Trash2, UserMinus, UserPlus, Wrench } from 'lucide-react'
import SearchBox from '../components/common/SearchBox'
import Pagination from '../components/common/Pagination'
import StatusPill from '../components/common/StatusPill'
import TableState from '../components/common/TableState'
import ConfirmDialog from '../components/common/ConfirmDialog'
import VehicleFormModal from '../components/vehicles/VehicleFormModal'
import AssignDriverModal from '../components/vehicles/AssignDriverModal'
import { driverApi, vehicleApi } from '../lib/api'
import { errorText } from '../lib/api/client'
import { useDebounced, usePagedList } from '../lib/hooks'
import { useToast } from '../lib/toast'
import type { Xe } from '../types/domain'

type Filters = { keyword: string; trangThai: string }

/** Bảng 3.15 (danh sách) + 3.16 (tìm kiếm) + 3.17–3.20 (thêm/sửa/xoá/trạng thái) + gán/hủy gán tài xế. */
export default function VehiclesPage() {
  const toast = useToast()
  const [driverNames, setDriverNames] = useState<Record<number, string>>({})
  const [keyword, setKeyword] = useState('')
  const [trangThai, setTrangThai] = useState('')
  const debounced = useDebounced<Filters>({ keyword, trangThai })
  const { data, rows, trang, setTrang, loading, error, reload } = usePagedList(
    (page, f: Filters) => vehicleApi.list(f.keyword, page, 10, f.trangThai === '' ? undefined : Number(f.trangThai)), debounced,
  )
  const [editing, setEditing] = useState<Xe | 'new' | null>(null)
  const [removing, setRemoving] = useState<Xe | null>(null)
  const [maintaining, setMaintaining] = useState<Xe | null>(null)
  const [assigning, setAssigning] = useState<Xe | null>(null)

  useEffect(() => {
    driverApi.list('', 1, 100)
      .then((page) => setDriverNames(Object.fromEntries(page.danhSach.map((driver) => [driver.idTX, driver.hoTen]))))
      .catch(() => setDriverNames({}))
  }, [])

  async function confirmRemove() {
    if (!removing) return
    try { const res = await vehicleApi.remove(removing.idXe); toast.success(res.message); setRemoving(null); reload() }
    catch (err) { toast.error(errorText(err)); setRemoving(null) }
  }
  async function confirmMaintain() {
    if (!maintaining) return
    const next = maintaining.trangThai === 1 ? 0 : 1
    try { const res = await vehicleApi.setStatus(maintaining.idXe, next); toast.success(res.thongBao ?? 'Cập nhật trạng thái thành công'); setMaintaining(null); reload() }
    catch (err) { toast.error(errorText(err)); setMaintaining(null) }
  }
  async function unassign(xe: Xe) {
    try { const res = await vehicleApi.unassignDriver(xe.idXe); toast.success(res.thongBao ?? 'Đã hủy gán tài xế'); reload() }
    catch (err) { toast.error(errorText(err)) }
  }

  return (
    <>
      <div className="page-head">
        <div><p className="eyebrow">BẢNG 3.15–3.20</p><h1>Đội xe</h1><p>Quản lý xe bồn, trạng thái vận hành và gán tài xế phụ trách.</p></div>
        <button className="btn btn-primary" onClick={() => setEditing('new')}><Plus size={16} />Thêm xe</button>
      </div>

      <div className="panel">
        <div className="toolbar">
          <SearchBox value={keyword} onChange={setKeyword} placeholder="Tìm theo biển số xe…" />
          <select value={trangThai} onChange={(e) => setTrangThai(e.target.value)}>
            <option value="">Tất cả trạng thái</option><option value="1">Đang hoạt động</option><option value="0">Bảo trì</option>
          </select>
        </div>
        <div className="table-wrap">
          <table>
            <thead><tr><th>Biển số</th><th>Trọng tải</th><th>Tài xế phụ trách</th><th>Trạng thái</th><th></th></tr></thead>
            <tbody>
              <TableState cols={5} loading={loading} error={error} empty={rows.length === 0} searching={Boolean(keyword || trangThai)}
                emptyText="Chưa có xe nào." noMatchText="Không tìm thấy xe phù hợp." />
              {rows.map((xe) => (
                <tr key={xe.idXe}>
                  <td className="plate">{xe.bienSo}</td>
                  <td>{xe.trongTai} m³</td>
                  <td>
                    {xe.idTX ? (
                      <span className="row-actions" style={{ alignItems: 'center' }}>
                        <span className="tag">{driverNames[xe.idTX] || `Tài xế #${xe.idTX}`}</span>
                        <button className="icon-btn" title="Hủy gán" onClick={() => unassign(xe)}><UserMinus size={16} /></button>
                      </span>
                    ) : (
                      <button className="btn btn-sm" onClick={() => setAssigning(xe)}><UserPlus size={14} />Gán tài xế</button>
                    )}
                  </td>
                  <td><StatusPill tone={xe.trangThai === 1 ? 'ok' : 'neutral'}>{xe.trangThai === 1 ? 'Đang hoạt động' : 'Bảo trì'}</StatusPill></td>
                  <td className="actions">
                    <div className="row-actions">
                      <button className="icon-btn" title="Sửa" onClick={() => setEditing(xe)}><Pencil size={16} /></button>
                      <button className="icon-btn" title={xe.trangThai === 1 ? 'Chuyển bảo trì' : 'Kích hoạt lại'} onClick={() => setMaintaining(xe)}><Wrench size={16} /></button>
                      <button className="icon-btn" title="Xoá" style={{ color: 'var(--danger)' }} onClick={() => setRemoving(xe)}><Trash2 size={16} /></button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <Pagination page={data} onChange={setTrang} />
      </div>

      {editing && <VehicleFormModal editing={editing === 'new' ? null : editing} onClose={() => setEditing(null)} onSaved={() => { setEditing(null); toast.success(editing === 'new' ? 'Thêm xe thành công' : 'Cập nhật xe thành công'); reload() }} />}
      {assigning && <AssignDriverModal xe={assigning} onClose={() => setAssigning(null)} onSaved={() => { setAssigning(null); toast.success('Gán tài xế thành công'); reload() }} />}
      {removing && <ConfirmDialog title="Xoá xe" danger confirmLabel="Xoá xe" message={`Bạn có chắc chắn muốn xoá xe "${removing.bienSo}"? Hành động không thể hoàn tác.`} onConfirm={confirmRemove} onClose={() => setRemoving(null)} />}
      {maintaining && <ConfirmDialog title={maintaining.trangThai === 1 ? 'Chuyển xe sang nghỉ' : 'Đưa xe vào hoạt động'} confirmLabel="Xác nhận" message={`Cập nhật xe "${maintaining.bienSo}" thành ${maintaining.trangThai === 1 ? 'nghỉ/bảo trì' : 'đang hoạt động'}? Báo cáo hư hỏng do tài xế hoặc điều phối viên thực hiện.`} onConfirm={confirmMaintain} onClose={() => setMaintaining(null)} />}
    </>
  )
}
