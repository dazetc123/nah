package com.example.betong.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Bảng 3.84 - Bảng cơ sở dữ liệu đơn hàng
 */
@Entity
@Table(name = "don_hang")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idDH")
    private Long idDH;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idKH", referencedColumnName = "idKH", nullable = false)
    private KhachHang khachHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idCT", referencedColumnName = "idCT", nullable = false)
    private CongTrinh congTrinh;

    @Column(name = "ngayDat")
    private LocalDate ngayDat;

    @Column(name = "thoiGianGiao")
    private LocalDateTime thoiGianGiao;

    @Column(name = "tongKhoiLuong")
    private Double tongKhoiLuong;

    @Column(name = "tongTien")
    private Double tongTien;

    // Trạng thái đơn hàng: chờ xử lý / xác nhận / từ chối / đang giao / hoàn thành...
    @Column(name = "trangThai")
    private Integer trangThai;

    @Column(name = "ghiChu", length = 255)
    private String ghiChu;

    @Column(name = "lyDoTuChoi", length = 255)
    private String lyDoTuChoi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idTram", referencedColumnName = "idTram")
    private TramTron tramTron;

    @Version
    @Column(name = "version")
    private Long version;

    @OneToMany(mappedBy = "donHang", cascade = CascadeType.ALL)
    private List<ChiTietDonHang> chiTietDonHangs;

    @OneToMany(mappedBy = "donHang", cascade = CascadeType.ALL)
    private List<Chuyen> danhSachChuyen;
}