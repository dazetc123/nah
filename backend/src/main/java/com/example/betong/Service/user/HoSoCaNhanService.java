package com.example.betong.Service.user;

import com.example.betong.DTO.request.user.CapNhatHoSoRequest;
import com.example.betong.DTO.request.user.DoiMatKhauRequest;
import com.example.betong.DTO.response.user.TaiKhoanResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * Bảng 3.39 - Cập nhật thông tin cá nhân, Bảng 3.40 - Đổi mật khẩu.
 * Báo cáo mô tả Tác nhân = Khách hàng, nhưng nghiệp vụ "tự xem/sửa hồ sơ,
 * tự đổi mật khẩu của chính mình" là nhu cầu chung của MỌI tài khoản đã
 * đăng nhập (Quản lý, Nhân viên điều phối, Tài xế, Khách hàng) — không có
 * lý do nghiệp vụ để giới hạn riêng cho Khách hàng, nên áp dụng chung cho
 * mọi vai trò. Đây là phần "hệ thống thật cần có" dù báo cáo không tách
 * riêng usecase cho từng vai trò.
 */
public interface HoSoCaNhanService {

    /** Xem thông tin hồ sơ của chính tài khoản đang đăng nhập. */
    TaiKhoanResponse xemHoSo(String tenDangNhap);

    /** Bảng 3.39 - Cập nhật thông tin cá nhân (họ tên, sđt, email, địa chỉ nếu là Khách hàng). */
    TaiKhoanResponse capNhatHoSo(String tenDangNhap, CapNhatHoSoRequest request);

    /** Bảng 3.40 - Đổi mật khẩu (yêu cầu đúng mật khẩu hiện tại). */
    void doiMatKhau(String tenDangNhap, DoiMatKhauRequest request);

    TaiKhoanResponse capNhatAnhDaiDien(String tenDangNhap, MultipartFile anh);
}
