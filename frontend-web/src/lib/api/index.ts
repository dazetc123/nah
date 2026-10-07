import { query, request } from './client'
import type {
  LoginResponse, PageResponse, TaiKhoan, TaoTaiKhoanForm, TaoTaiKhoanResponse,
  ThongBaoResponse, TramTron, TramTronForm, Xe, XeForm,
  TaiXe, TaiXeForm, LoaiBeTong, LoaiBeTongForm, BaoCaoTinhTrangXe, CongTrinh, CongTrinhForm, DatBeTong, DatBeTongForm, DonHang,
  ChuyenResponse, BaoCaoDongResponse, BaoCaoTongHopResponse
} from '../../types/domain'

type Paged<T> = Promise<PageResponse<T>>
const pageQuery = (tuKhoa: string, trang: number, soLuong: number, extra: Record<string, string | number | undefined> = {}) =>
  query({ tuKhoa, trang, soLuong, ...extra })

export const authApi = {
  login: (dinhDanh: string, matKhau: string) =>
    request<LoginResponse>('/api/auth/dang-nhap', { method: 'POST', body: { dinhDanh, matKhau }, auth: false }),
  register: (body: unknown) =>
    request<{ idTK: number; tenDangNhap: string; message?: string }>('/api/auth/dang-ky', { method: 'POST', body, auth: false }),
  forgot: (dinhDanh: string, kenh: 'EMAIL' | 'SMS') =>
    request<ThongBaoResponse>('/api/auth/quen-mat-khau', { method: 'POST', body: { dinhDanh, kenh }, auth: false }),
  reset: (body: { dinhDanh: string; maOtp: string; matKhauMoi: string; xacNhanMatKhauMoi: string }) =>
    request<ThongBaoResponse>('/api/auth/dat-lai-mat-khau', { method: 'POST', body, auth: false }),
  firstPassword: (matKhauMoi: string, xacNhanMatKhauMoi: string) =>
    request<LoginResponse>('/api/auth/doi-mat-khau-lan-dau', { method: 'POST', body: { matKhauMoi, xacNhanMatKhauMoi } }),
  logout: () => request<ThongBaoResponse>('/api/auth/dang-xuat', { method: 'POST' }),
}

export const accountApi = {
  staff: (tuKhoa: string, trang: number, soLuong: number): Paged<TaiKhoan> =>
    request(`/api/quan-ly/tai-khoan${pageQuery(tuKhoa, trang, soLuong)}`),
  customers: (tuKhoa: string, trang: number, soLuong: number): Paged<TaiKhoan> =>
    request(`/api/quan-ly/tai-khoan/khach-hang${pageQuery(tuKhoa, trang, soLuong)}`),
  detail: (id: number) => request<TaiKhoan>(`/api/quan-ly/tai-khoan/${id}`),
  dispatcherDetail: (id: number) => request<TaiKhoan>(`/api/quan-ly/tai-khoan/nhan-vien-dieu-phoi/${id}`),
  driverDetail: (id: number) => request<TaiKhoan>(`/api/quan-ly/tai-khoan/tai-xe/${id}`),
  customerDetail: (id: number) => request<TaiKhoan>(`/api/quan-ly/tai-khoan/khach-hang/${id}`),
  create: (b: TaoTaiKhoanForm) =>
    request<TaoTaiKhoanResponse>('/api/quan-ly/tai-khoan', { method: 'POST', body: b }),
  update: (id: number, b: { hoTen: string; email: string; sdt: string; soGPLX?: string }) =>
    request<TaiKhoan>(`/api/quan-ly/tai-khoan/${id}`, { method: 'PUT', body: b }),
  setStatus: (id: number, trangThai: number) =>
    request<TaiKhoan>(`/api/quan-ly/tai-khoan/${id}/trang-thai`, { method: 'PUT', body: { trangThai } }),
  allForRoles: (tuKhoa: string, trang: number, soLuong: number): Paged<TaiKhoan> =>
    request(`/api/quan-ly/phan-quyen${pageQuery(tuKhoa, trang, soLuong)}`),
  changeRole: (id: number, tenVaiTroMoi: string) =>
    request<TaiKhoan>(`/api/quan-ly/phan-quyen/${id}`, { method: 'PUT', body: { tenVaiTroMoi } }),
}

