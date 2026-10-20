package com.example.betong.DTO.request.taixe;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/** Bảng 3.25 - Sửa tài xế. Không đổi idTK (gắn cứng với tài khoản lúc tạo). */
@Getter
@Setter
public class SuaTaiXeRequest {

    @NotBlank(message = "Vui lòng nhập họ tên")
    private String hoTen;

    @NotBlank(message = "Vui lòng nhập số giấy phép lái xe")
    private String soGPLX;

    @NotBlank(message = "Vui lòng nhập số điện thoại")
    @Pattern(regexp = "^0[0-9]{9}$", message = "Số điện thoại không đúng định dạng (10 số, bắt đầu bằng 0)")
    private String sdt;

    /** 1 = đang hoạt động, 0 = ngừng hoạt động. */
    private Integer trangThai;
}
