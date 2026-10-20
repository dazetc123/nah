import { useEffect, useState } from 'react'
import { Image, MapPin, Upload } from 'lucide-react'
import Field from '../components/common/Field'
import Pagination from '../components/common/Pagination'
import TableState from '../components/common/TableState'
import { vehicleApi, vehicleReportApi } from '../lib/api'
import { errorText } from '../lib/api/client'
import { useToast } from '../lib/toast'
import { usePagedList } from '../lib/hooks'
import { useAuth } from '../lib/auth'
import { API_URL } from '../lib/api/client'

export default function VehicleReportPage() {
  const { session } = useAuth()
  if (session?.tenVaiTro === 'Quản lý') return <VehicleReportList />
  return <VehicleReportForm reporter={session?.tenVaiTro === 'Nhân viên điều phối' ? 'dispatcher' : 'driver'} />
}

function VehicleReportForm({ reporter }: { reporter: 'driver' | 'dispatcher' }) {
  const toast = useToast()
  const [bienSo, setBienSo] = useState('')
  const [vehicles, setVehicles] = useState<{ idXe: number; bienSo: string }[]>([])
  const [trangThaiXe, setTrangThaiXe] = useState('0')
  const [diaChiHu, setDiaChiHu] = useState('')
  const [soDienThoaiTaiXe, setSoDienThoaiTaiXe] = useState('')
  const [noiDung, setNoiDung] = useState('')
  const [anh, setAnh] = useState<File>()
  const [error, setError] = useState('')
  const [locating, setLocating] = useState(false)

  useEffect(() => {
    vehicleApi.list('', 1, 100)
      .then((page) => setVehicles(page.danhSach.map(({ idXe, bienSo }) => ({ idXe, bienSo }))))
      .catch(() => setVehicles([]))
  }, [])

  function useCurrentLocation() {
    if (!navigator.geolocation) { setError('Thiết bị không hỗ trợ định vị GPS'); return }
    setLocating(true); setError('')
    navigator.geolocation.getCurrentPosition(
      ({ coords }) => {
        setDiaChiHu(`Vị trí GPS: ${coords.latitude.toFixed(6)}, ${coords.longitude.toFixed(6)}`)
        setLocating(false)
      },
      () => { setError('Không lấy được vị trí GPS. Hãy cấp quyền định vị hoặc nhập địa chỉ thủ công.'); setLocating(false) },
      { enableHighAccuracy: true, timeout: 10000, maximumAge: 30000 },
    )
  }

  async function submit() {
    const vehicle = vehicles.find((item) => item.bienSo.toLowerCase() === bienSo.trim().toLowerCase())
    if (!vehicle || !noiDung.trim() || !diaChiHu.trim() || !soDienThoaiTaiXe.trim() || !anh) {
      setError('Vui lòng chọn đúng biển số, nhập địa chỉ, thông tin báo cáo và chọn ảnh minh họa'); return
    }
    try {
      await vehicleReportApi.create(vehicle.idXe, { trangThaiXe: Number(trangThaiXe), diaChiHu: diaChiHu.trim(), soDienThoaiTaiXe: soDienThoaiTaiXe.trim(), nguyenNhan: noiDung.trim(), noiDung: noiDung.trim(), anh })
      setBienSo(''); setNoiDung(''); setDiaChiHu(''); setSoDienThoaiTaiXe(''); setAnh(undefined)
      toast.success('Đã gửi báo cáo tình trạng xe'); setError('')
    } catch (err) { setError(errorText(err, 'Gửi báo cáo thất bại')) }
  }
  return <><div className="page-head"><div><p className="eyebrow">{reporter === 'dispatcher' ? 'ĐIỀU PHỐI VIÊN' : 'TÀI XẾ'}</p><h1>Báo cáo tình trạng xe</h1><p>Chọn biển số xe, xác định vị trí và gửi ảnh minh chứng sự cố.</p></div></div><div className="panel" style={{ maxWidth: 720 }}><div className="panel-body"><div className="form-stack"><Field label="Biển số xe"><input list="vehicle-plates" value={bienSo} onChange={(e) => setBienSo(e.target.value)} placeholder="Chọn hoặc nhập biển số xe" /><datalist id="vehicle-plates">{vehicles.map((vehicle) => <option key={vehicle.idXe} value={vehicle.bienSo} />)}</datalist></Field><div className="form-grid"><Field label="Trạng thái xe"><select value={trangThaiXe} onChange={(e) => setTrangThaiXe(e.target.value)}><option value="1">Đang hoạt động</option><option value="0">Bảo trì</option></select></Field><Field label="Số điện thoại tài xế"><input value={soDienThoaiTaiXe} onChange={(e) => setSoDienThoaiTaiXe(e.target.value)} placeholder="090..." /></Field></div><Field label="Vị trí xe gặp sự cố"><div className="row-actions"><input value={diaChiHu} onChange={(e) => setDiaChiHu(e.target.value)} placeholder="Nhập địa chỉ hoặc dùng GPS" /><button type="button" className="btn btn-sm" onClick={useCurrentLocation} disabled={locating}><MapPin size={15} />{locating ? 'Đang lấy vị trí…' : 'Vị trí hiện tại'}</button></div></Field><Field label="Mô tả tình trạng / nguyên nhân"><textarea rows={5} value={noiDung} onChange={(e) => setNoiDung(e.target.value)} placeholder="Mô tả sự cố và nguyên nhân…" /></Field><Field label="Ảnh minh họa (có thể chụp trực tiếp)"><label className="btn" style={{ display: 'inline-flex' }}><Upload size={15} />{anh ? anh.name : 'Chọn hoặc chụp ảnh'}<input type="file" accept="image/*" capture="environment" hidden onChange={(e) => setAnh(e.target.files?.[0])} /></label>{anh && <span className="hint">{anh.name}</span>}</Field>{error && <div className="form-error">{error}</div>}<button className="btn btn-primary" onClick={submit}>Gửi báo cáo</button></div></div></div></>
}

