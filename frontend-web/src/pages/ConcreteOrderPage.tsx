import { useEffect, useMemo, useState } from 'react'
import { CircleAlert, LoaderCircle, MapPin } from 'lucide-react'
import Field from '../components/common/Field'
import ConfirmDialog from '../components/common/ConfirmDialog'
import { concreteOrderApi, constructionApi } from '../lib/api'
import { errorText } from '../lib/api/client'
import { useToast } from '../lib/toast'
import type { CongTrinh, DatBeTong, LoaiBeTong } from '../types/domain'

const money = (value: number) => new Intl.NumberFormat('vi-VN').format(value) + ' đ'

export default function ConcreteOrderPage() {
  const toast = useToast()
  const [types, setTypes] = useState<LoaiBeTong[]>([])
  const [sites, setSites] = useState<CongTrinh[]>([])
  
  const [idLBT, setIdLBT] = useState('')
  const [khoiLuong, setKhoiLuong] = useState('')
  const [idCT, setIdCT] = useState('')
  const [diaChiMoi, setDiaChiMoi] = useState('')
  const [thoiGianGiao, setThoiGianGiao] = useState('')
  const [ghiChu, setGhiChu] = useState('')
  
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const [cancelOpen, setCancelOpen] = useState(false)
  const [result, setResult] = useState<DatBeTong | null>(null)
  const [locating, setLocating] = useState(false)

  useEffect(() => {
    Promise.all([concreteOrderApi.concreteTypes('', 1, 100), constructionApi.customerList('', 1, 100)])
      .then(([typePage, sitePage]) => { 
        setTypes(typePage.danhSach.filter((item) => item.trangThai === 1))
        setSites(sitePage.danhSach) 
      })
      .catch((err) => setError(errorText(err, 'Không thể tải dữ liệu đặt bê tông')))
  }, [])

  function useCurrentLocation() {
    if (!navigator.geolocation) { setError('Thiết bị không hỗ trợ định vị GPS'); return }
    setLocating(true); setError('')
    navigator.geolocation.getCurrentPosition(
      ({ coords }) => {
        setDiaChiMoi(`Tọa độ GPS: ${coords.latitude.toFixed(6)}, ${coords.longitude.toFixed(6)}`)
        setLocating(false)
      },
      () => { setError('Không lấy được vị trí GPS. Vui lòng cấp quyền định vị hoặc nhập thủ công.'); setLocating(false) },
      { enableHighAccuracy: true, timeout: 10000, maximumAge: 30000 },
    )
  }

  const selectedType = useMemo(() => types.find((item) => item.idLBT === Number(idLBT)), [types, idLBT])
  const total = selectedType ? selectedType.donGia * Number(khoiLuong || 0) : 0

  async function placeOrder() {
    setError('')
    if (!selectedType) { setError('Vui lòng chọn loại bê tông'); return }
    if (!Number.isFinite(Number(khoiLuong)) || Number(khoiLuong) < 1) { setError('Khối lượng tối thiểu là 1'); return }
    if (!thoiGianGiao) { setError('Vui lòng chọn thời gian giao hàng'); return }
    if ((!idCT || idCT === 'other') && !diaChiMoi.trim()) { setError('Vui lòng chọn công trình hoặc nhập địa chỉ giao hàng mới'); return }
    
    const date = new Date(thoiGianGiao)
    if (Number.isNaN(date.getTime()) || date <= new Date()) { setError('Thời gian giao hàng phải lớn hơn hiện tại'); return }
    const hour = date.getHours()
    if (hour < 6 || hour >= 18) { setError('Thời gian giao hàng phải trong khung 06:00–18:00'); return }
    
    setLoading(true)
    try {
      const response = await concreteOrderApi.create({ 
        idLBT: selectedType.idLBT, 
        khoiLuong: Number(khoiLuong), 
        idCT: idCT && idCT !== 'other' ? Number(idCT) : undefined, 
        diaChiMoi: !idCT || idCT === 'other' ? diaChiMoi.trim() : undefined, 
        thoiGianGiao, 
        ghiChu: ghiChu.trim() || undefined 
      })
      setResult(response)
      toast.success(response.thongBao ?? 'Đặt bê tông thành công')
    } catch (err) { 
      setError(errorText(err, 'Đặt hàng thất bại, yêu cầu thử lại sau')) 
    } finally { 
      setLoading(false) 
    }
  }

  function cancelOrder() { 
    setCancelOpen(false)
    setIdLBT('')
    setKhoiLuong('')
    setIdCT('')
    setDiaChiMoi('')
    setThoiGianGiao('')
    setGhiChu('')
    setError('') 
  }

  if (result) return (
    <div className="panel order-success" style={{ maxWidth: 600, margin: '40px auto' }}>
      <div className="panel-body">
        <h2>Đặt bê tông thành công</h2>
        <p>{result.thongBao ?? 'Đơn hàng đã được tiếp nhận và đang chờ xử lý.'}</p>
        <p>Mã đơn hàng: <b>#{result.idDH}</b></p>
        <button className="btn btn-primary" onClick={() => setResult(null)} style={{ marginTop: 24 }}>Đặt đơn mới</button>
      </div>
    </div>
  )

  return (
    <>
      <div className="page-head">
        <div>
          <p className="eyebrow">KHÁCH HÀNG</p>
          <h1>Đặt bê tông</h1>
          <p>Điền thông tin đơn hàng, chọn địa điểm và thời gian giao hàng.</p>
        </div>
      </div>
      
      <div className="panel" style={{ maxWidth: 800 }}>
        <div className="panel-body">
          <div className="form-stack">
            <div className="form-grid">
              <Field label="Loại (mác) bê tông">
                <select value={idLBT} onChange={(e) => setIdLBT(e.target.value)}>
                  <option value="">Chọn loại bê tông</option>
                  {types.map((item) => (
                    <option key={item.idLBT} value={item.idLBT}>{item.macBeTong} — {money(item.donGia)}/m³</option>
                  ))}
                </select>
              </Field>
              <Field label="Khối lượng cần đặt (m³)">
                <input type="number" min="1" step="0.1" value={khoiLuong} onChange={(e) => setKhoiLuong(e.target.value)} placeholder="Nhập số lượng..." />
              </Field>
            </div>

            <div className="form-grid">
              <Field label="Công trình đã đăng ký">
                <select value={idCT} onChange={(e) => { setIdCT(e.target.value); if (e.target.value !== 'other' && e.target.value !== '') setDiaChiMoi('') }} className={idCT === '' ? 'placeholder' : ''}>
                  <option value="" disabled hidden>Chọn công trình hoặc chọn Khác...</option>
                  <option value="other">Khác (Tự nhập địa chỉ giao hàng)</option>
                  {sites.map((site) => (
                    <option key={site.idCT} value={site.idCT}>{site.tenCongTrinh} — {site.diaChi}</option>
                  ))}
                </select>
              </Field>
              <Field label="Thời gian giao hàng">
                <input type="datetime-local" value={thoiGianGiao} onChange={(e) => setThoiGianGiao(e.target.value)} />
              </Field>
            </div>

            {(!idCT || idCT === 'other') && (
              <Field label="Địa chỉ giao hàng (Nhập tay)">
                <div className="row-actions">
                  <input style={{ flex: 1 }} value={diaChiMoi} onChange={(e) => setDiaChiMoi(e.target.value)} placeholder="Nhập địa chỉ hoặc tên công trình mới..." />
                  <button type="button" className="btn" onClick={useCurrentLocation} disabled={locating} style={{ minWidth: 'max-content' }}>
                    <MapPin size={15} />
                    {locating ? 'Đang lấy vị trí…' : 'Vị trí hiện tại'}
                  </button>
                </div>
              </Field>
            )}

            <Field label="Ghi chú (không bắt buộc)">
              <textarea rows={3} value={ghiChu} onChange={(e) => setGhiChu(e.target.value)} placeholder="Nhập yêu cầu thêm nếu có..." />
            </Field>

            {selectedType && (
              <div className="notice" style={{ marginTop: 8 }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', width: '100%', alignItems: 'center' }}>
                  <b>Tổng tiền tạm tính:</b>
                  <strong style={{ fontSize: 18, color: 'var(--text-strong)' }}>{money(total)}</strong>
                </div>
              </div>
            )}

            <OrderError message={error} />
            
            <div className="profile-form-actions">
              <button className="btn" onClick={() => setCancelOpen(true)}>Hủy nhập</button>
              <button className="btn btn-primary" onClick={placeOrder} disabled={loading}>
                {loading && <LoaderCircle size={15} className="spin" />}
                Xác nhận đặt hàng
              </button>
            </div>
          </div>
        </div>
      </div>
      
      {cancelOpen && (
        <ConfirmDialog 
          title="Hủy đặt hàng" 
          message="Bạn có chắc muốn hủy? Toàn bộ thông tin đang nhập sẽ bị xóa." 
          confirmLabel="Xác nhận hủy" 
          danger 
          onConfirm={async () => { cancelOrder() }} 
          onClose={() => setCancelOpen(false)} 
        />
      )}
    </>
  )
}

function OrderError({ message }: { message: string }) { 
  return message ? <div className="form-error" role="alert"><CircleAlert size={16} />{message}</div> : null 
}