export const vehicleApi = {
  list: (tuKhoa: string, trang: number, soLuong: number, trangThai?: number): Paged<Xe> =>
    request(`/api/quan-ly/xe${pageQuery(tuKhoa, trang, soLuong, { trangThai })}`),
  create: (b: XeForm) => request<Xe>('/api/quan-ly/xe', { method: 'POST', body: b }),
  update: (id: number, b: XeForm) => request<Xe>(`/api/quan-ly/xe/${id}`, { method: 'PUT', body: b }),
  remove: (id: number) => request<ThongBaoResponse>(`/api/quan-ly/xe/${id}`, { method: 'DELETE' }),
  setStatus: (id: number, trangThai: number) =>
    request<Xe>(`/api/quan-ly/xe/${id}/trang-thai`, { method: 'PUT', body: { trangThai } }),
  assignDriver: (id: number, idTX: number) =>
    request<Xe>(`/api/quan-ly/xe/${id}/tai-xe`, { method: 'PUT', body: { idTX } }),
  unassignDriver: (id: number) => request<Xe>(`/api/quan-ly/xe/${id}/tai-xe`, { method: 'DELETE' }),
}

export const plantApi = {
  list: (tuKhoa: string, trang: number, soLuong: number): Paged<TramTron> =>
    request(`/api/quan-ly/tram-tron${pageQuery(tuKhoa, trang, soLuong)}`),
  create: (b: TramTronForm) => request<TramTron>('/api/quan-ly/tram-tron', { method: 'POST', body: b }),
  update: (id: number, b: TramTronForm) => request<TramTron>(`/api/quan-ly/tram-tron/${id}`, { method: 'PUT', body: b }),
  remove: (id: number) => request<ThongBaoResponse>(`/api/quan-ly/tram-tron/${id}`, { method: 'DELETE' }),
  setCapacity: (id: number, congSuat: number) =>
    request<TramTron>(`/api/quan-ly/tram-tron/${id}/cong-suat`, { method: 'PUT', body: { congSuat } }),
}

export const driverApi = {
  list: (tuKhoa: string, trang: number, soLuong: number): Paged<TaiXe> =>
    request(`/api/quan-ly/tai-xe${pageQuery(tuKhoa, trang, soLuong)}`),
  detail: (id: number) => request<TaiXe>(`/api/quan-ly/tai-xe/${id}`),
  create: (body: TaiXeForm) => request<TaiXe>('/api/quan-ly/tai-xe', { method: 'POST', body }),
  update: (id: number, body: Omit<TaiXeForm, 'idTK'>) =>
    request<TaiXe>(`/api/quan-ly/tai-xe/${id}`, { method: 'PUT', body }),
  remove: (id: number) => request<ThongBaoResponse>(`/api/quan-ly/tai-xe/${id}`, { method: 'DELETE' }),
}

export const concreteApi = {
  list: (tuKhoa: string, trang: number, soLuong: number): Paged<LoaiBeTong> =>
    request(`/api/quan-ly/loai-be-tong${pageQuery(tuKhoa, trang, soLuong)}`),
  detail: (id: number) => request<LoaiBeTong>(`/api/quan-ly/loai-be-tong/${id}`),
  create: (body: LoaiBeTongForm) => request<LoaiBeTong>('/api/quan-ly/loai-be-tong', { method: 'POST', body }),
  update: (id: number, body: LoaiBeTongForm) =>
    request<LoaiBeTong>(`/api/quan-ly/loai-be-tong/${id}`, { method: 'PUT', body }),
  remove: (id: number) => request<ThongBaoResponse>(`/api/quan-ly/loai-be-tong/${id}`, { method: 'DELETE' }),
}

export const vehicleReportApi = {
  create: (idXe: number, bodyData: {
    trangThaiXe: number
    diaChiHu: string
    soDienThoaiTaiXe: string
    nguyenNhan: string
    noiDung: string
    anh: File
  }) => {
    const body = new FormData()
    body.append('anh', bodyData.anh)
    const params = query({
      trangThaiXe: bodyData.trangThaiXe,
      diaChiHu: bodyData.diaChiHu,
      soDienThoaiTaiXe: bodyData.soDienThoaiTaiXe,
      nguyenNhan: bodyData.nguyenNhan,
      noiDung: bodyData.noiDung,
    })
    return request<BaoCaoTinhTrangXe>(`/api/dieu-phoi/xe/${idXe}/bao-cao${params}`, { method: 'POST', body })
  },
  list: (trang: number, soLuong: number): Paged<BaoCaoTinhTrangXe> =>
    request(`/api/quan-ly/bao-cao-xe${pageQuery('', trang, soLuong)}`),
}