function VehicleReportList() {
  const { data, rows, trang, setTrang, loading, error } = usePagedList(
    (page) => vehicleReportApi.list(page, 20), null,
  )
  return <><div className="page-head"><div><p className="eyebrow">QUẢN LÝ VẬN HÀNH</p><h1>Báo cáo lỗi xe</h1><p>Tiếp nhận thông tin lỗi do tài xế và nhân viên điều phối báo cáo để kịp thời xử lý.</p></div></div><div className="panel vehicle-report-list"><div className="table-wrap"><table><thead><tr><th>Xe</th><th>Nội dung lỗi</th><th>Người báo cáo</th><th>Thời gian</th><th>Ảnh</th></tr></thead><tbody><TableState cols={5} loading={loading} error={error} empty={rows.length === 0} searching={false} emptyText="Chưa có báo cáo lỗi xe." noMatchText="Không có dữ liệu phù hợp." />{rows.map((report) => <tr key={report.idBaoCao}><td><b>{report.bienSo || `Xe #${report.idXe}`}</b><br /><span className="muted">{report.trangThaiXe === 1 ? 'Đang hoạt động' : 'Bảo trì'}</span></td><td className="vehicle-report-content"><b>{report.nguyenNhan || '—'}</b><br />{report.noiDung}<br /><span className="muted">{report.diaChiHu || 'Chưa có địa chỉ hư xe'}{report.soDienThoaiTaiXe ? ` · ${report.soDienThoaiTaiXe}` : ''}</span></td><td>{report.nguoiBaoCao || (report.idTX ? `Tài xế #${report.idTX}` : '—')}</td><td className="muted">{new Date(report.thoiGian).toLocaleString('vi-VN')}</td><td>{report.anhMinhChung ? <a href={report.anhMinhChung.startsWith('http') ? report.anhMinhChung : `${API_URL.replace(/\/$/, '')}/${report.anhMinhChung.replace(/^\//, '')}`} target="_blank" rel="noreferrer" className="vehicle-report-image"><Image size={16} />Xem ảnh</a> : <span className="muted">Không có</span>}</td></tr>)}</tbody></table></div><Pagination page={data} onChange={setTrang} /></div></>
}
