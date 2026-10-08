package com.example.betong.Service.xe;

import com.example.betong.DTO.request.xe.XeRequest;
import com.example.betong.DTO.response.common.PageResponse;
import com.example.betong.DTO.response.xe.XeResponse;

public interface XeService {
    PageResponse<XeResponse> danhSach(String tuKhoa, Integer trangThai, int trang, int soLuong);

    XeResponse xemChiTiet(Long idXe);

    XeResponse them(XeRequest request);

    XeResponse sua(Long idXe, XeRequest request);

    void xoa(Long idXe);

    XeResponse capNhatTrangThai(Long idXe, Integer trangThai);

    XeResponse ganTaiXe(Long idXe, Long idTX);

    XeResponse huyGanTaiXe(Long idXe);
}
