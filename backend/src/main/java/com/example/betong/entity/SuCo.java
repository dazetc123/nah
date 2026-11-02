package com.example.betong.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Bảng 3.88 - Bảng cơ sở dữ liệu sự cố
 */
@Entity
@Table(name = "su_co")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuCo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idSuCo")
    private Long idSuCo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idChuyen", referencedColumnName = "idChuyen")
    private Chuyen chuyen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idXe")
    private Xe xe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idTX")
    private TaiXe taiXe;

    @Column(length = 100)
    private String nguoiBaoCao;

    @Column(name = "loaiSuCo", length = 100)
    private String loaiSuCo;

    @Column(name = "moTa", length = 255)
    private String moTa;

    @Column(length = 255)
    private String diaChiHu;

    @Column(length = 1000)
    private String nguyenNhan;

    @Column
    private Integer trangThaiXe;

    @Column(length = 500)
    private String anhMinhChung;

    @Column(name = "mucDoUuTien")
    private Integer mucDoUuTien;

    @Column(name = "thoiDiem")
    private LocalDateTime thoiDiem;

    // Trạng thái xử lý sự cố: mới / đang xử lý / đã xử lý
    @Column(name = "trangThai")
    private Integer trangThai;
}
