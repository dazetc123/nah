package com.example.betong.Service.user.impl;

import com.example.betong.DTO.request.user.CapNhatHoSoRequest;
import com.example.betong.DTO.request.user.DoiMatKhauRequest;
import com.example.betong.DTO.response.user.TaiKhoanResponse;
import com.example.betong.Exception.AppException;
import com.example.betong.Service.user.HoSoCaNhanService;
import com.example.betong.Service.file.FileStorageService;
import com.example.betong.entity.KhachHang;
import com.example.betong.entity.TaiKhoan;
import com.example.betong.repository.KhachHangRepository;
import com.example.betong.repository.TaiKhoanRepository;
import com.example.betong.repository.TaiXeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataAccessException;
import org.springframework.web.multipart.MultipartFile;

/**
 * Bảng 3.39 - Cập nhật thông tin cá nhân, Bảng 3.40 - Đổi mật khẩu.
 * Không có bước "kiểm tra là Quản lý" ở đây như các Service khác trong
 * module — usecase này CHO PHÉP MỌI tài khoản đã đăng nhập tự thao tác
 * trên chính hồ sơ của mình (không thao tác trên tài khoản người khác).
 */
@Service
public class HoSoCaNhanServiceImpl implements HoSoCaNhanService {

    private final TaiKhoanRepository taiKhoanRepository;
    private final TaiXeRepository taiXeRepository;
    private final KhachHangRepository khachHangRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    public HoSoCaNhanServiceImpl(TaiKhoanRepository taiKhoanRepository,
                                 TaiXeRepository taiXeRepository,
                                 KhachHangRepository khachHangRepository,
                                 PasswordEncoder passwordEncoder,
                                 FileStorageService fileStorageService) {
        this.taiKhoanRepository = taiKhoanRepository;
        this.taiXeRepository = taiXeRepository;
        this.khachHangRepository = khachHangRepository;
        this.passwordEncoder = passwordEncoder;
        this.fileStorageService = fileStorageService;
    }

    @Override
    public TaiKhoanResponse xemHoSo(String tenDangNhap) {
        return sangResponse(timTaiKhoanHoacBaoLoi(tenDangNhap));
    }

