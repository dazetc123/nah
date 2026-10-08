package com.example.betong.Service.taixechuyen.impl;

import com.example.betong.Common.TrangThaiChuyen;
import com.example.betong.DTO.request.taixechuyen.DaDenCongTrinhRequest;
import com.example.betong.DTO.response.common.PageResponse;
import com.example.betong.DTO.response.taixechuyen.ChuyenChiTietResponse;
import com.example.betong.DTO.response.taixechuyen.ChuyenDanhSachResponse;
import com.example.betong.Exception.AppException;
import com.example.betong.Service.file.FileStorageService;
import com.example.betong.Service.notification.NotificationService;
import com.example.betong.Service.taixechuyen.ChuyenTaiXeService;
import com.example.betong.entity.Chuyen;
import com.example.betong.entity.CongTrinh;
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
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;

@Service
public class ChuyenTaiXeServiceImpl implements ChuyenTaiXeService {

    private static final int DONHANG_HOAN_THANH = 4;
    private static final int XE_SAN_SANG = 1;

    /** Bán kính (mét) quanh tọa độ công trình được coi là "đã đến" - luồng 2.a usecase "Xác nhận đã đến công trình". */
    public static final int BAN_KINH_CHO_PHEP_M = 500;
    private static final double SAI_SO_KHOI_LUONG = 1e-6;

    private final ChuyenRepository chuyenRepository;
    private final TaiXeRepository taiXeRepository;
    private final TaiKhoanRepository taiKhoanRepository;
    private final DonHangRepository donHangRepository;
    private final XeRepository xeRepository;
    private final NotificationService notificationService;
    private final FileStorageService fileStorageService;

