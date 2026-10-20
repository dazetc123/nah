package com.example.betong.Service.auth.impl;

import com.example.betong.DTO.request.auth.DoiMatKhauLanDauRequest;
import com.example.betong.DTO.request.auth.LoginRequest;
import com.example.betong.DTO.request.auth.RegisterRequest;
import com.example.betong.DTO.response.auth.LoginResponse;
import com.example.betong.DTO.response.auth.RegisterResponse;
import com.example.betong.DTO.response.auth.ThongBaoResponse;
import com.example.betong.DTO.request.auth.QuenMatKhauRequest;
import com.example.betong.DTO.request.auth.DatLaiMatKhauRequest;
import com.example.betong.Service.auth.AuthService;
import com.example.betong.Service.notification.NotificationService;
import com.example.betong.entity.KhachHang;
import com.example.betong.entity.TaiKhoan;
import com.example.betong.entity.VaiTro;
import com.example.betong.Exception.AuthException;
import com.example.betong.repository.KhachHangRepository;
import com.example.betong.repository.TaiKhoanRepository;
import com.example.betong.repository.VaiTroRepository;
import com.example.betong.Security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * LƯU Ý: getter/setter của KhachHang (setTenKhachHang, setSdt, setEmail,
 * setTaiKhoan) viết theo đúng tên cột Bảng 3.85. Sửa lại nếu entity
 * KhachHang của bạn đặt tên khác.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final TaiKhoanRepository taiKhoanRepository;
    private final VaiTroRepository vaiTroRepository;
    private final KhachHangRepository khachHangRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final NotificationService notificationService;
    private static final SecureRandom RANDOM = new SecureRandom();

    public AuthServiceImpl(TaiKhoanRepository taiKhoanRepository,
                           VaiTroRepository vaiTroRepository,
                           KhachHangRepository khachHangRepository,
                           PasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil,
                           NotificationService notificationService) {
        this.taiKhoanRepository = taiKhoanRepository;
        this.vaiTroRepository = vaiTroRepository;
        this.khachHangRepository = khachHangRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.notificationService = notificationService;
    }

    // ================================================================
    // ĐĂNG NHẬP — Bảng 3.2
    // ================================================================
    @Override
    public LoginResponse dangNhap(LoginRequest request) {

        TaiKhoan taiKhoan = taiKhoanRepository
                .findByTenDangNhapOrEmail(request.getDinhDanh().trim())
                .orElseThrow(() -> new AuthException(
                        "Tên đăng nhập/email hoặc mật khẩu không đúng", HttpStatus.UNAUTHORIZED));

        if (taiKhoan.getTrangThai() == null || taiKhoan.getTrangThai() != 1) {
            throw new AuthException(
                    "Tài khoản đã bị khóa, vui lòng liên hệ quản trị viên", HttpStatus.FORBIDDEN);
        }

        if (!passwordEncoder.matches(request.getMatKhau(), taiKhoan.getMatKhau())) {
            throw new AuthException(
                    "Tên đăng nhập/email hoặc mật khẩu không đúng", HttpStatus.UNAUTHORIZED);
        }

        // Cờ bắt buộc đổi mật khẩu (tài khoản do Quản lý cấp cho NV điều phối/Tài xế)
        boolean phaiDoiMatKhau = taiKhoan.getPhaiDoiMatKhau() != null && taiKhoan.getPhaiDoiMatKhau() == 1;

        String tenVaiTro = taiKhoan.getVaiTro().getTenVaiTro();
        String token = jwtUtil.generateToken(taiKhoan.getIdTK(), taiKhoan.getTenDangNhap(), tenVaiTro, phaiDoiMatKhau);

        return new LoginResponse(token, taiKhoan.getIdTK(), taiKhoan.getHoTen(), tenVaiTro, phaiDoiMatKhau);
    }

    // ================================================================
    // ĐĂNG KÝ — Bảng 3.1
    // ================================================================
    @Override
    @Transactional
    public RegisterResponse dangKy(RegisterRequest request) {

        if (!request.getMatKhau().equals(request.getXacNhanMatKhau())) {
            throw new AuthException("Mật khẩu xác nhận không khớp, vui lòng nhập lại", HttpStatus.BAD_REQUEST);
        }

        if (taiKhoanRepository.existsByTenDangNhap(request.getTenDangNhap())) {
            throw new AuthException("Tên đăng nhập đã tồn tại, vui lòng chọn tên khác", HttpStatus.CONFLICT);
        }
        if (taiKhoanRepository.existsByEmail(request.getEmail())) {
            throw new AuthException("Email đã được đăng ký, vui lòng dùng email khác", HttpStatus.CONFLICT);
        }

        VaiTro vaiTroKhachHang = vaiTroRepository.findByTenVaiTro("Khách hàng")
                .orElseThrow(() -> new AuthException(
                        "Chưa cấu hình vai trò Khách hàng trong hệ thống", HttpStatus.INTERNAL_SERVER_ERROR));

        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.setTenDangNhap(request.getTenDangNhap().trim());
        taiKhoan.setEmail(request.getEmail().trim());
        taiKhoan.setMatKhau(passwordEncoder.encode(request.getMatKhau()));
        taiKhoan.setHoTen(request.getHoTen().trim());
        taiKhoan.setSdt(request.getSdt().trim());
        taiKhoan.setTrangThai(1);
        taiKhoan.setDaXacThuc(0);
        taiKhoan.setPhaiDoiMatKhau(0); // khách hàng tự đăng ký, tự chọn mật khẩu -> không bắt đổi
        taiKhoan.setVaiTro(vaiTroKhachHang);
        taiKhoan = taiKhoanRepository.save(taiKhoan);

        taoKhachHang(taiKhoan, request);

        return new RegisterResponse(taiKhoan.getIdTK(), taiKhoan.getTenDangNhap(), "Đăng ký tài khoản thành công");
    }

    private void taoKhachHang(TaiKhoan taiKhoan, RegisterRequest request) {
        KhachHang khachHang = new KhachHang();
        khachHang.setTaiKhoan(taiKhoan);
        khachHang.setTenKhachHang(request.getHoTen().trim());
        khachHang.setSdt(request.getSdt().trim());
        khachHang.setEmail(request.getEmail().trim());
        khachHangRepository.save(khachHang);
    }

    // ================================================================
    // ĐỔI MẬT KHẨU LẦN ĐẦU — bị thiếu hoàn toàn trong file cũ của bạn,
    // đây chính là nguyên nhân Controller gọi authService.doiMatKhauLanDau(...)
    // mà không compile được (method không tồn tại).
    // ================================================================
    @Override
    @Transactional
    public LoginResponse doiMatKhauLanDau(String tenDangNhap, DoiMatKhauLanDauRequest request) {

        if (!request.getMatKhauMoi().equals(request.getXacNhanMatKhauMoi())) {
            throw new AuthException("Mật khẩu xác nhận không khớp, vui lòng nhập lại", HttpStatus.BAD_REQUEST);
        }

        TaiKhoan taiKhoan = taiKhoanRepository.findByTenDangNhapOrEmail(tenDangNhap)
                .orElseThrow(() -> new AuthException("Không tìm thấy tài khoản", HttpStatus.NOT_FOUND));

        taiKhoan.setMatKhau(passwordEncoder.encode(request.getMatKhauMoi()));
        taiKhoan.setPhaiDoiMatKhau(0);
        taiKhoanRepository.save(taiKhoan);

        String tenVaiTro = taiKhoan.getVaiTro().getTenVaiTro();
        String token = jwtUtil.generateToken(taiKhoan.getIdTK(), taiKhoan.getTenDangNhap(), tenVaiTro, false);

        return new LoginResponse(token, taiKhoan.getIdTK(), taiKhoan.getHoTen(), tenVaiTro, false);
    }

    @Override
    @Transactional
    public ThongBaoResponse guiOtpQuenMatKhau(QuenMatKhauRequest request) {
        String dinhDanh = request.getDinhDanh().trim();
        TaiKhoan taiKhoan = taiKhoanRepository.findByTenDangNhapOrEmail(dinhDanh)
                .or(() -> taiKhoanRepository.findBySdt(dinhDanh))
                .orElseThrow(() -> new AuthException("Không tìm thấy tài khoản", HttpStatus.NOT_FOUND));
        if (request.getKenh() == QuenMatKhauRequest.KenhGui.EMAIL
                && (taiKhoan.getEmail() == null || taiKhoan.getEmail().isBlank())) {
            throw new AuthException("Tài khoản chưa có email", HttpStatus.BAD_REQUEST);
        }
        if (request.getKenh() == QuenMatKhauRequest.KenhGui.SMS
                && (taiKhoan.getSdt() == null || taiKhoan.getSdt().isBlank())) {
            throw new AuthException("Tài khoản chưa có số điện thoại", HttpStatus.BAD_REQUEST);
        }
        String otp = String.format("%06d", RANDOM.nextInt(1_000_000));
        taiKhoan.setMaXacThucHash(passwordEncoder.encode(otp));
        taiKhoan.setOtpMucDich("DAT_LAI_MAT_KHAU");
        taiKhoan.setMaXacThucHetHan(LocalDateTime.now().plusMinutes(5));
        taiKhoanRepository.save(taiKhoan);
        notificationService.sendOtp(taiKhoan, request.getKenh(), otp);
        String kenh = request.getKenh() == QuenMatKhauRequest.KenhGui.EMAIL ? "email" : "SMS";
        return new ThongBaoResponse("Mã OTP đã được gửi qua " + kenh);
    }

    @Override
    @Transactional
    public ThongBaoResponse datLaiMatKhau(DatLaiMatKhauRequest request) {
        if (!request.getMatKhauMoi().equals(request.getXacNhanMatKhauMoi())) {
            throw new AuthException("Mật khẩu xác nhận không khớp", HttpStatus.BAD_REQUEST);
        }
        String dinhDanh = request.getDinhDanh().trim();
        TaiKhoan taiKhoan = taiKhoanRepository.findByTenDangNhapOrEmail(dinhDanh)
                .or(() -> taiKhoanRepository.findBySdt(dinhDanh))
                .orElseThrow(() -> new AuthException("Không tìm thấy tài khoản", HttpStatus.NOT_FOUND));
        boolean hopLe = taiKhoan.getMaXacThucHash() != null
                && "DAT_LAI_MAT_KHAU".equals(taiKhoan.getOtpMucDich())
                && taiKhoan.getMaXacThucHetHan() != null
                && taiKhoan.getMaXacThucHetHan().isAfter(LocalDateTime.now())
                && passwordEncoder.matches(request.getMaOtp(), taiKhoan.getMaXacThucHash());
        if (!hopLe) {
            throw new AuthException("Mã OTP không đúng hoặc đã hết hạn", HttpStatus.BAD_REQUEST);
        }
        taiKhoan.setMatKhau(passwordEncoder.encode(request.getMatKhauMoi()));
        taiKhoan.setPhaiDoiMatKhau(0);
        taiKhoan.setMaXacThucHash(null);
        taiKhoan.setOtpMucDich(null);
        taiKhoan.setMaXacThucHetHan(null);
        taiKhoanRepository.save(taiKhoan);
        return new ThongBaoResponse("Đặt lại mật khẩu thành công");
    }

    @Override
    @Transactional
    public LoginResponse dangNhapBangGoogle(OAuth2User googleUser) {
        String emailAttribute = googleUser.getAttribute("email");
        if (emailAttribute == null || emailAttribute.isBlank()) {
            throw new AuthException("Google không cung cấp email tài khoản", HttpStatus.UNAUTHORIZED);
        }
        final String email = emailAttribute.trim().toLowerCase();

        TaiKhoan taiKhoan = taiKhoanRepository.findByTenDangNhapOrEmail(email)
                .orElseGet(() -> taoTaiKhoanGoogle(googleUser, email));

        if (taiKhoan.getTrangThai() == null || taiKhoan.getTrangThai() != 1) {
            throw new AuthException(
                    "Tài khoản đã bị khóa, vui lòng liên hệ quản trị viên", HttpStatus.FORBIDDEN);
        }

        String tenVaiTro = taiKhoan.getVaiTro().getTenVaiTro();
        boolean phaiDoiMatKhau = taiKhoan.getPhaiDoiMatKhau() != null
                && taiKhoan.getPhaiDoiMatKhau() == 1;
        String token = jwtUtil.generateToken(
                taiKhoan.getIdTK(), taiKhoan.getTenDangNhap(), tenVaiTro, phaiDoiMatKhau);
        return new LoginResponse(
                token, taiKhoan.getIdTK(), taiKhoan.getHoTen(), tenVaiTro, phaiDoiMatKhau);
    }

    private TaiKhoan taoTaiKhoanGoogle(OAuth2User googleUser, String email) {
        VaiTro vaiTroKhachHang = vaiTroRepository.findByTenVaiTro("Khách hàng")
                .orElseThrow(() -> new AuthException(
                        "Chưa cấu hình vai trò Khách hàng trong hệ thống",
                        HttpStatus.INTERNAL_SERVER_ERROR));

        String hoTen = googleUser.getAttribute("name");
        if (hoTen == null || hoTen.isBlank()) {
            hoTen = email.substring(0, email.indexOf('@'));
        }

        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.setTenDangNhap("google_" + UUID.randomUUID());
        taiKhoan.setEmail(email);
        taiKhoan.setMatKhau(passwordEncoder.encode(UUID.randomUUID().toString()));
        taiKhoan.setHoTen(hoTen.trim());
        taiKhoan.setTrangThai(1);
        taiKhoan.setDaXacThuc(1);
        taiKhoan.setPhaiDoiMatKhau(0);
        taiKhoan.setVaiTro(vaiTroKhachHang);
        taiKhoan = taiKhoanRepository.save(taiKhoan);

        KhachHang khachHang = new KhachHang();
        khachHang.setTaiKhoan(taiKhoan);
        khachHang.setTenKhachHang(taiKhoan.getHoTen());
        khachHang.setEmail(email);
        khachHangRepository.save(khachHang);
        return taiKhoan;
    }
}