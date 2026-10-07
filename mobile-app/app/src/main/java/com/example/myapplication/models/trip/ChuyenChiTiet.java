package com.example.myapplication.models.trip;

/** Khớp với ChuyenChiTietResponse bên backend - chi tiết đầy đủ 1 chuyến. */
public class ChuyenChiTiet {
    private long idChuyen;
    private long idDH;

    private String tenCongTrinh;
    private String diaChiCongTrinh;
    private Double viDoCongTrinh;
    private Double kinhDoCongTrinh;
    private String sdtCongTrinh;

    private String macBeTong;
    private Double khoiLuong;

    private String tenTram;
    private String diaChiTram;

    private String thoiGianXuatPhat;
    private String thoiGianDen;

    private String bienSo;
    private String ghiChu;

    private int trangThai;
    private String tenTrangThai;

    public long getIdChuyen() { return idChuyen; }
    public long getIdDH() { return idDH; }
    public String getTenCongTrinh() { return tenCongTrinh; }
    public String getDiaChiCongTrinh() { return diaChiCongTrinh; }
    public Double getViDoCongTrinh() { return viDoCongTrinh; }
    public Double getKinhDoCongTrinh() { return kinhDoCongTrinh; }
    public String getSdtCongTrinh() { return sdtCongTrinh; }
    public String getMacBeTong() { return macBeTong; }
    public Double getKhoiLuong() { return khoiLuong; }
    public String getTenTram() { return tenTram; }
    public String getDiaChiTram() { return diaChiTram; }
    public String getThoiGianXuatPhat() { return thoiGianXuatPhat; }
    public String getThoiGianDen() { return thoiGianDen; }
    public String getBienSo() { return bienSo; }
    public String getGhiChu() { return ghiChu; }
    public int getTrangThai() { return trangThai; }
    public String getTenTrangThai() { return tenTrangThai; }
}
