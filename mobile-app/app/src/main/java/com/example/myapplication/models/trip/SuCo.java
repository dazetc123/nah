package com.example.myapplication.models.trip;

/** Khớp SuCoResponse bên backend - kết quả gửi báo cáo sự cố. */
public class SuCo {
    private long idSuCo;
    private Long idChuyen;
    private String bienSo;
    private String loaiSuCo;
    private String moTa;
    private Integer mucDoUuTien;
    private String tenMucDoUuTien;
    private String viTri;
    private String anhMinhChung;
    private String thoiDiem;
    private Integer trangThai;
    private String tenTrangThai;

    public long getIdSuCo() { return idSuCo; }
    public Long getIdChuyen() { return idChuyen; }
    public String getBienSo() { return bienSo; }
    public String getLoaiSuCo() { return loaiSuCo; }
    public String getMoTa() { return moTa; }
    public Integer getMucDoUuTien() { return mucDoUuTien; }
    public String getTenMucDoUuTien() { return tenMucDoUuTien; }
    public String getViTri() { return viTri; }
    public String getAnhMinhChung() { return anhMinhChung; }
    public String getThoiDiem() { return thoiDiem; }
    public Integer getTrangThai() { return trangThai; }
    public String getTenTrangThai() { return tenTrangThai; }
}
