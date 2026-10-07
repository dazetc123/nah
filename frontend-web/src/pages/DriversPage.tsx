import { useEffect, useState } from 'react'
import { Pencil, Plus, Trash2 } from 'lucide-react'
import SearchBox from '../components/common/SearchBox'
import Pagination from '../components/common/Pagination'
import StatusPill from '../components/common/StatusPill'
import TableState from '../components/common/TableState'
import ConfirmDialog from '../components/common/ConfirmDialog'
import DriverFormModal from '../components/drivers/DriverFormModal'
import { driverApi } from '../lib/api'
import { errorText } from '../lib/api/client'
import { useToast } from '../lib/toast'
import { useDebounced, usePagedList } from '../lib/hooks'
import type { TaiXe } from '../types/domain'

export default function DriversPage() {
  const toast = useToast()
  
  const [keyword, setKeyword] = useState('')
  const debounced = useDebounced(keyword)
  
  const { data, rows, loading, error, reload, setTrang } = usePagedList(
    (page, kw) => driverApi.list(kw, page, 20), debounced
  )
  
  const [editing, setEditing] = useState<TaiXe | 'new' | null>(null)
  const [removing, setRemoving] = useState<TaiXe | null>(null)

  async function remove() {
    if (!removing) return
    try {
      await driverApi.remove(removing.idTX)
      toast.success('Xóa tài xế thành công')
      setRemoving(null)
      reload()
    } catch (err) {
      toast.error(errorText(err, 'Không thể xóa tài xế'))
      setRemoving(null)
    }
  }

  return <>
    <div className="page-head">
      <div>
        <p className="eyebrow">QUẢN LÝ VẬN HÀNH</p>
        <h1>Tài xế</h1>
        <p>Quản lý hồ sơ, giấy phép và thông tin liên hệ của tài xế.</p>
      </div>
      <button className="btn btn-primary" onClick={() => setEditing('new')}><Plus size={16} />Thêm tài xế</button>
    </div>
    
    <div className="panel">
      <div className="toolbar">
        <SearchBox value={keyword} onChange={setKeyword} placeholder="Tìm theo họ tên, số GPLX, số điện thoại…" />
      </div>
      <div className="table-wrap">
        <table>
          <thead>
            <tr><th>Họ tên</th><th>Số GPLX</th><th>Số điện thoại</th><th>Xe đang gán</th><th>Trạng thái</th><th /></tr>
          </thead>
          <tbody>
            <TableState cols={6} loading={loading} error={error} empty={!rows.length} searching={Boolean(debounced)} emptyText="Chưa có hồ sơ tài xế." noMatchText="Không tìm thấy tài xế." />
            {rows.map((driver) => <tr key={driver.idTX}>
              <td><b>{driver.hoTen}</b></td>
              <td>{driver.soGPLX}</td>
              <td>{driver.sdt}</td>
              <td>{driver.bienSoXeDangGan || 'Chưa gán'}</td>
              <td><StatusPill tone={driver.trangThai === 1 ? 'ok' : 'neutral'}>{driver.trangThai === 1 ? 'Đang hoạt động' : 'Tạm dừng'}</StatusPill></td>
              <td className="actions">
                <div className="row-actions">
                  <button className="icon-btn" title="Sửa" onClick={() => setEditing(driver)}><Pencil size={16} /></button>
                  <button className="icon-btn" title="Xóa" style={{ color: 'var(--danger)' }} onClick={() => setRemoving(driver)}><Trash2 size={16} /></button>
                </div>
              </td>
            </tr>)}
          </tbody>
        </table>
      </div>
      <Pagination page={data} onChange={setTrang} />
    </div>

    {editing && <DriverFormModal editing={editing} onClose={() => setEditing(null)} onSaved={() => { setEditing(null); reload() }} />}
    {removing && <ConfirmDialog title="Xóa tài xế" message={`Bạn có chắc muốn xóa tài xế ${removing.hoTen}?`} confirmLabel="Xóa tài xế" danger onConfirm={remove} onClose={() => setRemoving(null)} />}
  </>
}
