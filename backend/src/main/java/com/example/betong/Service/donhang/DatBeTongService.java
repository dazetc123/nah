package com.example.betong.Service.donhang;

import com.example.betong.DTO.request.donhang.DatBeTongRequest;
import com.example.betong.DTO.response.donhang.DatBeTongResponse;

public interface DatBeTongService {
    DatBeTongResponse datHang(String tenDangNhap, DatBeTongRequest request);
}
