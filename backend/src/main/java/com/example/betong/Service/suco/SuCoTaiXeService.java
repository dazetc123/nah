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
}
