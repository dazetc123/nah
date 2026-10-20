package com.example.betong.DTO.response.donhang;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder(toBuilder = true)
public class DonHangResponse {
    private Long idDH;
    private Long idLBT;
    private String macBeTong;
    private Double khoiLuong;
    private Double donGia;
    private Double thanhTien;
    private Long idCT;
    private String tenCongTrinh;
    private String diaChiGiao;
    private LocalDate ngayDat;
    private LocalDateTime thoiGianGiao;
    private Double tongTien;
    private Integer trangThai;
    private String tenTrangThai;
    private String ghiChu;
    private String lyDoTuChoi;
    private Long idXe;
    private String bienSoXe;
    private Long idTram;
    private String tenTram;
    private Double viDo;
    private Double kinhDo;
    private LocalDateTime thoiDiemGPS;
    private String thongBao;
}
