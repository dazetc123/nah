package com.example.betong.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Bảng 3.94 - Bảng cơ sở dữ liệu xe
 */
@Entity
@Table(name = "xe")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Xe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idXe")
    private Long idXe;

    @Column(name = "bienSo", length = 20, unique = true, nullable = false)
    private String bienSo;

    @Column(name = "trongTai", nullable = false)
    private Double trongTai;

    // Trạng thái: đang hoạt động / bảo trì
    @Column(name = "trangThai", nullable = false)
    private Integer trangThai;

    @ManyToOne(fetch = FetchType.LAZY)
    // Xe có thể được tạo trước khi được phân công tài xế.
    @JoinColumn(name = "idTX", referencedColumnName = "idTX", nullable = true)
    private TaiXe taiXe;

    @OneToMany(mappedBy = "xe")
    private List<Chuyen> danhSachChuyen;

    @OneToMany(mappedBy = "xe")
    private List<ViTriGPS> danhSachViTriGPS;
}
