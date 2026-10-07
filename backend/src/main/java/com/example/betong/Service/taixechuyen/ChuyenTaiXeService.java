package com.example.betong.Service.taixechuyen;

import com.example.betong.DTO.response.common.PageResponse;
import com.example.betong.DTO.response.taixechuyen.ChuyenChiTietResponse;
import com.example.betong.DTO.response.taixechuyen.ChuyenDanhSachResponse;

/**
 * Mục 2.2.1 báo cáo - Quản lý chuyến (tác nhân Tài xế).
 * Mọi method nhận "tenDangNhap" lấy từ Authentication của request hiện
 * tại, KHÔNG nhận idTX từ client — tài xế chỉ thao tác trên chuyến của
 * chính mình (giống quy ước của HoSoCaNhanService).
 */
public interface ChuyenTaiXeService {

    PageResponse<ChuyenDanhSachResponse> danhSachChuyen(String tenDangNhap, Integer trangThai,
                                                         int trang, int soLuong);

    ChuyenChiTietResponse chiTietChuyen(String tenDangNhap, Long idChuyen);

    ChuyenChiTietResponse nhanChuyen(String tenDangNhap, Long idChuyen);

    ChuyenChiTietResponse batDauChuyen(String tenDangNhap, Long idChuyen);

    ChuyenChiTietResponse hoanThanhChuyen(String tenDangNhap, Long idChuyen);
}
