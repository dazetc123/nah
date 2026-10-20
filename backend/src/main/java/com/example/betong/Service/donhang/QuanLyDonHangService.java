package com.example.betong.Service.donhang;

import com.example.betong.DTO.response.common.PageResponse;
import com.example.betong.DTO.response.donhang.DonHangResponse;

public interface QuanLyDonHangService {
    PageResponse<DonHangResponse> danhSach(String tenDangNhap, String tuKhoa, int trang, int soLuong);
    DonHangResponse chiTiet(String tenDangNhap, Long idDH);
    DonHangResponse theoDoi(String tenDangNhap, Long idDH);
    DonHangResponse huy(String tenDangNhap, Long idDH);
}
