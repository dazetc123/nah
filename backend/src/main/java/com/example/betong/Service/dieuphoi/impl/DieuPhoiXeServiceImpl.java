package com.example.betong.Service.dieuphoi.impl;

import com.example.betong.DTO.request.dieuphoi.TaoChuyenRequest;
import com.example.betong.DTO.response.common.PageResponse;
import com.example.betong.DTO.response.dieuphoi.ChuyenResponse;
import com.example.betong.DTO.response.donhang.TramTronKhaDungResponse;
import com.example.betong.DTO.response.taixe.TaiXeResponse;
import com.example.betong.DTO.response.xe.XeResponse;
import com.example.betong.Exception.AppException;
import com.example.betong.Common.TrangThaiChuyen;
import com.example.betong.Service.dieuphoi.DieuPhoiXeService;
import com.example.betong.Service.notification.NotificationService;
import com.example.betong.entity.*;
import com.example.betong.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DieuPhoiXeServiceImpl implements DieuPhoiXeService {
    private static final int DA_PHAN_BO_TRAM = 6;
    private static final int DANG_GIAO = 3;
    private final ChuyenRepository chuyenRepository;
    private final XeRepository xeRepository;
    private final TaiXeRepository taiXeRepository;
    private final TramTronRepository tramTronRepository;
    private final DonHangRepository donHangRepository;
    private final NotificationService notificationService;

    public DieuPhoiXeServiceImpl(ChuyenRepository chuyenRepository, XeRepository xeRepository,
                                 TaiXeRepository taiXeRepository, TramTronRepository tramTronRepository,
                                 DonHangRepository donHangRepository, NotificationService notificationService) {
        this.chuyenRepository = chuyenRepository;
        this.xeRepository = xeRepository;
        this.taiXeRepository = taiXeRepository;
        this.tramTronRepository = tramTronRepository;
        this.donHangRepository = donHangRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<XeResponse> timXeRanh(String tuKhoa, int trang, int soLuong) {
        return PageResponse.tu(chuyenRepository.timXeRanh(normalize(tuKhoa),
                PageRequest.of(Math.max(0, trang - 1), Math.max(1, soLuong))), this::xeResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TaiXeResponse> timTaiXeRanh(String tuKhoa, int trang, int soLuong) {
        return PageResponse.tu(chuyenRepository.timTaiXeRanh(normalize(tuKhoa),
                PageRequest.of(Math.max(0, trang - 1), Math.max(1, soLuong))), this::taiXeResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TramTronKhaDungResponse> tramTronKhaDung(Long idDH) {
        DonHang d = timDonHang(idDH);
        if (!Integer.valueOf(DA_PHAN_BO_TRAM).equals(d.getTrangThai())) {
            throw new AppException("Đơn hàng chưa ở trạng thái phân bổ trạm trộn", HttpStatus.CONFLICT);
        }
        return tramTronRepository.timKhaDung(d.getTongKhoiLuong()).stream()
                .map(t -> TramTronKhaDungResponse.builder().idTram(t.getIdTram()).tenTram(t.getTenTram())
                        .diaChi(t.getDiaChi()).congSuat(t.getCongSuat()).trangThai(t.getTrangThai()).build())
                .toList();
    }

    @Override
    @Transactional
    public ChuyenResponse taoChuyen(TaoChuyenRequest request) {
        DonHang donHang = timDonHang(request.getIdDH());
        if (!Integer.valueOf(DA_PHAN_BO_TRAM).equals(donHang.getTrangThai())) {
            throw new AppException("Đơn hàng chưa được phân bổ trạm trộn", HttpStatus.CONFLICT);
        }
        if (donHang.getTramTron() == null || !donHang.getTramTron().getIdTram().equals(request.getIdTram())) {
            throw new AppException("Trạm trộn được chọn không khớp với phân bổ của đơn hàng", HttpStatus.CONFLICT);
        }
        if (chuyenRepository.existsByDonHang_IdDH(request.getIdDH())) {
            throw new AppException("Đơn hàng đã có chuyến giao hàng", HttpStatus.CONFLICT);
        }
        Xe xe = xeRepository.findById(request.getIdXe())
                .orElseThrow(() -> new AppException("Không tìm thấy xe", HttpStatus.NOT_FOUND));
        TaiXe taiXe = taiXeRepository.findById(request.getIdTX())
                .orElseThrow(() -> new AppException("Không tìm thấy tài xế", HttpStatus.NOT_FOUND));
        TramTron tram = tramTronRepository.findById(request.getIdTram())
                .orElseThrow(() -> new AppException("Không tìm thấy trạm trộn", HttpStatus.NOT_FOUND));
        if (!Integer.valueOf(1).equals(xe.getTrangThai()) || chuyenRepository.xeDangBan(xe.getIdXe())) {
            throw new AppException("Xe không khả dụng hoặc đã được phân công cho chuyến khác", HttpStatus.CONFLICT);
        }
        if (!Integer.valueOf(1).equals(taiXe.getTrangThai()) || chuyenRepository.taiXeDangBan(taiXe.getIdTX())) {
            throw new AppException("Tài xế không khả dụng hoặc đã được phân công cho chuyến khác", HttpStatus.CONFLICT);
        }
        if (!Integer.valueOf(1).equals(tram.getTrangThai()) || tram.getCongSuat() == null
                || tram.getCongSuat() < donHang.getTongKhoiLuong()) {
            throw new AppException("Trạm trộn không đủ điều kiện thực hiện chuyến", HttpStatus.CONFLICT);
        }
        // Chuyến vừa tạo ở trạng thái Chờ nhận (mục 2.2.1) — tài xế phải tự
        // Nhận chuyến rồi mới Bắt đầu chuyến từ app di động, KHÔNG coi như
        // đang giao ngay khi điều phối vừa phân công.
        Chuyen chuyen = Chuyen.builder().donHang(donHang).xe(xe).taiXe(taiXe).tramTron(tram)
                .khoiLuong(donHang.getTongKhoiLuong()).trangThai(TrangThaiChuyen.CHO_NHAN)
                .thoiGianXuatPhat(request.getThoiGianXuatPhat()).thoiGianDen(request.getThoiGianDen()).build();
        Chuyen saved = chuyenRepository.saveAndFlush(chuyen);
        // Đơn hàng (DonHang.trangThai, thang đo riêng) chuyển sang Đang giao
        // ngay khi đã có chuyến, độc lập với trạng thái chi tiết của Chuyen.
        donHang.setTrangThai(DANG_GIAO);
        donHangRepository.saveAndFlush(donHang);
        notificationService.notifyDriverAssignment(saved.getIdChuyen(), taiXe.getHoTen(), xe.getBienSo());
        return sangResponse(saved, "Tạo chuyến và phân công tài xế thành công");
    }

    private DonHang timDonHang(Long id) {
        return donHangRepository.findById(id).orElseThrow(
                () -> new AppException("Đơn hàng không tồn tại hoặc đã bị xóa", HttpStatus.NOT_FOUND));
    }
    private String normalize(String value) { return value == null || value.isBlank() ? null : value.trim(); }

    private XeResponse xeResponse(Xe x) {
        return XeResponse.builder().idXe(x.getIdXe()).bienSo(x.getBienSo()).trongTai(x.getTrongTai())
                .trangThai(x.getTrangThai()).idTX(x.getTaiXe() == null ? null : x.getTaiXe().getIdTX()).build();
    }
    private TaiXeResponse taiXeResponse(TaiXe t) {
        return TaiXeResponse.builder().idTX(t.getIdTX()).idTK(t.getTaiKhoan().getIdTK())
                .tenDangNhap(t.getTaiKhoan().getTenDangNhap()).hoTen(t.getHoTen()).soGPLX(t.getSoGPLX())
                .sdt(t.getSdt()).trangThai(t.getTrangThai())
                .bienSoXeDangGan(t.getDanhSachXe() == null || t.getDanhSachXe().isEmpty()
                        ? null : t.getDanhSachXe().get(0).getBienSo()).build();
    }
    private ChuyenResponse sangResponse(Chuyen c, String message) {
        return ChuyenResponse.builder().idChuyen(c.getIdChuyen()).idDH(c.getDonHang().getIdDH())
                .idTram(c.getTramTron().getIdTram()).tenTram(c.getTramTron().getTenTram())
                .idXe(c.getXe().getIdXe()).bienSo(c.getXe().getBienSo()).idTX(c.getTaiXe().getIdTX())
                .tenTaiXe(c.getTaiXe().getHoTen()).khoiLuong(c.getKhoiLuong()).trangThai(c.getTrangThai())
                .thoiGianXuatPhat(c.getThoiGianXuatPhat()).thoiGianDen(c.getThoiGianDen())
                .thongBao(message).build();
    }
}
