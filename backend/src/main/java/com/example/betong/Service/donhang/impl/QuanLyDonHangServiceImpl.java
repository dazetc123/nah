package com.example.betong.Service.donhang.impl;

import com.example.betong.DTO.response.common.PageResponse;
import com.example.betong.DTO.response.donhang.DonHangResponse;
import com.example.betong.Exception.AppException;
import com.example.betong.Service.donhang.QuanLyDonHangService;
import com.example.betong.entity.*;
import com.example.betong.repository.DonHangRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuanLyDonHangServiceImpl implements QuanLyDonHangService {
    private static final int CHO_XU_LY = 0;
    private final DonHangRepository repository;

    public QuanLyDonHangServiceImpl(DonHangRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DonHangResponse> danhSach(String tenDangNhap, String tuKhoa, int trang, int soLuong) {
        String keyword = tuKhoa == null || tuKhoa.isBlank() ? null : tuKhoa.trim();
        return PageResponse.tu(repository.timCuaKhach(tenDangNhap, keyword,
                PageRequest.of(Math.max(0, trang - 1), Math.max(1, soLuong))), this::sangResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public DonHangResponse chiTiet(String tenDangNhap, Long idDH) {
        return sangResponse(timCuaKhach(tenDangNhap, idDH));
    }

    @Override
    @Transactional(readOnly = true)
    public DonHangResponse theoDoi(String tenDangNhap, Long idDH) {
        DonHangResponse response = sangResponse(timCuaKhach(tenDangNhap, idDH));
        return response.getViDo() == null
                ? response.toBuilder().thongBao("Chưa có thông tin vị trí xe").build()
                : response;
    }

    @Override
    @Transactional
    public DonHangResponse huy(String tenDangNhap, Long idDH) {
        DonHang donHang = timCuaKhach(tenDangNhap, idDH);
        if (!Integer.valueOf(CHO_XU_LY).equals(donHang.getTrangThai())) {
            throw new AppException("Không thể hủy đơn hàng vì đơn hàng đã được xử lý", HttpStatus.CONFLICT);
        }
        donHang.setTrangThai(5);
        donHang.setLyDoTuChoi("Khách hàng đã hủy đơn hàng");
        return sangResponse(repository.save(donHang), "Hủy đơn hàng thành công");
    }

    private DonHang timCuaKhach(String tenDangNhap, Long idDH) {
        return repository.findCuaKhach(tenDangNhap, idDH)
                .orElseThrow(() -> new AppException("Đơn hàng không tồn tại hoặc đã bị xóa", HttpStatus.NOT_FOUND));
    }

    private DonHangResponse sangResponse(DonHang d) { return sangResponse(d, null); }

    private DonHangResponse sangResponse(DonHang d, String thongBao) {
        ChiTietDonHang ct = d.getChiTietDonHangs() == null || d.getChiTietDonHangs().isEmpty()
                ? null : d.getChiTietDonHangs().get(0);
        ViTriGPS gps = null;
        if (d.getDanhSachChuyen() != null && !d.getDanhSachChuyen().isEmpty()) {
            Chuyen chuyen = d.getDanhSachChuyen().get(d.getDanhSachChuyen().size() - 1);
            if (chuyen.getDanhSachViTriGPS() != null && !chuyen.getDanhSachViTriGPS().isEmpty()) {
                gps = chuyen.getDanhSachViTriGPS().get(chuyen.getDanhSachViTriGPS().size() - 1);
            }
        }
        return DonHangResponse.builder().idDH(d.getIdDH()).idLBT(ct == null ? null : ct.getLoaiBeTong().getIdLBT())
                .macBeTong(ct == null ? null : ct.getLoaiBeTong().getMacBeTong())
                .khoiLuong(d.getTongKhoiLuong()).donGia(ct == null ? null : ct.getDonGia())
                .thanhTien(ct == null ? null : ct.getThanhTien()).idCT(d.getCongTrinh().getIdCT())
                .tenCongTrinh(d.getCongTrinh().getTenCongTrinh()).diaChiGiao(d.getCongTrinh().getDiaChi())
                .ngayDat(d.getNgayDat()).thoiGianGiao(d.getThoiGianGiao()).tongTien(d.getTongTien())
                .trangThai(d.getTrangThai()).tenTrangThai(tenTrangThai(d.getTrangThai()))
                .ghiChu(d.getGhiChu()).lyDoTuChoi(d.getLyDoTuChoi())
                .idXe(gps == null ? null : gps.getXe().getIdXe()).bienSoXe(gps == null ? null : gps.getXe().getBienSo())
                .viDo(gps == null ? null : gps.getViDo()).kinhDo(gps == null ? null : gps.getKinhDo())
                .thoiDiemGPS(gps == null ? null : gps.getThoiDiem()).thongBao(thongBao).build();
    }

    private String tenTrangThai(Integer status) {
        return switch (status == null ? -1 : status) {
            case 0 -> "Chờ xử lý"; case 1 -> "Đã xác nhận"; case 2 -> "Từ chối";
            case 3 -> "Đang giao"; case 4 -> "Hoàn thành"; case 5 -> "Đã hủy";
            case 6 -> "Đã phân bổ trạm trộn";
            default -> "Không xác định";
        };
    }
}
