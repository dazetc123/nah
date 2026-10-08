package com.example.betong.DTO.response.dieuphoi;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder(toBuilder = true)
public class TheoDoiXeResponse {
    private Long idChuyen;
    private Long idDH;
    private Long idXe;
    private String bienSo;
    private Long idTX;
    private String tenTaiXe;
    private Long idTram;
    private String tenTram;
    private Integer trangThaiChuyen;
    private String tenTrangThai;
    private Double viDo;
    private Double kinhDo;
    private Double tocDo;
    private LocalDateTime thoiDiemGPS;

    // Tiến độ do tài xế cập nhật từ app (mục 2.2.3)
    private LocalDateTime thoiGianXuatPhat;
    private LocalDateTime thoiGianDen;
    private Double khoangCachDen;
    private Boolean canKiemTraDen;
    private String ghiChuDen;
    private Double khoiLuongThucGiao;
    private LocalDateTime thoiGianGiaoXong;
    private String anhMinhChung;
    private LocalDateTime thoiGianHoanThanh;
    private String thongBao;
}
