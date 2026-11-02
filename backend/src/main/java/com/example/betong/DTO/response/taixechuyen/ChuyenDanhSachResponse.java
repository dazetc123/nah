package com.example.betong.DTO.response.taixechuyen;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Bảng 2.2 (usecase "Xem danh sách chuyến được phân công") - mỗi dòng
 * trong danh sách chuyến của tài xế đang đăng nhập.
 */
@Getter
@Builder
@AllArgsConstructor
public class ChuyenDanhSachResponse {
    private Long idChuyen;
    private Long idDH;
    private String tenCongTrinh;
    private Double khoiLuong;
    private LocalDateTime thoiGianGiao;
    private String bienSo;
    private Integer trangThai;
    private String tenTrangThai;
}
