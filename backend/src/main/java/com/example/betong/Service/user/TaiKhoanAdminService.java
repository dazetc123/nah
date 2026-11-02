package com.example.betong.Service.user;

import com.example.betong.DTO.request.user.CapNhatTaiKhoanRequest;
import com.example.betong.DTO.request.user.TaoTaiKhoanRequest;
import com.example.betong.DTO.request.user.TrangThaiRequest;
import com.example.betong.DTO.response.common.PageResponse;
import com.example.betong.DTO.response.user.TaiKhoanResponse;
import com.example.betong.DTO.response.user.TaoTaiKhoanResponse;

/**
 * Quản lý tài khoản Nhân viên điều phối / Tài xế / Khách hàng, đúng các
 * usecase Bảng 3.3, 3.4, 3.5, 3.6, 3.7, 3.8, 3.9, 3.10. KHÔNG có hàm xóa
 * tài khoản — báo cáo chỉ định nghĩa Khóa/Mở (Bảng 3.6), không có usecase
 * xóa. KHÔNG có hàm đổi vai trò — thuộc PhanQuyenService (Bảng 3.11).
 */
public interface TaiKhoanAdminService {

    /** Bảng 3.7 (xem DS NV/TX) + Bảng 3.3 (tìm kiếm): tuKhoa rỗng = xem tất cả. */
    PageResponse<TaiKhoanResponse> danhSachNhanVienDieuPhoiVaTaiXe(String tuKhoa, int trang, int soLuong);

    /** Bảng 3.8 - Xem danh sách khách hàng. */
    PageResponse<TaiKhoanResponse> danhSachKhachHang(String tuKhoa, int trang, int soLuong);

    /** Bảng 3.9 (chi tiết TK NV/TX) và Bảng 3.10 (chi tiết KH) dùng chung — cùng shape dữ liệu. */
    TaiKhoanResponse xemChiTiet(Long idTK);

    /** Bảng 3.4 - Thêm tài khoản NV điều phối/Tài xế, hệ thống tự sinh mật khẩu mặc định. */
    TaoTaiKhoanResponse themTaiKhoan(TaoTaiKhoanRequest request, String tenDangNhapNguoiThucHien);

    /** Bảng 3.5 - Sửa thông tin tài khoản NV điều phối/Tài xế (KHÔNG đổi vai trò ở đây). */
    TaiKhoanResponse suaTaiKhoan(Long idTK, CapNhatTaiKhoanRequest request, String tenDangNhapNguoiThucHien);

    /** Bảng 3.6 - Khóa/Mở tài khoản NV điều phối/Tài xế/Khách hàng (không áp dụng cho Quản lý). */
    TaiKhoanResponse khoaMoTaiKhoan(Long idTK, TrangThaiRequest request, String tenDangNhapNguoiThucHien);
}