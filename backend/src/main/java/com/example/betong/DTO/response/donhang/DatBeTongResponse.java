package com.example.betong.DTO.response.donhang;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DatBeTongResponse {
    private Long idDH;
    private Long idLBT;
    private String macBeTong;
    private Double khoiLuong;
    private Double donGia;
    private Double thanhTien;
    private Long idCT;
    private String diaChiGiao;
    private LocalDateTime thoiGianGiao;
    private Double tongTien;
    private Integer trangThai;
    private String thongBao;
}
