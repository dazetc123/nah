package com.example.betong.DTO.request.donhang;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CapNhatTrangThaiDonHangRequest {
    @NotNull(message = "Vui lòng chọn trạng thái đơn hàng")
    private Integer trangThai;
}
