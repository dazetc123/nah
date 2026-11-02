package com.example.betong.Service.betong.impl;

import com.example.betong.DTO.request.betong.LoaiBeTongRequest;
import com.example.betong.DTO.response.betong.LoaiBeTongResponse;
import com.example.betong.DTO.response.common.PageResponse;
import com.example.betong.Exception.AppException;
import com.example.betong.Service.betong.LoaiBeTongService;
import com.example.betong.entity.LichSuGiaBeTong;
import com.example.betong.entity.LoaiBeTong;
import com.example.betong.repository.ChiTietDonHangRepository;
import com.example.betong.repository.LichSuGiaBeTongRepository;
import com.example.betong.repository.LoaiBeTongRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Bám sát Bảng 3.50 (Thêm mới), 3.51 (Chỉnh sửa), 3.52 (Tìm kiếm), 3.53
 * (Xóa), 3.54 (Xem chi tiết) - Tác nhân duy nhất: Quản lý.
 */
@Service
public class LoaiBeTongServiceImpl implements LoaiBeTongService {

    private final LoaiBeTongRepository loaiBeTongRepository;
    private final ChiTietDonHangRepository chiTietDonHangRepository;
    private final LichSuGiaBeTongRepository lichSuGiaBeTongRepository;

    public LoaiBeTongServiceImpl(LoaiBeTongRepository loaiBeTongRepository,
                                  ChiTietDonHangRepository chiTietDonHangRepository,
                                  LichSuGiaBeTongRepository lichSuGiaBeTongRepository) {
        this.loaiBeTongRepository = loaiBeTongRepository;
        this.chiTietDonHangRepository = chiTietDonHangRepository;
        this.lichSuGiaBeTongRepository = lichSuGiaBeTongRepository;
    }

    // ================================================================
    // Bảng 3.52 - Tìm kiếm (kiêm xem danh sách)
    // "Không tìm thấy loại bê tông phù hợp" xử lý ở Frontend (empty-state),
    // giống hệt cách Xe/TramTron/TaiXe đang làm - không throw lỗi ở đây.
    // ================================================================
    @Override
    public PageResponse<LoaiBeTongResponse> danhSach(String tuKhoa, int trang, int soLuong) {
        String tuKhoaThucSu = (tuKhoa == null || tuKhoa.isBlank()) ? null : tuKhoa.trim();
        Pageable pageable = PageRequest.of(Math.max(0, trang - 1), Math.max(1, soLuong));
        return PageResponse.tu(loaiBeTongRepository.timKiem(tuKhoaThucSu, pageable), this::sangResponse);
    }

    // ================================================================
    // Bảng 3.54 - Xem chi tiết (kèm số đơn hàng đã sử dụng)
    // ================================================================
    @Override
    public LoaiBeTongResponse xemChiTiet(Long idLBT) {
        LoaiBeTong loaiBeTong = timHoacBaoLoi(idLBT);
        long soDonHang = chiTietDonHangRepository.countByLoaiBeTong_IdLBT(idLBT);

        return LoaiBeTongResponse.builder()
                .idLBT(loaiBeTong.getIdLBT())
                .macBeTong(loaiBeTong.getMacBeTong())
                .thanhPhan(loaiBeTong.getThanhPhan())
                .donGia(loaiBeTong.getDonGia())
                .trangThai(loaiBeTong.getTrangThai())
                .moTa(loaiBeTong.getMoTa())
                .soDonHangDaSuDung(soDonHang)
                .build();
    }

