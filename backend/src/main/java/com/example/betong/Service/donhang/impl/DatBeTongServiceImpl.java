package com.example.betong.Service.donhang.impl;

import com.example.betong.DTO.request.donhang.DatBeTongRequest;
import com.example.betong.DTO.response.donhang.DatBeTongResponse;
import com.example.betong.Exception.AppException;
import com.example.betong.Service.donhang.DatBeTongService;
import com.example.betong.Service.notification.NotificationService;
import com.example.betong.entity.*;
import com.example.betong.repository.*;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;

@Service
public class DatBeTongServiceImpl implements DatBeTongService {
    private static final int CHO_XU_LY = 0;
    private final TaiKhoanRepository taiKhoanRepository;
    private final KhachHangRepository khachHangRepository;
    private final LoaiBeTongRepository loaiBeTongRepository;
    private final CongTrinhRepository congTrinhRepository;
    private final DonHangRepository donHangRepository;
    private final ChiTietDonHangRepository chiTietRepository;
    private final NotificationService notificationService;

    public DatBeTongServiceImpl(TaiKhoanRepository taiKhoanRepository, KhachHangRepository khachHangRepository,
                                LoaiBeTongRepository loaiBeTongRepository, CongTrinhRepository congTrinhRepository,
                                DonHangRepository donHangRepository, ChiTietDonHangRepository chiTietRepository,
                                NotificationService notificationService) {
        this.taiKhoanRepository = taiKhoanRepository;
        this.khachHangRepository = khachHangRepository;
        this.loaiBeTongRepository = loaiBeTongRepository;
        this.congTrinhRepository = congTrinhRepository;
        this.donHangRepository = donHangRepository;
        this.chiTietRepository = chiTietRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public DatBeTongResponse datHang(String tenDangNhap, DatBeTongRequest request) {
        TaiKhoan taiKhoan = taiKhoanRepository.findByTenDangNhapOrEmail(tenDangNhap)
                .orElseThrow(() -> new AppException("Không xác định được tài khoản", HttpStatus.UNAUTHORIZED));
        KhachHang khachHang = khachHangRepository.findByTaiKhoanIdTK(taiKhoan.getIdTK())
                .orElseThrow(() -> new AppException("Tài khoản chưa có hồ sơ khách hàng", HttpStatus.FORBIDDEN));
        LoaiBeTong loai = loaiBeTongRepository.findById(request.getIdLBT())
                .orElseThrow(() -> new AppException("Không tìm thấy loại bê tông", HttpStatus.NOT_FOUND));
        if (!Integer.valueOf(1).equals(loai.getTrangThai())) {
            throw new AppException("Bê tông đang ngừng cung cấp hoặc hết hàng", HttpStatus.CONFLICT);
        }
        if (request.getThoiGianGiao().toLocalTime().isBefore(LocalTime.of(6, 0))
                || request.getThoiGianGiao().toLocalTime().isAfter(LocalTime.of(18, 0))) {
            throw new AppException("Thời gian giao hàng phải trong khung 06:00 - 18:00",
                    HttpStatus.BAD_REQUEST);
        }
        CongTrinh congTrinh;
        if (request.getIdCT() != null) {
            congTrinh = congTrinhRepository.findById(request.getIdCT())
                    .orElseThrow(() -> new AppException("Không tìm thấy công trình", HttpStatus.NOT_FOUND));
            if (!congTrinh.getKhachHang().getIdKH().equals(khachHang.getIdKH())) {
                throw new AppException("Công trình không thuộc khách hàng đang đăng nhập", HttpStatus.FORBIDDEN);
            }
        } else {
            if (request.getDiaChiMoi() == null || request.getDiaChiMoi().isBlank()) {
                throw new AppException("Vui lòng chọn công trình hoặc nhập địa chỉ giao hàng",
                        HttpStatus.BAD_REQUEST);
            }
            congTrinh = CongTrinh.builder().khachHang(khachHang)
                    .tenCongTrinh("Địa chỉ giao hàng mới")
                    .diaChi(request.getDiaChiMoi().trim()).sdt(khachHang.getSdt()).build();
            congTrinh = congTrinhRepository.save(congTrinh);
        }
        double thanhTien = loai.getDonGia() * request.getKhoiLuong();
        try {
            DonHang donHang = DonHang.builder().khachHang(khachHang).congTrinh(congTrinh)
                    .ngayDat(java.time.LocalDate.now()).thoiGianGiao(request.getThoiGianGiao())
                    .tongKhoiLuong(request.getKhoiLuong()).tongTien(thanhTien)
                    .trangThai(CHO_XU_LY).ghiChu(request.getGhiChu()).build();
            donHang = donHangRepository.save(donHang);
            ChiTietDonHang chiTiet = ChiTietDonHang.builder().donHang(donHang).loaiBeTong(loai)
                    .khoiLuong(request.getKhoiLuong()).donGia(loai.getDonGia()).thanhTien(thanhTien).build();
            chiTietRepository.save(chiTiet);
            notificationService.notifyDispatchersOfNewOrder(donHang.getIdDH());
            return DatBeTongResponse.builder().idDH(donHang.getIdDH()).idLBT(loai.getIdLBT())
                    .macBeTong(loai.getMacBeTong()).khoiLuong(request.getKhoiLuong()).donGia(loai.getDonGia())
                    .thanhTien(thanhTien).idCT(congTrinh.getIdCT()).diaChiGiao(congTrinh.getDiaChi())
                    .thoiGianGiao(request.getThoiGianGiao()).tongTien(thanhTien).trangThai(CHO_XU_LY)
                    .thongBao("Đặt bê tông thành công. Đơn hàng đang chờ xử lý").build();
        } catch (DataAccessException ex) {
            throw new AppException("Lỗi kết nối, vui lòng thử lại khi mạng ổn định",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
