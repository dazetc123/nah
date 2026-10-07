import { useEffect, useState } from 'react'
import { Pencil, Plus, Trash2 } from 'lucide-react'
import SearchBox from '../components/common/SearchBox'
import Pagination from '../components/common/Pagination'
import TableState from '../components/common/TableState'
import Modal from '../components/common/Modal'
import Field from '../components/common/Field'
import { concreteApi } from '../lib/api'
import { errorText } from '../lib/api/client'
import { useToast } from '../lib/toast'
import type { LoaiBeTong, PageResponse } from '../types/domain'

export default function ConcreteTypesPage() {
  const toast = useToast()
  const [rows, setRows] = useState<LoaiBeTong[]>([])
  const [page, setPage] = useState<PageResponse<LoaiBeTong> | null>(null)
  const [trang, setTrang] = useState(1)
  const [keyword, setKeyword] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [editing, setEditing] = useState<LoaiBeTong | 'new' | null>(null)
  async function reload() { setLoading(true); try { const result = await concreteApi.list(keyword, trang, 20); setRows(result.danhSach); setPage(result) } catch (err) { setError(errorText(err, 'Không thể tải loại bê tông')) } finally { setLoading(false) } }
  useEffect(() => { reload() }, [keyword, trang])
  async function remove(item: LoaiBeTong) { if (!window.confirm(`Xóa mác ${item.macBeTong}?`)) return; try { await concreteApi.remove(item.idLBT); toast.success('Đã xóa loại bê tông'); reload() } catch (err) { toast.error(errorText(err, 'Xóa loại bê tông thất bại')) } }
  return <><div className="page-head"><div><p className="eyebrow">DANH MỤC SẢN PHẨM</p><h1>Loại bê tông</h1><p>Quản lý mác, thành phần, đơn giá và trạng thái cung cấp.</p></div><button className="btn btn-primary" onClick={() => setEditing('new')}><Plus size={16} />Thêm loại bê tông</button></div><div className="panel"><div className="toolbar"><SearchBox value={keyword} onChange={setKeyword} placeholder="Tìm theo mác hoặc thành phần…" /></div><div className="table-wrap"><table><thead><tr><th>Mác bê tông</th><th>Thành phần</th><th>Đơn giá</th><th>Trạng thái</th><th>Mô tả</th><th>Đã dùng trong đơn</th><th /></tr></thead><tbody><TableState cols={7} loading={loading} error={error} empty={!rows.length} searching={Boolean(keyword)} emptyText="Chưa có loại bê tông." noMatchText="Không tìm thấy loại phù hợp." />{rows.map((item) => <tr key={item.idLBT}><td><b>{item.macBeTong}</b></td><td>{item.thanhPhan}</td><td>{item.donGia.toLocaleString('vi-VN')} đ</td><td><span className={`tag ${item.trangThai === 1 ? '' : 'muted'}`}>{item.trangThai === 1 ? 'Đang cung cấp' : 'Ngừng cung cấp'}</span></td><td>{item.moTa || '—'}</td><td>{item.soDonHangDaSuDung ?? 0}</td><td className="actions"><button className="icon-btn" onClick={() => setEditing(item)}><Pencil size={16} /></button><button className="icon-btn" onClick={() => remove(item)}><Trash2 size={16} /></button></td></tr>)}</tbody></table></div><Pagination page={page} onChange={setTrang} /></div>{editing && <ConcreteModal editing={editing === 'new' ? null : editing} onClose={() => setEditing(null)} onSaved={() => { setEditing(null); toast.success('Đã lưu loại bê tông'); reload() }} />}</>
}

function ConcreteModal({ editing, onClose, onSaved }: { editing: LoaiBeTong | null; onClose: () => void; onSaved: () => void }) {
  const [macBeTong, setMac] = useState(editing?.macBeTong ?? '')
  const [thanhPhan, setThanhPhan] = useState(editing?.thanhPhan ?? '')
  const [donGia, setDonGia] = useState(String(editing?.donGia ?? ''))
  const [trangThai, setTrangThai] = useState(editing?.trangThai ?? 1)
  const [moTa, setMoTa] = useState(editing?.moTa ?? '')
  const [error, setError] = useState('')
  async function submit() { if (!macBeTong || !thanhPhan || Number(donGia) <= 0) { setError('Vui lòng nhập đủ mác, thành phần và đơn giá hợp lệ'); return } try { const body = { macBeTong, thanhPhan, donGia: Number(donGia), trangThai, moTa }; if (editing) await concreteApi.update(editing.idLBT, body); else await concreteApi.create(body); onSaved() } catch (err) { setError(errorText(err, 'Không thể lưu loại bê tông')) } }
  return <Modal title={editing ? 'Sửa loại bê tông' : 'Thêm loại bê tông'} onClose={onClose} footer={<><button className="btn" onClick={onClose}>Hủy</button><button className="btn btn-primary" onClick={submit}>Lưu</button></>}><div className="form-stack"><Field label="Mác bê tông"><input value={macBeTong} onChange={(e) => setMac(e.target.value)} placeholder="Ví dụ: M250" /></Field><Field label="Thành phần"><input value={thanhPhan} onChange={(e) => setThanhPhan(e.target.value)} /></Field><Field label="Đơn giá"><input type="number" min="0.1" value={donGia} onChange={(e) => setDonGia(e.target.value)} /></Field><Field label="Trạng thái cung cấp"><select value={trangThai} onChange={(e) => setTrangThai(Number(e.target.value))}><option value={1}>Đang cung cấp</option><option value={0}>Ngừng cung cấp</option></select></Field><Field label="Mô tả"><textarea value={moTa} onChange={(e) => setMoTa(e.target.value)} rows={3} /></Field>{error && <div className="form-error">{error}</div>}</div></Modal>
}
