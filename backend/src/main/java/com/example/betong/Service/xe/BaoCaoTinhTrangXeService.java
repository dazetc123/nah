package com.example.betong.Service.xe;

import com.example.betong.DTO.response.common.PageResponse;
import com.example.betong.DTO.response.xe.BaoCaoTinhTrangXeResponse;
import org.springframework.web.multipart.MultipartFile;

public interface BaoCaoTinhTrangXeService {
    BaoCaoTinhTrangXeResponse taoBaoCao(Long idXe, String tenDangNhap, Integer trangThaiXe,
                                         String diaChiHu, String soDienThoaiTaiXe,
                                         String nguyenNhan, String noiDung, MultipartFile anh);
    PageResponse<BaoCaoTinhTrangXeResponse> danhSachBaoCao(int trang, int soLuong);
}
