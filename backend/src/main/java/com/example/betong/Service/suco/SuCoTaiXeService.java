package com.example.betong.Service.suco;

import com.example.betong.DTO.response.common.PageResponse;
import com.example.betong.DTO.response.suco.SuCoResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * Mục 2.2.3 báo cáo - Usecase "Báo cáo sự cố" (tác nhân Tài xế).
 * Sự cố luôn gắn với chuyến tài xế đang thực hiện; nếu app không gửi
 * idChuyen, hệ thống tự lấy chuyến đang thực hiện gần nhất của tài xế.
 */
public interface SuCoTaiXeService {

    SuCoResponse baoCaoSuCo(String tenDangNhap, Long idChuyen, String loaiSuCo, String moTa,
                            Integer mucDoUuTien, Double viDo, Double kinhDo, MultipartFile anh);

    PageResponse<SuCoResponse> danhSachSuCoCuaToi(String tenDangNhap, int trang, int soLuong);

    /** Bước 6 / luồng 6.a: Nhân viên điều phối xem danh sách sự cố tài xế gửi lên. */
    PageResponse<SuCoResponse> danhSachChoDieuPhoi(Integer trangThai, int trang, int soLuong);

    /** Điều phối cập nhật trạng thái xử lý: 1 = Đang xử lý, 2 = Đã xử lý. */
    SuCoResponse capNhatTrangThai(Long idSuCo, Integer trangThai);
}
