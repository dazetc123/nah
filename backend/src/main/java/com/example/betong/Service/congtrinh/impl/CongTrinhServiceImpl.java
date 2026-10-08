package com.example.betong.Service.congtrinh.impl;

import com.example.betong.DTO.request.congtrinh.CapNhatCongTrinhRequest;
import com.example.betong.DTO.request.congtrinh.ThemCongTrinhRequest;
import com.example.betong.DTO.response.congtrinh.CongTrinhResponse;
import com.example.betong.Exception.AppException;
import com.example.betong.Service.congtrinh.CongTrinhService;
import com.example.betong.entity.CongTrinh;
import com.example.betong.entity.KhachHang;
import com.example.betong.repository.CongTrinhRepository;
import com.example.betong.repository.KhachHangRepository;
import com.example.betong.repository.DonHangRepository;
import com.example.betong.repository.TaiKhoanRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.example.betong.DTO.response.common.PageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CongTrinhServiceImpl implements CongTrinhService {

    private final CongTrinhRepository repository;
    private final KhachHangRepository khachHangRepository;
    private final DonHangRepository donHangRepository;
    private final TaiKhoanRepository taiKhoanRepository;

    public CongTrinhServiceImpl(CongTrinhRepository repository, KhachHangRepository khachHangRepository,
                                DonHangRepository donHangRepository, TaiKhoanRepository taiKhoanRepository) {
        this.repository = repository;
        this.khachHangRepository = khachHangRepository;
        this.donHangRepository = donHangRepository;
        this.taiKhoanRepository = taiKhoanRepository;
    }

    @Override
    @Transactional
    public CongTrinhResponse them(ThemCongTrinhRequest request) {
        KhachHang khachHang = khachHangRepository.findById(request.getIdKH())
                .orElseThrow(() -> new AppException("Không tìm thấy khách hàng", HttpStatus.NOT_FOUND));
        CongTrinh congTrinh = CongTrinh.builder()
                .khachHang(khachHang)
                .tenCongTrinh(request.getTenCongTrinh().trim())
                .diaChi(request.getDiaChi().trim())
                .viDo(request.getViDo())
                .kinhDo(request.getKinhDo())
                .sdt(request.getSdt().trim())
                .build();
        try {
            return sangResponse(repository.save(congTrinh), "Thêm công trình thành công");
        } catch (DataAccessException ex) {
            throw new AppException("Thêm công trình thất bại, yêu cầu thử lại sau",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CongTrinhResponse> timKiem(String tuKhoa, int trang, int soLuong) {
        String tuKhoaThucSu = tuKhoa == null || tuKhoa.isBlank() ? null : tuKhoa.trim();
        Pageable pageable = PageRequest.of(Math.max(0, trang - 1), Math.max(1, soLuong));
        try {
            return PageResponse.tu(repository.timKiem(tuKhoaThucSu, pageable),
                    congTrinh -> sangResponse(congTrinh, null));
        } catch (DataAccessException ex) {
            throw new AppException("Không thể tải dữ liệu, Yêu cầu kiểm tra kết nối",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CongTrinhResponse> timKiemCuaKhachHang(String tuKhoa, String tenDangNhap,
                                                                int trang, int soLuong) {
        Long idTaiKhoan = taiKhoanIdTheoTenDangNhap(tenDangNhap);
        KhachHang khachHang = khachHangRepository.findByTaiKhoanIdTK(idTaiKhoan)
                .orElseThrow(() -> new AppException("Không tìm thấy hồ sơ khách hàng",
                        HttpStatus.FORBIDDEN));
        String tuKhoaThucSu = tuKhoa == null || tuKhoa.isBlank() ? null : tuKhoa.trim();
        Pageable pageable = PageRequest.of(Math.max(0, trang - 1), Math.max(1, soLuong));
        try {
            return PageResponse.tu(repository.timKiemCuaKhachHang(khachHang.getIdKH(),
                            tuKhoaThucSu, pageable),
                    congTrinh -> sangResponse(congTrinh, null));
        } catch (DataAccessException ex) {
            throw new AppException("Không thể tải dữ liệu, Yêu cầu kiểm tra kết nối",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CongTrinhResponse layChiTiet(Long idCT) {
        return sangResponse(timHoacBaoLoi(idCT), null);
    }

    @Override
    @Transactional
    public CongTrinhResponse capNhat(Long idCT, CapNhatCongTrinhRequest request) {
        CongTrinh congTrinh = timHoacBaoLoi(idCT);
        congTrinh.setTenCongTrinh(request.getTenCongTrinh().trim());
        congTrinh.setDiaChi(request.getDiaChi().trim());
        congTrinh.setViDo(request.getViDo());
        congTrinh.setKinhDo(request.getKinhDo());
        congTrinh.setSdt(request.getSdt().trim());

        try {
            return sangResponse(repository.save(congTrinh), "Sửa công trình thành công");
        } catch (DataAccessException ex) {
            throw new AppException("Cập nhật thông tin thất bại, yêu cầu thử lại sau",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public void xoa(Long idCT) {
        CongTrinh congTrinh = timHoacBaoLoi(idCT);
        xoaCongTrinh(congTrinh, idCT);
    }

    @Override
    @Transactional
    public void xoaCuaKhachHang(Long idCT, String tenDangNhap) {
        KhachHang khachHang = khachHangRepository.findByTaiKhoanIdTK(
                        taiKhoanIdTheoTenDangNhap(tenDangNhap))
                .orElseThrow(() -> new AppException("Không tìm thấy hồ sơ khách hàng", HttpStatus.FORBIDDEN));
        CongTrinh congTrinh = timHoacBaoLoi(idCT);
        if (congTrinh.getKhachHang() == null
                || !congTrinh.getKhachHang().getIdKH().equals(khachHang.getIdKH())) {
            throw new AppException("Bạn không có quyền xóa công trình này", HttpStatus.FORBIDDEN);
        }
        xoaCongTrinh(congTrinh, idCT);
    }

    private void xoaCongTrinh(CongTrinh congTrinh, Long idCT) {
        if (donHangRepository.existsByCongTrinh_IdCT(idCT)) {
            throw new AppException("Có lỗi, vui lòng thử lại sau", HttpStatus.CONFLICT);
        }
        try {
            repository.delete(congTrinh);
            repository.flush();
        } catch (DataAccessException ex) {
            throw new AppException("Có lỗi, vui lòng thử lại sau",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Long taiKhoanIdTheoTenDangNhap(String tenDangNhap) {
        return taiKhoanRepository.findByTenDangNhapOrEmail(tenDangNhap)
                .map(taiKhoan -> taiKhoan.getIdTK())
                .orElseThrow(() -> new AppException("Không xác định được tài khoản khách hàng",
                        HttpStatus.UNAUTHORIZED));
    }

    private CongTrinh timHoacBaoLoi(Long idCT) {
        return repository.findById(idCT)
                .orElseThrow(() -> new AppException("Không tìm thấy công trình", HttpStatus.NOT_FOUND));
    }

    private CongTrinhResponse sangResponse(CongTrinh congTrinh, String thongBao) {
        return CongTrinhResponse.builder()
                .idCT(congTrinh.getIdCT())
                .idKH(congTrinh.getKhachHang() == null ? null : congTrinh.getKhachHang().getIdKH())
                .tenCongTrinh(congTrinh.getTenCongTrinh())
                .diaChi(congTrinh.getDiaChi())
                .viDo(congTrinh.getViDo())
                .kinhDo(congTrinh.getKinhDo())
                .sdt(congTrinh.getSdt())
                .thongBao(thongBao)
                .build();
    }
}
