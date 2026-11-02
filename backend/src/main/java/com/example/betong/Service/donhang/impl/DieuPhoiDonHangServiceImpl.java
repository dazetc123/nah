package com.example.betong.Service.donhang.impl;

import com.example.betong.DTO.request.donhang.CapNhatTrangThaiDonHangRequest;
import com.example.betong.DTO.request.donhang.TuChoiDonHangRequest;
import com.example.betong.DTO.response.common.PageResponse;
import com.example.betong.DTO.response.donhang.DonHangResponse;
import com.example.betong.DTO.response.donhang.TramTronKhaDungResponse;
import com.example.betong.Exception.AppException;
import com.example.betong.Service.donhang.DieuPhoiDonHangService;
import com.example.betong.Service.notification.NotificationService;
import com.example.betong.entity.*;
import com.example.betong.repository.DonHangRepository;
import com.example.betong.repository.TramTronRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DieuPhoiDonHangServiceImpl implements DieuPhoiDonHangService {
    private static final int CHO_XU_LY = 0;
    private static final int DA_XAC_NHAN = 1;
    private static final int TU_CHOI = 2;
    private static final int DA_PHAN_BO_TRAM = 6;
    private static final int DANG_GIAO = 3;
    private static final int HOAN_THANH = 4;
    private static final int DA_HUY = 5;

    private final DonHangRepository donHangRepository;
    private final TramTronRepository tramTronRepository;
    private final NotificationService notificationService;

    public DieuPhoiDonHangServiceImpl(DonHangRepository donHangRepository,
                                      TramTronRepository tramTronRepository,
                                      NotificationService notificationService) {
        this.donHangRepository = donHangRepository;
        this.tramTronRepository = tramTronRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DonHangResponse> danhSachChoXuLy(int trang, int soLuong) {
        return PageResponse.tu(donHangRepository.findByTrangThaiOrderByNgayDatAscIdDHAsc(
                CHO_XU_LY, PageRequest.of(Math.max(0, trang - 1), Math.max(1, soLuong))), this::sangResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DonHangResponse> danhSachTheoTrangThai(int trangThai, int trang, int soLuong) {
        PageRequest page = PageRequest.of(Math.max(0, trang - 1), Math.max(1, soLuong));
        // Đơn đã phân bổ trạm nhưng đã có chuyến thì không hiện để tạo chuyến lần nữa
        return PageResponse.tu(trangThai == DA_PHAN_BO_TRAM
                ? donHangRepository.timChuaCoChuyen(trangThai, page)
                : donHangRepository.findByTrangThaiOrderByNgayDatAscIdDHAsc(trangThai, page), this::sangResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public DonHangResponse chiTiet(Long idDH) {
        return sangResponse(timDonHang(idDH));
    }

    @Override
    @Transactional
    public DonHangResponse xacNhan(Long idDH) {
        DonHang donHang = timDonHang(idDH);
        yeuCauTrangThai(donHang, CHO_XU_LY);
        donHang.setTrangThai(DA_XAC_NHAN);
        DonHang saved = donHangRepository.saveAndFlush(donHang);
        notificationService.notifyOrderStatusChanged(idDH, "Đơn hàng đã được xác nhận");
        return sangResponse(saved, "Xác nhận đơn hàng thành công");
    }

    @Override
    @Transactional
    public DonHangResponse tuChoi(Long idDH, TuChoiDonHangRequest request) {
        DonHang donHang = timDonHang(idDH);
        yeuCauTrangThai(donHang, CHO_XU_LY);
        donHang.setTrangThai(TU_CHOI);
        donHang.setLyDoTuChoi(request.getLyDo().trim());
        DonHang saved = donHangRepository.saveAndFlush(donHang);
        notificationService.notifyOrderStatusChanged(idDH, "Đơn hàng bị từ chối: " + donHang.getLyDoTuChoi());
        return sangResponse(saved, "Từ chối đơn hàng thành công");
    }

    @Override
    @Transactional(readOnly = true)
    public List<TramTronKhaDungResponse> tramKhaDung(Long idDH) {
        DonHang donHang = timDonHang(idDH);
        yeuCauTrangThai(donHang, DA_XAC_NHAN);
        return tramTronRepository.timKhaDung(donHang.getTongKhoiLuong()).stream()
                .map(t -> TramTronKhaDungResponse.builder().idTram(t.getIdTram()).tenTram(t.getTenTram())
                        .diaChi(t.getDiaChi()).congSuat(t.getCongSuat()).trangThai(t.getTrangThai()).build())
                .toList();
    }

    @Override
    @Transactional
    public DonHangResponse phanBoTram(Long idDH, Long idTram) {
        DonHang donHang = timDonHang(idDH);
        yeuCauTrangThai(donHang, DA_XAC_NHAN);
        TramTron tram = tramTronRepository.findById(idTram)
                .orElseThrow(() -> new AppException("Không tìm thấy trạm trộn", HttpStatus.NOT_FOUND));
        if (!Integer.valueOf(1).equals(tram.getTrangThai())
                || tram.getCongSuat() == null || tram.getCongSuat() < donHang.getTongKhoiLuong()) {
            throw new AppException("Trạm trộn không đủ điều kiện phân bổ", HttpStatus.CONFLICT);
        }
        donHang.setTramTron(tram);
        donHang.setTrangThai(DA_PHAN_BO_TRAM);
        DonHang saved = donHangRepository.saveAndFlush(donHang);
        notificationService.notifyOrderStatusChanged(idDH, "Đơn hàng đã được phân bổ trạm trộn " + tram.getTenTram());
        return sangResponse(saved, "Phân bổ trạm trộn thành công");
    }

    @Override
    @Transactional
    public DonHangResponse capNhatTrangThai(Long idDH, CapNhatTrangThaiDonHangRequest request) {
        DonHang donHang = timDonHang(idDH);
        int next = request.getTrangThai();
        if (!laTrangThaiHopLe(next) || !duocChuyen(donHang.getTrangThai(), next)) {
            throw new AppException("Trạng thái mới không hợp lệ với trạng thái hiện tại", HttpStatus.CONFLICT);
        }
        donHang.setTrangThai(next);
        DonHang saved = donHangRepository.saveAndFlush(donHang);
        notificationService.notifyOrderStatusChanged(idDH, "Trạng thái đơn hàng: " + tenTrangThai(next));
        return sangResponse(saved, "Cập nhật trạng thái đơn hàng thành công");
    }

    private DonHang timDonHang(Long idDH) {
        return donHangRepository.findById(idDH)
                .orElseThrow(() -> new AppException("Đơn hàng không tồn tại hoặc đã bị xóa", HttpStatus.NOT_FOUND));
    }

    private void yeuCauTrangThai(DonHang d, int expected) {
        if (!Integer.valueOf(expected).equals(d.getTrangThai())) {
            throw new AppException("Đơn hàng đã thay đổi trạng thái", HttpStatus.CONFLICT);
        }
    }

    private boolean laTrangThaiHopLe(int status) {
        return status >= CHO_XU_LY && status <= DA_PHAN_BO_TRAM;
    }

    private boolean duocChuyen(Integer current, int next) {
        if (current == null || current == TU_CHOI || current == HOAN_THANH || current == DA_HUY) return false;
        return (current == DA_PHAN_BO_TRAM && next == DANG_GIAO)
                || (current == DANG_GIAO && next == HOAN_THANH)
                || (current == DA_XAC_NHAN && next == DA_PHAN_BO_TRAM);
    }

    private DonHangResponse sangResponse(DonHang d) { return sangResponse(d, null); }

    private DonHangResponse sangResponse(DonHang d, String thongBao) {
        ChiTietDonHang ct = d.getChiTietDonHangs() == null || d.getChiTietDonHangs().isEmpty()
                ? null : d.getChiTietDonHangs().get(0);
        TramTron tram = d.getTramTron();
        return DonHangResponse.builder().idDH(d.getIdDH()).idLBT(ct == null ? null : ct.getLoaiBeTong().getIdLBT())
                .macBeTong(ct == null ? null : ct.getLoaiBeTong().getMacBeTong()).khoiLuong(d.getTongKhoiLuong())
                .donGia(ct == null ? null : ct.getDonGia()).thanhTien(ct == null ? null : ct.getThanhTien())
                .idCT(d.getCongTrinh().getIdCT()).tenCongTrinh(d.getCongTrinh().getTenCongTrinh())
                .diaChiGiao(d.getCongTrinh().getDiaChi()).ngayDat(d.getNgayDat()).thoiGianGiao(d.getThoiGianGiao())
                .tongTien(d.getTongTien()).trangThai(d.getTrangThai()).tenTrangThai(tenTrangThai(d.getTrangThai()))
                .ghiChu(d.getGhiChu()).lyDoTuChoi(d.getLyDoTuChoi()).idTram(tram == null ? null : tram.getIdTram())
                .tenTram(tram == null ? null : tram.getTenTram()).thongBao(thongBao).build();
    }

    private String tenTrangThai(Integer status) {
        return switch (status == null ? -1 : status) {
            case 0 -> "Chờ xử lý"; case 1 -> "Đã xác nhận"; case 2 -> "Bị từ chối";
            case 3 -> "Đang giao"; case 4 -> "Hoàn thành"; case 5 -> "Đã hủy";
            case 6 -> "Đã phân bổ trạm trộn"; default -> "Không xác định";
        };
    }
}
