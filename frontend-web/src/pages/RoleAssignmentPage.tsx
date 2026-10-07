import { useState } from 'react'
import { ArrowRightLeft } from 'lucide-react'
import SearchBox from '../components/common/SearchBox'
import Pagination from '../components/common/Pagination'
import StatusPill from '../components/common/StatusPill'
import TableState from '../components/common/TableState'
import ConfirmDialog from '../components/common/ConfirmDialog'
import { accountApi } from '../lib/api'
import { errorText } from '../lib/api/client'
import { useDebounced, usePagedList } from '../lib/hooks'
import { useToast } from '../lib/toast'
import type { TaiKhoan } from '../types/domain'

const SWAP: Record<string, string> = { 'Nhân viên điều phối': 'Tài xế', 'Tài xế': 'Nhân viên điều phối' }
function toneOf(v: string): 'danger' | 'neutral' | 'accent' {
  return v === 'Quản lý' ? 'danger' : v === 'Khách hàng' ? 'neutral' : 'accent'
}

/** Bảng 3.11 — chỉ hoán đổi qua lại giữa Nhân viên điều phối và Tài xế (xem PhanQuyenServiceImpl). */
export default function RoleAssignmentPage() {
  const toast = useToast()
  const [keyword, setKeyword] = useState('')
  const debounced = useDebounced(keyword)
  const { data, rows, trang, setTrang, loading, error, reload } = usePagedList(
    (page, kw) => accountApi.allForRoles(kw, page, 10), debounced,
  )
  const [target, setTarget] = useState<TaiKhoan | null>(null)

  async function confirm() {
    if (!target) return
    try {
      const res = await accountApi.changeRole(target.idTK, SWAP[target.tenVaiTro])
      toast.success(res.thongBao ?? 'Cập nhật quyền thành công')
      setTarget(null); reload()
    } catch (err) { toast.error(errorText(err)); setTarget(null) }
  }

  return (
    <div className="role-page">
      <div className="page-head"><div><p className="eyebrow">BẢNG 3.11</p><h1>Phân quyền</h1><p>Hoán đổi vai trò giữa Nhân viên điều phối và Tài xế.</p></div></div>

      <div className="panel">
        <div className="toolbar"><SearchBox value={keyword} onChange={setKeyword} placeholder="Tìm theo họ tên…" /></div>
        <div className="table-wrap">
          <table className="role-table">
            <colgroup><col className="role-account-col" /><col className="role-current-col" /><col className="role-actions-col" /></colgroup>
            <thead><tr><th>Tài khoản</th><th>Vai trò hiện tại</th><th>Thao tác</th></tr></thead>
            <tbody>
              <TableState cols={3} loading={loading} error={error} empty={rows.length === 0} searching={Boolean(debounced)}
                emptyText="Chưa có tài khoản nào." noMatchText="Không tìm thấy tài khoản phù hợp." />
              {rows.map((tk) => {
                const swappable = Boolean(SWAP[tk.tenVaiTro])
                return (
                  <tr key={tk.idTK}>
                    <td><div style={{ fontWeight: 600 }}>{tk.hoTen}</div></td>
                    <td><StatusPill tone={toneOf(tk.tenVaiTro)}>{tk.tenVaiTro}</StatusPill></td>
                    <td className="actions">
                      {swappable ? (
                        <button className="btn btn-sm" onClick={() => setTarget(tk)}><ArrowRightLeft size={14} />Chuyển thành {SWAP[tk.tenVaiTro]}</button>
                      ) : (
                        <span className="muted" style={{ fontSize: 12 }}>Không thể đổi vai trò</span>
                      )}
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </div>
        <Pagination page={data} onChange={setTrang} />
      </div>

      {target && (
        <ConfirmDialog title="Đổi vai trò" confirmLabel="Xác nhận đổi"
          message={`Chuyển "${target.hoTen}" từ ${target.tenVaiTro} sang ${SWAP[target.tenVaiTro]}?`}
          onConfirm={confirm} onClose={() => setTarget(null)} />
      )}
    </div>
  )
}
