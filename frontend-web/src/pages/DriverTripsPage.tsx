import { useState } from 'react'
import { CheckCircle2, Clock, MapPin, Truck } from 'lucide-react'
import Pagination from '../components/common/Pagination'
import StatusPill from '../components/common/StatusPill'
import TableState from '../components/common/TableState'
import { driverTripsApi } from '../lib/api'
import { usePagedList } from '../lib/hooks'

function formatDateTime(iso?: string) {
  if (!iso) return '--'
  return new Date(iso).toLocaleString('vi-VN', { dateStyle: 'short', timeStyle: 'short' })
}

export default function DriverTripsPage() {
  const { data, rows, loading, error, setTrang } = usePagedList((page) => driverTripsApi.list('', page, 20), null)

  return <>
    <div className="page-head">
      <div>
        <p className="eyebrow">CÔNG VIỆC CỦA TÔI</p>
        <h1>Lịch trình & Chuyến đi</h1>
        <p>Danh sách các chuyến xe bê tông được phân công cho bạn.</p>
      </div>
    </div>
    
    <div className="panel">
      <div className="table-wrap">
        <table>
          <thead>
            <tr><th>Mã chuyến</th><th>Trạm xuất phát</th><th>Khối lượng</th><th>Thời gian</th><th>Trạng thái</th></tr>
          </thead>
          <tbody>
            <TableState cols={5} loading={loading} error={error} empty={!rows.length} searching={false} emptyText="Bạn chưa được phân công chuyến nào." noMatchText="Không tìm thấy chuyến đi nào." />
            {rows.map((trip) => (
              <tr key={trip.idChuyen}>
                <td><b>#{trip.idChuyen}</b><br/><small>ĐH: #{trip.idDH}</small></td>
                <td><div style={{ display: 'flex', alignItems: 'center', gap: 6 }}><MapPin size={14} className="icon-muted"/> {trip.tenTram}</div><small>Xe: {trip.bienSo}</small></td>
                <td>{trip.khoiLuong} m³</td>
                <td>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: 13 }}><Clock size={14} className="icon-muted"/> Đi: {trip.thoiGianXuatPhat ? formatDateTime(trip.thoiGianXuatPhat) : '--'}</div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: 13, marginTop: 4 }}><CheckCircle2 size={14} className="icon-muted"/> Đến: {trip.thoiGianDen ? formatDateTime(trip.thoiGianDen) : '--'}</div>
                </td>
                <td>
                  <StatusPill tone={trip.trangThai === 1 ? 'neutral' : trip.trangThai === 2 ? 'info' : trip.trangThai === 3 ? 'ok' : 'critical'}>
                    {trip.trangThai === 1 ? 'Chờ chạy' : trip.trangThai === 2 ? 'Đang giao' : trip.trangThai === 3 ? 'Hoàn thành' : 'Đã hủy'}
                  </StatusPill>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <Pagination page={data} onChange={setTrang} />
    </div>
  </>
}
