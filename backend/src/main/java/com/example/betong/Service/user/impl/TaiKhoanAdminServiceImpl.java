package com.example.betong.Service.user.impl;

import com.example.betong.DTO.request.user.CapNhatTaiKhoanRequest;
import com.example.betong.DTO.request.user.TaoTaiKhoanRequest;
import com.example.betong.DTO.request.user.TrangThaiRequest;
import com.example.betong.DTO.response.common.PageResponse;
import com.example.betong.DTO.response.user.TaiKhoanResponse;
import com.example.betong.DTO.response.user.TaoTaiKhoanResponse;
import com.example.betong.Exception.AppException;
import com.example.betong.Service.user.TaiKhoanAdminService;
import com.example.betong.Service.notification.NotificationService;
import com.example.betong.entity.TaiKhoan;
import com.example.betong.entity.TaiXe;
import com.example.betong.entity.VaiTro;
import com.example.betong.repository.KhachHangRepository;
import com.example.betong.repository.TaiKhoanRepository;
import com.example.betong.repository.TaiXeRepository;
import com.example.betong.repository.VaiTroRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class TaiKhoanAdminServiceImpl implements TaiKhoanAdminService {

    /** Chỉ 2 vai trò Quản lý được phép tạo qua Bảng 3.4 — Khách hàng tự đăng ký (Bảng 3.1), Quản lý không tạo qua giao diện này. */
    private static final Set<String> VAI_TRO_DUOC_TAO = Set.of("Nhân viên điều phối", "Tài xế");
    private final TaiKhoanRepository taiKhoanRepository;
    private final TaiXeRepository taiXeRepository;
    private final KhachHangRepository khachHangRepository;
    private final VaiTroRepository vaiTroRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    public TaiKhoanAdminServiceImpl(TaiKhoanRepository taiKhoanRepository,
                                    TaiXeRepository taiXeRepository,
                                    KhachHangRepository khachHangRepository,
                                    VaiTroRepository vaiTroRepository,
                                    PasswordEncoder passwordEncoder,
                                    NotificationService notificationService) {
        this.taiKhoanRepository = taiKhoanRepository;
        this.taiXeRepository = taiXeRepository;
        this.khachHangRepository = khachHangRepository;
        this.vaiTroRepository = vaiTroRepository;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService;
    }

    // ================================================================
    // Bảng 3.7 + Bảng 3.3
    // ================================================================
    @Override
    public PageResponse<TaiKhoanResponse> danhSachNhanVienDieuPhoiVaTaiXe(String tuKhoa, int trang, int soLuong) {
        String tuKhoaThucSu = chuanHoa(tuKhoa);
        Pageable pageable = PageRequest.of(Math.max(0, trang - 1), soLuong);

        // Ghép kết quả 2 vai trò (NV điều phối + Tài xế) — DB không có 1 cột chung để lọc "không phải Khách hàng/Quản lý"
        // nên gọi repository theo từng vai trò trong whitelist rồi gộp lại, phù hợp vì số vai trò cố định và ít (2 vai trò).
        List<TaiKhoan> ketQua = new java.util.ArrayList<>();
        for (String vaiTro : VAI_TRO_DUOC_TAO) {
            ketQua.addAll(taiKhoanRepository.timKiem(tuKhoaThucSu, vaiTro, Pageable.unpaged()).getContent());
        }
        ketQua.sort((a, b) -> Long.compare(b.getIdTK(), a.getIdTK()));

        int tuVitri = Math.min((int) pageable.getOffset(), ketQua.size());
        int denVitri = Math.min(tuVitri + pageable.getPageSize(), ketQua.size());
        List<TaiKhoanResponse> trangHienTai = ketQua.subList(tuVitri, denVitri).stream().map(this::sangResponse).toList();
        int tongSoTrang = (int) Math.ceil((double) ketQua.size() / pageable.getPageSize());

        return new PageResponse<>(trangHienTai, trang, Math.max(1, tongSoTrang), ketQua.size());
    }

    // ================================================================
    // Bảng 3.8
    // ================================================================
    @Override
    public PageResponse<TaiKhoanResponse> danhSachKhachHang(String tuKhoa, int trang, int soLuong) {
        String tuKhoaThucSu = chuanHoa(tuKhoa);
        Pageable pageable = PageRequest.of(Math.max(0, trang - 1), soLuong);
        return PageResponse.tu(taiKhoanRepository.timKiem(tuKhoaThucSu, "Khách hàng", pageable), this::sangResponse);
    }

    // ================================================================
    // Bảng 3.9 + Bảng 3.10
    // ================================================================
    @Override
    public TaiKhoanResponse xemChiTiet(Long idTK) {
        return sangResponse(timTaiKhoanHoacBaoLoi(idTK));
    }

    // ================================================================
    // Bảng 3.4
    // ================================================================
    @Override
    @Transactional
    public TaoTaiKhoanResponse themTaiKhoan(TaoTaiKhoanRequest request, String tenDangNhapNguoiThucHien) {
        kiemTraLaQuanLy(tenDangNhapNguoiThucHien);
        String email = request.getEmail() == null ? null : request.getEmail().trim();
        String sdt = request.getSdt() == null ? null : request.getSdt().trim();
        if ((email == null || email.isBlank()) && (sdt == null || sdt.isBlank())) {
            throw new AppException("Cần cung cấp email hoặc số điện thoại để gửi mật khẩu tạm",
                    HttpStatus.BAD_REQUEST);
        }

        // 4.a/4.b: sai định dạng hoặc bỏ trống — riêng "vai trò" phải nằm trong danh sách được phép tạo qua đây.
        if (!VAI_TRO_DUOC_TAO.contains(request.getTenVaiTro())) {
            throw new AppException("Vai trò không hợp lệ, chỉ được tạo tài khoản Nhân viên điều phối hoặc Tài xế",
                    HttpStatus.BAD_REQUEST);
        }
        boolean laTaiXe = "Tài xế".equals(request.getTenVaiTro());
        if (laTaiXe && (request.getSoGPLX() == null || request.getSoGPLX().isBlank())) {
            throw new AppException("Vui lòng nhập số giấy phép lái xe cho tài khoản Tài xế", HttpStatus.BAD_REQUEST);
        }

        if (taiKhoanRepository.existsByTenDangNhap(request.getTenDangNhap().trim())) {
            throw new AppException("Tên đăng nhập đã tồn tại, vui lòng chọn tên khác", HttpStatus.CONFLICT);
        }
        if (email != null && !email.isBlank() && taiKhoanRepository.existsByEmail(email)) {
            throw new AppException("Email đã được sử dụng, vui lòng dùng email khác", HttpStatus.CONFLICT);
        }

        VaiTro vaiTro = layVaiTroHoacBaoLoi(request.getTenVaiTro());
        String matKhauMacDinh = request.getMatKhau();

        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.setTenDangNhap(request.getTenDangNhap().trim());
        taiKhoan.setMatKhau(passwordEncoder.encode(matKhauMacDinh));
        taiKhoan.setHoTen(request.getHoTen().trim());
        taiKhoan.setEmail(email);
        taiKhoan.setSdt(sdt);
        taiKhoan.setTrangThai(1);
        taiKhoan.setDaXacThuc(1); // Quản lý tạo trực tiếp, không cần xác thực OTP như tự đăng ký
        taiKhoan.setPhaiDoiMatKhau(1); // bắt buộc đổi mật khẩu ở lần đăng nhập đầu tiên
        taiKhoan.setVaiTro(vaiTro);
        taiKhoan = taiKhoanRepository.save(taiKhoan);

        if (laTaiXe) {
            TaiXe taiXe = new TaiXe();
            taiXe.setTaiKhoan(taiKhoan);
            taiXe.setHoTen(taiKhoan.getHoTen());
            taiXe.setSdt(taiKhoan.getSdt());
            taiXe.setSoGPLX(request.getSoGPLX());
            taiXe.setTrangThai(1);
            taiXeRepository.save(taiXe);
        }

        String thongBao = "Thêm tài khoản thành công";
        try {
            notificationService.sendTemporaryPassword(taiKhoan, matKhauMacDinh);
        } catch (AppException ex) {
            // Tài khoản và mật khẩu đã được lưu; lỗi kênh thông báo không được
            // làm thao tác tạo tài khoản thất bại hoặc rollback giao dịch.
            thongBao += ". Không thể gửi thông tin qua kênh thông báo: " + ex.getMessage();
        }
        return new TaoTaiKhoanResponse(taiKhoan.getIdTK(), taiKhoan.getTenDangNhap(), matKhauMacDinh,
                thongBao);
    }

    // ================================================================
    // Bảng 3.5
    // ================================================================
    @Override
    @Transactional
    public TaiKhoanResponse suaTaiKhoan(Long idTK, CapNhatTaiKhoanRequest request, String tenDangNhapNguoiThucHien) {
        kiemTraLaQuanLy(tenDangNhapNguoiThucHien);

        TaiKhoan taiKhoan = timTaiKhoanHoacBaoLoi(idTK);
        if (!VAI_TRO_DUOC_TAO.contains(taiKhoan.getVaiTro().getTenVaiTro())) {
            throw new AppException("Chỉ có thể sửa tài khoản Nhân viên điều phối hoặc Tài xế", HttpStatus.BAD_REQUEST);
        }
        if ("Tài xế".equals(taiKhoan.getVaiTro().getTenVaiTro())) {
            throw new AppException(
                    "Quản lý không được sửa hồ sơ tài xế. Tài xế tự cập nhật thông tin trong hồ sơ cá nhân",
                    HttpStatus.FORBIDDEN);
        }

        if (!taiKhoan.getEmail().equalsIgnoreCase(request.getEmail())
                && taiKhoanRepository.existsByEmail(request.getEmail().trim())) {
            throw new AppException("Email đã được sử dụng, vui lòng dùng email khác", HttpStatus.CONFLICT);
        }

        taiKhoan.setHoTen(request.getHoTen().trim());
        taiKhoan.setEmail(request.getEmail().trim());
        taiKhoan.setSdt(request.getSdt());
        taiKhoanRepository.save(taiKhoan);

        return sangResponse(taiKhoan);
    }

    // ================================================================
    // Bảng 3.6 — Chỉ áp dụng NV điều phối/Tài xế/Khách hàng, KHÔNG áp dụng Quản lý
    // (đúng mô tả Bảng 3.6: "Khóa hoặc mở tài khoản cho nhân viên điều phối, tài xế hoặc khách hàng")
    // ================================================================
    @Override
    @Transactional
    public TaiKhoanResponse khoaMoTaiKhoan(Long idTK, TrangThaiRequest request, String tenDangNhapNguoiThucHien) {
        kiemTraLaQuanLy(tenDangNhapNguoiThucHien);

        TaiKhoan taiKhoan = timTaiKhoanHoacBaoLoi(idTK);
        if ("Quản lý".equals(taiKhoan.getVaiTro().getTenVaiTro())) {
            throw new AppException("Không thể khóa/mở tài khoản Quản lý", HttpStatus.FORBIDDEN);
        }

        taiKhoan.setTrangThai(request.getTrangThai());
        taiKhoanRepository.save(taiKhoan);

        // Bước 6: "Thông báo đã khóa hoặc mở tài khoản + tên tài khoản đã khóa"
        String hanhDong = request.getTrangThai() == 1 ? "mở" : "khóa";
        TaiKhoanResponse response = sangResponse(taiKhoan);
        response.setThongBao("Đã " + hanhDong + " tài khoản " + taiKhoan.getHoTen());
        return response;
    }

    // ================================================================
    // Hàm hỗ trợ nội bộ
    // ================================================================
    private String chuanHoa(String tuKhoa) {
        return (tuKhoa == null || tuKhoa.isBlank()) ? null : tuKhoa.trim();
    }

    private TaiKhoan timTaiKhoanHoacBaoLoi(Long idTK) {
        return taiKhoanRepository.findById(idTK)
                .orElseThrow(() -> new AppException("Không tìm thấy tài khoản", HttpStatus.NOT_FOUND));
    }

    private VaiTro layVaiTroHoacBaoLoi(String tenVaiTro) {
        return vaiTroRepository.findByTenVaiTro(tenVaiTro)
                .orElseThrow(() -> new AppException("Vai trò chưa được cấu hình trong hệ thống", HttpStatus.INTERNAL_SERVER_ERROR));
    }

    /** Chỉ Quản lý được thao tác trên module này (tất cả usecase Bảng 3.3-3.11 đều có Tác nhân = Quản lý). */
    private void kiemTraLaQuanLy(String tenDangNhap) {
        TaiKhoan nguoiThucHien = taiKhoanRepository.findByTenDangNhapOrEmail(tenDangNhap)
                .orElseThrow(() -> new AppException("Không xác định được người thực hiện", HttpStatus.UNAUTHORIZED));
        if (!"Quản lý".equals(nguoiThucHien.getVaiTro().getTenVaiTro())) {
            throw new AppException("Chỉ Quản lý mới có quyền thực hiện thao tác này", HttpStatus.FORBIDDEN);
        }
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
            khachHangRepository.findByTaiKhoanIdTK(taiKhoan.getIdTK()).ifPresent(kh -> {
                builder.idKH(kh.getIdKH());
                builder.tenKhachHang(kh.getTenKhachHang());
                builder.diaChi(kh.getDiaChi());
            });
        }
        return builder.build();
    }
}