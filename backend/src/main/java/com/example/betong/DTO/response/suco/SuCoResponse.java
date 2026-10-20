package com.example.betong.DTO.response.suco;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/** Mục 2.2.3 - Usecase "Báo cáo sự cố": kết quả gửi báo cáo kèm trạng thái xử lý. */
@Getter
@Builder
@AllArgsConstructor
public class SuCoResponse {
    private Long idSuCo;
    private Long idChuyen;
    private String bienSo;
    private String tenTaiXe;
    private String nguoiBaoCao;
    private String loaiSuCo;
    private String moTa;
    private Integer mucDoUuTien;
    private String tenMucDoUuTien;
    private String viTri;
    private String anhMinhChung;
    private LocalDateTime thoiDiem;
    private Integer trangThai;
    private String tenTrangThai;
}