    // ================================================================
    // Bảng 3.50 - Thêm mới loại bê tông
    // ================================================================
    @Override
    @Transactional
    public LoaiBeTongResponse them(LoaiBeTongRequest request) {
        // Luồng phụ: "Mác bê tông đã tồn tại trong hệ thống"
        if (loaiBeTongRepository.existsByMacBeTongIgnoreCase(request.getMacBeTong().trim())) {
            throw new AppException("Mác bê tông đã tồn tại trong hệ thống", HttpStatus.CONFLICT);
        }

        LoaiBeTong loaiBeTong = LoaiBeTong.builder()
                .macBeTong(request.getMacBeTong().trim())
                .thanhPhan(request.getThanhPhan().trim())
                .donGia(request.getDonGia())
                .trangThai(request.getTrangThai())
                .moTa(request.getMoTa() == null ? null : request.getMoTa().trim())
                .build();

        try {
            loaiBeTong = loaiBeTongRepository.save(loaiBeTong);
            // Ghi nhận mức giá khởi điểm vào lịch sử giá ngay khi tạo mới.
            luuLichSuGia(loaiBeTong, request.getDonGia());
            return sangResponse(loaiBeTong, "Thêm mới loại bê tông thành công");
        } catch (DataAccessException ex) {
            throw new AppException("Thêm mới loại bê tông thất bại, yêu cầu thử lại sau", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ================================================================
    // Bảng 3.51 - Chỉnh sửa loại bê tông
    // ================================================================
    @Override
    @Transactional
    public LoaiBeTongResponse sua(Long idLBT, LoaiBeTongRequest request) {
        LoaiBeTong loaiBeTong = timHoacBaoLoi(idLBT);

        if (loaiBeTongRepository.existsByMacBeTongIgnoreCaseAndIdLBTNot(request.getMacBeTong().trim(), idLBT)) {
            throw new AppException("Mác bê tông đã tồn tại trong hệ thống", HttpStatus.CONFLICT);
        }

        boolean doiGia = !Objects.equals(loaiBeTong.getDonGia(), request.getDonGia());

        loaiBeTong.setMacBeTong(request.getMacBeTong().trim());
        loaiBeTong.setThanhPhan(request.getThanhPhan().trim());
        loaiBeTong.setDonGia(request.getDonGia());
        loaiBeTong.setTrangThai(request.getTrangThai());
        loaiBeTong.setMoTa(request.getMoTa() == null ? null : request.getMoTa().trim());

        try {
            loaiBeTong = loaiBeTongRepository.save(loaiBeTong);
            // Đơn giá đổi -> ghi thêm 1 dòng lịch sử giá (bảng lich_su_gia_be_tong, mục 3.3.1).
            if (doiGia) {
                luuLichSuGia(loaiBeTong, request.getDonGia());
            }
            return sangResponse(loaiBeTong, "Sửa loại bê tông thành công");
        } catch (DataAccessException ex) {
            throw new AppException("Cập nhật loại bê tông thất bại, yêu cầu thử lại sau", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ================================================================
    // Bảng 3.53 - Xóa loại bê tông
    // (bước 5 "hiển thị xác nhận xóa" thuộc Frontend; API này xử lý từ
    //  lúc Quản lý đã bấm "Xác nhận")
    // ================================================================
    @Override
    @Transactional
    public void xoa(Long idLBT) {
        LoaiBeTong loaiBeTong = timHoacBaoLoi(idLBT);

        // Luồng phụ: "Loại bê tông đang được dùng"
        if (chiTietDonHangRepository.existsByLoaiBeTong_IdLBT(idLBT)) {
            throw new AppException("Không thể xóa vì loại bê tông đang được dùng trong đơn hàng", HttpStatus.CONFLICT);
        }

        try {
            loaiBeTongRepository.delete(loaiBeTong);
        } catch (DataAccessException ex) {
            throw new AppException("Xóa loại bê tông thất bại, yêu cầu thử lại sau", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ================================================================
    // Hàm hỗ trợ nội bộ
    // ================================================================
    private LoaiBeTong timHoacBaoLoi(Long idLBT) {
        return loaiBeTongRepository.findById(idLBT)
                .orElseThrow(() -> new AppException("Không tìm thấy loại bê tông", HttpStatus.NOT_FOUND));
    }

    private void luuLichSuGia(LoaiBeTong loaiBeTong, Double donGia) {
        LichSuGiaBeTong lichSu = LichSuGiaBeTong.builder()
                .loaiBeTong(loaiBeTong)
                .donGia(donGia)
                .ngayApDung(LocalDate.now())
                .build();
        lichSuGiaBeTongRepository.save(lichSu);
    }

    private LoaiBeTongResponse sangResponse(LoaiBeTong l) {
        return sangResponse(l, null);
    }

    private LoaiBeTongResponse sangResponse(LoaiBeTong l, String thongBao) {
        return LoaiBeTongResponse.builder()
                .idLBT(l.getIdLBT())
                .macBeTong(l.getMacBeTong())
                .thanhPhan(l.getThanhPhan())
                .donGia(l.getDonGia())
                .trangThai(l.getTrangThai())
                .moTa(l.getMoTa())
                .thongBao(thongBao)
                .build();
    }
}
