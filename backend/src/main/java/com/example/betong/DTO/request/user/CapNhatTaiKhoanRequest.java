package com.example.betong.DTO.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Bảng 3.5 - Sửa thông tin tài khoản nhân viên điều phối/tài xế.
 * CHÚ Ý: không có trường tenVaiTro ở đây — đổi vai trò là một usecase
 * riêng (Bảng 3.11 - Phân quyền), không gộp vào form sửa thông tin.
 */
@Getter
@Setter
public class CapNhatTaiKhoanRequest {

    @NotBlank(message = "Vui lòng nhập họ tên")
    private String hoTen;

    @NotBlank(message = "Vui lòng nhập email")
    @Email(message = "Email không đúng định dạng")
    private String email;

    private String sdt;

    /** Chỉ áp dụng khi tài khoản đang mang vai trò Tài xế, bỏ qua nếu không phải. */
    private String soGPLX;
}
