import { useEffect, useState, type FormEvent } from 'react'
import { LoaderCircle } from 'lucide-react'
import Modal from '../common/Modal'
import Field from '../common/Field'
import { accountApi, constructionApi } from '../../lib/api'
import { errorText } from '../../lib/api/client'
import type { CongTrinh, TaiKhoan } from '../../types/domain'

export default function ConstructionFormModal({ editing, customerId, onClose, onSaved }: {
  editing: CongTrinh | null
  customerId?: number
  onClose: () => void
  onSaved: () => void
}) {
  const [tenCongTrinh, setTenCongTrinh] = useState(editing?.tenCongTrinh ?? '')
  const [diaChi, setDiaChi] = useState(editing?.diaChi ?? '')
  const [viDo, setViDo] = useState(String(editing?.viDo ?? ''))
  const [kinhDo, setKinhDo] = useState(String(editing?.kinhDo ?? ''))
  const [sdt, setSdt] = useState(editing?.sdt ?? '')
  const [customers, setCustomers] = useState<TaiKhoan[]>([])
  const [selectedCustomer, setSelectedCustomer] = useState(String(customerId ?? ''))
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  useEffect(() => {
    if (!editing && !customerId) accountApi.customers('', 1, 100).then((page) => setCustomers(page.danhSach)).catch(() => setCustomers([]))
  }, [customerId, editing])
  async function submit(e: FormEvent) {
    e.preventDefault()
    if (!tenCongTrinh.trim() || !diaChi.trim() || !sdt.trim() || !viDo || !kinhDo) {
      setError('Vui lòng nhập đầy đủ thông tin công trình'); return
    }
    if (!/^0[0-9]{9}$/.test(sdt.trim())) { setError('Số điện thoại phải gồm 10 số và bắt đầu bằng 0'); return }
    const latitude = Number(viDo); const longitude = Number(kinhDo)
    if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
      setError('Vĩ độ phải từ -90 đến 90 và kinh độ từ -180 đến 180'); return
    }
    setLoading(true); setError('')
    try {
      if (editing) await constructionApi.update(editing.idCT, { tenCongTrinh: tenCongTrinh.trim(), diaChi: diaChi.trim(), viDo: latitude, kinhDo: longitude, sdt: sdt.trim() })
      else if (selectedCustomer) await constructionApi.create({ idKH: Number(selectedCustomer), tenCongTrinh: tenCongTrinh.trim(), diaChi: diaChi.trim(), viDo: latitude, kinhDo: longitude, sdt: sdt.trim() })
      else throw new Error('Thiếu mã khách hàng để tạo công trình')
      onSaved()
    } catch (err) { setError(errorText(err, 'Không thể lưu công trình')) }
    finally { setLoading(false) }
  }
  return <Modal title={editing ? 'Cập nhật công trình' : 'Thêm công trình'} onClose={onClose} width={650}
    footer={<><button className="btn" onClick={onClose} disabled={loading}>Hủy</button><button className="btn btn-primary" onClick={submit} disabled={loading}>{loading && <LoaderCircle size={15} className="spin" />}Lưu công trình</button></>}>
    <form className="form-stack" onSubmit={submit} noValidate>
      {!editing && !customerId && <Field label="Khách hàng"><select value={selectedCustomer} onChange={(e) => setSelectedCustomer(e.target.value)}><option value="">Chọn khách hàng</option>{customers.map((customer) => <option key={customer.idKH ?? customer.idTK} value={customer.idKH ?? customer.idTK}>{customer.hoTen}</option>)}</select></Field>}
      <Field label="Tên công trình"><input value={tenCongTrinh} onChange={(e) => setTenCongTrinh(e.target.value)} autoFocus /></Field>
      <Field label="Địa chỉ"><input value={diaChi} onChange={(e) => setDiaChi(e.target.value)} /></Field>
      <div className="form-grid"><Field label="Vĩ độ"><input type="number" step="any" value={viDo} onChange={(e) => setViDo(e.target.value)} /></Field><Field label="Kinh độ"><input type="number" step="any" value={kinhDo} onChange={(e) => setKinhDo(e.target.value)} /></Field></div>
      <Field label="Số điện thoại liên hệ"><input inputMode="tel" value={sdt} onChange={(e) => setSdt(e.target.value)} /></Field>
      {error && <div className="form-error">{error}</div>}
    </form>
  </Modal>
}
