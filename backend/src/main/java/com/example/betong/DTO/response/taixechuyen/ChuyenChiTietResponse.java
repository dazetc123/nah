package com.example.betong.DTO.response.taixechuyen;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Bảng 2.2 (usecase "Xem thông tin chi tiết chuyến") - đầy đủ thông tin
 * một chuyến: công trình nhận, khối lượng, mác bê tông, trạm trộn xuất
 * phát, vị trí công trình trên bản đồ, ghi chú điều phối, số điện thoại.
 */
@Getter
@Builder
@AllArgsConstructor
public class ChuyenChiTietResponse {
    private Long idChuyen;
    private Long idDH;

    private String tenCongTrinh;
    private String diaChiCongTrinh;
    private Double viDoCongTrinh;
    private Double kinhDoCongTrinh;
    private String sdtCongTrinh;

    private String macBeTong;
    private Double khoiLuong;

    private String tenTram;
    private String diaChiTram;

    private LocalDateTime thoiGianGiao;        // thời gian giao dự kiến của đơn hàng
    private LocalDateTime thoiGianNhan;
    private LocalDateTime thoiGianXuatPhat;
    private LocalDateTime thoiGianDen;         // thời điểm xác nhận đã đến công trình
    private LocalDateTime thoiGianGiaoXong;
    private LocalDateTime thoiGianHoanThanh;

    // Xác nhận đã đến công trình
    private Double khoangCachDen;
    private String ghiChuDen;
    private Boolean canKiemTraDen;
    private Integer banKinhChoPhep;            // mét

    // Xác nhận giao hàng thành công
    private Double khoiLuongThucGiao;
    private String ghiChuGiaoHang;
    private String anhMinhChung;
    private Double tongKhoiLuongDonHang;
    private Double tongKhoiLuongDaGiao;

    private String bienSo;
    private String ghiChu;

    private Integer trangThai;
    private String tenTrangThai;
}
