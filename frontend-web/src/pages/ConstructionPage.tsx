import { useState } from 'react'
import { Pencil, Plus, Trash2 } from 'lucide-react'
import SearchBox from '../components/common/SearchBox'
import Pagination from '../components/common/Pagination'
import TableState from '../components/common/TableState'
import ConfirmDialog from '../components/common/ConfirmDialog'
import ConstructionFormModal from '../components/constructions/ConstructionFormModal'
import { constructionApi } from '../lib/api'
import { errorText } from '../lib/api/client'
import { useAuth } from '../lib/auth'
import { useDebounced, usePagedList } from '../lib/hooks'
import { useToast } from '../lib/toast'
import type { CongTrinh } from '../types/domain'

export default function ConstructionPage() {
  const { session } = useAuth()
  const manager = session?.tenVaiTro === 'Quản lý'
  const toast = useToast()
  const [keyword, setKeyword] = useState('')
  const debounced = useDebounced(keyword)
  const { data, rows, setTrang, loading, error, reload } = usePagedList(
    (page, kw) => manager ? constructionApi.managerList(kw, page, 10) : constructionApi.customerList(kw, page, 10), debounced,
  )
  const [editing, setEditing] = useState<CongTrinh | 'new' | null>(null)
  const [removing, setRemoving] = useState<CongTrinh | null>(null)
  async function remove() {
    if (!removing) return
    try {
      const result = manager ? await constructionApi.remove(removing.idCT) : await constructionApi.customerRemove(removing.idCT)
      toast.success(result.message); setRemoving(null); reload()
    } catch (err) { toast.error(errorText(err)); setRemoving(null) }
  }
  return <>
    <div className="page-head"><div><p className="eyebrow">{manager ? 'QUẢN LÝ CÔNG TRÌNH' : 'CÔNG TRÌNH CỦA TÔI'}</p><h1>Công trình</h1><p>{manager ? 'Quản lý danh sách công trình và thông tin liên hệ khách hàng.' : 'Quản lý các công trình đã đăng ký của bạn.'}</p></div><button className="btn btn-primary" onClick={() => setEditing('new')}><Plus size={16} />Thêm công trình</button></div>
    <div className="panel"><div className="toolbar"><SearchBox value={keyword} onChange={setKeyword} placeholder="Tìm theo tên công trình, địa chỉ…" /></div><div className="table-wrap"><table><thead><tr><th>Công trình</th><th>Địa chỉ</th><th>Tọa độ</th><th>Số điện thoại</th><th /></tr></thead><tbody><TableState cols={5} loading={loading} error={error} empty={!rows.length} searching={Boolean(debounced)} emptyText="Chưa có công trình." noMatchText="Không tìm thấy công trình." />{rows.map((item) => <tr key={item.idCT}><td><b>{item.tenCongTrinh}</b></td><td>{item.diaChi}</td><td>{item.viDo}, {item.kinhDo}</td><td>{item.sdt}</td><td className="actions"><div className="row-actions"><button className="icon-btn" title="Sửa" onClick={() => setEditing(item)}><Pencil size={16} /></button><button className="icon-btn" title="Xóa" style={{ color: 'var(--danger)' }} onClick={() => setRemoving(item)}><Trash2 size={16} /></button></div></td></tr>)}</tbody></table></div><Pagination page={data} onChange={setTrang} /></div>
    {editing && <ConstructionFormModal editing={editing === 'new' ? null : editing} customerId={session?.idTK} onClose={() => setEditing(null)} onSaved={() => { setEditing(null); toast.success('Đã lưu công trình'); reload() }} />}
    {removing && <ConfirmDialog title="Xóa công trình" danger confirmLabel="Xóa" message={`Bạn có chắc muốn xóa công trình "${removing.tenCongTrinh}"?`} onConfirm={remove} onClose={() => setRemoving(null)} />}
  </>
}