    // ================================================================
    // Bảng 3.39 - Cập nhật thông tin cá nhân
    // ================================================================
    @Override
    @Transactional
    public TaiKhoanResponse capNhatHoSo(String tenDangNhap, CapNhatHoSoRequest request) {
        TaiKhoan taiKhoan = timTaiKhoanHoacBaoLoi(tenDangNhap);

        // 4.a/4.b đã được @Valid (NotBlank hoTen, đúng định dạng Email) xử lý ở Controller.
        // Còn lại: trùng email với TÀI KHOẢN KHÁC thì báo lỗi thay vì để vỡ ràng buộc unique.
        if (request.getEmail() != null && !request.getEmail().isBlank()
                && !request.getEmail().equalsIgnoreCase(taiKhoan.getEmail())
                && taiKhoanRepository.existsByEmail(request.getEmail().trim())) {
            throw new AppException("Email đã được sử dụng, vui lòng dùng email khác", HttpStatus.CONFLICT);
        }

        taiKhoan.setHoTen(request.getHoTen().trim());
        taiKhoan.setSdt(request.getSdt());
        taiKhoan.setNgaySinh(request.getNgaySinh());
        taiKhoan.setGioiTinh(request.getGioiTinh());
        taiKhoan.setDiaChiThuongTru(request.getDiaChiThuongTru());
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            taiKhoan.setEmail(request.getEmail().trim());
        }
        try {
            taiKhoanRepository.save(taiKhoan);

            taiXeRepository.findByTaiKhoanIdTK(taiKhoan.getIdTK()).ifPresent(taiXe -> {
                taiXe.setHoTen(taiKhoan.getHoTen());
                taiXe.setSdt(taiKhoan.getSdt());
                taiXeRepository.save(taiXe);
            });

            khachHangRepository.findByTaiKhoanIdTK(taiKhoan.getIdTK()).ifPresent(kh -> {
                kh.setTenKhachHang(taiKhoan.getHoTen());
                kh.setSdt(taiKhoan.getSdt());
                kh.setEmail(taiKhoan.getEmail());
                if (request.getDiaChi() != null && !request.getDiaChi().isBlank()) {
                    kh.setDiaChi(request.getDiaChi().trim());
                }
                if (request.getDiaChiThuongTru() != null && !request.getDiaChiThuongTru().isBlank()) {
                    kh.setDiaChi(request.getDiaChiThuongTru().trim());
                }
                khachHangRepository.save(kh);
            });

            TaiKhoanResponse response = sangResponse(taiKhoan);
            response.setThongBao("Cập nhật thông tin thành công");
            return response;
        } catch (DataAccessException ex) {
            throw new AppException("Cập nhật thông tin thất bại, yêu cầu thử lại sau",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ================================================================
    // Bảng 3.40 - Đổi mật khẩu
    // ================================================================
    @Override
    @Transactional
    public void doiMatKhau(String tenDangNhap, DoiMatKhauRequest request) {
        TaiKhoan taiKhoan = timTaiKhoanHoacBaoLoi(tenDangNhap);

        // 4.a "Nếu sai định dạng của mật khẩu, yêu cầu nhập lại" — hiểu rộng gồm cả
        // trường hợp mật khẩu hiện tại không đúng hoặc xác nhận không khớp.
        if (!passwordEncoder.matches(request.getMatKhauHienTai(), taiKhoan.getMatKhau())) {
            throw new AppException("Mật khẩu hiện tại không đúng", HttpStatus.BAD_REQUEST);
        }
        if (!request.getMatKhauMoi().equals(request.getXacNhanMatKhauMoi())) {
            throw new AppException("Xác nhận mật khẩu mới không khớp", HttpStatus.BAD_REQUEST);
        }
        // Bổ sung thực tế (báo cáo không yêu cầu, nhưng là thực hành bảo mật chuẩn):
        // không cho đổi "mật khẩu mới" trùng y hệt mật khẩu đang dùng.
        if (passwordEncoder.matches(request.getMatKhauMoi(), taiKhoan.getMatKhau())) {
            throw new AppException("Mật khẩu mới phải khác mật khẩu hiện tại", HttpStatus.BAD_REQUEST);
        }

        try {
            taiKhoan.setMatKhau(passwordEncoder.encode(request.getMatKhauMoi()));
            taiKhoan.setPhaiDoiMatKhau(0);
            taiKhoanRepository.save(taiKhoan);
        } catch (DataAccessException ex) {
            throw new AppException("Đổi mật khẩu thất bại, yêu cầu thử lại sau",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        // Bước 6: "Thông báo đổi mật khẩu thành công" — trả 204 No Content ở Controller.
    }

    @Override
    @Transactional
    public TaiKhoanResponse capNhatAnhDaiDien(String tenDangNhap, MultipartFile anh) {
        TaiKhoan taiKhoan = timTaiKhoanHoacBaoLoi(tenDangNhap);
        taiKhoan.setAnhDaiDien(fileStorageService.storeImage(anh, "ho-so"));
        taiKhoanRepository.save(taiKhoan);
        TaiKhoanResponse response = sangResponse(taiKhoan);
        response.setThongBao("Cập nhật ảnh hồ sơ thành công");
        return response;
    }

    // ================================================================
    // Hàm hỗ trợ nội bộ
    // ================================================================
    private TaiKhoan timTaiKhoanHoacBaoLoi(String tenDangNhap) {
        return taiKhoanRepository.findByTenDangNhapOrEmail(tenDangNhap)
                .orElseThrow(() -> new AppException("Không xác định được người thực hiện", HttpStatus.UNAUTHORIZED));
    }

    private TaiKhoanResponse sangResponse(TaiKhoan taiKhoan) {
        TaiKhoanResponse.TaiKhoanResponseBuilder builder = TaiKhoanResponse.builder()
                .idTK(taiKhoan.getIdTK())
                .tenDangNhap(taiKhoan.getTenDangNhap())
                .email(taiKhoan.getEmail())
                .sdt(taiKhoan.getSdt())
                .hoTen(taiKhoan.getHoTen())
                .anhDaiDien(taiKhoan.getAnhDaiDien())
                .ngaySinh(taiKhoan.getNgaySinh())
                .diaChiThuongTru(taiKhoan.getDiaChiThuongTru())
                .gioiTinh(taiKhoan.getGioiTinh())
                .trangThai(taiKhoan.getTrangThai())
                .tenVaiTro(taiKhoan.getVaiTro().getTenVaiTro())
                .phaiDoiMatKhau(taiKhoan.getPhaiDoiMatKhau());

        if ("Tài xế".equals(taiKhoan.getVaiTro().getTenVaiTro())) {
            taiXeRepository.findByTaiKhoanIdTK(taiKhoan.getIdTK()).ifPresent(tx -> builder.soGPLX(tx.getSoGPLX()));
        }
        if ("Khách hàng".equals(taiKhoan.getVaiTro().getTenVaiTro())) {
            KhachHang kh = khachHangRepository.findByTaiKhoanIdTK(taiKhoan.getIdTK()).orElse(null);
            if (kh != null) {
                builder.idKH(kh.getIdKH());
                builder.tenKhachHang(kh.getTenKhachHang());
                builder.diaChi(kh.getDiaChi());
            }
        }
        return builder.build();
    }
}
