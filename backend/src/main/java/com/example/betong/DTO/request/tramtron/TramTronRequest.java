package com.example.betong.DTO.request.tramtron;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/**
 * Dùng chung cho Bảng 3.18 (Thêm trạm trộn) và Bảng 3.20 (Sửa trạm trộn).
 * Các ràng buộc @NotBlank/@NotNull/@Pattern tương ứng bước 4 "Hệ thống
 * kiểm tra thông tin" — vi phạm sẽ rơi vào nhánh phụ 4.a (sai định dạng)
 * hoặc 4.b (bỏ trống dữ liệu) của cả 2 usecase.
 */
@Getter
@Setter
public class TramTronRequest {

    @NotBlank(message = "Vui lòng nhập tên trạm trộn")
    private String tenTram;

    @NotBlank(message = "Vui lòng nhập địa chỉ")
    private String diaChi;

    @NotNull(message = "Vui lòng nhập công suất")
    @DecimalMin(value = "0.1", message = "Công suất phải lớn hơn 0")
    private Double congSuat;

    @NotBlank(message = "Vui lòng nhập số điện thoại")
    @Pattern(regexp = "^0[0-9]{9}$", message = "Số điện thoại không đúng định dạng (10 số, bắt đầu bằng 0)")
    private String sdt;

    /** 1 = hoạt động, 0 = ngừng hoạt động. Mặc định 1 nếu không truyền khi tạo mới. */
    private Integer trangThai;
}
