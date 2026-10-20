package com.example.betong.Service.dieuphoi;

import com.example.betong.DTO.request.dieuphoi.TaoChuyenRequest;
import com.example.betong.DTO.response.common.PageResponse;
import com.example.betong.DTO.response.dieuphoi.ChuyenResponse;
import com.example.betong.DTO.response.taixe.TaiXeResponse;
import com.example.betong.DTO.response.xe.XeResponse;
import com.example.betong.DTO.response.donhang.TramTronKhaDungResponse;

public interface DieuPhoiXeService {
    PageResponse<XeResponse> timXeRanh(String tuKhoa, int trang, int soLuong);
    PageResponse<TaiXeResponse> timTaiXeRanh(String tuKhoa, int trang, int soLuong);
    java.util.List<TramTronKhaDungResponse> tramTronKhaDung(Long idDH);
    ChuyenResponse taoChuyen(TaoChuyenRequest request);
}
