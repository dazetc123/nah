import { useEffect, useRef, useState } from 'react'
import { Copy, Eye, KeyRound, Lock, Pencil, Plus, Unlock } from 'lucide-react'
import Modal from '../components/common/Modal'
import SearchBox from '../components/common/SearchBox'
import Pagination from '../components/common/Pagination'
import StatusPill from '../components/common/StatusPill'
import TableState from '../components/common/TableState'
import ConfirmDialog from '../components/common/ConfirmDialog'
import AccountFormModal from '../components/accounts/AccountFormModal'
import AccountDetailModal from '../components/accounts/AccountDetailModal'
import { accountApi, driverApi } from '../lib/api'
import { errorText } from '../lib/api/client'
import { useDebounced, usePagedList } from '../lib/hooks'
import { useToast } from '../lib/toast'
import type { TaiKhoan } from '../types/domain'

const initials = (name: string) => name.split(' ').filter(Boolean).map((p) => p[0]).slice(-2).join('').toUpperCase() || '?'

/** Bảng 3.3 (tìm kiếm) + 3.7 (xem danh sách) gộp lại: có ô tìm kiếm, không nhập gì thì xem toàn bộ. */
export default function AccountsPage() {
  const toast = useToast()
  const [keyword, setKeyword] = useState('')
  const [category, setCategory] = useState<'staff' | 'customers'>('staff')
  const [detail, setDetail] = useState<{ id: number; kind: 'dispatcher' | 'driver' | 'customer' } | null>(null)
  const debounced = useDebounced(keyword)
  const { data, rows, trang, setTrang, loading, error, reload } = usePagedList(
    (page, filters) => filters.category === 'customers' ? accountApi.customers(filters.keyword, page, 10) : accountApi.staff(filters.keyword, page, 10), { keyword: debounced, category },
  )
  const [editing, setEditing] = useState<TaiKhoan | 'new' | null>(null)
  const [toggling, setToggling] = useState<TaiKhoan | null>(null)
  const [created, setCreated] = useState<{ tenDangNhap: string; matKhau: string } | null>(null)
  const repairing = useRef(false)

  useEffect(() => {
    if (category !== 'staff' || repairing.current) return
    const drivers = rows.filter((account) => account.tenVaiTro === 'Tài xế')
    if (!drivers.length) return
    repairing.current = true
    driverApi.list('', 1, 100).then(async (page) => {
      const existing = new Set(page.danhSach.map((driver) => driver.idTK))
      const missing = drivers.filter((account) => !existing.has(account.idTK))
      for (const account of missing) {
        if (!account.sdt || !account.soGPLX) continue
        await driverApi.create({
          idTK: account.idTK,
          hoTen: account.hoTen,
          soGPLX: account.soGPLX,
          sdt: account.sdt,
          trangThai: 1,
        })
      }
      if (missing.length) reload()
    }).catch((err) => toast.error(errorText(err, 'Không thể khởi tạo hồ sơ tài xế')))
      .finally(() => { repairing.current = false })
  }, [category, rows])

  async function confirmToggle() {
    if (!toggling) return
    const next = toggling.trangThai === 1 ? 0 : 1
    try {
      const res = await accountApi.setStatus(toggling.idTK, next)
      toast.success(res.thongBao ?? 'Cập nhật trạng thái thành công')
      setToggling(null); reload()
    } catch (err) { toast.error(errorText(err)) }
  }

  function copyPassword() {
    if (!created) return
    navigator.clipboard?.writeText(created.matKhau).then(() => toast.success('Đã sao chép mật khẩu'))
  }

  return (
    <>
      <div className="page-head">
        <div><p className="eyebrow">QUẢN LÝ TÀI KHOẢN</p><h1>{category === 'customers' ? 'Tài khoản khách hàng' : 'Tài khoản nhân viên'}</h1><p>{category === 'customers' ? 'Xem thông tin khách hàng đã đăng ký.' : 'Nhân viên điều phối và Tài xế — tài khoản do Quản lý cấp.'}</p></div>
        {category === 'staff' && <button className="btn btn-primary" onClick={() => setEditing('new')}><Plus size={16} />Cấp tài khoản</button>}
      </div>

      <div className="tabs"><button className={category === 'staff' ? 'tab active' : 'tab'} onClick={() => { setCategory('staff'); setKeyword('') }}>Nhân viên</button><button className={category === 'customers' ? 'tab active' : 'tab'} onClick={() => { setCategory('customers'); setKeyword('') }}>Khách hàng</button></div>
      <div className="panel">
        <div className="toolbar">
          <SearchBox value={keyword} onChange={setKeyword} placeholder="Tìm theo họ tên, email, SĐT…" />
        </div>
        <div className="table-wrap">
          <table>
            <thead><tr><th>Tài khoản</th><th>Vai trò</th><th>SĐT</th><th>Email</th><th>Trạng thái</th><th></th></tr></thead>
            <tbody>
              <TableState cols={6} loading={loading} error={error} empty={rows.length === 0} searching={Boolean(debounced)}
                emptyText="Chưa có tài khoản nhân viên nào." noMatchText="Không tìm thấy tài khoản phù hợp." />
              {rows.map((tk) => (
                <tr key={tk.idTK}>
                  <td><div className="person"><span className="avatar">{initials(tk.hoTen)}</span><div style={{ fontWeight: 600 }}>{tk.hoTen}</div></div></td>
                  <td><StatusPill tone="accent">{tk.tenVaiTro}</StatusPill></td>
                  <td>{tk.sdt || '—'}</td>
                  <td>{tk.email || '—'}</td>
                  <td>
                    <StatusPill tone={tk.trangThai === 1 ? 'ok' : 'danger'}>{tk.trangThai === 1 ? 'Đang hoạt động' : 'Đã khoá'}</StatusPill>
                    {Boolean(tk.phaiDoiMatKhau) && <span className="tag" style={{ marginLeft: 6 }}><KeyRound size={10} style={{ verticalAlign: '-1px' }} /> Chờ đổi MK</span>}
                  </td>
                  <td className="actions">
                    <div className="row-actions">
                      <button className="icon-btn" title="Xem chi tiết" onClick={() => setDetail({ id: tk.idTK, kind: category === 'customers' ? 'customer' : tk.tenVaiTro === 'Tài xế' ? 'driver' : 'dispatcher' })}><Eye size={16} /></button>
                      {category === 'staff' && <>
                        {tk.tenVaiTro !== 'Tài xế' && <button className="icon-btn" title="Sửa" onClick={() => setEditing(tk)}><Pencil size={16} /></button>}
                        <button className="icon-btn" title={tk.trangThai === 1 ? 'Khoá' : 'Mở khoá'} onClick={() => setToggling(tk)}>
                          {tk.trangThai === 1 ? <Lock size={16} /> : <Unlock size={16} />}
                        </button>
                      </>}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <Pagination page={data} onChange={setTrang} />
      </div>

      {editing && (
        <AccountFormModal editing={editing === 'new' ? null : editing} onClose={() => setEditing(null)}
          onSaved={(c) => { setEditing(null); reload(); if (c) setCreated(c); else toast.success('Cập nhật thành công') }} />
      )}

      {toggling && (
        <ConfirmDialog
          title={toggling.trangThai === 1 ? 'Khoá tài khoản' : 'Mở khoá tài khoản'}
          message={`Bạn có chắc chắn muốn ${toggling.trangThai === 1 ? 'khoá' : 'mở khoá'} tài khoản "${toggling.hoTen}"?`}
          confirmLabel={toggling.trangThai === 1 ? 'Khoá' : 'Mở khoá'} danger={toggling.trangThai === 1}
          onConfirm={confirmToggle} onClose={() => setToggling(null)}
        />
      )}

      {created && (
        <Modal title="Cấp tài khoản thành công" onClose={() => setCreated(null)} footer={<button className="btn btn-primary" onClick={() => setCreated(null)}>Đã lưu mật khẩu, đóng lại</button>}>
          <p className="confirm-text">
            Tài khoản đã được tạo. Mật khẩu dưới đây chỉ hiển thị <b>một lần duy nhất</b> — hãy sao chép và gửi cho nhân viên ngay.
          </p>
          <div className="form-grid" style={{ gridTemplateColumns: '1fr auto', marginTop: 12, alignItems: 'end' }}>
            <div className="field"><label>Mật khẩu ban đầu</label><input className="mono" value={created.matKhau} readOnly /></div>
            <button type="button" className="btn" onClick={copyPassword}><Copy size={15} />Sao chép</button>
          </div>
        </Modal>
      )}
      {detail && <AccountDetailModal id={detail.id} kind={detail.kind} onClose={() => setDetail(null)} />}
    </>
  )
}