export const profileApi = {
  get: () => request<TaiKhoan>('/api/nguoi-dung/ho-so'),
  uploadAvatar: (anh: File) => {
    const body = new FormData()
    body.append('anh', anh)
    return request<TaiKhoan>('/api/nguoi-dung/ho-so/anh', { method: 'POST', body })
  },
  update: (b: { hoTen: string; sdt?: string; email?: string; diaChi?: string; ngaySinh?: string; gioiTinh?: string; diaChiThuongTru?: string }) =>
    request<TaiKhoan>('/api/nguoi-dung/ho-so', { method: 'PUT', body: b }),
  changePassword: (b: { matKhauHienTai: string; matKhauMoi: string; xacNhanMatKhauMoi: string }) =>
    request<void>('/api/nguoi-dung/doi-mat-khau', { method: 'PUT', body: b }),
}

export const constructionApi = {
  managerList: (tuKhoa: string, trang: number, soLuong: number): Paged<CongTrinh> =>
    request(`/api/quan-ly/cong-trinh${pageQuery(tuKhoa, trang, soLuong)}`),
  customerList: (tuKhoa: string, trang: number, soLuong: number): Paged<CongTrinh> =>
    request(`/api/khach-hang/cong-trinh${pageQuery(tuKhoa, trang, soLuong)}`),
  detail: (id: number) => request<CongTrinh>(`/api/quan-ly/cong-trinh/${id}`),
  create: (body: CongTrinhForm & { idKH: number }) =>
    request<CongTrinh>('/api/quan-ly/cong-trinh', { method: 'POST', body }),
  update: (id: number, body: CongTrinhForm) =>
    request<CongTrinh>(`/api/quan-ly/cong-trinh/${id}`, { method: 'PUT', body }),
  remove: (id: number) => request<ThongBaoResponse>(`/api/quan-ly/cong-trinh/${id}`, { method: 'DELETE' }),
  customerRemove: (id: number) => request<ThongBaoResponse>(`/api/khach-hang/cong-trinh/${id}`, { method: 'DELETE' }),
}

export const concreteOrderApi = {
  concreteTypes: (tuKhoa: string, trang: number, soLuong: number): Paged<LoaiBeTong> =>
    request(`/api/khach-hang/dat-be-tong/loai-be-tong${pageQuery(tuKhoa, trang, soLuong)}`),
  create: (body: DatBeTongForm) =>
    request<DatBeTong>('/api/khach-hang/dat-be-tong/dat-hang', { method: 'POST', body }),
}

export const customerOrderApi = {
  list: (tuKhoa: string, trang: number, soLuong: number): Paged<DonHang> =>
    request(`/api/khach-hang/don-hang${pageQuery(tuKhoa, trang, soLuong)}`),
  detail: (id: number) => request<DonHang>(`/api/khach-hang/don-hang/${id}`),
  track: (id: number) => request<DonHang>(`/api/khach-hang/don-hang/${id}/theo-doi`),
  cancel: (id: number) => request<DonHang>(`/api/khach-hang/don-hang/${id}/huy`, { method: 'PUT' }),
}

export const reportApi = {
  list: (tuNgay: string, denNgay: string) => request<BaoCaoDongResponse[]>(`/api/quan-ly/bao-cao?tuNgay=${tuNgay}&denNgay=${denNgay}`),
  summary: (tuNgay: string, denNgay: string) => request<BaoCaoTongHopResponse>(`/api/quan-ly/bao-cao/thong-ke?tuNgay=${tuNgay}&denNgay=${denNgay}`)
}

export const driverTripsApi = {
  list: (tuKhoa: string, trang: number, soLuong: number) => request<PageResponse<ChuyenResponse>>('/api/dieu-phoi/chuyen-xe' + pageQuery(tuKhoa, trang, soLuong))
}
