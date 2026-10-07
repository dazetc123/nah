package com.example.betong.DTO.request.betong;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Getter;
import lombok.Setter;

/**
 * Dùng chung cho Bảng 3.50 (Thêm mới loại bê tông) và Bảng 3.51 (Chỉnh
 * sửa loại bê tông). Vi phạm @NotBlank/@NotNull rơi vào nhánh phụ "Nhập
 * sai định dạng" / "Nhập thiếu thông tin bắt buộc" của cả 2 usecase.
 */
@Getter
@Setter
public class LoaiBeTongRequest {

    @NotBlank(message = "Vui lòng nhập mác bê tông")
    private String macBeTong;

    @NotBlank(message = "Vui lòng nhập thành phần")
    private String thanhPhan;

    @NotNull(message = "Vui lòng nhập đơn giá")
    @DecimalMin(value = "0.1", message = "Đơn giá phải lớn hơn 0")
    private Double donGia;

    @NotNull(message = "Vui lòng chọn trạng thái cung cấp")
    @Min(value = 0, message = "Trạng thái cung cấp chỉ được là 0 hoặc 1")
    @Max(value = 1, message = "Trạng thái cung cấp chỉ được là 0 hoặc 1")
    private Integer trangThai;

    private String moTa;
}
