package com.example.betong.Service.dieuphoi.impl;

import com.example.betong.Common.TrangThaiChuyen;
import com.example.betong.DTO.response.common.PageResponse;
import com.example.betong.DTO.response.dieuphoi.TheoDoiXeResponse;
import com.example.betong.Exception.AppException;
import com.example.betong.Service.dieuphoi.TheoDoiXeService;
import com.example.betong.entity.Chuyen;
import com.example.betong.entity.ViTriGPS;
import com.example.betong.repository.ChuyenRepository;
import com.example.betong.repository.ViTriGPSRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TheoDoiXeServiceImpl implements TheoDoiXeService {
    private final ChuyenRepository chuyenRepository;
    private final ViTriGPSRepository viTriGPSRepository;

    public TheoDoiXeServiceImpl(ChuyenRepository chuyenRepository, ViTriGPSRepository viTriGPSRepository) {
        this.chuyenRepository = chuyenRepository;
        this.viTriGPSRepository = viTriGPSRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TheoDoiXeResponse> danhSachXeDangHoatDong(int trang, int soLuong) {
        return PageResponse.tu(chuyenRepository.timChuyenDangHoatDong(
                PageRequest.of(Math.max(0, trang - 1), Math.max(1, soLuong))), this::sangResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public TheoDoiXeResponse viTriMoiNhat(Long idChuyen) {
        Chuyen chuyen = timChuyen(idChuyen);
        ViTriGPS gps = viTriGPSRepository.findTopByChuyen_IdChuyenOrderByThoiDiemDesc(idChuyen).orElse(null);
        TheoDoiXeResponse response = sangResponse(chuyen, gps);
        return gps == null ? response.toBuilder().thongBao("Không thể cập nhật vị trí xe").build() : response;
    }

    @Override
    @Transactional(readOnly = true)
    public TheoDoiXeResponse trangThaiChuyen(Long idChuyen) {
        return sangResponse(timChuyen(idChuyen));
    }

    private Chuyen timChuyen(Long idChuyen) {
        return chuyenRepository.findById(idChuyen)
                .orElseThrow(() -> new AppException("Không có dữ liệu chuyến giao hàng phù hợp", HttpStatus.NOT_FOUND));
    }

    private TheoDoiXeResponse sangResponse(Chuyen c) {
        ViTriGPS gps = viTriGPSRepository.findTopByChuyen_IdChuyenOrderByThoiDiemDesc(c.getIdChuyen()).orElse(null);
        return sangResponse(c, gps);
    }

    private TheoDoiXeResponse sangResponse(Chuyen c, ViTriGPS gps) {
        return TheoDoiXeResponse.builder().idChuyen(c.getIdChuyen())
                .idDH(c.getDonHang().getIdDH()).idXe(c.getXe().getIdXe()).bienSo(c.getXe().getBienSo())
                .idTX(c.getTaiXe().getIdTX()).tenTaiXe(c.getTaiXe().getHoTen())
                .idTram(c.getTramTron().getIdTram()).tenTram(c.getTramTron().getTenTram())
                .trangThaiChuyen(c.getTrangThai()).tenTrangThai(tenTrangThai(c.getTrangThai()))
                .viDo(gps == null ? null : gps.getViDo()).kinhDo(gps == null ? null : gps.getKinhDo())
                .tocDo(gps == null ? null : gps.getTocDo())
                .thoiDiemGPS(gps == null ? null : gps.getThoiDiem()).build();
    }

    private String tenTrangThai(Integer status) {
        return TrangThaiChuyen.tenHienThi(status);
    }
}
