package com.example.betong.DTO.response.dieuphoi;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChuyenResponse {
    private Long idChuyen;
    private Long idDH;
    private Long idTram;
    private String tenTram;
    private Long idXe;
    private String bienSo;
    private Long idTX;
    private String tenTaiXe;
    private Double khoiLuong;
    private Integer trangThai;
    private LocalDateTime thoiGianXuatPhat;
    private LocalDateTime thoiGianDen;
    private String thongBao;
}
