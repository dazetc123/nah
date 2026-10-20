package com.example.betong.DTO.response.xe;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter @Builder
public class BaoCaoTinhTrangXeResponse {
    private Long idBaoCao;
    private Long idXe;
    private String bienSo;
    private Long idTX;
    private String nguoiBaoCao;
    private String noiDung;
    private Integer trangThaiXe;
    private String diaChiHu;
    private String soDienThoaiTaiXe;
    private String nguyenNhan;
    private String anhMinhChung;
    private LocalDateTime thoiGian;
}
