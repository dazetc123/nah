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
    private Double viDoTram;
    private Double kinhDoTram;

    private String thoiGianGiao;        // giờ giao dự kiến
    private String thoiGianNhan;
    private String thoiGianXuatPhat;
    private String thoiGianDen;         // thời điểm xác nhận đã đến công trình
    private String thoiGianGiaoXong;
    private String thoiGianHoanThanh;

    private Double khoangCachDen;
    private String ghiChuDen;
    private Boolean canKiemTraDen;
    private Integer banKinhChoPhep;

    private Double khoiLuongThucGiao;
    private String ghiChuGiaoHang;
    private String anhMinhChung;
    private Double tongKhoiLuongDonHang;
    private Double tongKhoiLuongDaGiao;

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
    public Double getViDoTram() { return viDoTram; }
    public Double getKinhDoTram() { return kinhDoTram; }
    public String getThoiGianGiao() { return thoiGianGiao; }
    public String getThoiGianNhan() { return thoiGianNhan; }
    public String getThoiGianXuatPhat() { return thoiGianXuatPhat; }
    public String getThoiGianDen() { return thoiGianDen; }
    public String getThoiGianGiaoXong() { return thoiGianGiaoXong; }
    public String getThoiGianHoanThanh() { return thoiGianHoanThanh; }
    public Double getKhoangCachDen() { return khoangCachDen; }
    public String getGhiChuDen() { return ghiChuDen; }
    public boolean isCanKiemTraDen() { return Boolean.TRUE.equals(canKiemTraDen); }
    public Integer getBanKinhChoPhep() { return banKinhChoPhep; }
    public Double getKhoiLuongThucGiao() { return khoiLuongThucGiao; }
    public String getGhiChuGiaoHang() { return ghiChuGiaoHang; }
    public String getAnhMinhChung() { return anhMinhChung; }
    public Double getTongKhoiLuongDonHang() { return tongKhoiLuongDonHang; }
    public Double getTongKhoiLuongDaGiao() { return tongKhoiLuongDaGiao; }
    public String getBienSo() { return bienSo; }
    public String getGhiChu() { return ghiChu; }
    public int getTrangThai() { return trangThai; }
    public String getTenTrangThai() { return tenTrangThai; }
}
