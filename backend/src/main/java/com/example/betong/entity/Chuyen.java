package com.example.betong.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Bảng 3.82 - Bảng cơ sở dữ liệu chuyến
 */
@Entity
@Table(name = "chuyen")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chuyen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idChuyen")
    private Long idChuyen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idDH", referencedColumnName = "idDH", nullable = false)
    private DonHang donHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idXe", referencedColumnName = "idXe", nullable = false)
    private Xe xe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idTX", referencedColumnName = "idTX", nullable = false)
    private TaiXe taiXe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idTram", referencedColumnName = "idTram", nullable = false)
    private TramTron tramTron;

    @Column(name = "khoiLuong")
    private Double khoiLuong;

    @Column(name = "thoiGianXuatPhat")
    private LocalDateTime thoiGianXuatPhat;

    // Thời điểm tài xế xác nhận "Đã đến công trình" (mục 2.2.3)
    @Column(name = "thoiGianDen")
    private LocalDateTime thoiGianDen;

    // ===== Usecase "Xác nhận đã đến công trình" (mục 2.2.3) =====
    @Column(name = "viDoDen")
    private Double viDoDen;

    @Column(name = "kinhDoDen")
    private Double kinhDoDen;

    @Column(name = "khoangCachDen")
    private Double khoangCachDen; // mét, so với tọa độ công trình

    @Column(name = "ghiChuDen", length = 255)
    private String ghiChuDen;

    // true nếu xác nhận ngoài bán kính hoặc không lấy được GPS -> điều phối cần kiểm tra
    @Column(name = "canKiemTraDen")
    private Boolean canKiemTraDen;

    // ===== Usecase "Xác nhận giao hàng thành công" (mục 2.2.3) =====
    @Column(name = "khoiLuongThucGiao")
    private Double khoiLuongThucGiao;

    @Column(name = "thoiGianGiaoXong")
    private LocalDateTime thoiGianGiaoXong;

    @Column(name = "ghiChuGiaoHang", length = 255)
    private String ghiChuGiaoHang;

    @Column(name = "anhMinhChung", length = 500)
    private String anhMinhChung;

    // ===== Usecase "Nhận chuyến" / "Hoàn thành chuyến" (mục 2.2.1) =====
    @Column(name = "thoiGianNhan")
    private LocalDateTime thoiGianNhan;

    @Column(name = "thoiGianHoanThanh")
    private LocalDateTime thoiGianHoanThanh;

    // Trạng thái chuyến: đang giao / đã đến / hoàn thành...
    @Column(name = "trangThai")
    private Integer trangThai;

    @OneToMany(mappedBy = "chuyen", cascade = CascadeType.ALL)
    private List<ViTriGPS> danhSachViTriGPS;

    @OneToMany(mappedBy = "chuyen", cascade = CascadeType.ALL)
    private List<SuCo> danhSachSuCo;
}
