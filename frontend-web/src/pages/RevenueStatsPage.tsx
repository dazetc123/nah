import { useEffect, useState } from 'react'
import { Calendar, DollarSign, Package, Truck, Download } from 'lucide-react'
import { reportApi } from '../lib/api'
import { errorText } from '../lib/api/client'
import type { BaoCaoTongHopResponse, BaoCaoDongResponse } from '../types/domain'

export default function RevenueStatsPage() {
  const [tuNgay, setTuNgay] = useState(() => {
    const d = new Date(); d.setDate(1); return d.toISOString().split('T')[0]
  })
  const [denNgay, setDenNgay] = useState(() => new Date().toISOString().split('T')[0])
  const [summary, setSummary] = useState<BaoCaoTongHopResponse | null>(null)
  const [rows, setRows] = useState<BaoCaoDongResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  async function loadData() {
    setLoading(true); setError('')
    try {
      const [sum, list] = await Promise.all([
        reportApi.summary(tuNgay, denNgay),
        reportApi.list(tuNgay, denNgay)
      ])
      setSummary(sum)
      setRows(list)
    } catch (err) {
      setError(errorText(err, 'Lỗi tải báo cáo'))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { loadData() }, [tuNgay, denNgay])

  const exportUrl = (format: string) => `http://localhost:8080/api/quan-ly/bao-cao/xuat?tuNgay=${tuNgay}&denNgay=${denNgay}&dinhDang=${format}`

  return <>
    <div className="page-head">
      <div>
        <p className="eyebrow">BÁO CÁO & THỐNG KÊ</p>
        <h1>Báo cáo hoạt động dự án</h1>
        <p>Tổng quan doanh thu, đơn hàng và các hoạt động của toàn dự án bê tông.</p>
      </div>
      <div style={{ display: 'flex', gap: 12 }}>
        <a href={exportUrl('excel')} target="_blank" className="btn"><Download size={16}/> Xuất Excel</a>
        <a href={exportUrl('pdf')} target="_blank" className="btn btn-primary"><Download size={16}/> Xuất PDF</a>
      </div>
    </div>
    
    <div className="panel" style={{ padding: 20, marginBottom: 24 }}>
      <div style={{ display: 'flex', gap: 12, alignItems: 'center', marginBottom: 24 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <Calendar size={16} className="icon-muted" />
          <span>Từ ngày:</span>
          <input type="date" className="input" style={{ width: 'auto' }} value={tuNgay} onChange={e => setTuNgay(e.target.value)} />
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <span>Đến ngày:</span>
          <input type="date" className="input" style={{ width: 'auto' }} value={denNgay} onChange={e => setDenNgay(e.target.value)} />
        </div>
      </div>

      {error ? <div className="form-error">{error}</div> : loading ? <p className="icon-muted">Đang phân tích dữ liệu...</p> : summary && (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: 20 }}>
          <div className="stat-card" style={{ padding: 20, border: '1px solid var(--line)', borderRadius: 8, background: 'var(--panel)' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 8, color: 'var(--text-muted)' }}>
              <DollarSign size={20} className="icon-muted" /> Tổng doanh thu
            </div>
            <h2 style={{ fontSize: 24, margin: 0, color: 'var(--accent)' }}>
              {new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(summary.doanhThu)}
            </h2>
          </div>
          
          <div className="stat-card" style={{ padding: 20, border: '1px solid var(--line)', borderRadius: 8, background: 'var(--panel)' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 8, color: 'var(--text-muted)' }}>
              <Package size={20} className="icon-muted" /> Tổng khối lượng
            </div>
            <h2 style={{ fontSize: 24, margin: 0 }}>{summary.sanLuongBeTong} m³</h2>
          </div>

          <div className="stat-card" style={{ padding: 20, border: '1px solid var(--line)', borderRadius: 8, background: 'var(--panel)' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 8, color: 'var(--text-muted)' }}>
              <Truck size={20} className="icon-muted" /> Tổng chuyến giao
            </div>
            <h2 style={{ fontSize: 24, margin: 0 }}>{summary.soChuyenGiao} chuyến</h2>
          </div>
          
          <div className="stat-card" style={{ padding: 20, border: '1px solid var(--line)', borderRadius: 8, background: 'var(--panel)' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 8, color: 'var(--text-muted)' }}>
              <Package size={20} className="icon-muted" /> Tổng đơn hàng
            </div>
            <h2 style={{ fontSize: 24, margin: 0 }}>{summary.soDonHang} đơn</h2>
          </div>
        </div>
      )}
    </div>

    <div className="panel">
      <div className="panel-head">
        <h2>Chi tiết hoạt động</h2>
      </div>
      <div className="table-wrap">
        <table>
          <thead>
            <tr><th>Mã ĐH</th><th>Ngày đặt</th><th>Sản lượng</th><th>Doanh thu</th><th>Trạng thái</th></tr>
          </thead>
          <tbody>
            {!rows.length && !loading && <tr><td colSpan={5} style={{ textAlign: 'center' }}>Không có dữ liệu trong khoảng thời gian này.</td></tr>}
            {rows.map(row => (
              <tr key={row.idDH}>
                <td><b>#{row.idDH}</b></td>
                <td>{new Date(row.ngayDat).toLocaleDateString('vi-VN')}</td>
                <td>{row.sanLuong} m³</td>
                <td style={{ color: 'var(--accent)', fontWeight: 500 }}>{new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(row.doanhThu)}</td>
                <td>{row.trangThai === 0 ? 'Mới' : row.trangThai === 1 ? 'Đang giao' : row.trangThai === 2 ? 'Hoàn thành' : 'Đã hủy'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  </>
}
