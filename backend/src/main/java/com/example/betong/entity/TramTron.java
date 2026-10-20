package com.example.betong.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Bảng 3.91 - Bảng cơ sở dữ liệu trạm trộn
 */
@Entity
@Table(name = "tram_tron")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TramTron {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idTram")
    private Long idTram;

    @Column(name = "tenTram", length = 100)
    private String tenTram;

    @Column(name = "diaChi", length = 255)
    private String diaChi;

    @Column(name = "congSuat")
    private Double congSuat;

    @Column(name = "SDT", length = 15)
    private String sdt;

    // Trạng thái hoạt động của trạm trộn
    @Column(name = "trangThai")
    private Integer trangThai;

    // Tọa độ trạm trộn - dùng vẽ tuyến đường gợi ý trên bản đồ app tài xế (mục 2.2.1)
    @Column(name = "viDo")
    private Double viDo;

    @Column(name = "kinhDo")
    private Double kinhDo;

    @OneToMany(mappedBy = "tramTron")
    private List<Chuyen> danhSachChuyen;
}