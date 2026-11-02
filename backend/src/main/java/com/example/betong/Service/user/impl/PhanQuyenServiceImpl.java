package com.example.betong.Service.user.impl;

import com.example.betong.DTO.request.user.PhanQuyenRequest;
import com.example.betong.DTO.response.common.PageResponse;
import com.example.betong.DTO.response.user.TaiKhoanResponse;
import com.example.betong.Exception.AppException;
import com.example.betong.Service.user.PhanQuyenService;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Bảng 3.11 - Phân quyền người dùng.
 *
 * GIẢ ĐỊNH NGHIỆP VỤ (báo cáo không nói rõ chi tiết, mình chọn phương án
 * an toàn nhất): chỉ cho phép hoán đổi vai trò qua lại giữa "Nhân viên
 * điều phối" và "Tài xế" — không cho phân quyền thành "Quản lý" (tránh
 * leo thang đặc quyền không kiểm soát qua API), không cho đổi thành/từ
 * "Khách hàng" (vì Khách hàng gắn với luồng tự đăng ký và dữ liệu công
 * trình/đơn hàng riêng, đổi qua lại dễ vỡ dữ liệu). Nếu thực tế đồ án
 * của bạn cần khác đi, chỉ cần sửa lại tập hợp VAI_TRO_DUOC_PHAN bên dưới.
 */
@Service
public class PhanQuyenServiceImpl implements PhanQuyenService {

    private static final Set<String> VAI_TRO_DUOC_PHAN = Set.of("Nhân viên điều phối", "Tài xế");

    private final TaiKhoanRepository taiKhoanRepository;
    private final TaiXeRepository taiXeRepository;
    private final KhachHangRepository khachHangRepository;
    private final VaiTroRepository vaiTroRepository;

    public PhanQuyenServiceImpl(TaiKhoanRepository taiKhoanRepository,
                                TaiXeRepository taiXeRepository,
                                KhachHangRepository khachHangRepository,
                                VaiTroRepository vaiTroRepository) {
        this.taiKhoanRepository = taiKhoanRepository;
        this.taiXeRepository = taiXeRepository;
        this.khachHangRepository = khachHangRepository;
        this.vaiTroRepository = vaiTroRepository;
    }

    @Override
    public PageResponse<TaiKhoanResponse> danhSachTatCaTaiKhoan(String tuKhoa, int trang, int soLuong) {
        String tuKhoaThucSu = (tuKhoa == null || tuKhoa.isBlank()) ? null : tuKhoa.trim();
        Pageable pageable = PageRequest.of(Math.max(0, trang - 1), soLuong);
        // tenVaiTro = null -> lấy TẤT CẢ vai trò, đúng bước 2 của usecase
        return PageResponse.tu(taiKhoanRepository.timKiem(tuKhoaThucSu, null, pageable), this::sangResponse);
    }

    @Override
    @Transactional
    public TaiKhoanResponse doiVaiTro(Long idTK, PhanQuyenRequest request, String tenDangNhapNguoiThucHien) {
        kiemTraLaQuanLy(tenDangNhapNguoiThucHien);

        TaiKhoan taiKhoan = taiKhoanRepository.findById(idTK)
                .orElseThrow(() -> new AppException("Không tìm thấy tài khoản", HttpStatus.NOT_FOUND));

        String vaiTroCu = taiKhoan.getVaiTro().getTenVaiTro();
        String vaiTroMoi = request.getTenVaiTroMoi();

        // Bước 4.a: "Nếu không hợp lệ thông báo chọn lại"
        if ("Quản lý".equals(vaiTroCu)) {
            throw new AppException("Không thể đổi vai trò của tài khoản Quản lý", HttpStatus.FORBIDDEN);
        }
        if (!VAI_TRO_DUOC_PHAN.contains(vaiTroMoi)) {
            throw new AppException("Vai trò không hợp lệ, chỉ được phân quyền Nhân viên điều phối hoặc Tài xế",
                    HttpStatus.BAD_REQUEST);
        }
        if ("Khách hàng".equals(vaiTroCu)) {
            throw new AppException("Không thể phân quyền lại tài khoản Khách hàng", HttpStatus.BAD_REQUEST);
        }
        if (vaiTroCu.equals(vaiTroMoi)) {
            throw new AppException("Tài khoản đã mang vai trò này rồi", HttpStatus.BAD_REQUEST);
        }

        VaiTro vaiTroMoiEntity = vaiTroRepository.findByTenVaiTro(vaiTroMoi)
                .orElseThrow(() -> new AppException("Vai trò chưa được cấu hình trong hệ thống", HttpStatus.INTERNAL_SERVER_ERROR));

        // Dọn dữ liệu hồ sơ con (TaiXe) tương ứng khi đổi vai trò, tránh dữ liệu mồ côi/thiếu.
        if ("Tài xế".equals(vaiTroCu)) {
            // Đổi RA khỏi Tài xế -> xóa hồ sơ TaiXe cũ (không còn thuộc vai trò này nữa)
            taiXeRepository.findByTaiKhoanIdTK(idTK).ifPresent(taiXeRepository::delete);
        } else if ("Tài xế".equals(vaiTroMoi)) {
            // Đổi SANG Tài xế (từ Nhân viên điều phối) -> tạo hồ sơ TaiXe mới, để trống GPLX
            // (Quản lý bổ sung GPLX sau qua Bảng 3.5 - Sửa tài khoản)
            TaiXe taiXe = new TaiXe();
            taiXe.setTaiKhoan(taiKhoan);
            taiXe.setHoTen(taiKhoan.getHoTen());
            taiXe.setSdt(taiKhoan.getSdt());
            taiXe.setTrangThai(1);
            taiXeRepository.save(taiXe);
        }

        taiKhoan.setVaiTro(vaiTroMoiEntity);
        taiKhoanRepository.save(taiKhoan);

        // Bước 6: "Hệ thống thông báo cập nhật quyền thành công"
        TaiKhoanResponse response = sangResponse(taiKhoan);
        response.setThongBao("Cập nhật quyền thành công");
        return response;
    }

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
