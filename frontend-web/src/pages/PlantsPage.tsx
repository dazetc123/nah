import { useState } from 'react'
import { Gauge, Pencil, Plus, Trash2 } from 'lucide-react'
import SearchBox from '../components/common/SearchBox'
import Pagination from '../components/common/Pagination'
import StatusPill from '../components/common/StatusPill'
import TableState from '../components/common/TableState'
import ConfirmDialog from '../components/common/ConfirmDialog'
import PlantFormModal from '../components/plants/PlantFormModal'
import CapacityModal from '../components/plants/CapacityModal'
import { plantApi } from '../lib/api'
import { errorText } from '../lib/api/client'
import { useDebounced, usePagedList } from '../lib/hooks'
import { useToast } from '../lib/toast'
import type { TramTron } from '../types/domain'

/** Bảng 3.21 (thêm) 3.22/3.24 (danh sách+tìm kiếm) 3.23 (sửa) 3.25 (xóa) + Quản lý công suất. */
export default function PlantsPage() {
  const toast = useToast()
  const [keyword, setKeyword] = useState('')
  const debounced = useDebounced(keyword)
  const { data, rows, trang, setTrang, loading, error, reload } = usePagedList(
    (page, kw) => plantApi.list(kw, page, 10), debounced,
  )
  const [editing, setEditing] = useState<TramTron | 'new' | null>(null)
  const [capacityTarget, setCapacityTarget] = useState<TramTron | null>(null)
  const [removing, setRemoving] = useState<TramTron | null>(null)

  async function confirmRemove() {
    if (!removing) return
    try { const res = await plantApi.remove(removing.idTram); toast.success(res.message); setRemoving(null); reload() }
    catch (err) { toast.error(errorText(err)); setRemoving(null) }
  }

  return (
    <>
      <div className="page-head">
        <div><p className="eyebrow">BẢNG 3.21–3.25</p><h1>Trạm trộn</h1><p>Quản lý trạm trộn và công suất sản xuất.</p></div>
        <button className="btn btn-primary" onClick={() => setEditing('new')}><Plus size={16} />Thêm trạm trộn</button>
      </div>

      <div className="panel">
        <div className="toolbar"><SearchBox value={keyword} onChange={setKeyword} placeholder="Tìm theo tên trạm, địa chỉ…" /></div>
        <div className="table-wrap">
          <table>
            <thead><tr><th>Trạm trộn</th><th>Địa chỉ</th><th>SĐT</th><th>Công suất</th><th>Trạng thái</th><th></th></tr></thead>
            <tbody>
              <TableState cols={6} loading={loading} error={error} empty={rows.length === 0} searching={Boolean(debounced)}
                emptyText="Chưa có trạm trộn nào." noMatchText="Không tìm thấy trạm trộn phù hợp." />
              {rows.map((p) => (
                <tr key={p.idTram}>
                  <td style={{ fontWeight: 600 }}>{p.tenTram}</td>
                  <td>{p.diaChi}</td>
                  <td>{p.sdt}</td>
                  <td>{p.congSuat} m³/giờ</td>
                  <td><StatusPill tone={p.trangThai === 1 ? 'ok' : 'neutral'}>{p.trangThai === 1 ? 'Hoạt động' : 'Ngừng hoạt động'}</StatusPill></td>
                  <td className="actions">
                    <div className="row-actions">
                      <button className="icon-btn" title="Công suất" onClick={() => setCapacityTarget(p)}><Gauge size={16} /></button>
                      <button className="icon-btn" title="Sửa" onClick={() => setEditing(p)}><Pencil size={16} /></button>
                      <button className="icon-btn" title="Xoá" style={{ color: 'var(--danger)' }} onClick={() => setRemoving(p)}><Trash2 size={16} /></button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <Pagination page={data} onChange={setTrang} />
      </div>

      {editing && <PlantFormModal editing={editing === 'new' ? null : editing} onClose={() => setEditing(null)} onSaved={() => { setEditing(null); toast.success(editing === 'new' ? 'Thêm trạm trộn thành công' : 'Cập nhật trạm trộn thành công'); reload() }} />}
      {capacityTarget && <CapacityModal plant={capacityTarget} onClose={() => setCapacityTarget(null)} onSaved={() => { setCapacityTarget(null); toast.success('Cập nhật công suất thành công'); reload() }} />}
      {removing && <ConfirmDialog title="Xoá trạm trộn" danger confirmLabel="Xoá trạm" message={`Bạn có chắc chắn muốn xoá trạm trộn "${removing.tenTram}"? Hành động không thể hoàn tác.`} onConfirm={confirmRemove} onClose={() => setRemoving(null)} />}
    </>
  )
}
