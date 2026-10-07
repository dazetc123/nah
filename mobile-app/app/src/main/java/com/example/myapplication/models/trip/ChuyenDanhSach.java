package com.example.myapplication.models.trip;

import java.time.LocalDateTime;

/** Khớp với ChuyenDanhSachResponse bên backend - mỗi dòng trong danh sách chuyến. */
public class ChuyenDanhSach {
    private long idChuyen;
    private long idDH;
    private String tenCongTrinh;
    private Double khoiLuong;
    private String thoiGianGiao;
    private String bienSo;
    private int trangThai;
    private String tenTrangThai;

    public long getIdChuyen() { return idChuyen; }
    public long getIdDH() { return idDH; }
    public String getTenCongTrinh() { return tenCongTrinh; }
    public Double getKhoiLuong() { return khoiLuong; }
    public String getThoiGianGiao() { return thoiGianGiao; }
    public String getBienSo() { return bienSo; }
    public int getTrangThai() { return trangThai; }
    public String getTenTrangThai() { return tenTrangThai; }
}
