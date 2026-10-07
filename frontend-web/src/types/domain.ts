// Field đặt trùng tên với DTO Java (com.example.betong.DTO.*) để nhận JSON không cần map lại.

export type VaiTro = 'Quản lý' | 'Nhân viên điều phối' | 'Tài xế' | 'Khách hàng'

/** Phiên đăng nhập lưu ở localStorage (khớp LoginResponse). */
export interface Session {
  accessToken: string
  idTK: number
  hoTen: string
  anhDaiDien?: string | null
  tenVaiTro: VaiTro | string
  phaiDoiMatKhau: boolean
}

export interface LoginResponse {
  accessToken: string
  tokenType?: string
  idTK: number
  hoTen: string
  tenVaiTro: string
  phaiDoiMatKhau: boolean
}

export interface PageResponse<T> {
  danhSach: T[]
  trangHienTai: number // đánh số từ 1
  tongSoTrang: number
  tongSoPhanTu: number
}

export interface ThongBaoResponse {
  message: string
}

/** TaiKhoanResponse.java — trangThai: 1 = hoạt động, 0 = khoá; phaiDoiMatKhau: 1/0 */
export interface TaiKhoan {
  idTK: number
  tenDangNhap: string
  email?: string | null
  sdt?: string | null
  hoTen: string
  anhDaiDien?: string | null
  trangThai: number
  tenVaiTro: string
  phaiDoiMatKhau?: number | null
  soGPLX?: string | null
  diaChi?: string | null
  ngaySinh?: string | null
  diaChiThuongTru?: string | null
  gioiTinh?: string | null
  idKH?: number | null
  tenKhachHang?: string | null
  /** Chỉ có khi backend đã bổ sung idTX vào TaiKhoanResponse (xem backend-patches). */
  idTX?: number | null
  thongBao?: string | null
}

export interface TaoTaiKhoanResponse {
  idTK: number
  tenDangNhap: string
  matKhauMacDinh?: string
  message?: string
  thongBao?: string
}

/** XeResponse.java — trangThai: 1 = đang hoạt động, 0 = bảo trì; idTX vắng mặt khi chưa gán tài xế */
export interface Xe {
  idXe: number
  bienSo: string
  trongTai: number
  trangThai: number
  idTX?: number | null
  thongBao?: string
}

/** TramTronResponse.java — trangThai: 1 = hoạt động, 0 = ngừng; congSuat tính theo m³/giờ */
export interface TramTron {
  idTram: number
  tenTram: string
  diaChi: string
  congSuat: number
  sdt: string
  trangThai: number
  thongBao?: string | null
}

export interface TaiXe {
  idTX: number
  idTK: number
  tenDangNhap?: string
  hoTen: string
  soGPLX: string
  sdt: string
  trangThai: number
  bienSoXeDangGan?: string | null
  thongBao?: string | null
}

export interface TaiXeForm {
  idTK: number
  hoTen: string
  soGPLX: string
  sdt: string
  trangThai?: number
}

export interface LoaiBeTong {
  idLBT: number
  macBeTong: string
  thanhPhan: string
  donGia: number
  /** 1 = đang cung cấp, 0 = ngừng cung cấp */
  trangThai: number
  moTa?: string | null
  soDonHangDaSuDung?: number
  thongBao?: string | null
}

export interface LoaiBeTongForm {
  macBeTong: string
  thanhPhan: string
  donGia: number
  trangThai: number
  moTa?: string
}

export interface BaoCaoTinhTrangXe {
  idBaoCao: number
  idXe: number
  bienSo: string
  idTX: number
  nguoiBaoCao?: string | null
  noiDung: string
  trangThaiXe?: number | null
  diaChiHu?: string | null
  soDienThoaiTaiXe?: string | null
  nguyenNhan?: string | null
  anhMinhChung?: string | null
  thoiGian: string
}

export interface CongTrinh {
  idCT: number
  idKH: number
  tenCongTrinh: string
  diaChi: string
  viDo: number
  kinhDo: number
  sdt: string
  thongBao?: string | null
}

export interface CongTrinhForm {
  tenCongTrinh: string
  diaChi: string
  viDo: number
  kinhDo: number
  sdt: string
}

export interface DatBeTong {
  idDH: number
  idLBT: number
  macBeTong: string
  khoiLuong: number
  donGia: number
  thanhTien: number
  idCT?: number | null
  diaChiGiao: string
  thoiGianGiao: string
  tongTien: number
  trangThai: number
  thongBao?: string | null
}

export interface DatBeTongForm {
  idLBT: number
  khoiLuong: number
  idCT?: number
  diaChiMoi?: string
  thoiGianGiao: string
  ghiChu?: string
}

export interface DonHang {
  idDH: number
  idLBT?: number | null
  macBeTong?: string | null
  khoiLuong: number
  donGia?: number | null
  thanhTien?: number | null
  idCT?: number | null
  tenCongTrinh?: string | null
  diaChiGiao: string
  ngayDat?: string | null
  thoiGianGiao: string
  tongTien: number
  trangThai: number
  tenTrangThai: string
  ghiChu?: string | null
  lyDoTuChoi?: string | null
  idXe?: number | null
  bienSoXe?: string | null
  viDo?: number | null
  kinhDo?: number | null
  thoiDiemGPS?: string | null
  thongBao?: string | null
}

export interface XeForm {
  bienSo: string
  trongTai: number
  trangThai: number
}

export interface TramTronForm {
  tenTram: string
  diaChi: string
  congSuat: number
  sdt: string
  trangThai: number
}

export interface TaoTaiKhoanForm {
  tenDangNhap: string
  matKhau: string
  hoTen: string
  email: string
  sdt: string
  tenVaiTro: 'Nhân viên điều phối' | 'Tài xế'
  soGPLX: string
}

// Compatibility models for the legacy presentation components.
export type View = 'overview' | 'vehicles' | 'plants' | 'accounts'
export type AccountCategory = 'staff' | 'dispatchers' | 'drivers' | 'customers'
export type Status = 'Đang hoạt động' | 'Đang bảo trì' | 'Tạm dừng'

export interface Vehicle {
  idXe: number
  bienSo: string
  trongTai: number
  trangThai: Status
  taiXe: string
  tuyen: string
  capNhat: string
}

export interface Plant {
  idTram: number
  tenTram: string
  diaChi: string
  congSuat: number
  trangThai: Status
  donHang: number
}

export interface Account {
  idTK: number
  hoTen: string
  email: string
  tenVaiTro: string
  trangThai: Status
}

export interface ChuyenResponse {
  idChuyen: number;
  idDH: number;
  idTram: number;
  tenTram: string;
  idXe: number;
  bienSo: string;
  idTX: number;
  tenTaiXe: string;
  khoiLuong: number;
  trangThai: number;
  thoiGianXuatPhat?: string;
  thoiGianDen?: string;
  thongBao?: string;
}

export interface BaoCaoDongResponse { idDH: number; ngayDat: string; sanLuong: number; doanhThu: number; trangThai: number }
export interface BaoCaoTongHopResponse { tuNgay: string; denNgay: string; soDonHang: number; soChuyenGiao: number; sanLuongBeTong: number; doanhThu: number; thongBao: string }
