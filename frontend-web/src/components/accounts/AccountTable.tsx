import type { Account } from '../../types/domain'
import StatusPill from '../common/StatusPill'

export default function AccountTable({ rows }: { rows: Account[] }) {
  return <table><thead><tr><th>Họ và tên</th><th>Email</th><th>Vai trò</th><th>Trạng thái</th><th /></tr></thead><tbody>{rows.length === 0 ? <tr><td className="empty-state" colSpan={5}>Chưa có dữ liệu tài khoản từ cơ sở dữ liệu.</td></tr> : rows.map((a) => <tr key={a.idTK}><td><div className="person"><span className="avatar tiny">{a.hoTen.split(' ').map((n) => n[0]).slice(-2).join('')}</span><b>{a.hoTen}</b></div></td><td>{a.email}</td><td><span className="role">{a.tenVaiTro}</span></td><td>  <StatusPill tone={a.trangThai === 'Đang hoạt động' ? 'ok' : a.trangThai === 'Đang bảo trì' ? 'neutral' : 'danger'}>{a.trangThai}</StatusPill></td><td><button className="more-btn">•••</button></td></tr>)}</tbody></table>
}
