import { useEffect, useState } from 'react'
import { LoaderCircle } from 'lucide-react'
import Modal from '../common/Modal'
import StatusPill from '../common/StatusPill'
import UserAvatar from '../common/UserAvatar'
import { accountApi } from '../../lib/api'
import { errorText } from '../../lib/api/client'
import type { TaiKhoan } from '../../types/domain'

type Kind = 'dispatcher' | 'driver' | 'customer'

const labels: Record<Kind, string> = {
  dispatcher: 'Nhân viên điều phối',
  driver: 'Tài xế',
  customer: 'Khách hàng',
}

export default function AccountDetailModal({ id, kind, onClose }: { id: number; kind: Kind; onClose: () => void }) {
  const [account, setAccount] = useState<TaiKhoan | null>(null)
  const [error, setError] = useState('')
  useEffect(() => {
    const load = kind === 'dispatcher'
      ? accountApi.dispatcherDetail(id)
      : kind === 'driver'
        ? accountApi.driverDetail(id)
        : accountApi.customerDetail(id)
    load.then(setAccount).catch((err) => setError(errorText(err, 'Không thể tải chi tiết tài khoản')))
  }, [id, kind])

  return <Modal title={`Chi tiết ${labels[kind]}`} onClose={onClose} width={620}>
    {!account && !error && <div className="loading-screen" style={{ minHeight: 160 }}><LoaderCircle className="spin" size={22} /></div>}
    {error && <div className="form-error">{error}</div>}
    {account && <>
      <div className="account-detail-identity">
        <UserAvatar name={account.hoTen} src={account.anhDaiDien} className="account-detail-avatar" />
        <div><h4>{account.hoTen || '—'}</h4><span className="muted">{account.email || 'Chưa cập nhật email'}</span></div>
      </div>
      <div className="detail-list">
      <dt>Ảnh đại diện</dt><dd>{account.anhDaiDien ? 'Đã cập nhật' : 'Chưa cập nhật'}</dd>
      <dt>Họ tên</dt><dd>{account.hoTen || '—'}</dd>
      <dt>Vai trò</dt><dd><StatusPill tone="accent">{account.tenVaiTro || labels[kind]}</StatusPill></dd>
      <dt>Email</dt><dd>{account.email || '—'}</dd>
      <dt>Số điện thoại</dt><dd>{account.sdt || '—'}</dd>
      <dt>Ngày sinh</dt><dd>{account.ngaySinh || 'Chưa cập nhật'}</dd>
      <dt>Giới tính</dt><dd>{account.gioiTinh || 'Chưa cập nhật'}</dd>
      <dt>Địa chỉ</dt><dd>{account.diaChi || account.diaChiThuongTru || 'Chưa cập nhật'}</dd>
      {account.diaChi && account.diaChiThuongTru && account.diaChi !== account.diaChiThuongTru && (
        <><dt>Địa chỉ thường trú</dt><dd>{account.diaChiThuongTru}</dd></>
      )}
      <dt>Trạng thái</dt><dd><StatusPill tone={account.trangThai === 1 ? 'ok' : 'danger'}>{account.trangThai === 1 ? 'Đang hoạt động' : 'Đã khóa'}</StatusPill></dd>
      {kind === 'driver' && <><dt>Số GPLX</dt><dd>{account.soGPLX || '—'}</dd><dt>Mã tài xế</dt><dd>{account.idTX ?? '—'}</dd></>}
      {kind === 'customer' && <><dt>Tên khách hàng</dt><dd>{account.tenKhachHang || account.hoTen || '—'}</dd><dt>Địa chỉ</dt><dd>{account.diaChi || '—'}</dd><dt>Mã khách hàng</dt><dd>{account.idKH ?? '—'}</dd></>}
      </div>
    </>}
  </Modal>
}
