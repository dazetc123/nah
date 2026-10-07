package com.example.betong.Service.tramtron;

import com.example.betong.DTO.request.tramtron.CongSuatRequest;
import com.example.betong.DTO.request.tramtron.TramTronRequest;
import com.example.betong.DTO.response.common.PageResponse;
import com.example.betong.DTO.response.tramtron.TramTronResponse;

/**
 * Đúng 5 usecase Bảng 3.18-3.22: Thêm, Xóa, Sửa, Tìm kiếm, Quản lý công
 * suất. "Xem danh sách" không có bảng riêng trong báo cáo — dùng chung
 * API với Tìm kiếm (Bảng 3.21), tuKhoa rỗng = xem tất cả (giống Xe).
 */
public interface TramTronService {

    /** Bảng 3.21 - Tìm kiếm (kiêm xem danh sách khi tuKhoa rỗng). */
    PageResponse<TramTronResponse> danhSach(String tuKhoa, int trang, int soLuong);

    TramTronResponse layChiTiet(Long idTram);

    /** Bảng 3.18 - Thêm trạm trộn. */
    TramTronResponse them(TramTronRequest request);

    /** Bảng 3.20 - Sửa trạm trộn. */
    TramTronResponse sua(Long idTram, TramTronRequest request);

    /** Bảng 3.19 - Xóa trạm trộn. */
    void xoa(Long idTram);

    /** Bảng 3.22 - Quản lý công suất (usecase riêng, chỉ đổi công suất). */
    TramTronResponse capNhatCongSuat(Long idTram, CongSuatRequest request);
}
