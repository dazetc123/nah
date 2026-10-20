package com.example.betong.Service.user;

import com.example.betong.DTO.request.user.PhanQuyenRequest;
import com.example.betong.DTO.response.common.PageResponse;
import com.example.betong.DTO.response.user.TaiKhoanResponse;

/**
 * Bảng 3.11 - Phân quyền người dùng. Tách riêng khỏi TaiKhoanAdminService
 * vì báo cáo mô tả đây là một usecase độc lập, có màn hình riêng
 * ("giao diện chứa danh sách TẤT CẢ tài khoản") khác với màn hình Sửa
 * tài khoản (Bảng 3.5, chỉ sửa hồ sơ, không đổi vai trò).
 */
public interface PhanQuyenService {

    /** Bước 2: "Hệ thống hiển thị giao diện chứa danh sách tất cả tài khoản" — không lọc theo vai trò như Bảng 3.7/3.8. */
    PageResponse<TaiKhoanResponse> danhSachTatCaTaiKhoan(String tuKhoa, int trang, int soLuong);

    /** Bước 3-6: đổi vai trò cho 1 tài khoản. */
    TaiKhoanResponse doiVaiTro(Long idTK, PhanQuyenRequest request, String tenDangNhapNguoiThucHien);
}
