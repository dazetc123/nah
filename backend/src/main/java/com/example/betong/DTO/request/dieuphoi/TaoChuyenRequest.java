package com.example.betong.DTO.request.dieuphoi;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TaoChuyenRequest {
    @NotNull private Long idDH;
    @NotNull private Long idTram;
    @NotNull private Long idXe;
    @NotNull private Long idTX;
    private LocalDateTime thoiGianXuatPhat;
    private LocalDateTime thoiGianDen;
}
