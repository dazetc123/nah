package com.example.myapplication.models.trip;

/** Body gửi lên API "Xác nhận đã đến công trình" - khớp DaDenCongTrinhRequest bên backend. */
public class DaDenRequest {
    private final Double viDo;
    private final Double kinhDo;
    private final String ghiChu;

    public DaDenRequest(Double viDo, Double kinhDo, String ghiChu) {
        this.viDo = viDo;
        this.kinhDo = kinhDo;
        this.ghiChu = ghiChu;
    }
}
