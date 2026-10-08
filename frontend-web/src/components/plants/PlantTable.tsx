import type { Plant } from '../../types/domain'
import StatusPill from '../common/StatusPill'

export default function PlantTable({ rows }: { rows: Plant[] }) {
  return <table><thead><tr><th>Tên trạm</th><th>Địa chỉ</th><th>Công suất</th><th>Đơn hàng</th><th>Trạng thái</th><th /></tr></thead><tbody>{rows.length === 0 ? <tr><td className="empty-state" colSpan={6}>Chưa có dữ liệu trạm trộn từ cơ sở dữ liệu.</td></tr> : rows.map((p) => <tr key={p.idTram}><td><b>{p.tenTram}</b></td><td>{p.diaChi}</td><td>{p.congSuat} m³/giờ</td><td>{p.donHang} đơn</td><td>  <StatusPill tone={p.trangThai === 'Đang hoạt động' ? 'ok' : p.trangThai === 'Đang bảo trì' ? 'neutral' : 'danger'}>{p.trangThai}</StatusPill></td><td><button className="more-btn">•••</button></td></tr>)}</tbody></table>
}
