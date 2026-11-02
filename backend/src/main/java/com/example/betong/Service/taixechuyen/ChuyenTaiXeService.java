package com.example.betong.Service.taixechuyen;

import com.example.betong.DTO.request.taixechuyen.DaDenCongTrinhRequest;
import com.example.betong.DTO.response.common.PageResponse;
import com.example.betong.DTO.response.taixechuyen.ChuyenChiTietResponse;
import com.example.betong.DTO.response.taixechuyen.ChuyenDanhSachResponse;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

/**
 * Mục 2.2.1 (Quản lý chuyến) và 2.2.3 (Cập nhật trạng thái) báo cáo - tác nhân Tài xế.
 * Mọi method nhận "tenDangNhap" lấy từ Authentication của request hiện
 * tại, KHÔNG nhận idTX từ client — tài xế chỉ thao tác trên chuyến của
 * chính mình (giống quy ước của HoSoCaNhanService).
 */
public interface ChuyenTaiXeService {

    /** ngay = null: không lọc theo ngày; ngược lại chỉ lấy chuyến có ngày giao dự kiến đúng ngày đó. */
    PageResponse<ChuyenDanhSachResponse> danhSachChuyen(String tenDangNhap, Integer trangThai, LocalDate ngay,
                                                         int trang, int soLuong);

    ChuyenChiTietResponse chiTietChuyen(String tenDangNhap, Long idChuyen);

    ChuyenChiTietResponse nhanChuyen(String tenDangNhap, Long idChuyen);

    ChuyenChiTietResponse batDauChuyen(String tenDangNhap, Long idChuyen);

    ChuyenChiTietResponse xacNhanDaDen(String tenDangNhap, Long idChuyen, DaDenCongTrinhRequest request);

    ChuyenChiTietResponse xacNhanGiaoHang(String tenDangNhap, Long idChuyen, Double khoiLuongThucGiao,
                                          String ghiChu, MultipartFile anhMinhChung);

    ChuyenChiTietResponse hoanThanhChuyen(String tenDangNhap, Long idChuyen);
}
