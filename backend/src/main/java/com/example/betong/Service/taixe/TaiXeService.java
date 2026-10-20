package com.example.betong.Service.taixe;

import com.example.betong.DTO.request.taixe.SuaTaiXeRequest;
import com.example.betong.DTO.request.taixe.ThemTaiXeRequest;
import com.example.betong.DTO.response.common.PageResponse;
import com.example.betong.DTO.response.taixe.TaiXeResponse;

/** Đúng 5 usecase Bảng 3.23-3.27: Thêm, Xóa, Sửa, Tìm kiếm tài xế, Gán tài xế cho xe. */
public interface TaiXeService {

    /** Bảng 3.26 - Tìm kiếm (kiêm xem danh sách khi tuKhoa rỗng). */
    PageResponse<TaiXeResponse> danhSach(String tuKhoa, int trang, int soLuong);

    TaiXeResponse layChiTiet(Long idTX);

    /** Bảng 3.23 - Thêm tài xế (gắn hồ sơ cho tài khoản có sẵn vai trò Tài xế). */
    TaiXeResponse them(ThemTaiXeRequest request);

    /** Bảng 3.25 - Sửa tài xế. */
    TaiXeResponse sua(Long idTX, SuaTaiXeRequest request);

    /**
     * Bảng 3.24 - Xóa tài xế. Theo lựa chọn của bạn (phương án 1): chỉ
     * xóa hồ sơ TaiXe, GIỮ NGUYÊN tài khoản đăng nhập (TaiKhoan không bị
     * đụng tới, vẫn đăng nhập được nhưng không còn hồ sơ tài xế).
     */
    void xoa(Long idTX);
}
