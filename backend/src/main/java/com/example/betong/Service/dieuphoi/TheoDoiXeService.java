package com.example.betong.Service.dieuphoi;

import com.example.betong.DTO.response.common.PageResponse;
import com.example.betong.DTO.response.dieuphoi.TheoDoiXeResponse;

public interface TheoDoiXeService {
    PageResponse<TheoDoiXeResponse> danhSachXeDangHoatDong(int trang, int soLuong);
    TheoDoiXeResponse viTriMoiNhat(Long idChuyen);
    TheoDoiXeResponse trangThaiChuyen(Long idChuyen);
}
