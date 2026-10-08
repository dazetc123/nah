import type { Vehicle } from '../../types/domain'
import StatusPill from '../common/StatusPill'

export default function VehicleTable({ rows }: { rows: Vehicle[] }) {
  return <table><thead><tr><th>Biển số xe</th><th>Tải trọng</th><th>Tài xế</th><th>Tuyến đường</th><th>Trạng thái</th><th>Cập nhật</th><th /></tr></thead><tbody>{rows.length === 0 ? <tr><td className="empty-state" colSpan={7}>Chưa có dữ liệu xe từ cơ sở dữ liệu.</td></tr> : rows.map((v) => <tr key={v.idXe}><td><b className="plate">{v.bienSo}</b></td><td>{v.trongTai} tấn</td><td>{v.taiXe}</td><td className="route">{v.tuyen}</td><td>  <StatusPill tone={v.trangThai === 'Đang hoạt động' ? 'ok' : v.trangThai === 'Đang bảo trì' ? 'neutral' : 'danger'}>{v.trangThai}</StatusPill></td><td className="muted">{v.capNhat}</td><td><button className="more-btn">•••</button></td></tr>)}</tbody></table>
}
