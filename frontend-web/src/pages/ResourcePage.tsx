import { Search } from 'lucide-react'
import type { Account, AccountCategory, Plant, Vehicle, View } from '../types/domain'
import VehicleTable from '../components/vehicles/VehicleTable'
import PlantTable from '../components/plants/PlantTable'
import AccountTable from '../components/accounts/AccountTable'

type Rows = Vehicle[] | Plant[] | Account[]
export default function ResourcePage({ type, query, setQuery, rows, onAdd, category, onCategory }: { type: Exclude<View, 'overview'>; query: string; setQuery: (value: string) => void; rows: Rows; onAdd: () => void; category: AccountCategory; onCategory: (category: AccountCategory) => void }) {
  const labels = { vehicles: ['Đội xe', 'Quản lý phương tiện và tài xế', 'Thêm xe'], plants: ['Trạm trộn', 'Theo dõi công suất và trạng thái các trạm', 'Thêm trạm'], accounts: ['Tài khoản', 'Quản lý người dùng và phân quyền', 'Tạo tài khoản'] }[type]
  const accountTabs: { id: AccountCategory; label: string }[] = [{ id: 'staff', label: 'Nhân viên' }, { id: 'dispatchers', label: 'Điều phối viên' }, { id: 'drivers', label: 'Tài xế' }, { id: 'customers', label: 'Khách hàng' }]
  return <><div className="page-heading"><div><p className="eyebrow">QUẢN LÝ HỆ THỐNG</p><h1>{labels[0]}</h1><p className="subheading">{labels[1]}</p></div><button className="primary-btn" onClick={onAdd}>＋ {labels[2]}</button></div>{type === 'accounts' && <div className="account-tabs">{accountTabs.map((tab) => <button key={tab.id} className={category === tab.id ? 'account-tab active' : 'account-tab'} onClick={() => onCategory(tab.id)}>{tab.label}</button>)}</div>}<section className="panel table-panel resource-table"><div className="toolbar"><div className="search-box"><Search size={17} /><input value={query} onChange={(e) => setQuery(e.target.value)} placeholder="Tìm kiếm..." /></div><select defaultValue="all"><option value="all">Tất cả trạng thái</option><option>Đang hoạt động</option><option>Đang bảo trì</option></select></div>{type === 'vehicles' && <VehicleTable rows={rows as Vehicle[]} />}{type === 'plants' && <PlantTable rows={rows as Plant[]} />}{type === 'accounts' && <AccountTable rows={rows as Account[]} />}</section></>
}
