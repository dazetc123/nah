package com.example.betong.DTO.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Dữ liệu form đăng ký (Bảng 3.1 - bước 4: "Thực hiện thao tác theo yêu
 * cầu của phương thức đã chọn").
 *
 * Các annotation validate dưới đây tương ứng bước 5 "Hệ thống kiểm tra
 * thông tin":
 *   - @NotBlank  -> nhánh 5.b "để trống một trong số các trường"
 *   - @Email / @Pattern / @Size -> nhánh 5.a "sai định dạng dữ liệu"
 * Nếu vi phạm, GlobalExceptionHandler sẽ trả lỗi 400 kèm thông báo cụ
 * thể, tương đương "yêu cầu nhập lại, quay lại bước 4" của usecase.
 */
@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "Vui lòng nhập tên đăng nhập")
    @Size(min = 4, max = 50, message = "Tên đăng nhập phải từ 4 đến 50 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9_.]+$", message = "Tên đăng nhập chỉ được chứa chữ, số, dấu chấm hoặc gạch dưới")
    private String tenDangNhap;

    @NotBlank(message = "Vui lòng nhập email")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Vui lòng nhập mật khẩu")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String matKhau;

    @NotBlank(message = "Vui lòng nhập xác nhận mật khẩu")
    private String xacNhanMatKhau;

    @NotBlank(message = "Vui lòng nhập họ tên")
    @Size(max = 100, message = "Họ tên tối đa 100 ký tự")
    private String hoTen;

    @NotBlank(message = "Vui lòng nhập số điện thoại")
    @Pattern(regexp = "^0[0-9]{9}$", message = "Số điện thoại không đúng định dạng (10 số, bắt đầu bằng 0)")
    private String sdt;
}
