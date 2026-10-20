package com.example.betong.DTO.response.taixe;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class TaiXeResponse {
    private Long idTX;
    private Long idTK;
    private String tenDangNhap;
    private String hoTen;
    private String anhDaiDien;
    private LocalDate ngaySinh;
    private String diaChiThuongTru;
    private String gioiTinh;
    private String soGPLX;
    private String sdt;
    private Integer trangThai;

    /** Biển số xe đang gán cho tài xế này, null nếu chưa gán xe nào. */
    private String bienSoXeDangGan;

    private String thongBao;
}
