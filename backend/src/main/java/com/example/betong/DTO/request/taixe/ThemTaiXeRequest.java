package com.example.betong.DTO.request.taixe;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/**
 * Bảng 3.23 - Thêm tài xế: gắn hồ sơ chi tiết (GPLX, SĐT...) cho MỘT
 * tài khoản đã tồn tại và đang mang vai trò "Tài xế" nhưng chưa có hồ sơ
 * TaiXe — vì entity TaiXe bắt buộc phải gắn với 1 TaiKhoan có sẵn
 * (không tự tạo tài khoản đăng nhập mới ở đây, việc đó thuộc Bảng 3.4).
 */
@Getter
@Setter
public class ThemTaiXeRequest {

    @NotNull(message = "Vui lòng chọn tài khoản")
    private Long idTK;

    @NotBlank(message = "Vui lòng nhập họ tên")
    private String hoTen;

    @NotBlank(message = "Vui lòng nhập số giấy phép lái xe")
    private String soGPLX;

    @NotBlank(message = "Vui lòng nhập số điện thoại")
    @Pattern(regexp = "^0[0-9]{9}$", message = "Số điện thoại không đúng định dạng (10 số, bắt đầu bằng 0)")
    private String sdt;
}
