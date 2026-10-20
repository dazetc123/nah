import { useEffect, useState, type FormEvent } from 'react'
import { CircleAlert, LoaderCircle } from 'lucide-react'
import Modal from '../common/Modal'
import Field from '../common/Field'
import { driverApi, vehicleApi } from '../../lib/api'
import { errorText } from '../../lib/api/client'
import type { TaiXe, Xe } from '../../types/domain'

export default function AssignDriverModal({ xe, onClose, onSaved }: { xe: Xe; onClose: () => void; onSaved: () => void }) {
  const [drivers, setDrivers] = useState<TaiXe[]>([])
  const [idTX, setIdTX] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const [loadingDrivers, setLoadingDrivers] = useState(true)

  useEffect(() => {
    setLoadingDrivers(true)
    driverApi.list('', 1, 100)
      .then((res) => setDrivers(res.danhSach.filter((driver) => !driver.bienSoXeDangGan)))
      .catch((err) => setError(errorText(err, 'Không thể tải danh sách tài xế')))
      .finally(() => setLoadingDrivers(false))
  }, [])

  async function submit(e: FormEvent) {
    e.preventDefault()
    const id = Number(idTX)
    if (!Number.isInteger(id) || id <= 0) { setError('Vui lòng chọn tài xế chưa được gán xe'); return }
    setError(''); setLoading(true)
    try { await vehicleApi.assignDriver(xe.idXe, id); onSaved() }
    catch (err) { setError(errorText(err, 'Gán tài xế thất bại')) }
    finally { setLoading(false) }
  }

  return (
    <Modal title={`Gán tài xế — ${xe.bienSo}`} onClose={onClose}
      footer={<>
        <button className="btn" onClick={onClose} disabled={loading}>Hủy</button>
        <button className="btn btn-primary" onClick={submit} disabled={loading}>{loading && <LoaderCircle size={15} className="spin" />}Gán tài xế</button>
      </>}>
      <form className="form-stack" onSubmit={submit} noValidate>
        <Field label="Tài xế chưa gán xe">
          <select value={idTX} onChange={(e) => setIdTX(e.target.value)} disabled={loadingDrivers} autoFocus>
            <option value="">{loadingDrivers ? 'Đang tải danh sách tài xế…' : 'Chọn tài xế'}</option>
            {drivers.map((driver) => <option key={driver.idTX} value={driver.idTX}>{driver.hoTen} — {driver.sdt}</option>)}
          </select>
        </Field>
        {!loadingDrivers && drivers.length === 0 && !error && <div className="muted">Không còn tài xế chưa được gán xe.</div>}
        {error && <div className="form-error" role="alert"><CircleAlert size={16} style={{ flexShrink: 0, marginTop: 1 }} />{error}</div>}
      </form>
    </Modal>
  )
}
