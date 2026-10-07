package com.example.betong.Service.xe.impl;

import com.example.betong.DTO.response.common.PageResponse;
import com.example.betong.DTO.response.xe.BaoCaoTinhTrangXeResponse;
import com.example.betong.Exception.AppException;
import com.example.betong.Service.file.FileStorageService;
import com.example.betong.Service.xe.BaoCaoTinhTrangXeService;
import com.example.betong.entity.*;
import com.example.betong.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
public class BaoCaoTinhTrangXeServiceImpl implements BaoCaoTinhTrangXeService {
    private final XeRepository xeRepository;
    private final TaiKhoanRepository taiKhoanRepository;
    private final TaiXeRepository taiXeRepository;
    private final SuCoRepository repository;
    private final FileStorageService fileStorageService;

    public BaoCaoTinhTrangXeServiceImpl(XeRepository xeRepository, TaiKhoanRepository taiKhoanRepository,
            TaiXeRepository taiXeRepository, SuCoRepository repository,
            FileStorageService fileStorageService) {
        this.xeRepository = xeRepository;
        this.taiKhoanRepository = taiKhoanRepository;
        this.taiXeRepository = taiXeRepository;
        this.repository = repository;
        this.fileStorageService = fileStorageService;
    }

    @Override
    @Transactional
    public BaoCaoTinhTrangXeResponse taoBaoCao(Long idXe, String tenDangNhap, Integer trangThaiXe,
            String diaChiHu, String soDienThoaiTaiXe, String nguyenNhan, String noiDung, MultipartFile anh) {
        Xe xe = xeRepository.findById(idXe)
                .orElseThrow(() -> new AppException("Không tìm thấy xe", HttpStatus.NOT_FOUND));
        TaiKhoan taiKhoan = taiKhoanRepository.findByTenDangNhapOrEmail(tenDangNhap)
                .orElseThrow(() -> new AppException("Không xác định được tài khoản", HttpStatus.UNAUTHORIZED));
        TaiXe taiXe = taiXeRepository.findByTaiKhoanIdTK(taiKhoan.getIdTK())
                .orElseThrow(() -> new AppException("Tài khoản chưa có hồ sơ tài xế", HttpStatus.FORBIDDEN));
        if (xe.getTaiXe() == null || !xe.getTaiXe().getIdTX().equals(taiXe.getIdTX())) {
            throw new AppException("Bạn chỉ được báo cáo chiếc xe đang được phân công", HttpStatus.FORBIDDEN);
        }
        if (trangThaiXe == null || (trangThaiXe != 0 && trangThaiXe != 1)) {
            throw new AppException("Trạng thái xe chỉ được là đang hoạt động (1) hoặc bảo trì (0)",
                    HttpStatus.BAD_REQUEST);
        }
        kiemTra(diaChiHu, "Địa chỉ xe bị hư");
        kiemTra(nguyenNhan, "Nguyên nhân");
        kiemTra(noiDung, "Nội dung lỗi");
        xe.setTrangThai(trangThaiXe);
        xeRepository.save(xe);
        SuCo report = SuCo.builder().xe(xe).taiXe(taiXe)
                .nguoiBaoCao(taiKhoan.getHoTen() != null ? taiKhoan.getHoTen() : taiKhoan.getTenDangNhap())
                .trangThaiXe(trangThaiXe).diaChiHu(diaChiHu.trim())
                .nguyenNhan(nguyenNhan.trim()).moTa(noiDung.trim())
                .anhMinhChung(fileStorageService.storeImage(anh, "bao-cao-xe"))
                .thoiDiem(LocalDateTime.now()).moTa(noiDung.trim()).loaiSuCo("Báo cáo tình trạng xe").build();
        return sangResponse(repository.save(report));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BaoCaoTinhTrangXeResponse> danhSachBaoCao(int trang, int soLuong) {
        return PageResponse.tu(repository.findAllByXeIsNotNullOrderByThoiDiemDesc(
                PageRequest.of(Math.max(0, trang - 1), Math.max(1, soLuong))), this::sangResponse);
    }

    private void kiemTra(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new AppException(field + " không được để trống", HttpStatus.BAD_REQUEST);
        }
    }

    private BaoCaoTinhTrangXeResponse sangResponse(SuCo r) {
        return BaoCaoTinhTrangXeResponse.builder().idBaoCao(r.getIdSuCo())
                .idXe(r.getXe().getIdXe()).bienSo(r.getXe().getBienSo())
                .idTX(r.getTaiXe() == null ? null : r.getTaiXe().getIdTX())
                .nguoiBaoCao(r.getNguoiBaoCao()).noiDung(r.getMoTa())
                .trangThaiXe(r.getTrangThaiXe()).diaChiHu(r.getDiaChiHu())
                .soDienThoaiTaiXe(null).nguyenNhan(r.getNguyenNhan())
                .anhMinhChung(r.getAnhMinhChung()).thoiGian(r.getThoiDiem()).build();
    }
}
