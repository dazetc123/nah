package com.example.betong.Service.taixechuyen.impl;

import com.example.betong.Common.TrangThaiChuyen;
import com.example.betong.DTO.response.common.PageResponse;
import com.example.betong.DTO.response.taixechuyen.ChuyenChiTietResponse;
import com.example.betong.DTO.response.taixechuyen.ChuyenDanhSachResponse;
import com.example.betong.Exception.AppException;
import com.example.betong.Service.notification.NotificationService;
import com.example.betong.Service.taixechuyen.ChuyenTaiXeService;
import com.example.betong.entity.Chuyen;
import com.example.betong.entity.DonHang;
import com.example.betong.entity.TaiKhoan;
import com.example.betong.entity.TaiXe;
import com.example.betong.entity.Xe;
import com.example.betong.repository.ChuyenRepository;
import com.example.betong.repository.DonHangRepository;
import com.example.betong.repository.TaiKhoanRepository;
import com.example.betong.repository.TaiXeRepository;
import com.example.betong.repository.XeRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ChuyenTaiXeServiceImpl implements ChuyenTaiXeService {

    private static final int DONHANG_DANG_GIAO = 3;
    private static final int DONHANG_HOAN_THANH = 4;
    private static final int XE_SAN_SANG = 1;

    private final ChuyenRepository chuyenRepository;
    private final TaiXeRepository taiXeRepository;
    private final TaiKhoanRepository taiKhoanRepository;
    private final DonHangRepository donHangRepository;
    private final XeRepository xeRepository;
    private final NotificationService notificationService;

    public ChuyenTaiXeServiceImpl(ChuyenRepository chuyenRepository, TaiXeRepository taiXeRepository,
                                  TaiKhoanRepository taiKhoanRepository, DonHangRepository donHangRepository,
                                  XeRepository xeRepository, NotificationService notificationService) {
        this.chuyenRepository = chuyenRepository;
        this.taiXeRepository = taiXeRepository;
        this.taiKhoanRepository = taiKhoanRepository;
        this.donHangRepository = donHangRepository;
        this.xeRepository = xeRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ChuyenDanhSachResponse> danhSachChuyen(String tenDangNhap, Integer trangThai,
                                                                int trang, int soLuong) {
        TaiXe taiXe = taiXeHienTai(tenDangNhap);
        return PageResponse.tu(
                chuyenRepository.timCuaTaiXe(taiXe.getIdTX(), trangThai,
                        PageRequest.of(Math.max(0, trang - 1), Math.max(1, soLuong))),
                this::sangDanhSachResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ChuyenChiTietResponse chiTietChuyen(String tenDangNhap, Long idChuyen) {
        TaiXe taiXe = taiXeHienTai(tenDangNhap);
        Chuyen chuyen = timChuyenCuaToi(idChuyen, taiXe.getIdTX());
        return sangChiTietResponse(chuyen);
    }

    @Override
    @Transactional
    public ChuyenChiTietResponse nhanChuyen(String tenDangNhap, Long idChuyen) {
        TaiXe taiXe = taiXeHienTai(tenDangNhap);
        Chuyen chuyen = timChuyenCuaToi(idChuyen, taiXe.getIdTX());
        if (!Integer.valueOf(TrangThaiChuyen.CHO_NHAN).equals(chuyen.getTrangThai())) {
            throw new AppException("Chuyến không còn ở trạng thái Chờ nhận", HttpStatus.CONFLICT);
        }
        chuyen.setTrangThai(TrangThaiChuyen.DA_NHAN);
        Chuyen saved = chuyenRepository.saveAndFlush(chuyen);
        notificationService.notifyOrderStatusChanged(chuyen.getDonHang().getIdDH(),
                "Tài xế " + taiXe.getHoTen() + " đã nhận chuyến #" + idChuyen);
        return sangChiTietResponse(saved);
    }

    @Override
    @Transactional
    public ChuyenChiTietResponse batDauChuyen(String tenDangNhap, Long idChuyen) {
        TaiXe taiXe = taiXeHienTai(tenDangNhap);
        Chuyen chuyen = timChuyenCuaToi(idChuyen, taiXe.getIdTX());
        if (!Integer.valueOf(TrangThaiChuyen.DA_NHAN).equals(chuyen.getTrangThai())) {
            throw new AppException("Chuyến phải ở trạng thái Đã nhận mới được bắt đầu", HttpStatus.CONFLICT);
        }
        chuyen.setTrangThai(TrangThaiChuyen.DANG_GIAO);
        chuyen.setThoiGianXuatPhat(LocalDateTime.now());
        Chuyen saved = chuyenRepository.saveAndFlush(chuyen);
        notificationService.notifyOrderStatusChanged(chuyen.getDonHang().getIdDH(),
                "Chuyến #" + idChuyen + " đã bắt đầu, xe " + chuyen.getXe().getBienSo() + " đang di chuyển");
        return sangChiTietResponse(saved);
    }

    @Override
    @Transactional
    public ChuyenChiTietResponse hoanThanhChuyen(String tenDangNhap, Long idChuyen) {
        TaiXe taiXe = taiXeHienTai(tenDangNhap);
        Chuyen chuyen = timChuyenCuaToi(idChuyen, taiXe.getIdTX());
        Integer hienTai = chuyen.getTrangThai();
        boolean duDieuKien = hienTai != null && hienTai >= TrangThaiChuyen.DANG_GIAO
                && hienTai < TrangThaiChuyen.HOAN_THANH;
        if (!duDieuKien) {
            throw new AppException("Chuyến phải đang được giao mới có thể hoàn thành", HttpStatus.CONFLICT);
        }
        chuyen.setTrangThai(TrangThaiChuyen.HOAN_THANH);
        chuyen.setThoiGianDen(LocalDateTime.now());
        Chuyen saved = chuyenRepository.saveAndFlush(chuyen);

        // Trả tài xế và xe về trạng thái Sẵn sàng, đóng đơn hàng
        taiXe.setTrangThai(XE_SAN_SANG);
        taiXeRepository.saveAndFlush(taiXe);
        Xe xe = chuyen.getXe();
        xe.setTrangThai(XE_SAN_SANG);
        xeRepository.saveAndFlush(xe);
        DonHang donHang = chuyen.getDonHang();
        donHang.setTrangThai(DONHANG_HOAN_THANH);
        donHangRepository.saveAndFlush(donHang);

        notificationService.notifyOrderStatusChanged(donHang.getIdDH(),
                "Chuyến #" + idChuyen + " đã hoàn thành");
        return sangChiTietResponse(saved);
    }

    // ===================== Helpers =====================

    private TaiXe taiXeHienTai(String tenDangNhap) {
        TaiKhoan taiKhoan = taiKhoanRepository.findByTenDangNhapOrEmail(tenDangNhap)
                .orElseThrow(() -> new AppException("Không tìm thấy tài khoản", HttpStatus.NOT_FOUND));
        return taiXeRepository.findByTaiKhoanIdTK(taiKhoan.getIdTK())
                .orElseThrow(() -> new AppException("Tài khoản chưa có hồ sơ tài xế", HttpStatus.FORBIDDEN));
    }

    private Chuyen timChuyenCuaToi(Long idChuyen, Long idTX) {
        return chuyenRepository.timCuaTaiXe(idChuyen, idTX)
                .orElseThrow(() -> new AppException(
                        "Không tìm thấy chuyến hoặc chuyến không được phân công cho bạn", HttpStatus.NOT_FOUND));
    }

    private ChuyenDanhSachResponse sangDanhSachResponse(Chuyen c) {
        DonHang d = c.getDonHang();
        return ChuyenDanhSachResponse.builder()
                .idChuyen(c.getIdChuyen())
                .idDH(d.getIdDH())
                .tenCongTrinh(d.getCongTrinh() == null ? null : d.getCongTrinh().getTenCongTrinh())
                .khoiLuong(c.getKhoiLuong())
                .thoiGianGiao(d.getThoiGianGiao())
                .bienSo(c.getXe().getBienSo())
                .trangThai(c.getTrangThai())
                .tenTrangThai(TrangThaiChuyen.tenHienThi(c.getTrangThai()))
                .build();
    }

    private ChuyenChiTietResponse sangChiTietResponse(Chuyen c) {
        DonHang d = c.getDonHang();
        var congTrinh = d.getCongTrinh();
        var tram = c.getTramTron();
        String macBeTong = (d.getChiTietDonHangs() == null || d.getChiTietDonHangs().isEmpty()) ? null
                : d.getChiTietDonHangs().get(0).getLoaiBeTong().getMacBeTong();
        return ChuyenChiTietResponse.builder()
                .idChuyen(c.getIdChuyen())
                .idDH(d.getIdDH())
                .tenCongTrinh(congTrinh == null ? null : congTrinh.getTenCongTrinh())
                .diaChiCongTrinh(congTrinh == null ? null : congTrinh.getDiaChi())
                .viDoCongTrinh(congTrinh == null ? null : congTrinh.getViDo())
                .kinhDoCongTrinh(congTrinh == null ? null : congTrinh.getKinhDo())
                .sdtCongTrinh(congTrinh == null ? null : congTrinh.getSdt())
                .macBeTong(macBeTong)
                .khoiLuong(c.getKhoiLuong())
                .tenTram(tram == null ? null : tram.getTenTram())
                .diaChiTram(tram == null ? null : tram.getDiaChi())
                .thoiGianXuatPhat(c.getThoiGianXuatPhat())
                .thoiGianDen(c.getThoiGianDen())
                .bienSo(c.getXe().getBienSo())
                .ghiChu(d.getGhiChu())
                .trangThai(c.getTrangThai())
                .tenTrangThai(TrangThaiChuyen.tenHienThi(c.getTrangThai()))
                .build();
    }
}
