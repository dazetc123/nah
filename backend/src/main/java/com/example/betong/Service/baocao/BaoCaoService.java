package com.example.betong.Service.baocao;

import com.example.betong.DTO.response.baocao.BaoCaoDongResponse;
import com.example.betong.DTO.response.baocao.BaoCaoTongHopResponse;

import java.time.LocalDate;
import java.util.List;

public interface BaoCaoService {
    List<BaoCaoDongResponse> loc(LocalDate tuNgay, LocalDate denNgay);
    BaoCaoTongHopResponse tongHop(LocalDate tuNgay, LocalDate denNgay);
    byte[] xuat(LocalDate tuNgay, LocalDate denNgay, String dinhDang);
}
