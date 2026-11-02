package com.example.betong.Service.betong;

import com.example.betong.DTO.request.betong.LoaiBeTongRequest;
import com.example.betong.DTO.response.betong.LoaiBeTongResponse;
import com.example.betong.DTO.response.common.PageResponse;

/** Đúng 5 usecase Bảng 3.50-3.54: Thêm mới, Chỉnh sửa, Tìm kiếm, Xóa, Xem chi tiết loại bê tông. */
public interface LoaiBeTongService {

    /** Bảng 3.52 - Tìm kiếm (kiêm xem danh sách khi tuKhoa rỗng, bước 3 của mọi usecase khác). */
    PageResponse<LoaiBeTongResponse> danhSach(String tuKhoa, int trang, int soLuong);

    /** Bảng 3.54 - Xem chi tiết, kèm số đơn hàng đã sử dụng. */
    LoaiBeTongResponse xemChiTiet(Long idLBT);

    /** Bảng 3.50 - Thêm mới loại bê tông. */
    LoaiBeTongResponse them(LoaiBeTongRequest request);

    /**
     * Bảng 3.51 - Chỉnh sửa loại bê tông. Nếu đơn giá thay đổi, tự động
     * ghi 1 dòng vào LichSuGiaBeTong để lưu vết lịch sử giá theo thời
     * gian (đúng thiết kế CSDL ở mục 3.3.1 - bảng lich_su_gia_be_tong).
     */
    LoaiBeTongResponse sua(Long idLBT, LoaiBeTongRequest request);

    /** Bảng 3.53 - Xóa loại bê tông. */
    void xoa(Long idLBT);
}
