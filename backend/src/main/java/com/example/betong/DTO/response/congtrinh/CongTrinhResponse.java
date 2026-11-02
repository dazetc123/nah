package com.example.betong.DTO.response.congtrinh;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CongTrinhResponse {
    private Long idCT;
    private Long idKH;
    private String tenCongTrinh;
    private String diaChi;
    private Double viDo;
    private Double kinhDo;
    private String sdt;
    private String thongBao;
}
