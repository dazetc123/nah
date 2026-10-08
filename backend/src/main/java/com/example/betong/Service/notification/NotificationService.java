package com.example.betong.Service.notification;

import com.example.betong.DTO.request.auth.QuenMatKhauRequest.KenhGui;
import com.example.betong.entity.TaiKhoan;

public interface NotificationService {
    void sendOtp(TaiKhoan taiKhoan, KenhGui kenh, String otp);

    void sendTemporaryPassword(TaiKhoan taiKhoan, String temporaryPassword);

    void notifyDispatchersOfNewOrder(Long idDH);

    void notifyOrderStatusChanged(Long idDH, String message);

    void notifyDriverAssignment(Long idChuyen, String tenTaiXe, String bienSo);

    /** Mục 2.2.3 - "Báo cáo sự cố" bước 6: thông báo tức thời cho Nhân viên điều phối. */
    void notifyDispatchersOfIncident(Long idSuCo, Long idChuyen, String loaiSuCo, boolean khanCap);
}
