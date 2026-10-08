package com.example.betong.Service.donhang;

import com.example.betong.DTO.request.donhang.CapNhatTrangThaiDonHangRequest;
import com.example.betong.DTO.request.donhang.TuChoiDonHangRequest;
import com.example.betong.DTO.response.common.PageResponse;
import com.example.betong.DTO.response.donhang.DonHangResponse;
import com.example.betong.DTO.response.donhang.TramTronKhaDungResponse;

import java.util.List;

public interface DieuPhoiDonHangService {
    PageResponse<DonHangResponse> danhSachChoXuLy(int trang, int soLuong);

    /** Danh sách đơn theo trạng thái (0 chờ xử lý, 1 đã xác nhận, 6 đã phân bổ trạm, ...) cho trang web điều phối. */
    PageResponse<DonHangResponse> danhSachTheoTrangThai(int trangThai, int trang, int soLuong);
    DonHangResponse chiTiet(Long idDH);
    DonHangResponse xacNhan(Long idDH);
    DonHangResponse tuChoi(Long idDH, TuChoiDonHangRequest request);
    List<TramTronKhaDungResponse> tramKhaDung(Long idDH);
    DonHangResponse phanBoTram(Long idDH, Long idTram);
    DonHangResponse capNhatTrangThai(Long idDH, CapNhatTrangThaiDonHangRequest request);
}
