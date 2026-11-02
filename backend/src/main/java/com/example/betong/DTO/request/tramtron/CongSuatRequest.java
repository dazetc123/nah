package com.example.betong.DTO.request.tramtron;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Bảng 3.22 - Quản lý công suất trạm trộn: usecase RIÊNG BIỆT, chỉ chỉnh
 * sửa duy nhất trường công suất (không đụng tên/địa chỉ/SĐT như Bảng 3.20).
 */
@Getter
@Setter
public class CongSuatRequest {

    @NotNull(message = "Vui lòng nhập công suất")
    @DecimalMin(value = "0.1", message = "Công suất phải lớn hơn 0")
    @DecimalMax(value = "1000", message = "Thông tin công suất không phù hợp với hệ thống")
    private Double congSuat;
}
