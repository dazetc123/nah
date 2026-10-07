package com.example.betong.Service.notification.impl;

import com.example.betong.DTO.request.auth.QuenMatKhauRequest.KenhGui;
import com.example.betong.Exception.AppException;
import com.example.betong.Service.notification.NotificationService;
import com.example.betong.entity.TaiKhoan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Service
public class NotificationServiceImpl implements NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);
    private final JavaMailSender mailSender;
    private final RestClient restClient = RestClient.create();

    @Value("${spring.mail.username:}")
    private String mailFrom;

    @Value("${app.sms.webhook-url:}")
    private String smsWebhookUrl;

    public NotificationServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendOtp(TaiKhoan taiKhoan, KenhGui kenh, String otp) {
        if (kenh == KenhGui.SMS) {
            sendSms(taiKhoan.getSdt(), "Mã OTP Betong của bạn là " + otp + ". Mã có hiệu lực trong 5 phút.");
            return;
        }
        sendEmail(taiKhoan.getEmail(), "Mã OTP Betong", "Mã OTP của bạn là " + otp + ". Mã có hiệu lực trong 5 phút.");
    }

    @Override
    public void sendTemporaryPassword(TaiKhoan taiKhoan, String temporaryPassword) {
        String content = "Tài khoản Betong: " + taiKhoan.getTenDangNhap()
                + "\nMật khẩu tạm thời: " + temporaryPassword
                + "\nVui lòng đổi mật khẩu ngay sau lần đăng nhập đầu tiên.";
        if (taiKhoan.getEmail() != null && !taiKhoan.getEmail().isBlank()) {
            sendEmail(taiKhoan.getEmail(), "Thông tin tài khoản Betong", content);
        } else {
            sendSms(taiKhoan.getSdt(), content);
        }
    }

    @Override
    public void notifyDispatchersOfNewOrder(Long idDH) {
        log.info("Đơn hàng mới #{} đã được lưu; cần thông báo cho Nhân viên điều phối", idDH);
    }

    @Override
    public void notifyOrderStatusChanged(Long idDH, String message) {
        log.info("Thông báo đơn hàng #{}: {}", idDH, message);
    }

    @Override
    public void notifyDriverAssignment(Long idChuyen, String tenTaiXe, String bienSo) {
        log.info("Thông báo phân công chuyến #{} cho tài xế {} - xe {}", idChuyen, tenTaiXe, bienSo);
    }

    private void sendEmail(String email, String subject, String content) {
        if (email == null || email.isBlank() || mailFrom.isBlank()) {
            throw new AppException("Chưa cấu hình email gửi thông báo", HttpStatus.SERVICE_UNAVAILABLE);
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(email);
        message.setSubject(subject);
        message.setText(content);
        try {
            mailSender.send(message);
        } catch (MailException ex) {
            throw new AppException("Không thể gửi email thông báo, vui lòng kiểm tra cấu hình SMTP",
                    HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    private void sendSms(String phone, String content) {
        if (phone == null || phone.isBlank() || smsWebhookUrl.isBlank()) {
            throw new AppException("Chưa cấu hình dịch vụ SMS gửi thông báo", HttpStatus.SERVICE_UNAVAILABLE);
        }
        try {
            restClient.post().uri(smsWebhookUrl)
                    .body(Map.of("to", phone, "message", content))
                    .retrieve().toBodilessEntity();
        } catch (RestClientException ex) {
            throw new AppException("Không thể gửi SMS thông báo, vui lòng kiểm tra cấu hình dịch vụ SMS",
                    HttpStatus.SERVICE_UNAVAILABLE);
        }
    }
}
