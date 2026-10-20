import { Activity, ClipboardList, Factory, Plus, Truck } from 'lucide-react'
import type { View } from '../types/domain'
import Metric from '../components/common/Metric'
import VehicleTable from '../components/vehicles/VehicleTable'

export default function OverviewPage({ onNavigate }: { onNavigate: (view: View) => void }) {
  return <><div className="page-heading"><div><p className="eyebrow">ĐIỀU HÀNH HỆ THỐNG</p><h1>Tổng quan</h1><p className="subheading">Dữ liệu được tải trực tiếp từ hệ thống của bạn.</p></div><button className="primary-btn" onClick={() => onNavigate('vehicles')}><Plus size={17} />Quản lý đội xe</button></div><div className="metric-grid"><Metric label="Đơn hàng hôm nay" value="—" delta="Chưa có dữ liệu" icon={<ClipboardList />} tone="blue" /><Metric label="Đang giao" value="—" delta="Chưa có dữ liệu" icon={<Truck />} tone="teal" /><Metric label="Xe hoạt động" value="—" delta="Chưa có dữ liệu" icon={<Activity />} tone="violet" /><Metric label="Sản lượng hôm nay" value="—" delta="Chưa có dữ liệu" icon={<Factory />} tone="orange" /></div><section className="panel table-panel"><div className="panel-header"><div><h2>Đội xe đang hoạt động</h2><p>Theo dõi dữ liệu từ API quản lý xe</p></div><button className="link-btn" onClick={() => onNavigate('vehicles')}>Mở đội xe →</button></div><VehicleTable rows={[]} /></section></>
}
