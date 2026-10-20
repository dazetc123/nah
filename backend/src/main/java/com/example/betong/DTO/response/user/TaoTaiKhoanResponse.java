package com.example.betong.DTO.response.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Bảng 3.4 bước 6 "Thông báo thêm tài khoản thành công". Trả kèm mật
 * khẩu do Quản lý chỉ định DUY NHẤT 1 LẦN ở phản hồi này để Quản lý gửi lại cho
 * nhân viên/tài xế — Backend không bao giờ trả mật khẩu ở bất kỳ API
 * nào khác sau đó (kể cả xem chi tiết/danh sách).
 */
@Getter
@AllArgsConstructor
public class TaoTaiKhoanResponse {
    private Long idTK;
    private String tenDangNhap;
    private String matKhauMacDinh;
    private String message;
}
