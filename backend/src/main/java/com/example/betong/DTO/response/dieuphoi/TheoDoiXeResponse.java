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
    private String thongBao;
}
