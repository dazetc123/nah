package com.example.betong.DTO.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DoiMatKhauRequest {
    @NotBlank(message = "Vui lòng nhập mật khẩu hiện tại")
    private String matKhauHienTai;
    @NotBlank(message = "Vui lòng nhập mật khẩu mới")
    @Size(min = 8, message = "Mật khẩu mới phải có ít nhất 8 ký tự")
    private String matKhauMoi;
    @NotBlank(message = "Vui lòng nhập xác nhận mật khẩu mới")
    private String xacNhanMatKhauMoi;
}
