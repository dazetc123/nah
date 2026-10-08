package com.example.betong.Service.congtrinh;

import com.example.betong.DTO.request.congtrinh.CapNhatCongTrinhRequest;
import com.example.betong.DTO.request.congtrinh.ThemCongTrinhRequest;
import com.example.betong.DTO.response.congtrinh.CongTrinhResponse;
import com.example.betong.DTO.response.common.PageResponse;

public interface CongTrinhService {
    PageResponse<CongTrinhResponse> timKiem(String tuKhoa, int trang, int soLuong);

    PageResponse<CongTrinhResponse> timKiemCuaKhachHang(String tuKhoa, String tenDangNhap,
                                                        int trang, int soLuong);

    CongTrinhResponse them(ThemCongTrinhRequest request);

    CongTrinhResponse layChiTiet(Long idCT);

    CongTrinhResponse capNhat(Long idCT, CapNhatCongTrinhRequest request);

    void xoa(Long idCT);

    void xoaCuaKhachHang(Long idCT, String tenDangNhap);
}
