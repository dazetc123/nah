package com.example.myapplication.models.common;

import java.util.List;

/** Khớp với com.example.betong.DTO.response.common.PageResponse bên backend. */
public class PageResponse<T> {
    private List<T> danhSach;
    private int trangHienTai;
    private int tongSoTrang;
    private long tongSoPhanTu;

    public List<T> getDanhSach() { return danhSach; }
    public int getTrangHienTai() { return trangHienTai; }
    public int getTongSoTrang() { return tongSoTrang; }
    public long getTongSoPhanTu() { return tongSoPhanTu; }
}
