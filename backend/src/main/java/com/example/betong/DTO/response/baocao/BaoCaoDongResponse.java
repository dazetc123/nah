package com.example.betong.DTO.response.baocao;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class BaoCaoDongResponse {
    private Long idDH;
    private LocalDate ngayDat;
    private Double sanLuong;
    private Double doanhThu;
    private Integer trangThai;
}