    public ChuyenTaiXeServiceImpl(ChuyenRepository chuyenRepository, TaiXeRepository taiXeRepository,
                                  TaiKhoanRepository taiKhoanRepository, DonHangRepository donHangRepository,
                                  XeRepository xeRepository, NotificationService notificationService,
                                  FileStorageService fileStorageService) {
        this.chuyenRepository = chuyenRepository;
        this.taiXeRepository = taiXeRepository;
        this.taiKhoanRepository = taiKhoanRepository;
        this.donHangRepository = donHangRepository;
        this.xeRepository = xeRepository;
        this.notificationService = notificationService;
        this.fileStorageService = fileStorageService;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ChuyenDanhSachResponse> danhSachChuyen(String tenDangNhap, Integer trangThai, LocalDate ngay,
                                                                int trang, int soLuong) {
        TaiXe taiXe = taiXeHienTai(tenDangNhap);
        LocalDateTime tuNgay = ngay == null ? null : ngay.atStartOfDay();
        LocalDateTime denNgay = ngay == null ? null : ngay.plusDays(1).atStartOfDay();
        return PageResponse.tu(
                chuyenRepository.timCuaTaiXe(taiXe.getIdTX(), trangThai, tuNgay, denNgay,
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
        // Luồng phụ 5.b: đang có chuyến khác chưa hoàn thành thì không cho nhận thêm
        if (chuyenRepository.taiXeCoChuyenKhacChuaXong(taiXe.getIdTX(), idChuyen)) {
            throw new AppException("Bạn đang có chuyến chưa hoàn thành, không thể nhận thêm chuyến",
                    HttpStatus.CONFLICT);
        }
        chuyen.setTrangThai(TrangThaiChuyen.DA_NHAN);
        chuyen.setThoiGianNhan(LocalDateTime.now());
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
    public ChuyenChiTietResponse xacNhanDaDen(String tenDangNhap, Long idChuyen, DaDenCongTrinhRequest request) {
        TaiXe taiXe = taiXeHienTai(tenDangNhap);
        Chuyen chuyen = timChuyenCuaToi(idChuyen, taiXe.getIdTX());
        if (!Integer.valueOf(TrangThaiChuyen.DANG_GIAO).equals(chuyen.getTrangThai())) {
            throw new AppException("Chuyến phải ở trạng thái Đang giao mới xác nhận đến công trình được",
                    HttpStatus.CONFLICT);
        }

        Double viDo = request == null ? null : request.getViDo();
        Double kinhDo = request == null ? null : request.getKinhDo();
        String ghiChu = request == null || request.getGhiChu() == null ? null : request.getGhiChu().trim();
        boolean coGhiChu = ghiChu != null && !ghiChu.isEmpty();
        boolean canKiemTra = false;
        Double khoangCach = null;

        if (viDo == null || kinhDo == null) {
            // Luồng 2.b: không lấy được vị trí -> chỉ cho xác nhận thủ công kèm ghi chú
            if (!coGhiChu) {
                throw new AppException("Không lấy được vị trí hiện tại. Vui lòng nhập ghi chú để xác nhận thủ công",
                        HttpStatus.UNPROCESSABLE_ENTITY);
            }
            canKiemTra = true;
        } else {
            CongTrinh congTrinh = chuyen.getDonHang().getCongTrinh();
            if (congTrinh != null && congTrinh.getViDo() != null && congTrinh.getKinhDo() != null) {
                khoangCach = khoangCachMet(viDo, kinhDo, congTrinh.getViDo(), congTrinh.getKinhDo());
                if (khoangCach > BAN_KINH_CHO_PHEP_M) {
                    // Luồng 2.a: ngoài bán kính -> yêu cầu nhập ghi chú lý do
                    if (!coGhiChu) {
                        throw new AppException(String.format(Locale.ROOT,
                                "Bạn đang cách công trình khoảng %.0f m (ngoài bán kính %d m). "
                                        + "Vui lòng nhập ghi chú lý do để xác nhận", khoangCach, BAN_KINH_CHO_PHEP_M),
                                HttpStatus.UNPROCESSABLE_ENTITY);
                    }
                    canKiemTra = true;
                }
            }
        }

        chuyen.setTrangThai(TrangThaiChuyen.DA_DEN);
        chuyen.setThoiGianDen(LocalDateTime.now());
        chuyen.setViDoDen(viDo);
        chuyen.setKinhDoDen(kinhDo);
        chuyen.setKhoangCachDen(khoangCach);
        chuyen.setGhiChuDen(coGhiChu ? ghiChu : null);
        chuyen.setCanKiemTraDen(canKiemTra);
        Chuyen saved = chuyenRepository.saveAndFlush(chuyen);

        notificationService.notifyOrderStatusChanged(chuyen.getDonHang().getIdDH(),
                "Xe " + chuyen.getXe().getBienSo() + " (chuyến #" + idChuyen + ") đã đến công trình"
                        + (canKiemTra ? " - cần điều phối kiểm tra lại vị trí" : ""));
        return sangChiTietResponse(saved);
    }

    @Override
    @Transactional
    public ChuyenChiTietResponse xacNhanGiaoHang(String tenDangNhap, Long idChuyen, Double khoiLuongThucGiao,
                                                 String ghiChu, MultipartFile anhMinhChung) {
        TaiXe taiXe = taiXeHienTai(tenDangNhap);
        Chuyen chuyen = timChuyenCuaToi(idChuyen, taiXe.getIdTX());
        if (!Integer.valueOf(TrangThaiChuyen.DA_DEN).equals(chuyen.getTrangThai())) {
            throw new AppException("Chuyến phải ở trạng thái Đã đến công trình mới xác nhận giao hàng được",
                    HttpStatus.CONFLICT);
        }
        // Luồng 4.a: thiếu khối lượng thực giao
        if (khoiLuongThucGiao == null || khoiLuongThucGiao <= 0) {
            throw new AppException("Vui lòng nhập khối lượng thực giao lớn hơn 0", HttpStatus.BAD_REQUEST);
        }
        String ghiChuSach = ghiChu == null ? null : ghiChu.trim();
        boolean coGhiChu = ghiChuSach != null && !ghiChuSach.isEmpty();
        // Luồng 4.b: giao nhiều hơn khối lượng của chuyến -> bắt buộc ghi chú lý do
        if (chuyen.getKhoiLuong() != null && khoiLuongThucGiao > chuyen.getKhoiLuong() + SAI_SO_KHOI_LUONG
                && !coGhiChu) {
            throw new AppException(String.format(Locale.ROOT,
                    "Khối lượng thực giao (%.1f m³) lớn hơn khối lượng của chuyến (%.1f m³). "
                            + "Vui lòng nhập ghi chú lý do", khoiLuongThucGiao, chuyen.getKhoiLuong()),
                    HttpStatus.UNPROCESSABLE_ENTITY);
        }

        if (anhMinhChung != null && !anhMinhChung.isEmpty()) {
            chuyen.setAnhMinhChung(fileStorageService.storeImage(anhMinhChung, "giao-hang"));
        }
        chuyen.setKhoiLuongThucGiao(khoiLuongThucGiao);
        chuyen.setGhiChuGiaoHang(coGhiChu ? ghiChuSach : null);
        chuyen.setThoiGianGiaoXong(LocalDateTime.now());
        chuyen.setTrangThai(TrangThaiChuyen.DA_GIAO_HANG);
        Chuyen saved = chuyenRepository.saveAndFlush(chuyen);

        DonHang donHang = chuyen.getDonHang();
        Double daGiao = chuyenRepository.tongKhoiLuongDaGiao(donHang.getIdDH());
        notificationService.notifyOrderStatusChanged(donHang.getIdDH(), String.format(Locale.ROOT,
                "Chuyến #%d đã giao %.1f m³. Đơn hàng đã giao %.1f/%.1f m³", idChuyen, khoiLuongThucGiao,
                daGiao == null ? 0 : daGiao, donHang.getTongKhoiLuong() == null ? 0 : donHang.getTongKhoiLuong()));
        return sangChiTietResponse(saved);
    }

    @Override
    @Transactional
    public ChuyenChiTietResponse hoanThanhChuyen(String tenDangNhap, Long idChuyen) {
        TaiXe taiXe = taiXeHienTai(tenDangNhap);
        Chuyen chuyen = timChuyenCuaToi(idChuyen, taiXe.getIdTX());
        // Luồng 5.a: chưa xác nhận giao hàng thành công thì không cho hoàn thành
        if (!Integer.valueOf(TrangThaiChuyen.DA_GIAO_HANG).equals(chuyen.getTrangThai())) {
            throw new AppException("Chuyến chưa được xác nhận giao hàng thành công. "
                    + "Vui lòng thực hiện bước Xác nhận giao hàng trước", HttpStatus.CONFLICT);
        }
        chuyen.setTrangThai(TrangThaiChuyen.HOAN_THANH);
        chuyen.setThoiGianHoanThanh(LocalDateTime.now());
        Chuyen saved = chuyenRepository.saveAndFlush(chuyen);

        // Trả tài xế và xe về trạng thái Sẵn sàng
        taiXe.setTrangThai(XE_SAN_SANG);
        taiXeRepository.saveAndFlush(taiXe);
        Xe xe = chuyen.getXe();
        xe.setTrangThai(XE_SAN_SANG);
        xeRepository.saveAndFlush(xe);

        // Chỉ đóng đơn hàng khi mọi chuyến của đơn đã hoàn thành
        DonHang donHang = chuyen.getDonHang();
        if (!chuyenRepository.donHangConChuyenKhacChuaXong(donHang.getIdDH(), idChuyen)) {
            donHang.setTrangThai(DONHANG_HOAN_THANH);
            donHangRepository.saveAndFlush(donHang);
        }

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
                        "Chuyến không còn hiệu lực hoặc đã được chuyển cho tài xế khác", HttpStatus.NOT_FOUND));
    }

    /** Khoảng cách 2 điểm trên mặt đất (công thức Haversine), đơn vị mét. */
    static double khoangCachMet(double lat1, double lon1, double lat2, double lon2) {
        double r = 6_371_000d;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 2 * r * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
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
                .viDoTram(tram == null ? null : tram.getViDo())
                .kinhDoTram(tram == null ? null : tram.getKinhDo())
                .thoiGianGiao(d.getThoiGianGiao())
                .thoiGianNhan(c.getThoiGianNhan())
                .thoiGianXuatPhat(c.getThoiGianXuatPhat())
                .thoiGianDen(c.getThoiGianDen())
                .thoiGianGiaoXong(c.getThoiGianGiaoXong())
                .thoiGianHoanThanh(c.getThoiGianHoanThanh())
                .khoangCachDen(c.getKhoangCachDen())
                .ghiChuDen(c.getGhiChuDen())
                .canKiemTraDen(c.getCanKiemTraDen())
                .banKinhChoPhep(BAN_KINH_CHO_PHEP_M)
                .khoiLuongThucGiao(c.getKhoiLuongThucGiao())
                .ghiChuGiaoHang(c.getGhiChuGiaoHang())
                .anhMinhChung(c.getAnhMinhChung())
                .tongKhoiLuongDonHang(d.getTongKhoiLuong())
                .tongKhoiLuongDaGiao(chuyenRepository.tongKhoiLuongDaGiao(d.getIdDH()))
                .bienSo(c.getXe().getBienSo())
                .ghiChu(d.getGhiChu())
                .trangThai(c.getTrangThai())
                .tenTrangThai(TrangThaiChuyen.tenHienThi(c.getTrangThai()))
                .build();
    }
}
