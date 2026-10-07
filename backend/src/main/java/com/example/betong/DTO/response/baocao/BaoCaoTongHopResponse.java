package com.example.betong.DTO.response.baocao;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class BaoCaoTongHopResponse {
    private LocalDate tuNgay;
    private LocalDate denNgay;
    private long soDonHang;
    private long soChuyenGiao;
    private double sanLuongBeTong;
    private double doanhThu;
    private String thongBao;
}
