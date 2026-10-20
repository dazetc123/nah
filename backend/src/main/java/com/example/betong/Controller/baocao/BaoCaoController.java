package com.example.betong.Controller.baocao;

import com.example.betong.DTO.response.baocao.BaoCaoDongResponse;
import com.example.betong.DTO.response.baocao.BaoCaoTongHopResponse;
import com.example.betong.Service.baocao.BaoCaoService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/quan-ly/bao-cao")
public class BaoCaoController {
    private final BaoCaoService service;
    public BaoCaoController(BaoCaoService service) { this.service = service; }

    @GetMapping
    public List<BaoCaoDongResponse> loc(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
                                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay) {
        return service.loc(tuNgay, denNgay);
    }

    @GetMapping("/thong-ke")
    public BaoCaoTongHopResponse thongKe(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
                                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay) {
        return service.tongHop(tuNgay, denNgay);
    }

    @GetMapping("/san-luong")
    public BaoCaoTongHopResponse sanLuong(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
                                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay) {
        return service.tongHop(tuNgay, denNgay);
    }

    @GetMapping("/doanh-thu")
    public BaoCaoTongHopResponse doanhThu(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
                                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay) {
        return service.tongHop(tuNgay, denNgay);
    }

    @GetMapping("/so-chuyen")
    public BaoCaoTongHopResponse soChuyen(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
                                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay) {
        return service.tongHop(tuNgay, denNgay);
    }

    @GetMapping("/xuat")
    public ResponseEntity<ByteArrayResource> xuat(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay,
            @RequestParam String dinhDang) {
        byte[] data = service.xuat(tuNgay, denNgay, dinhDang);
        boolean pdf = "pdf".equalsIgnoreCase(dinhDang);
        MediaType type = pdf ? MediaType.APPLICATION_PDF
                : MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String name = "bao-cao-" + tuNgay + "-den-" + denNgay + (pdf ? ".pdf" : ".xlsx");
        return ResponseEntity.ok().contentType(type).header(HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment().filename(name).build().toString()).body(new ByteArrayResource(data));
    }
}
