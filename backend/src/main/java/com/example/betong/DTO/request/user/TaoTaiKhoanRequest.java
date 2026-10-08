package com.example.betong.DTO.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TaoTaiKhoanRequest {
    @NotBlank(message = "Vui lòng nhập tên đăng nhập") private String tenDangNhap;
    @NotBlank(message = "Vui lòng nhập mật khẩu")
    @Size(min = 8, message = "Mật khẩu phải có ít nhất 8 ký tự") private String matKhau;
    @NotBlank(message = "Vui lòng nhập họ tên") private String hoTen;
    @Email(message = "Email không đúng định dạng") private String email;
    private String sdt;
    @NotBlank(message = "Vui lòng chọn vai trò") private String tenVaiTro;
    private String soGPLX;
}
