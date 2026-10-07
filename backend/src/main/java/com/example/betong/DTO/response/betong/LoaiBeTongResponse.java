package com.example.betong.DTO.response.betong;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LoaiBeTongResponse {
    private Long idLBT;
    private String macBeTong;
    private String thanhPhan;
    private Double donGia;
    private Integer trangThai;
    private String moTa;

    /** Chỉ có giá trị khi gọi "Xem chi tiết" (Bảng 3.54) — số đơn hàng đã dùng loại bê tông này. */
    private Long soDonHangDaSuDung;

    private String thongBao;
}
