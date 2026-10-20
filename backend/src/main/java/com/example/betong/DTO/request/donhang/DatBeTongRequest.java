package com.example.betong.DTO.request.donhang;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class DatBeTongRequest {
    @NotNull(message = "Vui lòng chọn loại bê tông")
    private Long idLBT;

    @NotNull(message = "Vui lòng nhập khối lượng")
    @DecimalMin(value = "1.0", message = "Khối lượng tối thiểu là 1 m3")
    private Double khoiLuong;

    private Long idCT;
    private String diaChiMoi;

    @NotNull(message = "Vui lòng nhập thời gian giao hàng")
    @Future(message = "Thời gian giao phải ở tương lai")
    private LocalDateTime thoiGianGiao;

    private String ghiChu;
}
