package com.example.betong.Service.baocao.impl;

import com.example.betong.DTO.response.baocao.BaoCaoDongResponse;
import com.example.betong.DTO.response.baocao.BaoCaoTongHopResponse;
import com.example.betong.Exception.AppException;
import com.example.betong.Service.baocao.BaoCaoService;
import com.example.betong.entity.DonHang;
import com.example.betong.repository.ChuyenRepository;
import com.example.betong.repository.DonHangRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BaoCaoServiceImpl implements BaoCaoService {
    private final DonHangRepository donHangRepository;
    private final ChuyenRepository chuyenRepository;

    public BaoCaoServiceImpl(DonHangRepository donHangRepository, ChuyenRepository chuyenRepository) {
        this.donHangRepository = donHangRepository;
        this.chuyenRepository = chuyenRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BaoCaoDongResponse> loc(LocalDate tuNgay, LocalDate denNgay) {
        validate(tuNgay, denNgay);
        return donHangRepository.findByNgayDatBetweenOrderByNgayDatAscIdDHAsc(tuNgay, denNgay)
                .stream().map(this::dong).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BaoCaoTongHopResponse tongHop(LocalDate tuNgay, LocalDate denNgay) {
        validate(tuNgay, denNgay);
        List<DonHang> donHangs = donHangRepository.findByNgayDatBetweenOrderByNgayDatAscIdDHAsc(tuNgay, denNgay);
        long soChuyen = chuyenRepository.countByThoiGianXuatPhatBetween(
                tuNgay.atStartOfDay(), denNgay.plusDays(1).atStartOfDay());
        double sanLuong = donHangs.stream().mapToDouble(d -> d.getTongKhoiLuong() == null ? 0 : d.getTongKhoiLuong()).sum();
        double doanhThu = donHangs.stream().filter(d -> d.getTrangThai() == null || d.getTrangThai() != 2)
                .mapToDouble(d -> d.getTongTien() == null ? 0 : d.getTongTien()).sum();
        return BaoCaoTongHopResponse.builder().tuNgay(tuNgay).denNgay(denNgay)
                .soDonHang(donHangs.size()).soChuyenGiao(soChuyen).sanLuongBeTong(sanLuong)
                .doanhThu(doanhThu).thongBao(donHangs.isEmpty() ? "Không có dữ liệu phù hợp" : null).build();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] xuat(LocalDate tuNgay, LocalDate denNgay, String dinhDang) {
        validate(tuNgay, denNgay);
        if (dinhDang == null || dinhDang.isBlank()) {
            throw new AppException("Vui lòng chọn định dạng báo cáo", HttpStatus.BAD_REQUEST);
        }
        List<BaoCaoDongResponse> rows = loc(tuNgay, denNgay);
        try {
            if ("excel".equalsIgnoreCase(dinhDang) || "xlsx".equalsIgnoreCase(dinhDang)) return excel(rows);
            if ("pdf".equalsIgnoreCase(dinhDang)) return pdf(tuNgay, denNgay, rows);
        } catch (Exception ex) {
            throw new AppException("Không thể xuất báo cáo, vui lòng thực hiện lại", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        throw new AppException("Định dạng báo cáo không được hỗ trợ", HttpStatus.BAD_REQUEST);
    }

    private BaoCaoDongResponse dong(DonHang d) {
        return BaoCaoDongResponse.builder().idDH(d.getIdDH()).ngayDat(d.getNgayDat())
                .sanLuong(d.getTongKhoiLuong()).doanhThu(d.getTongTien()).trangThai(d.getTrangThai()).build();
    }

    private byte[] excel(List<BaoCaoDongResponse> rows) throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("Bao cao");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Mã đơn");
            header.createCell(1).setCellValue("Ngày đặt");
            header.createCell(2).setCellValue("Sản lượng (m3)");
            header.createCell(3).setCellValue("Doanh thu");
            header.createCell(4).setCellValue("Trạng thái");
            for (int i = 0; i < rows.size(); i++) {
                var r = rows.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(r.getIdDH());
                row.createCell(1).setCellValue(r.getNgayDat() == null ? "" : r.getNgayDat().toString());
                row.createCell(2).setCellValue(r.getSanLuong() == null ? 0 : r.getSanLuong());
                row.createCell(3).setCellValue(r.getDoanhThu() == null ? 0 : r.getDoanhThu());
                row.createCell(4).setCellValue(r.getTrangThai() == null ? -1 : r.getTrangThai());
            }
            for (int i = 0; i < 5; i++) sheet.autoSizeColumn(i);
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private byte[] pdf(LocalDate tuNgay, LocalDate denNgay, List<BaoCaoDongResponse> rows) throws Exception {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();
            document.add(new Paragraph("BAO CAO DON HANG"));
            document.add(new Paragraph("Tu ngay " + tuNgay + " den ngay " + denNgay));
            for (BaoCaoDongResponse row : rows) {
                document.add(new Paragraph("Don #" + row.getIdDH() + " | " + row.getNgayDat()
                        + " | San luong: " + row.getSanLuong() + " m3 | Doanh thu: " + row.getDoanhThu()));
            }
            document.close();
            return out.toByteArray();
        }
    }

    private void validate(LocalDate tuNgay, LocalDate denNgay) {
        if (tuNgay == null || denNgay == null || tuNgay.isAfter(denNgay)) {
            throw new AppException("Khoảng thời gian không hợp lệ", HttpStatus.BAD_REQUEST);
        }
    }
}
