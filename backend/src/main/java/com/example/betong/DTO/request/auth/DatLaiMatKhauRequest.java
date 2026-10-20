package com.example.betong.DTO.request.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DatLaiMatKhauRequest {
    @NotBlank(message = "Vui lòng nhập email, số điện thoại hoặc tên đăng nhập")
    private String dinhDanh;

    @NotBlank(message = "Vui lòng nhập mã OTP")
    @Size(min = 6, max = 6, message = "Mã OTP phải gồm 6 chữ số")
    private String maOtp;

    @NotBlank(message = "Vui lòng nhập mật khẩu mới")
    @Size(min = 8, message = "Mật khẩu phải có ít nhất 8 ký tự")
    private String matKhauMoi;

    @NotBlank(message = "Vui lòng nhập xác nhận mật khẩu mới")
    private String xacNhanMatKhauMoi;
}
