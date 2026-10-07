package com.example.betong.DTO.request.taixechuyen;

import lombok.Getter;
import lombok.Setter;

/**
 * Mục 2.2.3 - Usecase "Xác nhận đã đến công trình".
 * viDo/kinhDo để trống = không lấy được vị trí (luồng 2.b), khi đó bắt buộc
 * có ghiChu để xác nhận thủ công. Nếu vị trí nằm ngoài bán kính cho phép
 * (luồng 2.a) cũng bắt buộc có ghiChu lý do.
 */
@Getter
@Setter
public class DaDenCongTrinhRequest {
    private Double viDo;
    private Double kinhDo;
    private String ghiChu;
}
