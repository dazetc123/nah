package com.example.betong.Service.suco.impl;

import com.example.betong.DTO.response.common.PageResponse;
import com.example.betong.DTO.response.suco.SuCoResponse;
import com.example.betong.Exception.AppException;
import com.example.betong.Service.file.FileStorageService;
import com.example.betong.Service.notification.NotificationService;
import com.example.betong.Service.suco.SuCoTaiXeService;
import com.example.betong.entity.Chuyen;
import com.example.betong.entity.SuCo;
import com.example.betong.entity.TaiKhoan;
import com.example.betong.entity.TaiXe;
import com.example.betong.repository.ChuyenRepository;
import com.example.betong.repository.SuCoRepository;
import com.example.betong.repository.TaiKhoanRepository;
import com.example.betong.repository.TaiXeRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class SuCoTaiXeServiceImpl implements SuCoTaiXeService {

    /** Các loại sự cố tài xế được chọn trên app (khớp danh sách trong FaultReportActivity). */
    public static final List<String> LOAI_SU_CO = List.of(
            "Hỏng xe", "Tai nạn", "Tắc đường kéo dài", "Công trình chưa sẵn sàng", "Khác");
    /** Luồng 6.a: sự cố nghiêm trọng -> điều phối cần điều động xe thay thế. */
    private static final Set<String> SU_CO_NGHIEM_TRONG = Set.of("Hỏng xe", "Tai nạn");

    public static final int UU_TIEN_THAP = 1;
    public static final int UU_TIEN_TRUNG_BINH = 2;
    public static final int UU_TIEN_CAO = 3;

    /** Trạng thái xử lý sự cố (cột su_co.trangThai). */
    public static final int SU_CO_MOI = 0;
    public static final int SU_CO_DANG_XU_LY = 1;
    public static final int SU_CO_DA_XU_LY = 2;

    private static final int CHUYEN_DA_NHAN = 1;
    private static final int CHUYEN_DA_GIAO_HANG = 4;

    private final SuCoRepository suCoRepository;
    private final ChuyenRepository chuyenRepository;
    private final TaiKhoanRepository taiKhoanRepository;
    private final TaiXeRepository taiXeRepository;
    private final FileStorageService fileStorageService;
    private final NotificationService notificationService;

    public SuCoTaiXeServiceImpl(SuCoRepository suCoRepository, ChuyenRepository chuyenRepository,
                                TaiKhoanRepository taiKhoanRepository, TaiXeRepository taiXeRepository,
                                FileStorageService fileStorageService, NotificationService notificationService) {
        this.suCoRepository = suCoRepository;
        this.chuyenRepository = chuyenRepository;
        this.taiKhoanRepository = taiKhoanRepository;
        this.taiXeRepository = taiXeRepository;
        this.fileStorageService = fileStorageService;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public SuCoResponse baoCaoSuCo(String tenDangNhap, Long idChuyen, String loaiSuCo, String moTa,
                                   Integer mucDoUuTien, Double viDo, Double kinhDo, MultipartFile anh) {
        TaiKhoan taiKhoan = taiKhoanRepository.findByTenDangNhapOrEmail(tenDangNhap)
                .orElseThrow(() -> new AppException("Không tìm thấy tài khoản", HttpStatus.NOT_FOUND));
        TaiXe taiXe = taiXeRepository.findByTaiKhoanIdTK(taiKhoan.getIdTK())
                .orElseThrow(() -> new AppException("Tài khoản chưa có hồ sơ tài xế", HttpStatus.FORBIDDEN));

        // Luồng 4.a: chưa chọn loại sự cố hoặc chưa nhập mô tả
        String loai = loaiSuCo == null ? "" : loaiSuCo.trim();
        String noiDung = moTa == null ? "" : moTa.trim();
        if (loai.isEmpty()) {
            throw new AppException("Vui lòng chọn loại sự cố", HttpStatus.BAD_REQUEST);
        }
        if (!LOAI_SU_CO.contains(loai)) {
            throw new AppException("Loại sự cố không hợp lệ", HttpStatus.BAD_REQUEST);
        }
        if (noiDung.isEmpty()) {
            throw new AppException("Vui lòng nhập mô tả chi tiết sự cố", HttpStatus.BAD_REQUEST);
        }
        if (noiDung.length() > 255) {
            throw new AppException("Mô tả sự cố tối đa 255 ký tự", HttpStatus.BAD_REQUEST);
        }
        int mucDo = mucDoUuTien == null ? UU_TIEN_TRUNG_BINH : mucDoUuTien;
        if (mucDo < UU_TIEN_THAP || mucDo > UU_TIEN_CAO) {
            throw new AppException("Mức độ ưu tiên chỉ được là 1 (Thấp), 2 (Trung bình) hoặc 3 (Cao)",
                    HttpStatus.BAD_REQUEST);
        }
        boolean nghiemTrong = SU_CO_NGHIEM_TRONG.contains(loai);
        if (nghiemTrong) mucDo = UU_TIEN_CAO;

        Chuyen chuyen = timChuyenDangThucHien(taiXe, idChuyen);

        String viTri = (viDo != null && kinhDo != null)
                ? String.format(Locale.US, "%.6f, %.6f", viDo, kinhDo) : null;

        SuCo suCo = SuCo.builder()
                .chuyen(chuyen)
                .xe(chuyen.getXe())
                .taiXe(taiXe)
                .nguoiBaoCao(taiKhoan.getHoTen() != null ? taiKhoan.getHoTen() : taiKhoan.getTenDangNhap())
                .loaiSuCo(loai)
                .moTa(noiDung)
                .diaChiHu(viTri)
                .mucDoUuTien(mucDo)
                .anhMinhChung(anh == null || anh.isEmpty() ? null : fileStorageService.storeImage(anh, "su-co"))
                .thoiDiem(LocalDateTime.now())
                .trangThai(SU_CO_MOI)
                .build();
        SuCo saved = suCoRepository.save(suCo);

        notificationService.notifyDispatchersOfIncident(saved.getIdSuCo(), chuyen.getIdChuyen(), loai, nghiemTrong);
        return sangResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SuCoResponse> danhSachSuCoCuaToi(String tenDangNhap, int trang, int soLuong) {
        TaiKhoan taiKhoan = taiKhoanRepository.findByTenDangNhapOrEmail(tenDangNhap)
                .orElseThrow(() -> new AppException("Không tìm thấy tài khoản", HttpStatus.NOT_FOUND));
        TaiXe taiXe = taiXeRepository.findByTaiKhoanIdTK(taiKhoan.getIdTK())
                .orElseThrow(() -> new AppException("Tài khoản chưa có hồ sơ tài xế", HttpStatus.FORBIDDEN));
        return PageResponse.tu(suCoRepository.findAllByTaiXe_IdTXAndChuyenIsNotNullOrderByThoiDiemDesc(
                taiXe.getIdTX(), PageRequest.of(Math.max(0, trang - 1), Math.max(1, soLuong))), this::sangResponse);
    }

    private Chuyen timChuyenDangThucHien(TaiXe taiXe, Long idChuyen) {
        if (idChuyen == null) {
            List<Chuyen> dangThucHien = chuyenRepository.timChuyenDangThucHien(taiXe.getIdTX());
            if (dangThucHien.isEmpty()) {
                throw new AppException("Bạn không có chuyến nào đang thực hiện để báo cáo sự cố",
                        HttpStatus.CONFLICT);
            }
            return dangThucHien.get(0);
        }
        Chuyen chuyen = chuyenRepository.timCuaTaiXe(idChuyen, taiXe.getIdTX())
                .orElseThrow(() -> new AppException(
                        "Không tìm thấy chuyến hoặc chuyến không được phân công cho bạn", HttpStatus.NOT_FOUND));
        Integer t = chuyen.getTrangThai();
        if (t == null || t < CHUYEN_DA_NHAN || t > CHUYEN_DA_GIAO_HANG) {
            throw new AppException("Chỉ báo cáo sự cố cho chuyến đang thực hiện", HttpStatus.CONFLICT);
        }
        return chuyen;
    }

    private SuCoResponse sangResponse(SuCo s) {
        return SuCoResponse.builder()
                .idSuCo(s.getIdSuCo())
                .idChuyen(s.getChuyen() == null ? null : s.getChuyen().getIdChuyen())
                .bienSo(s.getXe() == null ? null : s.getXe().getBienSo())
                .loaiSuCo(s.getLoaiSuCo())
                .moTa(s.getMoTa())
                .mucDoUuTien(s.getMucDoUuTien())
                .tenMucDoUuTien(tenMucDo(s.getMucDoUuTien()))
                .viTri(s.getDiaChiHu())
                .anhMinhChung(s.getAnhMinhChung())
                .thoiDiem(s.getThoiDiem())
                .trangThai(s.getTrangThai())
                .tenTrangThai(tenTrangThai(s.getTrangThai()))
                .build();
    }

    static String tenMucDo(Integer mucDo) {
        if (mucDo == null) return "Trung bình";
        return switch (mucDo) {
            case UU_TIEN_THAP -> "Thấp";
            case UU_TIEN_CAO -> "Cao";
            default -> "Trung bình";
        };
    }

    static String tenTrangThai(Integer trangThai) {
        if (trangThai == null) return "Mới tiếp nhận";
        return switch (trangThai) {
            case SU_CO_DANG_XU_LY -> "Đang xử lý";
            case SU_CO_DA_XU_LY -> "Đã xử lý";
            default -> "Mới tiếp nhận";
        };
    }
}
