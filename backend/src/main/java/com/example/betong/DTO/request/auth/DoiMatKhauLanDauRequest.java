package com.example.betong.DTO.request.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** Dữ liệu form đổi mật khẩu bắt buộc ở lần đăng nhập đầu tiên. */
@Getter
@Setter
public class DoiMatKhauLanDauRequest {

    @NotBlank(message = "Vui lòng nhập mật khẩu mới")
    @Size(min = 8, message = "Mật khẩu mới phải có ít nhất8 ký tự")
    private String matKhauMoi;

    @NotBlank(message = "Vui lòng nhập xác nhận mật khẩu mới")
    private String xacNhanMatKhauMoi;
}
