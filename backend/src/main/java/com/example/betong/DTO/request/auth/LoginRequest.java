package com.example.betong.DTO.request.auth;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Dữ liệu form đăng nhập (Bảng 3.2 - bước 4: "Nhập thông tin đúng theo
 * phương thức đã chọn").
 *
 * Các ràng buộc @NotBlank / @Size chính là bước 5 "Hệ thống kiểm tra
 * thông tin": nếu sai định dạng hoặc bỏ trống dữ liệu (5.a/5.b), Spring
 * Validation sẽ tự động chặn lại và trả lỗi 400 trước khi vào Service.
 */
@Getter
@Setter
public class LoginRequest {

    /** Có thể là tên đăng nhập hoặc email, tùy phương thức người dùng chọn ở bước 3. */
    @NotBlank(message = "Vui lòng nhập tên đăng nhập hoặc email")
    private String dinhDanh;

    @NotBlank(message = "Vui lòng nhập mật khẩu")
    @Size(min = 8, message = "Mật khẩu phải có ít nhất 8 ký tự")
    private String matKhau;
}
